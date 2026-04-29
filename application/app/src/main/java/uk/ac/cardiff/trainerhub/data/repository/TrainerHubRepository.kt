package uk.ac.cardiff.trainerhub.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import uk.ac.cardiff.trainerhub.data.local.AppDatabase
import uk.ac.cardiff.trainerhub.data.local.ClientEntity
import uk.ac.cardiff.trainerhub.data.local.ClientTrainerAssignmentEntity
import uk.ac.cardiff.trainerhub.data.local.ConsentRecordEntity
import uk.ac.cardiff.trainerhub.data.local.DemoDataSeeder
import uk.ac.cardiff.trainerhub.data.local.InvoiceEntity
import uk.ac.cardiff.trainerhub.data.local.PlanExerciseEntity
import uk.ac.cardiff.trainerhub.data.local.PlanWeekEntity
import uk.ac.cardiff.trainerhub.data.local.PaymentRecordEntity
import uk.ac.cardiff.trainerhub.data.local.TrainingPlanEntity
import uk.ac.cardiff.trainerhub.data.local.WorkoutSessionEntity
import uk.ac.cardiff.trainerhub.data.preferences.AppPreferencesRepository
import uk.ac.cardiff.trainerhub.domain.ClientDetail
import uk.ac.cardiff.trainerhub.domain.ClientSummary
import uk.ac.cardiff.trainerhub.domain.ClientSortMode
import uk.ac.cardiff.trainerhub.domain.ConsentDetail
import uk.ac.cardiff.trainerhub.domain.DashboardSnapshot
import uk.ac.cardiff.trainerhub.domain.ExercisePrescription
import uk.ac.cardiff.trainerhub.domain.InvoiceDraft
import uk.ac.cardiff.trainerhub.domain.InvoiceSummary
import uk.ac.cardiff.trainerhub.domain.NewClientDraft
import uk.ac.cardiff.trainerhub.domain.PlanDraft
import uk.ac.cardiff.trainerhub.domain.PlanWeekDetail
import uk.ac.cardiff.trainerhub.domain.SessionDraft
import uk.ac.cardiff.trainerhub.domain.SessionNoteDetail
import uk.ac.cardiff.trainerhub.domain.SessionSummary
import uk.ac.cardiff.trainerhub.domain.TrainerProfile
import uk.ac.cardiff.trainerhub.domain.TrainingPlanDetail
import java.time.LocalDate
import java.time.ZoneId

class TrainerHubRepository(
    private val database: AppDatabase,
    private val seeder: DemoDataSeeder,
    private val preferencesRepository: AppPreferencesRepository,
) {
    val remindersEnabled: Flow<Boolean> = preferencesRepository.remindersEnabled
    val clientSortMode: Flow<ClientSortMode> = preferencesRepository.clientSortMode

    suspend fun ensureSeeded() {
        withContext(Dispatchers.IO) {
            seeder.ensureSeeded()
        }
    }

    fun observeDashboard(): Flow<DashboardSnapshot> {
        return combine(
            database.trainerDao().observeCurrentTrainer(),
            database.clientDao().observeAll(),
            database.sessionDao().observeAll(),
            database.invoiceDao().observeAll(),
        ) { trainer, clients, sessions, invoices ->
            val todayRange = todayRange()
            val todaySessions = sessions
                .filter { it.scheduledAt in todayRange.first..todayRange.second }
                .sortedBy { it.scheduledAt }
                .map { session ->
                    SessionSummary(
                        id = session.id,
                        title = session.title,
                        scheduledAt = session.scheduledAt,
                        durationMinutes = session.durationMinutes,
                        location = session.location,
                        type = session.type,
                        status = session.status,
                        notes = session.notes,
                    )
                }

            val overdueInvoices = invoices.count { it.status == "OVERDUE" }
            val alerts = mutableListOf<String>()
            val onboardingCount = clients.count { it.status == "ONBOARDING" }

            if (overdueInvoices > 0) {
                alerts.add("$overdueInvoices overdue payment item(s) need attention.")
            }
            if (onboardingCount > 0) {
                alerts.add("$onboardingCount client(s) are still in onboarding.")
            }
            if (todaySessions.isEmpty()) {
                alerts.add("No sessions are booked for today.")
            }

            DashboardSnapshot(
                trainerName = trainer?.fullName ?: "Trainer",
                activeClientCount = clients.count { it.status != "INACTIVE" },
                todaySessions = todaySessions,
                overdueInvoices = overdueInvoices,
                alerts = alerts,
            )
        }
    }

    fun observeTrainerProfile(): Flow<TrainerProfile?> {
        return combine(
            database.trainerDao().observeCurrentTrainer(),
            database.gymDao().observeAll(),
            database.membershipDao().observeAll(),
        ) { trainer, gyms, memberships ->
            if (trainer == null) {
                null
            } else {
                val gymNames = mutableListOf<String>()
                for (membership in memberships) {
                    if (membership.trainerId == trainer.id) {
                        val gym = gyms.firstOrNull { it.id == membership.gymId }
                        if (gym != null) {
                            gymNames.add("${gym.name} (${membership.roleLabel})")
                        }
                    }
                }

                TrainerProfile(
                    id = trainer.id,
                    fullName = trainer.fullName,
                    headline = trainer.headline,
                    isVerified = trainer.isVerified,
                    bio = trainer.bio,
                    operatesIndependently = trainer.operatesIndependently,
                    gymAffiliations = gymNames,
                )
            }
        }
    }

    fun observeClients(): Flow<List<ClientSummary>> {
        return combine(
            database.clientDao().observeAll(),
            database.sessionDao().observeAll(),
            database.invoiceDao().observeAll(),
        ) { clients, sessions, invoices ->
            val now = System.currentTimeMillis()
            val summaries = mutableListOf<ClientSummary>()

            for (client in clients) {
                val nextSession = sessions
                    .filter {
                        it.clientId == client.id &&
                            it.status == "SCHEDULED" &&
                            it.scheduledAt >= now
                    }
                    .sortedBy { it.scheduledAt }
                    .firstOrNull()

                val clientInvoices = invoices.filter { it.clientId == client.id }
                val overdueCount = clientInvoices.count { it.status == "OVERDUE" }
                var outstandingTotal = 0

                for (invoice in clientInvoices) {
                    if (invoice.status == "DUE" || invoice.status == "OVERDUE") {
                        outstandingTotal += invoice.amountPence
                    }
                }

                summaries.add(
                    ClientSummary(
                        id = client.id,
                        fullName = client.fullName,
                        goal = client.goal,
                        status = client.status,
                        nextSessionAt = nextSession?.scheduledAt,
                        overdueInvoices = overdueCount,
                        totalOutstandingPence = outstandingTotal,
                    ),
                )
            }

            summaries
        }
    }

    fun observeClientDetail(clientId: String): Flow<ClientDetail?> {
        val identityFlow = combine(
            database.clientDao().observeById(clientId),
            database.trainerDao().observeCurrentTrainer(),
            database.gymDao().observeAll(),
            database.membershipDao().observeAll(),
            database.assignmentDao().observeAll(),
        ) { client, trainer, gyms, memberships, assignments ->
            if (client == null || trainer == null) {
                null
            } else {
                val activeAssignment = assignments.firstOrNull {
                    it.clientId == clientId && it.isActive
                }

                val assignedGymNames = mutableListOf<String>()
                for (membership in memberships) {
                    val assignmentMatches = activeAssignment != null && membership.trainerId == activeAssignment.trainerId
                    if (assignmentMatches) {
                        val gym = gyms.firstOrNull { it.id == membership.gymId }
                        if (gym != null) {
                            assignedGymNames.add(gym.name)
                        }
                    }
                }

                IdentityBlock(
                    client = client,
                    trainerName = trainer.fullName,
                    assignedGyms = assignedGymNames,
                )
            }
        }

        val planFlow = combine(
            database.planDao().observeByClientId(clientId),
            database.planWeekDao().observeAll(),
            database.planExerciseDao().observeAll(),
        ) { plans, weeks, exercises ->
            val output = mutableListOf<TrainingPlanDetail>()

            for (plan in plans) {
                val planWeeks = mutableListOf<PlanWeekDetail>()
                val matchingWeeks = weeks
                    .filter { it.planId == plan.id }
                    .sortedBy { it.weekNumber }

                for (week in matchingWeeks) {
                    val weekExercises = mutableListOf<ExercisePrescription>()
                    for (exercise in exercises) {
                        if (exercise.planWeekId == week.id) {
                            weekExercises.add(
                                ExercisePrescription(
                                    exerciseName = exercise.exerciseName,
                                    sets = exercise.sets,
                                    reps = exercise.reps,
                                    notes = exercise.notes,
                                ),
                            )
                        }
                    }

                    planWeeks.add(
                        PlanWeekDetail(
                            weekNumber = week.weekNumber,
                            summary = week.summary,
                            exercises = weekExercises,
                        ),
                    )
                }

                output.add(
                    TrainingPlanDetail(
                        id = plan.id,
                        title = plan.title,
                        focus = plan.focus,
                        isActive = plan.isActive,
                        weeks = planWeeks,
                    ),
                )
            }

            output
        }

        val activityFlow = combine(
            database.sessionDao().observeByClientId(clientId),
            database.sessionNoteDao().observeByClientId(clientId),
            database.consentDao().observeByClientId(clientId),
        ) { sessions, notes, consents ->
            val sessionSummaries = mutableListOf<SessionSummary>()
            for (session in sessions.sortedByDescending { it.scheduledAt }) {
                sessionSummaries.add(
                    SessionSummary(
                        id = session.id,
                        title = session.title,
                        scheduledAt = session.scheduledAt,
                        durationMinutes = session.durationMinutes,
                        location = session.location,
                        type = session.type,
                        status = session.status,
                        notes = session.notes,
                    ),
                )
            }

            val noteSummaries = mutableListOf<SessionNoteDetail>()
            for (note in notes.sortedByDescending { it.createdAt }) {
                val session = sessions.firstOrNull { it.id == note.sessionId }
                noteSummaries.add(
                    SessionNoteDetail(
                        id = note.id,
                        sessionTitle = session?.title ?: "Session note",
                        content = note.content,
                        createdAt = note.createdAt,
                    ),
                )
            }

            val consentSummaries = mutableListOf<ConsentDetail>()
            for (consent in consents.sortedByDescending { it.recordedAt }) {
                consentSummaries.add(
                    ConsentDetail(
                        id = consent.id,
                        consentType = consent.consentType,
                        granted = consent.granted,
                        recordedAt = consent.recordedAt,
                        details = consent.details,
                    ),
                )
            }

            ActivityBlock(
                sessions = sessionSummaries,
                notes = noteSummaries,
                consents = consentSummaries,
            )
        }

        val financeFlow = combine(
            database.invoiceDao().observeByClientId(clientId),
            database.paymentDao().observeAll(),
        ) { invoices, payments ->
            val invoiceSummaries = mutableListOf<InvoiceSummary>()

            for (invoice in invoices.sortedByDescending { it.dueAt }) {
                val payment = payments.firstOrNull { it.invoiceId == invoice.id }
                invoiceSummaries.add(
                    InvoiceSummary(
                        id = invoice.id,
                        description = invoice.description,
                        amountPence = invoice.amountPence,
                        dueAt = invoice.dueAt,
                        status = invoice.status,
                        paymentReference = payment?.reference,
                    ),
                )
            }

            invoiceSummaries
        }

        return combine(
            identityFlow,
            planFlow,
            activityFlow,
            financeFlow,
        ) { identity, plans, activity, invoices ->
            if (identity == null) {
                null
            } else {
                ClientDetail(
                    id = identity.client.id,
                    fullName = identity.client.fullName,
                    email = identity.client.email,
                    phone = identity.client.phone,
                    goal = identity.client.goal,
                    status = identity.client.status,
                    notes = identity.client.notes,
                    assignedTrainerName = identity.trainerName,
                    assignedGyms = identity.assignedGyms,
                    plans = plans,
                    sessions = activity.sessions,
                    invoices = invoices,
                    sessionNotes = activity.notes,
                    consents = activity.consents,
                )
            }
        }
    }

    suspend fun addClient(draft: NewClientDraft): Boolean {
        return withContext(Dispatchers.IO) {
            val trainer = database.trainerDao().getCurrentTrainer() ?: return@withContext false
            if (!trainer.isVerified) return@withContext false

            val now = System.currentTimeMillis()
            val clientId = seeder.newId("client")

            val client = ClientEntity(
                id = clientId,
                fullName = draft.fullName.trim(),
                email = draft.email.trim(),
                phone = draft.phone.trim(),
                goal = draft.goal.trim(),
                status = "ONBOARDING",
                notes = draft.notes.trim(),
                createdAt = now,
            )

            val assignment = ClientTrainerAssignmentEntity(
                id = seeder.newId("assignment"),
                clientId = clientId,
                trainerId = trainer.id,
                startDate = now,
                endDate = null,
                isActive = true,
            )

            val consent = ConsentRecordEntity(
                id = seeder.newId("consent"),
                clientId = clientId,
                consentType = "DATA_PROCESSING",
                granted = true,
                recordedAt = now,
                details = "Created during trainer onboarding.",
            )

            database.clientDao().insert(client)
            database.assignmentDao().insert(assignment)
            database.consentDao().insert(consent)
            true
        }
    }

    suspend fun createPlan(draft: PlanDraft): Boolean {
        return withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val planId = seeder.newId("plan")

            database.planDao().deactivatePlansForClient(draft.clientId)
            database.planDao().insert(
                TrainingPlanEntity(
                    id = planId,
                    clientId = draft.clientId,
                    title = draft.title.trim(),
                    focus = draft.focus.trim(),
                    isActive = true,
                    createdAt = now,
                ),
            )

            val weekItems = mutableListOf<PlanWeekEntity>()
            val exerciseItems = mutableListOf<PlanExerciseEntity>()

            for (week in draft.weeks) {
                val weekId = seeder.newId("week")
                weekItems.add(
                    PlanWeekEntity(
                        id = weekId,
                        planId = planId,
                        weekNumber = week.weekNumber,
                        summary = week.summary.trim(),
                    ),
                )

                for (exercise in week.exercises) {
                    exerciseItems.add(
                        PlanExerciseEntity(
                            id = seeder.newId("plan-ex"),
                            planWeekId = weekId,
                            exerciseName = exercise.exerciseName.trim(),
                            sets = exercise.sets,
                            reps = exercise.reps.trim(),
                            notes = exercise.notes.trim(),
                        ),
                    )
                }
            }

            database.planWeekDao().insertAll(weekItems)
            database.planExerciseDao().insertAll(exerciseItems)
            true
        }
    }

    suspend fun createSession(draft: SessionDraft): Boolean {
        return withContext(Dispatchers.IO) {
            val trainer = database.trainerDao().getCurrentTrainer() ?: return@withContext false

            database.sessionDao().insert(
                WorkoutSessionEntity(
                    id = seeder.newId("session"),
                    clientId = draft.clientId,
                    trainerId = trainer.id,
                    title = draft.title.trim(),
                    scheduledAt = draft.scheduledAt,
                    durationMinutes = draft.durationMinutes,
                    location = draft.location.trim(),
                    type = draft.type.trim(),
                    status = "SCHEDULED",
                    notes = draft.notes.trim(),
                ),
            )
            true
        }
    }

    suspend fun cancelSession(sessionId: String, notes: String): Boolean {
        return withContext(Dispatchers.IO) {
            database.sessionDao().updateStatus(sessionId, "CANCELLED", notes)
            true
        }
    }

    suspend fun createInvoice(draft: InvoiceDraft): Boolean {
        return withContext(Dispatchers.IO) {
            val trainer = database.trainerDao().getCurrentTrainer() ?: return@withContext false
            val status = if (draft.dueAt < System.currentTimeMillis()) {
                "OVERDUE"
            } else {
                "DUE"
            }

            database.invoiceDao().insert(
                InvoiceEntity(
                    id = seeder.newId("invoice"),
                    clientId = draft.clientId,
                    trainerId = trainer.id,
                    description = buildInvoiceDescription(draft.description, draft.notes),
                    amountPence = draft.amountPence,
                    dueAt = draft.dueAt,
                    status = status,
                    createdAt = System.currentTimeMillis(),
                ),
            )
            true
        }
    }

    suspend fun markInvoicePaid(invoiceId: String): Boolean {
        return withContext(Dispatchers.IO) {
            val invoice = database.invoiceDao().getById(invoiceId) ?: return@withContext false
            if (invoice.status == "PAID") return@withContext true

            database.invoiceDao().updateStatus(invoiceId, "PAID")
            database.paymentDao().insert(
                PaymentRecordEntity(
                    id = seeder.newId("payment"),
                    invoiceId = invoice.id,
                    paidAt = System.currentTimeMillis(),
                    amountPence = invoice.amountPence,
                    method = "Card",
                    reference = "LOCAL-${invoice.id.uppercase()}",
                ),
            )
            true
        }
    }

    suspend fun completeSession(sessionId: String, notes: String): Boolean {
        return withContext(Dispatchers.IO) {
            database.sessionDao().updateStatus(sessionId, "COMPLETED", notes)
            true
        }
    }

    suspend fun requestExport(clientId: String): Boolean {
        return insertPrivacyRecord(
            clientId = clientId,
            consentType = "EXPORT_REQUEST",
            details = "Client export requested inside the app.",
        )
    }

    suspend fun requestDelete(clientId: String): Boolean {
        return insertPrivacyRecord(
            clientId = clientId,
            consentType = "DELETE_REQUEST",
            details = "Client delete request created inside the app.",
        )
    }

    suspend fun resetDemoData() {
        withContext(Dispatchers.IO) {
            seeder.reset()
        }
    }

    suspend fun setRemindersEnabled(enabled: Boolean) {
        preferencesRepository.setRemindersEnabled(enabled)
    }

    suspend fun setClientSortMode(mode: ClientSortMode) {
        preferencesRepository.setClientSortMode(mode)
    }

    private suspend fun insertPrivacyRecord(
        clientId: String,
        consentType: String,
        details: String,
    ): Boolean {
        return withContext(Dispatchers.IO) {
            database.consentDao().insert(
                ConsentRecordEntity(
                    id = seeder.newId("consent"),
                    clientId = clientId,
                    consentType = consentType,
                    granted = true,
                    recordedAt = System.currentTimeMillis(),
                    details = details,
                ),
            )
            true
        }
    }

    private fun todayRange(): Pair<Long, Long> {
        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.now(zoneId)
        val start = today.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val end = today.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1
        return Pair(start, end)
    }

    private fun buildInvoiceDescription(
        title: String,
        notes: String,
    ): String {
        if (notes.isBlank()) {
            return title.trim()
        }
        return "${title.trim()} - ${notes.trim()}"
    }

    private data class IdentityBlock(
        val client: ClientEntity,
        val trainerName: String,
        val assignedGyms: List<String>,
    )

    private data class ActivityBlock(
        val sessions: List<SessionSummary>,
        val notes: List<SessionNoteDetail>,
        val consents: List<ConsentDetail>,
    )
}

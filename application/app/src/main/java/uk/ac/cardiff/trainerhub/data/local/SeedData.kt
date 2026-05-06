package uk.ac.cardiff.trainerhub.data.local

import java.util.UUID

class DemoDataSeeder(
    private val database: AppDatabase,
) {
    suspend fun ensureSeeded() {
        if (database.trainerDao().count() > 0) return
        seedAll()
    }

    suspend fun reset() {
        database.clearAllTables()
        seedAll()
    }

    private suspend fun seedAll() {
        val now = System.currentTimeMillis()
        val hour = 60 * 60 * 1000L
        val day = 24 * hour

        val trainer = TrainerEntity(
            id = "trainer-ash",
            fullName = "Ashley Reid",
            headline = "Verified performance coach",
            isVerified = true,
            bio = "Premium one-to-one strength and conditioning support for busy professionals.",
            operatesIndependently = true,
        )
        val gyms = listOf(
            GymEntity("gym-foundry", "Foundry West", "Cardiff Bay"),
            GymEntity("gym-canvas", "Canvas Strength Club", "Pontcanna"),
        )
        val memberships = listOf(
            TrainerGymMembershipEntity("membership-1", trainer.id, "gym-foundry", "Resident coach"),
            TrainerGymMembershipEntity("membership-2", trainer.id, "gym-canvas", "Partner trainer"),
        )

        val clients = listOf(
            ClientEntity("client-ava", "Ava Morgan", "ava.morgan@example.com", "07111111111", "Build lower-body strength for competition prep.", "ACTIVE", "Needs extra ankle mobility work.", now - 80 * day),
            ClientEntity("client-liam", "Liam Price", "liam.price@example.com", "07222222222", "Return to training after shoulder rehab.", "ATTENTION", "Reduce overhead volume on flare-up weeks.", now - 65 * day),
            ClientEntity("client-sophia", "Sophia Khan", "sophia.khan@example.com", "07333333333", "Improve race-day durability and lifting consistency.", "ACTIVE", "Responds well to short high-accountability check-ins.", now - 42 * day),
            ClientEntity("client-noah", "Noah Evans", "noah.evans@example.com", "07444444444", "Premium onboarding for strength and lifestyle reset.", "ONBOARDING", "Awaiting first gym-floor assessment.", now - 9 * day),
        )

        val assignments = clients.mapIndexed { index, client ->
            ClientTrainerAssignmentEntity(
                id = "assignment-${index + 1}",
                clientId = client.id,
                trainerId = trainer.id,
                startDate = client.createdAt,
                endDate = null,
                isActive = true,
            )
        }

        val plans = listOf(
            TrainingPlanEntity("plan-ava-1", "client-ava", "Meet prep block", "Posterior-chain power and competition confidence.", true, now - 14 * day),
            TrainingPlanEntity("plan-liam-1", "client-liam", "Rebuild shoulder capacity", "Controlled loading and pain-aware progression.", true, now - 10 * day),
            TrainingPlanEntity("plan-sophia-1", "client-sophia", "Hybrid race support", "Strength maintenance around endurance sessions.", true, now - 7 * day),
        )

        val planWeeks = listOf(
            PlanWeekEntity("week-ava-1", "plan-ava-1", 1, "Technique consolidation and heavy triples."),
            PlanWeekEntity("week-ava-2", "plan-ava-1", 2, "Intensity lift with reduced accessory fatigue."),
            PlanWeekEntity("week-liam-1", "plan-liam-1", 1, "Scapular control and tempo press rebuild."),
            PlanWeekEntity("week-liam-2", "plan-liam-1", 2, "Introduce moderate pressing exposure."),
            PlanWeekEntity("week-sophia-1", "plan-sophia-1", 1, "Strength support around track work."),
            PlanWeekEntity("week-sophia-2", "plan-sophia-1", 2, "Durability emphasis before long run."),
        )

        val planExercises = listOf(
            PlanExerciseEntity("plan-ex-1", "week-ava-1", "Competition squat", 4, "3", "Pause first rep."),
            PlanExerciseEntity("plan-ex-2", "week-ava-1", "Romanian deadlift", 3, "6", "Controlled eccentric."),
            PlanExerciseEntity("plan-ex-3", "week-ava-2", "Deadlift", 5, "2", "Work to heavy but crisp doubles."),
            PlanExerciseEntity("plan-ex-4", "week-liam-1", "Landmine press", 4, "8", "Pain-free range only."),
            PlanExerciseEntity("plan-ex-5", "week-liam-2", "Incline dumbbell press", 3, "10", "Neutral grip."),
            PlanExerciseEntity("plan-ex-6", "week-sophia-1", "Front squat", 4, "5", "RPE 7 across."),
            PlanExerciseEntity("plan-ex-7", "week-sophia-2", "Split squat", 3, "8/side", "Keep volume submaximal."),
        )

        val sessions = listOf(
            WorkoutSessionEntity("session-ava-today", "client-ava", trainer.id, "Strength calibration", now + 2 * hour, 60, "Foundry West", "IN_PERSON", "SCHEDULED", "Focus on opener confidence."),
            WorkoutSessionEntity("session-liam-today", "client-liam", trainer.id, "Shoulder rehab review", now + 5 * hour, 45, "Online", "ONLINE", "SCHEDULED", "Adjust pressing if pain exceeds 3/10."),
            WorkoutSessionEntity("session-sophia-upcoming", "client-sophia", trainer.id, "Hybrid performance block", now + day + 4 * hour, 60, "Canvas Strength Club", "IN_PERSON", "SCHEDULED", "Track fatigue before lower session."),
            WorkoutSessionEntity("session-noah-past", "client-noah", trainer.id, "Initial assessment", now - 2 * day, 75, "Foundry West", "IN_PERSON", "COMPLETED", "Movement screen completed."),
        )

        val sessionNotes = listOf(
            SessionNoteEntity("session-note-1", "session-noah-past", "client-noah", trainer.id, "Excellent adherence mindset. Hip hinge pattern needs coaching priority.", now - 2 * day + hour),
            SessionNoteEntity("session-note-2", "session-liam-today", "client-liam", trainer.id, "Previous week note: shoulder tolerated landmine work better than incline pressing.", now - day),
        )

        val invoices = listOf(
            InvoiceEntity("invoice-ava-1", "client-ava", trainer.id, "Premium coaching retainer - April", 18000, now - day, "OVERDUE", now - 12 * day),
            InvoiceEntity("invoice-liam-1", "client-liam", trainer.id, "Rehab block coaching - April", 15000, now + 2 * day, "DUE", now - 8 * day),
            InvoiceEntity("invoice-sophia-1", "client-sophia", trainer.id, "Hybrid coaching - April", 16500, now - 6 * day, "PAID", now - 18 * day),
        )

        val payments = listOf(
            PaymentRecordEntity("payment-1", "invoice-sophia-1", now - 5 * day, 16500, "Card", "PAY-APR-SOPHIA"),
        )

        val consents = listOf(
            ConsentRecordEntity("consent-1", "client-ava", "DATA_PROCESSING", true, now - 80 * day, "Signed onboarding privacy consent."),
            ConsentRecordEntity("consent-2", "client-liam", "COACHING", true, now - 65 * day, "Coaching agreement accepted."),
            ConsentRecordEntity("consent-3", "client-sophia", "PAYMENT", true, now - 42 * day, "Recurring payment consent stored."),
            ConsentRecordEntity("consent-4", "client-noah", "DATA_PROCESSING", true, now - 9 * day, "Assessment intake consent recorded."),
        )

        database.trainerDao().insertAll(listOf(trainer))
        database.gymDao().insertAll(gyms)
        database.membershipDao().insertAll(memberships)
        database.clientDao().insertAll(clients)
        database.assignmentDao().insertAll(assignments)
        database.planDao().insertAll(plans)
        database.planWeekDao().insertAll(planWeeks)
        database.planExerciseDao().insertAll(planExercises)
        database.sessionDao().insertAll(sessions)
        database.sessionNoteDao().insertAll(sessionNotes)
        database.invoiceDao().insertAll(invoices)
        database.paymentDao().insertAll(payments)
        database.consentDao().insertAll(consents)
    }

    fun newId(prefix: String): String = "$prefix-${UUID.randomUUID()}"
}

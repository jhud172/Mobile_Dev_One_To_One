package uk.ac.cardiff.trainerhub.domain

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

enum class ClientSortMode {
    NEXT_SESSION,
    PAYMENT_STATUS,
    NAME,
}

data class DashboardSnapshot(
    val trainerName: String,
    val activeClientCount: Int,
    val todaySessions: List<SessionSummary>,
    val overdueInvoices: Int,
    val alerts: List<String>,
)

data class TrainerProfile(
    val id: String,
    val fullName: String,
    val headline: String,
    val isVerified: Boolean,
    val bio: String,
    val operatesIndependently: Boolean,
    val gymAffiliations: List<String>,
)

data class ClientSummary(
    val id: String,
    val fullName: String,
    val goal: String,
    val status: String,
    val nextSessionAt: Long?,
    val overdueInvoices: Int,
    val totalOutstandingPence: Int,
)

data class ClientDetail(
    val id: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val goal: String,
    val status: String,
    val notes: String,
    val assignedTrainerName: String,
    val assignedGyms: List<String>,
    val plans: List<TrainingPlanDetail>,
    val sessions: List<SessionSummary>,
    val invoices: List<InvoiceSummary>,
    val sessionNotes: List<SessionNoteDetail>,
    val consents: List<ConsentDetail>,
)

data class TrainingPlanDetail(
    val id: String,
    val title: String,
    val focus: String,
    val isActive: Boolean,
    val weeks: List<PlanWeekDetail>,
)

data class PlanWeekDetail(
    val weekNumber: Int,
    val summary: String,
    val exercises: List<ExercisePrescription>,
)

data class ExercisePrescription(
    val exerciseName: String,
    val sets: Int,
    val reps: String,
    val notes: String,
)

data class SessionSummary(
    val id: String,
    val title: String,
    val scheduledAt: Long,
    val durationMinutes: Int,
    val location: String,
    val type: String,
    val status: String,
    val notes: String,
)

data class InvoiceSummary(
    val id: String,
    val description: String,
    val amountPence: Int,
    val dueAt: Long,
    val status: String,
    val paymentReference: String?,
)

data class SessionNoteDetail(
    val id: String,
    val sessionTitle: String,
    val content: String,
    val createdAt: Long,
)

data class ConsentDetail(
    val id: String,
    val consentType: String,
    val granted: Boolean,
    val recordedAt: Long,
    val details: String,
)

data class NewClientDraft(
    val fullName: String,
    val email: String,
    val phone: String,
    val goal: String,
    val notes: String,
)

data class PlanDraft(
    val clientId: String,
    val title: String,
    val focus: String,
    val weeks: List<PlanWeekDraft>,
)

data class PlanWeekDraft(
    val weekNumber: Int,
    val summary: String,
    val exercises: List<ExercisePrescription>,
)

data class SessionDraft(
    val clientId: String,
    val title: String,
    val scheduledAt: Long,
    val durationMinutes: Int,
    val location: String,
    val type: String,
    val notes: String,
)

data class InvoiceDraft(
    val clientId: String,
    val description: String,
    val amountPence: Int,
    val dueAt: Long,
    val notes: String,
)

object DateFormats {
    private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM")
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM, HH:mm")

    fun asDate(epochMillis: Long): String =
        dateFormatter.format(Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()))

    fun asDateTime(epochMillis: Long): String =
        dateTimeFormatter.format(Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()))
}

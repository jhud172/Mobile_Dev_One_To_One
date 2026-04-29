package uk.ac.cardiff.trainerhub.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trainers")
data class TrainerEntity(
    @PrimaryKey val id: String,
    val fullName: String,
    val headline: String,
    val isVerified: Boolean,
    val bio: String,
    val operatesIndependently: Boolean,
)

@Entity(tableName = "gyms")
data class GymEntity(
    @PrimaryKey val id: String,
    val name: String,
    val location: String,
)

@Entity(tableName = "trainer_gym_memberships")
data class TrainerGymMembershipEntity(
    @PrimaryKey val id: String,
    val trainerId: String,
    val gymId: String,
    val roleLabel: String,
)

@Entity(tableName = "clients")
data class ClientEntity(
    @PrimaryKey val id: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val goal: String,
    val status: String,
    val notes: String,
    val createdAt: Long,
)

@Entity(tableName = "client_trainer_assignments")
data class ClientTrainerAssignmentEntity(
    @PrimaryKey val id: String,
    val clientId: String,
    val trainerId: String,
    val startDate: Long,
    val endDate: Long?,
    val isActive: Boolean,
)

@Entity(tableName = "training_plans")
data class TrainingPlanEntity(
    @PrimaryKey val id: String,
    val clientId: String,
    val title: String,
    val focus: String,
    val isActive: Boolean,
    val createdAt: Long,
)

@Entity(tableName = "plan_weeks")
data class PlanWeekEntity(
    @PrimaryKey val id: String,
    val planId: String,
    val weekNumber: Int,
    val summary: String,
)

@Entity(tableName = "plan_exercises")
data class PlanExerciseEntity(
    @PrimaryKey val id: String,
    val planWeekId: String,
    val exerciseName: String,
    val sets: Int,
    val reps: String,
    val notes: String,
)

@Entity(tableName = "sessions")
data class WorkoutSessionEntity(
    @PrimaryKey val id: String,
    val clientId: String,
    val trainerId: String,
    val title: String,
    val scheduledAt: Long,
    val durationMinutes: Int,
    val location: String,
    val type: String,
    val status: String,
    val notes: String,
)

@Entity(tableName = "session_notes")
data class SessionNoteEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val clientId: String,
    val trainerId: String,
    val content: String,
    val createdAt: Long,
)

@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey val id: String,
    val clientId: String,
    val trainerId: String,
    val description: String,
    val amountPence: Int,
    val dueAt: Long,
    val status: String,
    val createdAt: Long,
)

@Entity(tableName = "payments")
data class PaymentRecordEntity(
    @PrimaryKey val id: String,
    val invoiceId: String,
    val paidAt: Long,
    val amountPence: Int,
    val method: String,
    val reference: String,
)

@Entity(tableName = "consents")
data class ConsentRecordEntity(
    @PrimaryKey val id: String,
    val clientId: String,
    val consentType: String,
    val granted: Boolean,
    val recordedAt: Long,
    val details: String,
)


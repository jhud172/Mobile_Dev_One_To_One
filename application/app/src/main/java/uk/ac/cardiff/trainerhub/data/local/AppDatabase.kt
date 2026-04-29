package uk.ac.cardiff.trainerhub.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        TrainerEntity::class,
        GymEntity::class,
        TrainerGymMembershipEntity::class,
        ClientEntity::class,
        ClientTrainerAssignmentEntity::class,
        TrainingPlanEntity::class,
        PlanWeekEntity::class,
        PlanExerciseEntity::class,
        WorkoutSessionEntity::class,
        SessionNoteEntity::class,
        InvoiceEntity::class,
        PaymentRecordEntity::class,
        ConsentRecordEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trainerDao(): TrainerDao
    abstract fun gymDao(): GymDao
    abstract fun membershipDao(): TrainerGymMembershipDao
    abstract fun clientDao(): ClientDao
    abstract fun assignmentDao(): AssignmentDao
    abstract fun planDao(): PlanDao
    abstract fun planWeekDao(): PlanWeekDao
    abstract fun planExerciseDao(): PlanExerciseDao
    abstract fun sessionDao(): SessionDao
    abstract fun sessionNoteDao(): SessionNoteDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun paymentDao(): PaymentDao
    abstract fun consentDao(): ConsentDao

    companion object {
        const val NAME = "trainer-hub.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    NAME,
                ).build().also { INSTANCE = it }
            }
    }
}


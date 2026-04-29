package uk.ac.cardiff.trainerhub.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TrainerDao {
    @Query("SELECT * FROM trainers LIMIT 1")
    fun observeCurrentTrainer(): Flow<TrainerEntity?>

    @Query("SELECT * FROM trainers LIMIT 1")
    suspend fun getCurrentTrainer(): TrainerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TrainerEntity>)

    @Query("SELECT COUNT(*) FROM trainers")
    suspend fun count(): Int
}

@Dao
interface GymDao {
    @Query("SELECT * FROM gyms")
    fun observeAll(): Flow<List<GymEntity>>

    @Query("SELECT * FROM gyms")
    suspend fun getAll(): List<GymEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<GymEntity>)
}

@Dao
interface TrainerGymMembershipDao {
    @Query("SELECT * FROM trainer_gym_memberships")
    fun observeAll(): Flow<List<TrainerGymMembershipEntity>>

    @Query("SELECT * FROM trainer_gym_memberships")
    suspend fun getAll(): List<TrainerGymMembershipEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TrainerGymMembershipEntity>)
}

@Dao
interface ClientDao {
    @Query("SELECT * FROM clients ORDER BY fullName ASC")
    fun observeAll(): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients WHERE id = :clientId LIMIT 1")
    fun observeById(clientId: String): Flow<ClientEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ClientEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ClientEntity>)

    @Update
    suspend fun update(item: ClientEntity)
}

@Dao
interface AssignmentDao {
    @Query("SELECT * FROM client_trainer_assignments")
    fun observeAll(): Flow<List<ClientTrainerAssignmentEntity>>

    @Query("SELECT * FROM client_trainer_assignments WHERE clientId = :clientId ORDER BY startDate DESC")
    suspend fun getByClientId(clientId: String): List<ClientTrainerAssignmentEntity>

    @Query("SELECT * FROM client_trainer_assignments WHERE clientId = :clientId AND isActive = 1 LIMIT 1")
    suspend fun getActiveByClientId(clientId: String): ClientTrainerAssignmentEntity?

    @Query("UPDATE client_trainer_assignments SET isActive = 0, endDate = :endedAt WHERE clientId = :clientId AND isActive = 1")
    suspend fun deactivateClientAssignments(clientId: String, endedAt: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ClientTrainerAssignmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ClientTrainerAssignmentEntity>)
}

@Dao
interface PlanDao {
    @Query("SELECT * FROM training_plans WHERE clientId = :clientId ORDER BY createdAt DESC")
    fun observeByClientId(clientId: String): Flow<List<TrainingPlanEntity>>

    @Query("UPDATE training_plans SET isActive = 0 WHERE clientId = :clientId")
    suspend fun deactivatePlansForClient(clientId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TrainingPlanEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TrainingPlanEntity>)
}

@Dao
interface PlanWeekDao {
    @Query("SELECT * FROM plan_weeks")
    fun observeAll(): Flow<List<PlanWeekEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<PlanWeekEntity>)
}

@Dao
interface PlanExerciseDao {
    @Query("SELECT * FROM plan_exercises")
    fun observeAll(): Flow<List<PlanExerciseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<PlanExerciseEntity>)
}

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions")
    fun observeAll(): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM sessions WHERE clientId = :clientId ORDER BY scheduledAt DESC")
    fun observeByClientId(clientId: String): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM sessions WHERE scheduledAt BETWEEN :rangeStart AND :rangeEnd ORDER BY scheduledAt ASC")
    suspend fun getSessionsBetween(rangeStart: Long, rangeEnd: Long): List<WorkoutSessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: WorkoutSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<WorkoutSessionEntity>)

    @Query("UPDATE sessions SET status = :status, notes = :notes WHERE id = :sessionId")
    suspend fun updateStatus(sessionId: String, status: String, notes: String)
}

@Dao
interface SessionNoteDao {
    @Query("SELECT * FROM session_notes WHERE clientId = :clientId ORDER BY createdAt DESC")
    fun observeByClientId(clientId: String): Flow<List<SessionNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: SessionNoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<SessionNoteEntity>)
}

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices")
    fun observeAll(): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE clientId = :clientId ORDER BY dueAt DESC")
    fun observeByClientId(clientId: String): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE id = :invoiceId LIMIT 1")
    suspend fun getById(invoiceId: String): InvoiceEntity?

    @Query("SELECT * FROM invoices WHERE status = 'OVERDUE'")
    suspend fun getOverdueInvoices(): List<InvoiceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: InvoiceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<InvoiceEntity>)

    @Query("UPDATE invoices SET status = :status WHERE id = :invoiceId")
    suspend fun updateStatus(invoiceId: String, status: String)
}

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments")
    fun observeAll(): Flow<List<PaymentRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: PaymentRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<PaymentRecordEntity>)
}

@Dao
interface ConsentDao {
    @Query("SELECT * FROM consents WHERE clientId = :clientId ORDER BY recordedAt DESC")
    fun observeByClientId(clientId: String): Flow<List<ConsentRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ConsentRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ConsentRecordEntity>)
}

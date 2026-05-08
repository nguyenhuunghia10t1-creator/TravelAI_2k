package com.travelai.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.travelai.data.db.entities.BudgetItemEntity
import com.travelai.data.db.entities.ChecklistItemEntity
import com.travelai.data.db.entities.ChatMessageEntity
import com.travelai.data.db.entities.ChatSessionEntity
import com.travelai.data.db.entities.TripPlanSnapshotEntity
import com.travelai.data.db.entities.TripProfileEntity

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_sessions ORDER BY updatedAt DESC LIMIT 1")
    suspend fun getLatestSession(): ChatSessionEntity?

    @Query("SELECT * FROM chat_sessions ORDER BY isPinned DESC, updatedAt DESC")
    suspend fun getSessions(): List<ChatSessionEntity>

    @Query("SELECT * FROM chat_sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getSession(sessionId: Long): ChatSessionEntity?

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY createdAt ASC, id ASC")
    suspend fun getMessagesForSession(sessionId: Long): List<ChatMessageEntity>

    @Query("SELECT * FROM trip_profiles WHERE sessionId = :sessionId LIMIT 1")
    suspend fun getTripProfile(sessionId: Long): TripProfileEntity?

    @Query("SELECT * FROM trip_plan_snapshots WHERE sessionId = :sessionId LIMIT 1")
    suspend fun getTripPlanSnapshot(sessionId: Long): TripPlanSnapshotEntity?

    @Query("SELECT * FROM budget_items WHERE sessionId = :sessionId ORDER BY createdAt ASC, id ASC")
    suspend fun getBudgetItems(sessionId: Long): List<BudgetItemEntity>

    @Query("SELECT * FROM checklist_items WHERE sessionId = :sessionId ORDER BY createdAt ASC, id ASC")
    suspend fun getChecklistItems(sessionId: Long): List<ChecklistItemEntity>

    @Insert
    suspend fun insertSession(session: ChatSessionEntity): Long

    @Insert
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Insert
    suspend fun insertTripProfile(profile: TripProfileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTripPlanSnapshot(snapshot: TripPlanSnapshotEntity)

    @Insert
    suspend fun insertBudgetItem(item: BudgetItemEntity): Long

    @Insert
    suspend fun insertChecklistItem(item: ChecklistItemEntity): Long

    @Query(
        """
        UPDATE budget_items
        SET category = :category,
            title = :title,
            amountVnd = :amountVnd,
            note = :note,
            updatedAt = :updatedAt
        WHERE id = :itemId AND sessionId = :sessionId
        """
    )
    suspend fun updateBudgetItem(
        sessionId: Long,
        itemId: Long,
        category: String,
        title: String,
        amountVnd: Long,
        note: String,
        updatedAt: Long
    )

    @Query("DELETE FROM budget_items WHERE id = :itemId AND sessionId = :sessionId")
    suspend fun deleteBudgetItem(sessionId: Long, itemId: Long)

    @Query(
        """
        UPDATE checklist_items
        SET isChecked = :isChecked,
            updatedAt = :updatedAt
        WHERE id = :itemId AND sessionId = :sessionId
        """
    )
    suspend fun updateChecklistItemChecked(
        sessionId: Long,
        itemId: Long,
        isChecked: Boolean,
        updatedAt: Long
    )

    @Query("DELETE FROM checklist_items WHERE id = :itemId AND sessionId = :sessionId")
    suspend fun deleteChecklistItem(sessionId: Long, itemId: Long)

    @Query("UPDATE chat_sessions SET updatedAt = :updatedAt WHERE id = :sessionId")
    suspend fun updateSessionUpdatedAt(sessionId: Long, updatedAt: Long)

    @Query(
        """
        UPDATE chat_sessions
        SET title = :title,
            updatedAt = :updatedAt
        WHERE id = :sessionId
        """
    )
    suspend fun renameSession(sessionId: Long, title: String, updatedAt: Long)

    @Query("UPDATE chat_sessions SET isPinned = :isPinned WHERE id = :sessionId")
    suspend fun updateSessionPinned(sessionId: Long, isPinned: Boolean)

    @Query("DELETE FROM chat_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: Long)
}

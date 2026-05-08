package com.travelai.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.travelai.data.db.entities.ChatMessageEntity
import com.travelai.data.db.entities.ChatSessionEntity
import com.travelai.data.db.entities.TripPlanSnapshotEntity
import com.travelai.data.db.entities.TripProfileEntity

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_sessions ORDER BY updatedAt DESC LIMIT 1")
    suspend fun getLatestSession(): ChatSessionEntity?

    @Query("SELECT * FROM chat_sessions ORDER BY updatedAt DESC")
    suspend fun getSessions(): List<ChatSessionEntity>

    @Query("SELECT * FROM chat_sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getSession(sessionId: Long): ChatSessionEntity?

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY createdAt ASC, id ASC")
    suspend fun getMessagesForSession(sessionId: Long): List<ChatMessageEntity>

    @Query("SELECT * FROM trip_profiles WHERE sessionId = :sessionId LIMIT 1")
    suspend fun getTripProfile(sessionId: Long): TripProfileEntity?

    @Query("SELECT * FROM trip_plan_snapshots WHERE sessionId = :sessionId LIMIT 1")
    suspend fun getTripPlanSnapshot(sessionId: Long): TripPlanSnapshotEntity?

    @Insert
    suspend fun insertSession(session: ChatSessionEntity): Long

    @Insert
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Insert
    suspend fun insertTripProfile(profile: TripProfileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTripPlanSnapshot(snapshot: TripPlanSnapshotEntity)

    @Query("UPDATE chat_sessions SET updatedAt = :updatedAt WHERE id = :sessionId")
    suspend fun updateSessionUpdatedAt(sessionId: Long, updatedAt: Long)
}

package com.travelai.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.travelai.data.db.entities.ChatMessageEntity
import com.travelai.data.db.entities.ChatSessionEntity
import com.travelai.data.db.entities.TripPlanSnapshotEntity
import com.travelai.data.db.entities.TripProfileEntity

@Database(
    entities = [
        ChatSessionEntity::class,
        ChatMessageEntity::class,
        TripProfileEntity::class,
        TripPlanSnapshotEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
}

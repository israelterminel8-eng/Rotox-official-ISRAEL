package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        WorldEntity::class,
        AccessoryEntity::class,
        OwnedAccessoryEntity::class,
        FriendRequestEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class RotoxDatabase : RoomDatabase() {
    abstract fun rotoxDao(): RotoxDao

    companion object {
        @Volatile
        private var INSTANCE: RotoxDatabase? = null

        fun getInstance(context: Context): RotoxDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    RotoxDatabase::class.java,
                    "rotox_alfa.db"
                ).build().also { INSTANCE = it }
            }
    }
}

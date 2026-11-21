package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["username"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val passwordHash: String,
    val rotoxCoins: Int = 250,
    val headColorHex: Long = 0xFFF5CD30,
    val torsoColorHex: Long = 0xFF1E6BE6,
    val leftArmColorHex: Long = 0xFFF5CD30,
    val rightArmColorHex: Long = 0xFFF5CD30,
    val leftLegColorHex: Long = 0xFF2EA043,
    val rightLegColorHex: Long = 0xFF2EA043,
    val equippedHatId: String? = "hat_red_cap",
    val equippedFaceId: String? = null,
    val equippedBackId: String? = null,
    val currentWorldId: Long? = null,
    val isCommunityBot: Boolean = false,
    val onlineStatus: String = "En línea"
)

@Entity(tableName = "worlds")
data class WorldEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val creatorUsername: String,
    val category: String,
    val skyTheme: String,
    val blocksJson: String,
    val likesCount: Int = 12,
    val activePlayersCount: Int = 4,
    val thumbnailKey: String = "custom",
    val isBuiltIn: Boolean = false
)

@Entity(tableName = "accessories")
data class AccessoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val slot: String,
    val price: Int,
    val creatorUsername: String,
    val isForSale: Boolean = true,
    val partsJson: String,
    val salesCount: Int = 0
)

@Entity(
    tableName = "owned_accessories",
    primaryKeys = ["userId", "accessoryId"]
)
data class OwnedAccessoryEntity(
    val userId: Long,
    val accessoryId: String
)

@Entity(tableName = "friend_requests")
data class FriendRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fromUserId: Long,
    val toUserId: Long,
    val status: String, // "PENDING", "ACCEPTED", "REJECTED"
    val createdAt: Long = System.currentTimeMillis()
)

package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RotoxDao {
    // Users
    @Query("SELECT * FROM users WHERE id = :id")
    fun observeUserById(id: Long): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: Long): UserEntity?

    @Query("SELECT * FROM users WHERE LOWER(username) = LOWER(:username) LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE id != :excludeUserId ORDER BY isCommunityBot ASC, username ASC")
    fun observeAllOtherUsers(excludeUserId: Long): Flow<List<UserEntity>>

    @Query("SELECT * FROM users")
    suspend fun getAllUsersOnce(): List<UserEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUsersIgnore(users: List<UserEntity>): List<Long>

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET rotoxCoins = rotoxCoins + :delta WHERE id = :userId")
    suspend fun addCoins(userId: Long, delta: Int)

    @Query("UPDATE users SET currentWorldId = :worldId, onlineStatus = :status WHERE id = :userId")
    suspend fun updateUserWorld(userId: Long, worldId: Long?, status: String)

    // Worlds
    @Query("SELECT * FROM worlds ORDER BY isBuiltIn DESC, id DESC")
    fun observeAllWorlds(): Flow<List<WorldEntity>>

    @Query("SELECT * FROM worlds WHERE id = :worldId")
    fun observeWorldById(worldId: Long): Flow<WorldEntity?>

    @Query("SELECT * FROM worlds WHERE id = :worldId")
    suspend fun getWorldById(worldId: Long): WorldEntity?

    @Query("SELECT COUNT(*) FROM worlds")
    suspend fun getWorldCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorld(world: WorldEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorlds(worlds: List<WorldEntity>)

    @Query("UPDATE worlds SET likesCount = likesCount + 1 WHERE id = :worldId")
    suspend fun incrementWorldLikes(worldId: Long)

    // Accessories
    @Query("SELECT * FROM accessories ORDER BY price ASC, name ASC")
    fun observeAllAccessories(): Flow<List<AccessoryEntity>>

    @Query("SELECT * FROM accessories WHERE id = :id")
    suspend fun getAccessoryById(id: String): AccessoryEntity?

    @Query("SELECT COUNT(*) FROM accessories")
    suspend fun getAccessoryCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccessory(accessory: AccessoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccessories(accessories: List<AccessoryEntity>)

    @Query("UPDATE accessories SET isForSale = :forSale, price = :price WHERE id = :accessoryId")
    suspend fun updateAccessoryListing(accessoryId: String, forSale: Boolean, price: Int)

    @Query("UPDATE accessories SET salesCount = salesCount + 1 WHERE id = :accessoryId")
    suspend fun incrementAccessorySales(accessoryId: String)

    // Owned Accessories
    @Query("SELECT accessoryId FROM owned_accessories WHERE userId = :userId")
    fun observeOwnedAccessoryIds(userId: Long): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM owned_accessories WHERE userId = :userId AND accessoryId = :accessoryId")
    suspend fun ownsAccessory(userId: Long, accessoryId: String): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOwnedAccessory(owned: OwnedAccessoryEntity)

    // Friend Requests
    @Query("SELECT * FROM friend_requests WHERE fromUserId = :userId OR toUserId = :userId ORDER BY createdAt DESC")
    fun observeFriendRequestsForUser(userId: Long): Flow<List<FriendRequestEntity>>

    @Query(
        "SELECT * FROM friend_requests WHERE (fromUserId = :a AND toUserId = :b) OR (fromUserId = :b AND toUserId = :a) LIMIT 1"
    )
    suspend fun getRequestBetween(a: Long, b: Long): FriendRequestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriendRequest(req: FriendRequestEntity): Long

    @Query("UPDATE friend_requests SET status = :status WHERE id = :requestId")
    suspend fun updateFriendRequestStatus(requestId: Long, status: String)
}

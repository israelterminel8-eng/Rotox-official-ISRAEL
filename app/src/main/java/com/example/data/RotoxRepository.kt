package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

data class FriendRequestUiModel(
    val requestId: Long,
    val otherUser: UserEntity,
    val status: String,
    val isIncoming: Boolean
)

data class FriendUiModel(
    val user: UserEntity,
    val currentWorldTitle: String?
)

class RotoxRepository(
    private val dao: RotoxDao,
    context: Context
) {
    private val prefs = context.getSharedPreferences("rotox_session_prefs", Context.MODE_PRIVATE)

    suspend fun ensureSeeded() {
        if (dao.getWorldCount() == 0) {
            dao.insertWorlds(DatabaseSeeder.defaultWorlds())
        }
        if (dao.getAccessoryCount() == 0) {
            dao.insertAccessories(DatabaseSeeder.defaultAccessories())
        }
        if (dao.getAllUsersOnce().isEmpty()) {
            dao.insertUsersIgnore(DatabaseSeeder.defaultCommunityUsers())
        }
    }

    fun getSavedSessionUserId(): Long? {
        val id = prefs.getLong("active_user_id", -1L)
        return if (id > 0L) id else null
    }

    fun saveSessionUserId(userId: Long?) {
        if (userId == null) {
            prefs.edit().remove("active_user_id").apply()
        } else {
            prefs.edit().putLong("active_user_id", userId).apply()
        }
    }

    fun observeUser(userId: Long): Flow<UserEntity?> = dao.observeUserById(userId)

    fun observeAllWorlds(): Flow<List<WorldEntity>> = dao.observeAllWorlds()

    fun observeWorldById(worldId: Long): Flow<WorldEntity?> = dao.observeWorldById(worldId)

    fun observeAllAccessories(): Flow<List<AccessoryEntity>> = dao.observeAllAccessories()

    fun observeOwnedAccessoryIds(userId: Long): Flow<Set<String>> =
        combine(dao.observeOwnedAccessoryIds(userId)) { arrays -> arrays[0].toSet() }

    fun observeOtherUsers(userId: Long): Flow<List<UserEntity>> = dao.observeAllOtherUsers(userId)

    fun observeFriendRequests(userId: Long): Flow<List<FriendRequestUiModel>> =
        combine(
            dao.observeFriendRequestsForUser(userId),
            dao.observeAllOtherUsers(userId)
        ) { requests, others ->
            val byId = others.associateBy { it.id }
            requests.mapNotNull { req ->
                val isIncoming = req.toUserId == userId
                val otherId = if (isIncoming) req.fromUserId else req.toUserId
                val other = byId[otherId] ?: return@mapNotNull null
                FriendRequestUiModel(
                    requestId = req.id,
                    otherUser = other,
                    status = req.status,
                    isIncoming = isIncoming
                )
            }
        }

    fun observeFriends(userId: Long): Flow<List<FriendUiModel>> =
        combine(
            dao.observeFriendRequestsForUser(userId),
            dao.observeAllOtherUsers(userId),
            dao.observeAllWorlds()
        ) { requests, others, worlds ->
            val byId = others.associateBy { it.id }
            val worldsById = worlds.associateBy { it.id }
            requests.filter { it.status == "ACCEPTED" }
                .mapNotNull { req ->
                    val otherId = if (req.fromUserId == userId) req.toUserId else req.fromUserId
                    val friend = byId[otherId] ?: return@mapNotNull null
                    val worldTitle = friend.currentWorldId?.let { worldsById[it]?.title }
                    FriendUiModel(user = friend, currentWorldTitle = worldTitle)
                }
                .distinctBy { it.user.id }
        }

    suspend fun createAccount(usernameRaw: String, passwordRaw: String): Result<UserEntity> {
        ensureSeeded()
        val username = usernameRaw.trim()
        if (username.length < 3) {
            return Result.failure(IllegalArgumentException("El usuario debe tener al menos 3 caracteres."))
        }
        if (!username.matches(Regex("^[A-Za-z0-9_.-]+$"))) {
            return Result.failure(IllegalArgumentException("Usa solo letras, números, guión o punto en el usuario."))
        }
        if (passwordRaw.length < 4) {
            return Result.failure(IllegalArgumentException("La contraseña debe tener al menos 4 caracteres."))
        }
        if (dao.getUserByUsername(username) != null) {
            return Result.failure(IllegalArgumentException("Ese nombre de usuario ya está ocupado."))
        }

        val user = UserEntity(
            username = username,
            passwordHash = SecurityUtils.hashPassword(passwordRaw),
            rotoxCoins = 250,
            equippedHatId = "hat_red_cap",
            onlineStatus = "En línea"
        )
        val newId = dao.insertUser(user)
        val created = dao.getUserById(newId)!!
        dao.insertOwnedAccessory(OwnedAccessoryEntity(newId, "hat_red_cap"))

        // Seed 2 incoming friend requests from classic community players so the player can immediately test Accept/Reject/Join
        val community = dao.getAllUsersOnce().filter { it.isCommunityBot }
        community.take(2).forEach { bot ->
            dao.insertFriendRequest(
                FriendRequestEntity(
                    fromUserId = bot.id,
                    toUserId = newId,
                    status = "PENDING"
                )
            )
        }
        saveSessionUserId(newId)
        return Result.success(created)
    }

    suspend fun loginAccount(usernameRaw: String, passwordRaw: String): Result<UserEntity> {
        ensureSeeded()
        val username = usernameRaw.trim()
        if (username.isBlank() || passwordRaw.isBlank()) {
            return Result.failure(IllegalArgumentException("Ingresa tu usuario y contraseña."))
        }
        val existing = dao.getUserByUsername(username)
            ?: return Result.failure(IllegalArgumentException("No encontramos una cuenta con ese usuario."))
        if (existing.isCommunityBot) {
            return Result.failure(IllegalArgumentException("Esta cuenta pertenece a un jugador de la comunidad."))
        }
        if (!SecurityUtils.verifyPassword(passwordRaw, existing.passwordHash)) {
            return Result.failure(IllegalArgumentException("Contraseña incorrecta."))
        }
        saveSessionUserId(existing.id)
        return Result.success(existing)
    }

    fun logout() {
        saveSessionUserId(null)
    }

    suspend fun updateBodyPartColor(userId: Long, part: BodyPart, colorHex: Long) {
        val user = dao.getUserById(userId) ?: return
        val updated = when (part) {
            BodyPart.HEAD -> user.copy(headColorHex = colorHex)
            BodyPart.TORSO -> user.copy(torsoColorHex = colorHex)
            BodyPart.ARMS -> user.copy(leftArmColorHex = colorHex, rightArmColorHex = colorHex)
            BodyPart.LEGS -> user.copy(leftLegColorHex = colorHex, rightLegColorHex = colorHex)
        }
        dao.updateUser(updated)
    }

    suspend fun equipAccessory(userId: Long, accessory: AccessoryEntity?) {
        val user = dao.getUserById(userId) ?: return
        if (accessory == null) return
        val slot = AccessorySlot.fromId(accessory.slot)
        val updated = when (slot) {
            AccessorySlot.HAT -> user.copy(
                equippedHatId = if (user.equippedHatId == accessory.id) null else accessory.id
            )
            AccessorySlot.FACE -> user.copy(
                equippedFaceId = if (user.equippedFaceId == accessory.id) null else accessory.id
            )
            AccessorySlot.BACK -> user.copy(
                equippedBackId = if (user.equippedBackId == accessory.id) null else accessory.id
            )
        }
        dao.updateUser(updated)
    }

    suspend fun buyAccessory(userId: Long, accessoryId: String): Result<String> {
        val user = dao.getUserById(userId) ?: return Result.failure(IllegalStateException("Usuario no encontrado"))
        val item = dao.getAccessoryById(accessoryId)
            ?: return Result.failure(IllegalStateException("Accesorio no encontrado"))
        if (dao.ownsAccessory(userId, accessoryId) > 0) {
            equipAccessory(userId, item)
            return Result.success("¡Ya lo tienes! Equipado en tu avatar.")
        }
        if (user.rotoxCoins < item.price) {
            return Result.failure(IllegalStateException("Te faltan ${item.price - user.rotoxCoins} R$ para comprarlo."))
        }
        dao.addCoins(userId, -item.price)
        dao.insertOwnedAccessory(OwnedAccessoryEntity(userId, accessoryId))
        dao.incrementAccessorySales(accessoryId)
        // Pay creator if another local user created it
        val creator = dao.getUserByUsername(item.creatorUsername)
        if (creator != null && creator.id != userId) {
            dao.addCoins(creator.id, item.price)
        }
        equipAccessory(userId, item)
        return Result.success("¡Comprado y equipado: ${item.name}!")
    }

    suspend fun createAndSellAccessory(
        userId: Long,
        nameRaw: String,
        slot: AccessorySlot,
        price: Int,
        parts: List<AccessoryPart>,
        putForSale: Boolean
    ): Result<AccessoryEntity> {
        val user = dao.getUserById(userId) ?: return Result.failure(IllegalStateException("Usuario no encontrado"))
        val name = nameRaw.trim()
        if (name.length < 3) {
            return Result.failure(IllegalArgumentException("Ponle un nombre de al menos 3 letras."))
        }
        if (parts.isEmpty()) {
            return Result.failure(IllegalArgumentException("Agrega al menos una pieza 3D a tu accesorio."))
        }
        val cleanPrice = price.coerceIn(10, 5000)
        val id = "custom_${userId}_${System.currentTimeMillis()}"
        val entity = AccessoryEntity(
            id = id,
            name = name,
            slot = slot.id,
            price = cleanPrice,
            creatorUsername = user.username,
            isForSale = putForSale,
            partsJson = BlockSerializer.encodeParts(parts),
            salesCount = if (putForSale) 1 else 0
        )
        dao.insertAccessory(entity)
        dao.insertOwnedAccessory(OwnedAccessoryEntity(userId, id))
        equipAccessory(userId, entity)
        if (putForSale) {
            // Instant community collector purchase bonus when publishing to the marketplace
            dao.addCoins(userId, (cleanPrice / 2).coerceAtLeast(15))
        }
        return Result.success(entity)
    }

    suspend fun toggleAccessoryForSale(accessoryId: String, forSale: Boolean, price: Int) {
        dao.updateAccessoryListing(accessoryId, forSale, price.coerceIn(10, 5000))
    }

    suspend fun publishWorld(
        userId: Long,
        titleRaw: String,
        descriptionRaw: String,
        category: String,
        skyTheme: SkyTheme,
        blocks: List<WorldBlock>
    ): Result<WorldEntity> {
        val user = dao.getUserById(userId) ?: return Result.failure(IllegalStateException("Usuario no encontrado"))
        val title = titleRaw.trim()
        if (title.length < 3) {
            return Result.failure(IllegalArgumentException("El nombre del juego debe tener al menos 3 letras."))
        }
        if (blocks.isEmpty()) {
            return Result.failure(IllegalArgumentException("Coloca al menos 1 bloque 3D en tu mundo."))
        }
        val world = WorldEntity(
            title = title,
            description = descriptionRaw.trim().ifBlank { "Mundo 3D creado por ${user.username} en ROTOX[ALFA]." },
            creatorUsername = user.username,
            category = category,
            skyTheme = skyTheme.id,
            blocksJson = BlockSerializer.encodeBlocks(blocks),
            likesCount = 1,
            activePlayersCount = 1,
            thumbnailKey = "custom",
            isBuiltIn = false
        )
        val id = dao.insertWorld(world)
        dao.addCoins(userId, 30) // Builder bonus
        return Result.success(world.copy(id = id))
    }

    suspend fun sendFriendRequest(fromUserId: Long, toUserId: Long): Result<String> {
        if (fromUserId == toUserId) {
            return Result.failure(IllegalArgumentException("No puedes enviarte solicitud a ti mismo."))
        }
        val existing = dao.getRequestBetween(fromUserId, toUserId)
        if (existing != null) {
            return when (existing.status) {
                "ACCEPTED" -> Result.success("¡Ya son amigos!")
                "PENDING" -> Result.success("Ya hay una solicitud pendiente.")
                else -> {
                    dao.insertFriendRequest(
                        existing.copy(
                            fromUserId = fromUserId,
                            toUserId = toUserId,
                            status = "PENDING",
                            createdAt = System.currentTimeMillis()
                        )
                    )
                    Result.success("Solicitud reenviada.")
                }
            }
        }
        val target = dao.getUserById(toUserId)
        dao.insertFriendRequest(
            FriendRequestEntity(
                fromUserId = fromUserId,
                toUserId = toUserId,
                status = "PENDING"
            )
        )
        return Result.success("Solicitud enviada a ${target?.username ?: "jugador"}.")
    }

    suspend fun acceptFriendRequest(requestId: Long) {
        dao.updateFriendRequestStatus(requestId, "ACCEPTED")
    }

    suspend fun rejectFriendRequest(requestId: Long) {
        dao.updateFriendRequestStatus(requestId, "REJECTED")
    }

    suspend fun simulateCommunityFriendAccept(requestId: Long) {
        dao.updateFriendRequestStatus(requestId, "ACCEPTED")
    }

    suspend fun enterWorld(userId: Long, worldId: Long) {
        dao.updateUserWorld(userId, worldId, "Jugando")
    }

    suspend fun leaveWorld(userId: Long) {
        dao.updateUserWorld(userId, null, "En línea")
    }

    suspend fun inviteFriendToMyWorld(friendId: Long, worldId: Long) {
        dao.updateUserWorld(friendId, worldId, "Jugando")
    }

    suspend fun awardPlayCoins(userId: Long, amount: Int) {
        if (amount > 0) dao.addCoins(userId, amount)
    }

    suspend fun buildAppearanceForUser(user: UserEntity, accessoriesMap: Map<String, AccessoryEntity>): AvatarAppearance {
        val hatParts = user.equippedHatId?.let { accessoriesMap[it]?.partsJson }?.let { BlockSerializer.decodeParts(it) } ?: emptyList()
        val faceParts = user.equippedFaceId?.let { accessoriesMap[it]?.partsJson }?.let { BlockSerializer.decodeParts(it) } ?: emptyList()
        val backParts = user.equippedBackId?.let { accessoriesMap[it]?.partsJson }?.let { BlockSerializer.decodeParts(it) } ?: emptyList()
        return AvatarAppearance(
            headColorHex = user.headColorHex,
            torsoColorHex = user.torsoColorHex,
            leftArmColorHex = user.leftArmColorHex,
            rightArmColorHex = user.rightArmColorHex,
            leftLegColorHex = user.leftLegColorHex,
            rightLegColorHex = user.rightLegColorHex,
            hatParts = hatParts,
            faceParts = faceParts,
            backParts = backParts
        )
    }

    suspend fun getWorldParticipants(worldId: Long, currentUserId: Long): List<NpcAvatarState> {
        val allUsers = dao.getAllUsersOnce()
        val accessories = dao.observeAllAccessories().first().associateBy { it.id }
        val requests = dao.observeFriendRequestsForUser(currentUserId).first()
        val friendIds = requests.filter { it.status == "ACCEPTED" }.map {
            if (it.fromUserId == currentUserId) it.toUserId else it.fromUserId
        }.toSet()

        val inWorld = allUsers.filter { it.id != currentUserId && it.currentWorldId == worldId }
        val greetings = listOf(
            "¡Hola! ¿Jugamos?",
            "¡Qué buen mapa 3D!",
            "¡Sígueme hacia la meta!",
            "¡ROTOX[ALFA] está genial!"
        )
        return inWorld.mapIndexed { index, u ->
            val angle = index * 1.7f
            val rx = kotlin.math.cos(angle.toDouble()).toFloat() * 2.2f
            val rz = kotlin.math.sin(angle.toDouble()).toFloat() * 2.2f - 1.5f
            NpcAvatarState(
                userId = u.id,
                username = u.username,
                appearance = buildAppearanceForUser(u, accessories),
                x = rx,
                y = 0.6f,
                z = rz,
                yawDeg = (index * 75f) % 360f,
                isFriend = friendIds.contains(u.id),
                chatLine = greetings[index % greetings.size]
            )
        }
    }
}

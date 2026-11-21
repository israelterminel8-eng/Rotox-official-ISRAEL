package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AccessoryEntity
import com.example.data.AccessoryPart
import com.example.data.AccessorySlot
import com.example.data.AvatarAppearance
import com.example.data.BlockSerializer
import com.example.data.BodyPart
import com.example.data.FriendRequestUiModel
import com.example.data.FriendUiModel
import com.example.data.NpcAvatarState
import com.example.data.RotoxDatabase
import com.example.data.RotoxRepository
import com.example.data.SkyTheme
import com.example.data.UserEntity
import com.example.data.WorldBlock
import com.example.data.WorldEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class LobbyTab(val label: String) {
    GAMES("Juegos"),
    AVATAR("Avatar"),
    CREATE("Crear 3D"),
    FRIENDS("Amigos")
}

sealed interface AuthUiState {
    data object Loading : AuthUiState
    data object LoggedOut : AuthUiState
    data class LoggedIn(val user: UserEntity) : AuthUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
class RotoxViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = RotoxDatabase.getInstance(application).rotoxDao()
    private val repository = RotoxRepository(dao, application)

    private val currentUserId = MutableStateFlow<Long?>(null)
    private val authCheckDone = MutableStateFlow(false)

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _bannerMessage = MutableStateFlow<String?>(null)
    val bannerMessage: StateFlow<String?> = _bannerMessage.asStateFlow()

    private val _selectedTab = MutableStateFlow(LobbyTab.GAMES)
    val selectedTab: StateFlow<LobbyTab> = _selectedTab.asStateFlow()

    private val _gameSearchQuery = MutableStateFlow("")
    val gameSearchQuery: StateFlow<String> = _gameSearchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Todos")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _friendSearchQuery = MutableStateFlow("")
    val friendSearchQuery: StateFlow<String> = _friendSearchQuery.asStateFlow()

    private val _showRequestsDialog = MutableStateFlow(false)
    val showRequestsDialog: StateFlow<Boolean> = _showRequestsDialog.asStateFlow()

    private val _activeWorldId = MutableStateFlow<Long?>(null)
    val activeWorldId: StateFlow<Long?> = _activeWorldId.asStateFlow()

    private val _worldParticipants = MutableStateFlow<List<NpcAvatarState>>(emptyList())
    val worldParticipants: StateFlow<List<NpcAvatarState>> = _worldParticipants.asStateFlow()

    val authState: StateFlow<AuthUiState> = combine(
        authCheckDone,
        currentUserId.flatMapLatest { uid ->
            if (uid == null) flowOf(null) else repository.observeUser(uid)
        }
    ) { ready, user ->
        when {
            !ready -> AuthUiState.Loading
            user == null -> AuthUiState.LoggedOut
            else -> AuthUiState.LoggedIn(user)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AuthUiState.Loading)

    val allWorlds: StateFlow<List<WorldEntity>> = repository.observeAllWorlds()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredWorlds: StateFlow<List<WorldEntity>> = combine(
        allWorlds,
        _gameSearchQuery,
        _selectedCategory
    ) { worlds, query, category ->
        val q = query.trim().lowercase()
        worlds.filter { w ->
            val matchesCat = when (category) {
                "Todos" -> true
                "Creados" -> !w.isBuiltIn
                else -> w.category.equals(category, ignoreCase = true)
            }
            val matchesQuery = q.isEmpty() ||
                w.title.lowercase().contains(q) ||
                w.creatorUsername.lowercase().contains(q) ||
                w.description.lowercase().contains(q)
            matchesCat && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAccessories: StateFlow<List<AccessoryEntity>> = repository.observeAllAccessories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val ownedAccessoryIds: StateFlow<Set<String>> = currentUserId.flatMapLatest { uid ->
        if (uid == null) flowOf(emptySet()) else repository.observeOwnedAccessoryIds(uid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val currentAppearance: StateFlow<AvatarAppearance> = combine(
        authState,
        allAccessories
    ) { state, accessories ->
        val user = (state as? AuthUiState.LoggedIn)?.user ?: return@combine AvatarAppearance()
        val map = accessories.associateBy { it.id }
        AvatarAppearance(
            headColorHex = user.headColorHex,
            torsoColorHex = user.torsoColorHex,
            leftArmColorHex = user.leftArmColorHex,
            rightArmColorHex = user.rightArmColorHex,
            leftLegColorHex = user.leftLegColorHex,
            rightLegColorHex = user.rightLegColorHex,
            hatParts = user.equippedHatId?.let { map[it]?.partsJson }?.let { BlockSerializer.decodeParts(it) } ?: emptyList(),
            faceParts = user.equippedFaceId?.let { map[it]?.partsJson }?.let { BlockSerializer.decodeParts(it) } ?: emptyList(),
            backParts = user.equippedBackId?.let { map[it]?.partsJson }?.let { BlockSerializer.decodeParts(it) } ?: emptyList()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AvatarAppearance())

    val otherUsers: StateFlow<List<UserEntity>> = currentUserId.flatMapLatest { uid ->
        if (uid == null) flowOf(emptyList()) else repository.observeOtherUsers(uid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val friendRequests: StateFlow<List<FriendRequestUiModel>> = currentUserId.flatMapLatest { uid ->
        if (uid == null) flowOf(emptyList()) else repository.observeFriendRequests(uid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val incomingPendingCount: StateFlow<Int> = combine(friendRequests) { arr ->
        arr[0].count { it.isIncoming && it.status == "PENDING" }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val friends: StateFlow<List<FriendUiModel>> = currentUserId.flatMapLatest { uid ->
        if (uid == null) flowOf(emptyList()) else repository.observeFriends(uid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeWorld: StateFlow<WorldEntity?> = _activeWorldId.flatMapLatest { wid ->
        if (wid == null) flowOf(null) else repository.observeWorldById(wid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
            currentUserId.value = repository.getSavedSessionUserId()
            authCheckDone.value = true
        }
    }

    fun clearAuthError() {
        _authError.value = null
    }

    fun clearBanner() {
        _bannerMessage.value = null
    }

    fun showBanner(msg: String) {
        _bannerMessage.value = msg
    }

    fun createAccount(username: String, password: String, confirmPassword: String) {
        if (password != confirmPassword) {
            _authError.value = "Las contraseñas no coinciden."
            return
        }
        viewModelScope.launch {
            val res = repository.createAccount(username, password)
            res.onSuccess { user ->
                _authError.value = null
                currentUserId.value = user.id
                _selectedTab.value = LobbyTab.GAMES
                _bannerMessage.value = "¡Bienvenido a ROTOX[ALFA], ${user.username}! Tienes 2 solicitudes de amistad."
            }.onFailure { err ->
                _authError.value = err.message ?: "No se pudo crear la cuenta."
            }
        }
    }

    fun loginAccount(username: String, password: String) {
        viewModelScope.launch {
            val res = repository.loginAccount(username, password)
            res.onSuccess { user ->
                _authError.value = null
                currentUserId.value = user.id
                _selectedTab.value = LobbyTab.GAMES
            }.onFailure { err ->
                _authError.value = err.message ?: "Error al iniciar sesión."
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            val uid = currentUserId.value
            if (uid != null) repository.leaveWorld(uid)
            repository.logout()
            _activeWorldId.value = null
            currentUserId.value = null
        }
    }

    fun selectTab(tab: LobbyTab) {
        _selectedTab.value = tab
    }

    fun updateGameSearch(q: String) {
        _gameSearchQuery.value = q
    }

    fun selectGameCategory(cat: String) {
        _selectedCategory.value = cat
    }

    fun updateFriendSearch(q: String) {
        _friendSearchQuery.value = q
    }

    fun setRequestsDialogVisible(visible: Boolean) {
        _showRequestsDialog.value = visible
    }

    fun updateBodyColor(part: BodyPart, colorHex: Long) {
        val uid = currentUserId.value ?: return
        viewModelScope.launch {
            repository.updateBodyPartColor(uid, part, colorHex)
        }
    }

    fun equipAccessory(accessory: AccessoryEntity) {
        val uid = currentUserId.value ?: return
        viewModelScope.launch {
            repository.equipAccessory(uid, accessory)
        }
    }

    fun buyAccessory(accessory: AccessoryEntity) {
        val uid = currentUserId.value ?: return
        viewModelScope.launch {
            val res = repository.buyAccessory(uid, accessory.id)
            _bannerMessage.value = res.getOrElse { it.message ?: "Error en compra" }
        }
    }

    fun toggleAccessorySale(accessory: AccessoryEntity, forSale: Boolean, price: Int) {
        viewModelScope.launch {
            repository.toggleAccessoryForSale(accessory.id, forSale, price)
            _bannerMessage.value = if (forSale) {
                "\"${accessory.name}\" puesto en venta por $price R$."
            } else {
                "\"${accessory.name}\" retirado de la venta."
            }
        }
    }

    fun createAndSellAccessory(
        name: String,
        slot: AccessorySlot,
        price: Int,
        parts: List<AccessoryPart>,
        putForSale: Boolean,
        onDone: () -> Unit
    ) {
        val uid = currentUserId.value ?: return
        viewModelScope.launch {
            val res = repository.createAndSellAccessory(uid, name, slot, price, parts, putForSale)
            res.onSuccess { created ->
                _bannerMessage.value = if (putForSale) {
                    "¡Accesorio \"${created.name}\" creado, equipado y en venta por ${created.price} R$!"
                } else {
                    "¡Accesorio \"${created.name}\" creado y equipado en tu avatar!"
                }
                onDone()
            }.onFailure { err ->
                _bannerMessage.value = err.message ?: "No se pudo crear el accesorio."
            }
        }
    }

    fun publishWorld(
        title: String,
        description: String,
        category: String,
        skyTheme: SkyTheme,
        blocks: List<WorldBlock>,
        playImmediately: Boolean
    ) {
        val uid = currentUserId.value ?: return
        viewModelScope.launch {
            val res = repository.publishWorld(uid, title, description, category, skyTheme, blocks)
            res.onSuccess { world ->
                _bannerMessage.value = "¡Juego 3D \"${world.title}\" publicado (+30 R$ bonus)!"
                if (playImmediately) {
                    enterWorld(world.id)
                } else {
                    _selectedTab.value = LobbyTab.GAMES
                }
            }.onFailure { err ->
                _bannerMessage.value = err.message ?: "No se pudo publicar el juego."
            }
        }
    }

    fun sendFriendRequest(targetUserId: Long) {
        val uid = currentUserId.value ?: return
        viewModelScope.launch {
            val res = repository.sendFriendRequest(uid, targetUserId)
            _bannerMessage.value = res.getOrElse { it.message ?: "Error al enviar solicitud" }
        }
    }

    fun acceptFriendRequest(requestId: Long) {
        viewModelScope.launch {
            repository.acceptFriendRequest(requestId)
            _bannerMessage.value = "¡Solicitud aceptada! Ya pueden unirse a sus partidas 3D."
        }
    }

    fun rejectFriendRequest(requestId: Long) {
        viewModelScope.launch {
            repository.rejectFriendRequest(requestId)
            _bannerMessage.value = "Solicitud rechazada."
        }
    }

    fun simulateFriendAccept(requestId: Long) {
        viewModelScope.launch {
            repository.simulateCommunityFriendAccept(requestId)
            _bannerMessage.value = "¡Tu amigo aceptó tu solicitud! Ahora pueden unirse en 3D."
        }
    }

    fun enterWorld(worldId: Long) {
        val uid = currentUserId.value ?: return
        viewModelScope.launch {
            repository.enterWorld(uid, worldId)
            _activeWorldId.value = worldId
            _worldParticipants.value = repository.getWorldParticipants(worldId, uid)
        }
    }

    fun leaveWorld() {
        val uid = currentUserId.value ?: return
        viewModelScope.launch {
            repository.leaveWorld(uid)
            _activeWorldId.value = null
            _worldParticipants.value = emptyList()
        }
    }

    fun joinFriendWorld(friend: FriendUiModel) {
        val uid = currentUserId.value ?: return
        viewModelScope.launch {
            val targetWorldId = friend.user.currentWorldId ?: 1L
            if (friend.user.currentWorldId == null) {
                repository.inviteFriendToMyWorld(friend.user.id, targetWorldId)
            }
            repository.enterWorld(uid, targetWorldId)
            _activeWorldId.value = targetWorldId
            _worldParticipants.value = repository.getWorldParticipants(targetWorldId, uid)
            _showRequestsDialog.value = false
            _bannerMessage.value = "Te uniste a la partida 3D de ${friend.user.username}!"
        }
    }

    fun inviteFriendToMyWorld(friend: FriendUiModel) {
        val uid = currentUserId.value ?: return
        val wid = _activeWorldId.value ?: return
        viewModelScope.launch {
            repository.inviteFriendToMyWorld(friend.user.id, wid)
            _worldParticipants.value = repository.getWorldParticipants(wid, uid)
            _bannerMessage.value = "¡${friend.user.username} se unió a tu mundo 3D!"
        }
    }

    fun awardPlayCoins(amount: Int) {
        val uid = currentUserId.value ?: return
        viewModelScope.launch {
            repository.awardPlayCoins(uid, amount)
        }
    }

    fun likeWorld(worldId: Long) {
        viewModelScope.launch {
            dao.incrementWorldLikes(worldId)
            _bannerMessage.value = "¡Gracias por tu Me Gusta!"
        }
    }
}

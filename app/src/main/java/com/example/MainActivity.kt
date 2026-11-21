package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Games
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AuthUiState
import com.example.ui.LobbyTab
import com.example.ui.RotoxViewModel
import com.example.ui.components.CoinChip
import com.example.ui.components.FriendRequestsButton
import com.example.ui.components.FriendRequestsMenuDialog
import com.example.ui.components.MiniBlockAvatarBadge
import com.example.ui.components.RotoxBrandBadge
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.AvatarScreen
import com.example.ui.screens.CreateStudioScreen
import com.example.ui.screens.FriendsScreen
import com.example.ui.screens.GamesScreen
import com.example.ui.screens.PlayWorldScreen
import com.example.ui.theme.RotoxTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RotoxTheme {
                RotoxAppRoot()
            }
        }
    }
}

@Composable
fun RotoxAppRoot(viewModel: RotoxViewModel = viewModel()) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val authError by viewModel.authError.collectAsStateWithLifecycle()
    val bannerMessage by viewModel.bannerMessage.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val filteredWorlds by viewModel.filteredWorlds.collectAsStateWithLifecycle()
    val allWorlds by viewModel.allWorlds.collectAsStateWithLifecycle()
    val gameSearch by viewModel.gameSearchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val allAccessories by viewModel.allAccessories.collectAsStateWithLifecycle()
    val ownedIds by viewModel.ownedAccessoryIds.collectAsStateWithLifecycle()
    val appearance by viewModel.currentAppearance.collectAsStateWithLifecycle()
    val otherUsers by viewModel.otherUsers.collectAsStateWithLifecycle()
    val friendSearch by viewModel.friendSearchQuery.collectAsStateWithLifecycle()
    val requests by viewModel.friendRequests.collectAsStateWithLifecycle()
    val pendingCount by viewModel.incomingPendingCount.collectAsStateWithLifecycle()
    val friends by viewModel.friends.collectAsStateWithLifecycle()
    val showRequestsDialog by viewModel.showRequestsDialog.collectAsStateWithLifecycle()
    val activeWorld by viewModel.activeWorld.collectAsStateWithLifecycle()
    val participants by viewModel.worldParticipants.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(bannerMessage) {
        val msg = bannerMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearBanner()
    }

    when (val state = authState) {
        AuthUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    RotoxBrandBadge()
                    CircularProgressIndicator()
                }
            }
        }

        AuthUiState.LoggedOut -> {
            AuthScreen(
                errorMessage = authError,
                onCreateAccount = viewModel::createAccount,
                onLogin = viewModel::loginAccount,
                onClearError = viewModel::clearAuthError
            )
        }

        is AuthUiState.LoggedIn -> {
            val user = state.user
            val playingWorld = activeWorld

            Box(modifier = Modifier.fillMaxSize()) {
                if (playingWorld != null) {
                    PlayWorldScreen(
                        world = playingWorld,
                        appearance = appearance,
                        participants = participants,
                        userCoins = user.rotoxCoins,
                        pendingRequestsCount = pendingCount,
                        onAwardCoins = viewModel::awardPlayCoins,
                        onOpenRequestsMenu = { viewModel.setRequestsDialogVisible(true) },
                        onExitWorld = viewModel::leaveWorld
                    )
                } else {
                    var showAccountMenu by remember { mutableStateOf(false) }

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        contentWindowInsets = WindowInsets(0, 0, 0, 0),
                        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                        topBar = {
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .windowInsetsPadding(WindowInsets.statusBars)
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    RotoxBrandBadge()

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        CoinChip(coins = user.rotoxCoins)
                                        FriendRequestsButton(
                                            pendingCount = pendingCount,
                                            onClick = { viewModel.setRequestsDialogVisible(true) },
                                            compact = true
                                        )
                                        Box {
                                            IconButton(
                                                onClick = { showAccountMenu = true },
                                                modifier = Modifier.testTag("account_menu_button")
                                            ) {
                                                MiniBlockAvatarBadge(user = user, sizeDp = 36)
                                            }
                                            DropdownMenu(
                                                expanded = showAccountMenu,
                                                onDismissRequest = { showAccountMenu = false }
                                            ) {
                                                DropdownMenuItem(
                                                    text = { Text("Cuenta: ${user.username}") },
                                                    onClick = { showAccountMenu = false },
                                                    enabled = false
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("Cerrar sesión") },
                                                    leadingIcon = {
                                                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                                                    },
                                                    onClick = {
                                                        showAccountMenu = false
                                                        viewModel.logout()
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        bottomBar = {
                            NavigationBar(
                                windowInsets = WindowInsets.navigationBars,
                                containerColor = MaterialTheme.colorScheme.surface
                            ) {
                                LobbyTab.entries.forEach { tab ->
                                    val selected = selectedTab == tab
                                    val icon = when (tab) {
                                        LobbyTab.GAMES -> if (selected) Icons.Filled.Games else Icons.Outlined.Games
                                        LobbyTab.AVATAR -> if (selected) Icons.Filled.Face else Icons.Outlined.Face
                                        LobbyTab.CREATE -> if (selected) Icons.Filled.Build else Icons.Outlined.Build
                                        LobbyTab.FRIENDS -> if (selected) Icons.Filled.Group else Icons.Outlined.Group
                                    }
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = { viewModel.selectTab(tab) },
                                        icon = { Icon(icon, contentDescription = tab.label) },
                                        label = { Text(tab.label, maxLines = 1) },
                                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (selectedTab) {
                                LobbyTab.GAMES -> GamesScreen(
                                    worlds = filteredWorlds,
                                    searchQuery = gameSearch,
                                    selectedCategory = selectedCategory,
                                    onSearchChange = viewModel::updateGameSearch,
                                    onCategorySelect = viewModel::selectGameCategory,
                                    onPlayWorld = viewModel::enterWorld,
                                    onLikeWorld = viewModel::likeWorld
                                )

                                LobbyTab.AVATAR -> AvatarScreen(
                                    user = user,
                                    appearance = appearance,
                                    accessories = allAccessories,
                                    ownedIds = ownedIds,
                                    onBodyColorChange = viewModel::updateBodyColor,
                                    onEquipAccessory = viewModel::equipAccessory,
                                    onBuyAccessory = viewModel::buyAccessory,
                                    onToggleForSale = viewModel::toggleAccessorySale
                                )

                                LobbyTab.CREATE -> CreateStudioScreen(
                                    baseAppearance = appearance,
                                    onPublishWorld = viewModel::publishWorld,
                                    onCreateAccessory = viewModel::createAndSellAccessory
                                )

                                LobbyTab.FRIENDS -> FriendsScreen(
                                    searchQuery = friendSearch,
                                    otherUsers = otherUsers,
                                    friends = friends,
                                    requests = requests,
                                    worlds = allWorlds,
                                    pendingCount = pendingCount,
                                    onSearchChange = viewModel::updateFriendSearch,
                                    onOpenRequestsMenu = { viewModel.setRequestsDialogVisible(true) },
                                    onSendFriendRequest = viewModel::sendFriendRequest,
                                    onAcceptRequest = viewModel::acceptFriendRequest,
                                    onJoinFriend = viewModel::joinFriendWorld
                                )
                            }
                        }
                    }
                }

                if (showRequestsDialog) {
                    FriendRequestsMenuDialog(
                        requests = requests,
                        friends = friends,
                        inWorldMode = playingWorld != null,
                        onDismiss = { viewModel.setRequestsDialogVisible(false) },
                        onAccept = viewModel::acceptFriendRequest,
                        onReject = viewModel::rejectFriendRequest,
                        onSimulateAcceptOutgoing = viewModel::simulateFriendAccept,
                        onJoinFriend = viewModel::joinFriendWorld,
                        onBringFriendToMyWorld = { friend ->
                            viewModel.inviteFriendToMyWorld(friend)
                            viewModel.setRequestsDialogVisible(false)
                        }
                    )
                }
            }
        }
    }
}

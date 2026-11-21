package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.FriendRequestUiModel
import com.example.data.FriendUiModel
import com.example.data.UserEntity
import com.example.data.WorldEntity
import com.example.ui.components.FriendRequestsButton
import com.example.ui.components.MiniBlockAvatarBadge
import com.example.ui.theme.RotoxBlue
import com.example.ui.theme.RotoxGreen

@Composable
fun FriendsScreen(
    searchQuery: String,
    otherUsers: List<UserEntity>,
    friends: List<FriendUiModel>,
    requests: List<FriendRequestUiModel>,
    worlds: List<WorldEntity>,
    pendingCount: Int,
    onSearchChange: (String) -> Unit,
    onOpenRequestsMenu: () -> Unit,
    onSendFriendRequest: (Long) -> Unit,
    onAcceptRequest: (Long) -> Unit,
    onJoinFriend: (FriendUiModel) -> Unit
) {
    val worldsById = worlds.associateBy { it.id }
    val q = searchQuery.trim().lowercase()
    val searchResults = otherUsers.filter { user ->
        q.isEmpty() || user.username.lowercase().contains(q)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Amigos y Jugadores",
                    style = MaterialTheme.typography.headlineSmall
                )
                FriendRequestsButton(
                    pendingCount = pendingCount,
                    onClick = onOpenRequestsMenu
                )
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Buscar amigos por usuario...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Buscar amigos") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Filled.Close, contentDescription = "Limpiar")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("friends_search_input")
            )
        }

        if (friends.isNotEmpty()) {
            item {
                Text(
                    text = "Tus Amigos (${friends.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = RotoxGreen
                )
            }
            items(friends, key = { "friend_${it.user.id}" }) { friend ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.5.dp, RotoxGreen)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MiniBlockAvatarBadge(friend.user, sizeDp = 46)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = friend.user.username,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = friend.currentWorldTitle?.let { "Jugando: $it" } ?: "En el Lobby",
                                style = MaterialTheme.typography.bodySmall,
                                color = RotoxBlue
                            )
                        }
                        Button(
                            onClick = { onJoinFriend(friend) },
                            colors = ButtonDefaults.buttonColors(containerColor = RotoxGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("friends_tab_join_${friend.user.id}")
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Unirse", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (q.isEmpty()) "Jugadores en ROTOX[ALFA]" else "Resultados para \"$searchQuery\"",
                style = MaterialTheme.typography.titleMedium
            )
        }

        if (searchResults.isEmpty()) {
            item {
                Text(
                    text = "No encontramos ningún jugador con ese nombre.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(searchResults, key = { "player_${it.id}" }) { player ->
                val rel = requests.firstOrNull { it.otherUser.id == player.id }
                val worldName = player.currentWorldId?.let { worldsById[it]?.title }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MiniBlockAvatarBadge(player, sizeDp = 44)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = player.username,
                                style = MaterialTheme.typography.titleSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = worldName?.let { "En: $it" } ?: player.onlineStatus,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        when {
                            rel?.status == "ACCEPTED" -> {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.tertiaryContainer
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Filled.Check, contentDescription = null, tint = RotoxGreen, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Amigos", style = MaterialTheme.typography.labelMedium, color = RotoxGreen)
                                    }
                                }
                            }
                            rel?.status == "PENDING" && rel.isIncoming -> {
                                Button(
                                    onClick = { onAcceptRequest(rel.requestId) },
                                    colors = ButtonDefaults.buttonColors(containerColor = RotoxGreen),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Aceptar")
                                }
                            }
                            rel?.status == "PENDING" -> {
                                OutlinedButton(
                                    onClick = onOpenRequestsMenu,
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Filled.Schedule, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Enviada")
                                }
                            }
                            else -> {
                                Button(
                                    onClick = { onSendFriendRequest(player.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = RotoxBlue),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("send_friend_request_${player.id}")
                                ) {
                                    Icon(Icons.Filled.PersonAdd, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Solicitar", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

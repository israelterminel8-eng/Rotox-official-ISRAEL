package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.rounded.MonetizationOn
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.FriendRequestUiModel
import com.example.data.FriendUiModel
import com.example.data.UserEntity
import com.example.ui.theme.RotoxBlue
import com.example.ui.theme.RotoxGold
import com.example.ui.theme.RotoxGreen
import com.example.ui.theme.RotoxRed
import com.example.ui.theme.RotoxYellow

@Composable
fun RotoxBrandBadge(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Classic tilted red & yellow brick emblem
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(RotoxRed)
                .border(2.dp, Color.White, RoundedCornerShape(7.dp)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(RotoxYellow)
            )
        }
        Text(
            text = "ROTOX[ALFA]",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun CoinChip(coins: Int, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFFFFF3C4),
        border = BorderStroke(1.5.dp, RotoxGold)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.MonetizationOn,
                contentDescription = "Rotox Coins",
                tint = Color(0xFFB37B00),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "$coins R$",
                style = MaterialTheme.typography.labelLarge,
                color = Color(0xFF6B4800),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun FriendRequestsButton(
    pendingCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    Surface(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .testTag("friend_requests_menu_button")
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = if (pendingCount > 0) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            1.5.dp,
            if (pendingCount > 0) RotoxRed else MaterialTheme.colorScheme.outline
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = if (compact) 10.dp else 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            BadgedBox(
                badge = {
                    if (pendingCount > 0) {
                        Badge(containerColor = RotoxRed, contentColor = Color.White) {
                            Text(pendingCount.toString())
                        }
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = "Menú de solicitudes de amigos",
                    tint = if (pendingCount > 0) RotoxRed else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
            if (!compact) {
                Text(
                    text = "Solicitudes",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun MiniBlockAvatarBadge(
    user: UserEntity,
    modifier: Modifier = Modifier,
    sizeDp: Int = 44
) {
    val headColor = Color(user.headColorHex or 0xFF000000L)
    val torsoColor = Color(user.torsoColorHex or 0xFF000000L)
    Box(
        modifier = modifier
            .size(sizeDp.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size((sizeDp * 0.40f).dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(headColor)
                    .border(1.dp, Color(0x44000000), RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .width((sizeDp * 0.56f).dp)
                    .height((sizeDp * 0.34f).dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(torsoColor)
                    .border(1.dp, Color(0x44000000), RoundedCornerShape(4.dp))
            )
        }
    }
}

@Composable
fun FriendRequestsMenuDialog(
    requests: List<FriendRequestUiModel>,
    friends: List<FriendUiModel>,
    inWorldMode: Boolean,
    onDismiss: () -> Unit,
    onAccept: (Long) -> Unit,
    onReject: (Long) -> Unit,
    onSimulateAcceptOutgoing: (Long) -> Unit,
    onJoinFriend: (FriendUiModel) -> Unit,
    onBringFriendToMyWorld: (FriendUiModel) -> Unit
) {
    val incoming = requests.filter { it.isIncoming && it.status == "PENDING" }
    val outgoing = requests.filter { !it.isIncoming && it.status == "PENDING" }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 560.dp),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Group,
                            contentDescription = null,
                            tint = RotoxBlue
                        )
                        Text(
                            text = "Solicitudes y Amigos",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_requests_dialog")
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Cerrar menú")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    item {
                        Text(
                            text = "Solicitudes recibidas (${incoming.size})",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (incoming.isEmpty()) {
                        item {
                            Text(
                                text = "No tienes solicitudes pendientes por ahora.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    } else {
                        items(incoming, key = { "in_${it.requestId}" }) { req ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    MiniBlockAvatarBadge(req.otherUser, sizeDp = 40)
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = req.otherUser.username,
                                            style = MaterialTheme.typography.titleSmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Quiere ser tu amigo",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Button(
                                        onClick = { onAccept(req.requestId) },
                                        colors = ButtonDefaults.buttonColors(containerColor = RotoxGreen),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                        modifier = Modifier.testTag("accept_request_${req.requestId}")
                                    ) {
                                        Icon(Icons.Filled.Check, contentDescription = "Aceptar", modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Aceptar")
                                    }
                                    OutlinedButton(
                                        onClick = { onReject(req.requestId) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                        border = BorderStroke(1.5.dp, RotoxRed),
                                        modifier = Modifier.testTag("reject_request_${req.requestId}")
                                    ) {
                                        Icon(Icons.Filled.Close, contentDescription = "Rechazar", tint = RotoxRed, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }

                    if (outgoing.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Solicitudes enviadas (${outgoing.size})",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        items(outgoing, key = { "out_${it.requestId}" }) { req ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    MiniBlockAvatarBadge(req.otherUser, sizeDp = 36)
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(req.otherUser.username, style = MaterialTheme.typography.titleSmall)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Filled.Schedule, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Spacer(Modifier.width(4.dp))
                                            Text("Pendiente", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    if (req.otherUser.isCommunityBot) {
                                        OutlinedButton(
                                            onClick = { onSimulateAcceptOutgoing(req.requestId) },
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text("Confirmar", style = MaterialTheme.typography.labelMedium)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Amigos listos para jugar (${friends.size})",
                            style = MaterialTheme.typography.titleSmall,
                            color = RotoxGreen
                        )
                    }
                    if (friends.isEmpty()) {
                        item {
                            Text(
                                text = "Acepta una solicitud o busca jugadores para unirte a sus mundos 3D.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(friends, key = { "fr_${it.user.id}" }) { friend ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    MiniBlockAvatarBadge(friend.user, sizeDp = 40)
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(friend.user.username, style = MaterialTheme.typography.titleSmall)
                                        Text(
                                            text = friend.currentWorldTitle?.let { "En: $it" } ?: "En el Lobby",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = RotoxBlue,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    if (inWorldMode) {
                                        Button(
                                            onClick = { onBringFriendToMyWorld(friend) },
                                            colors = ButtonDefaults.buttonColors(containerColor = RotoxBlue),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                            modifier = Modifier.testTag("invite_friend_${friend.user.id}")
                                        ) {
                                            Icon(Icons.Filled.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Que se una")
                                        }
                                    } else {
                                        Button(
                                            onClick = { onJoinFriend(friend) },
                                            colors = ButtonDefaults.buttonColors(containerColor = RotoxGreen),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                            modifier = Modifier.testTag("join_friend_${friend.user.id}")
                                        ) {
                                            Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Unirse")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

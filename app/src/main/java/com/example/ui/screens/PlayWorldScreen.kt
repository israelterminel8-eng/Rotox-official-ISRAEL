package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.data.AvatarAppearance
import com.example.data.BlockSerializer
import com.example.data.NpcAvatarState
import com.example.data.SkyTheme
import com.example.data.WorldEntity
import com.example.engine3d.ProjectedLabel
import com.example.engine3d.RenderMode
import com.example.engine3d.Rotox3DView
import com.example.ui.components.CoinChip
import com.example.ui.components.FriendRequestsButton
import com.example.ui.theme.RotoxBlue
import com.example.ui.theme.RotoxGold
import com.example.ui.theme.RotoxGreen
import com.example.ui.theme.RotoxRed
import kotlin.math.hypot
import kotlin.math.roundToInt

@Composable
fun PlayWorldScreen(
    world: WorldEntity,
    appearance: AvatarAppearance,
    participants: List<NpcAvatarState>,
    userCoins: Int,
    pendingRequestsCount: Int,
    onAwardCoins: (Int) -> Unit,
    onOpenRequestsMenu: () -> Unit,
    onExitWorld: () -> Unit
) {
    BackHandler { onExitWorld() }

    val blocks = remember(world.blocksJson) { BlockSerializer.decodeBlocks(world.blocksJson) }
    val skyTheme = remember(world.skyTheme) { SkyTheme.fromId(world.skyTheme) }

    var joyX by remember { mutableFloatStateOf(0f) }
    var joyY by remember { mutableFloatStateOf(0f) }
    var jumpSignal by remember { mutableIntStateOf(0) }
    var resetSignal by remember { mutableIntStateOf(0) }
    var goalReachedBanner by remember { mutableStateOf(false) }
    var lavaNotice by remember { mutableStateOf(false) }
    var sessionCoins by remember { mutableIntStateOf(0) }
    var projectedLabels by remember { mutableStateOf<List<ProjectedLabel>>(emptyList()) }

    Box(modifier = Modifier.fillMaxSize()) {
        Rotox3DView(
            mode = RenderMode.PLAY,
            avatarAppearance = appearance,
            skyTheme = skyTheme,
            worldBlocks = blocks,
            npcAvatars = participants,
            joyX = joyX,
            joyY = joyY,
            jumpSignal = jumpSignal,
            resetSignal = resetSignal,
            onCoinsCollectedDelta = { delta ->
                sessionCoins += delta
                onAwardCoins(delta)
            },
            onGoalReached = {
                if (!goalReachedBanner) {
                    goalReachedBanner = true
                    sessionCoins += 50
                    onAwardCoins(50)
                }
            },
            onLavaHit = {
                lavaNotice = true
            },
            onProjectedLabels = { labels ->
                projectedLabels = labels
            },
            modifier = Modifier.fillMaxSize()
        )

        // Projected 3D Friend/Player Nametags
        val density = LocalDensity.current
        projectedLabels.forEach { label ->
            val offsetX = label.screenX.roundToInt() - with(density) { 55.dp.roundToPx() }
            val offsetY = label.screenY.roundToInt() - with(density) { 26.dp.roundToPx() }
            Surface(
                modifier = Modifier.offset { IntOffset(offsetX, offsetY) },
                shape = RoundedCornerShape(8.dp),
                color = if (label.isFriend) Color(0xE62EA043) else Color(0xD91A2230),
                border = BorderStroke(1.dp, Color.White)
            ) {
                Text(
                    text = if (label.isFriend) "${label.username} (Amigo)" else label.username,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        // Top HUD Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xE6FFFFFF),
                border = BorderStroke(1.5.dp, Color(0xFFC8D2E0)),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onExitWorld,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("exit_world_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Salir del mundo",
                            tint = RotoxRed
                        )
                    }
                    Column {
                        Text(
                            text = world.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = Color(0xFF1A2230),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${participants.size + 1} jugadores en 3D",
                            style = MaterialTheme.typography.labelSmall,
                            color = RotoxBlue
                        )
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CoinChip(coins = userCoins)
                FriendRequestsButton(
                    pendingCount = pendingRequestsCount,
                    onClick = onOpenRequestsMenu,
                    compact = true
                )
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xE6FFFFFF),
                    border = BorderStroke(1.5.dp, Color(0xFFC8D2E0))
                ) {
                    IconButton(
                        onClick = {
                            goalReachedBanner = false
                            lavaNotice = false
                            resetSignal++
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Reaparecer", tint = Color(0xFF1A2230))
                    }
                }
            }
        }

        // Goal celebration or lava alert banner
        if (goalReachedBanner) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 74.dp, start = 20.dp, end = 20.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xF5FFF3C4),
                border = BorderStroke(2.dp, RotoxGold)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = RotoxGold, modifier = Modifier.size(30.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("¡Meta alcanzada! +50 R$", style = MaterialTheme.typography.titleSmall, color = Color(0xFF6B4800))
                        Text("Ganaste $sessionCoins R$ en esta partida.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF6B4800))
                    }
                    Button(
                        onClick = {
                            goalReachedBanner = false
                            resetSignal++
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RotoxGreen)
                    ) {
                        Text("Otra vez")
                    }
                }
            }
        } else if (lavaNotice) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 74.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xE6D92B2B)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("¡Tocaste la lava! Reapareciendo...", color = Color.White, style = MaterialTheme.typography.labelLarge)
                    Surface(
                        onClick = { lavaNotice = false },
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0x33FFFFFF)
                    ) {
                        Text("OK", color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                    }
                }
            }
        }

        // Bottom Controls: Virtual Thumbstick (left) + Jump Button (right)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp, vertical = 22.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            VirtualJoystick(
                onMove = { nx, ny ->
                    joyX = nx
                    joyY = ny
                }
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xAA1A2230)
                ) {
                    Text(
                        text = "Arrastra pantalla para cámara",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Spacer(Modifier.size(8.dp))
                Surface(
                    onClick = { jumpSignal++ },
                    shape = CircleShape,
                    color = RotoxGreen,
                    border = BorderStroke(3.dp, Color.White),
                    modifier = Modifier
                        .size(78.dp)
                        .testTag("jump_3d_button")
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowUpward,
                            contentDescription = "Saltar",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                        Text(
                            text = "SALTAR",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VirtualJoystick(
    onMove: (Float, Float) -> Unit
) {
    val maxRadiusPx = with(LocalDensity.current) { 42.dp.toPx() }
    var knobX by remember { mutableFloatStateOf(0f) }
    var knobY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .size(124.dp)
            .clip(CircleShape)
            .background(Color(0x661A2230))
            .border(2.dp, Color(0xCCFFFFFF), CircleShape)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        knobX = 0f
                        knobY = 0f
                        onMove(0f, 0f)
                    },
                    onDragCancel = {
                        knobX = 0f
                        knobY = 0f
                        onMove(0f, 0f)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val rawX = knobX + dragAmount.x
                        val rawY = knobY + dragAmount.y
                        val dist = hypot(rawX, rawY)
                        if (dist > maxRadiusPx) {
                            val scale = maxRadiusPx / dist
                            knobX = rawX * scale
                            knobY = rawY * scale
                        } else {
                            knobX = rawX
                            knobY = rawY
                        }
                        onMove(knobX / maxRadiusPx, knobY / maxRadiusPx)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(knobX.roundToInt(), knobY.roundToInt()) }
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xEEFFFFFF))
                .border(2.dp, RotoxBlue, CircleShape)
        )
    }
}

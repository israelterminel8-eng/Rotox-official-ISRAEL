package com.example.ui.screens

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.AccessoryPart
import com.example.data.AccessorySlot
import com.example.data.AvatarAppearance
import com.example.data.BlockType
import com.example.data.ClassicBrickColors
import com.example.data.SkyTheme
import com.example.data.WorldBlock
import com.example.engine3d.RenderMode
import com.example.engine3d.Rotox3DView
import com.example.ui.theme.RotoxBlue
import com.example.ui.theme.RotoxGreen
import com.example.ui.theme.RotoxRed
import kotlin.math.abs

@Composable
fun CreateStudioScreen(
    baseAppearance: AvatarAppearance,
    onPublishWorld: (
        title: String,
        description: String,
        category: String,
        skyTheme: SkyTheme,
        blocks: List<WorldBlock>,
        playImmediately: Boolean
    ) -> Unit,
    onCreateAccessory: (
        name: String,
        slot: AccessorySlot,
        price: Int,
        parts: List<AccessoryPart>,
        putForSale: Boolean,
        onDone: () -> Unit
    ) -> Unit
) {
    var studioMode by remember { mutableIntStateOf(0) } // 0 = Crear Juego 3D, 1 = Crear Accesorio 3D

    Column(modifier = Modifier.fillMaxSize()) {
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            SegmentedButton(
                selected = studioMode == 0,
                onClick = { studioMode = 0 },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                modifier = Modifier.testTag("studio_tab_world")
            ) {
                Text("Crear Juego 3D")
            }
            SegmentedButton(
                selected = studioMode == 1,
                onClick = { studioMode = 1 },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                modifier = Modifier.testTag("studio_tab_accessory")
            ) {
                Text("Crear Accesorio 3D")
            }
        }

        if (studioMode == 0) {
            WorldBuilderStudio(onPublishWorld = onPublishWorld)
        } else {
            AccessoryCrafterStudio(
                baseAppearance = baseAppearance,
                onCreateAccessory = onCreateAccessory
            )
        }
    }
}

@Composable
private fun WorldBuilderStudio(
    onPublishWorld: (String, String, String, SkyTheme, List<WorldBlock>, Boolean) -> Unit
) {
    val paletteTypes = remember {
        listOf(
            BlockType.BRICK,
            BlockType.GRASS,
            BlockType.WOOD,
            BlockType.STONE,
            BlockType.GOLD,
            BlockType.BLUE_BLOCK,
            BlockType.COIN,
            BlockType.LAVA,
            BlockType.GOAL
        )
    }

    var selectedBlockType by remember { mutableStateOf(BlockType.BRICK) }
    var eraseMode by remember { mutableStateOf(false) }
    var selectedSky by remember { mutableStateOf(SkyTheme.DAY) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Sandbox") }

    val blocks = remember {
        mutableStateListOf(
            WorldBlock(0f, 0.25f, 0f, 2f, 0.5f, 2f, BlockType.SPAWN.defaultColorHex, BlockType.SPAWN),
            WorldBlock(0f, 0.5f, -3f, 1f, 1f, 1f, BlockType.BRICK.defaultColorHex, BlockType.BRICK),
            WorldBlock(1f, 0.5f, -3f, 1f, 1f, 1f, BlockType.BRICK.defaultColorHex, BlockType.BRICK),
            WorldBlock(0f, 1.5f, -3f, 0.6f, 0.6f, 0.25f, BlockType.COIN.defaultColorHex, BlockType.COIN)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // 3D Interactive Studio Viewport
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(Color(selectedSky.skyRed, selectedSky.skyGreen, selectedSky.skyBlue))
        ) {
            Rotox3DView(
                mode = RenderMode.BUILD_WORLD,
                skyTheme = selectedSky,
                worldBlocks = blocks.toList(),
                onBuildTap = { (hx, hy, hz) ->
                    if (eraseMode) {
                        val targetIdx = blocks.indexOfLast {
                            it.type != BlockType.SPAWN &&
                                abs(it.x - hx) < 0.8f &&
                                abs(it.z - hz) < 0.8f
                        }
                        if (targetIdx >= 0) blocks.removeAt(targetIdx)
                    } else {
                        val size = if (selectedBlockType == BlockType.COIN) Triple(0.6f, 0.6f, 0.25f) else Triple(1f, 1f, 1f)
                        blocks.add(
                            WorldBlock(
                                x = hx,
                                y = hy,
                                z = hz,
                                w = size.first,
                                h = size.second,
                                d = size.third,
                                colorHex = selectedBlockType.defaultColorHex,
                                type = selectedBlockType
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xCCFFFFFF)
            ) {
                Text(
                    text = if (eraseMode) "Toca un bloque para borrar" else "Toca el suelo 3D para poner bloques • ${blocks.size} bloques",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF1A2230),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (eraseMode) RotoxRed else Color(0xDDFFFFFF),
                    modifier = Modifier.clickable { eraseMode = !eraseMode }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Modo borrar",
                            tint = if (eraseMode) Color.White else RotoxRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (eraseMode) "Borrando" else "Borrar",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (eraseMode) Color.White else Color(0xFF1A2230)
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xDDFFFFFF),
                    modifier = Modifier.clickable {
                        if (blocks.size > 1) blocks.removeAt(blocks.lastIndex)
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Deshacer",
                            tint = Color(0xFF1A2230),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Deshacer", style = MaterialTheme.typography.labelSmall, color = Color(0xFF1A2230))
                    }
                }
            }
        }

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Elige el bloque 3D a colocar:", style = MaterialTheme.typography.titleSmall)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(paletteTypes) { type ->
                    val selected = !eraseMode && selectedBlockType == type
                    FilterChip(
                        selected = selected,
                        onClick = {
                            eraseMode = false
                            selectedBlockType = type
                        },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(type.defaultColorHex or 0xFF000000L))
                            )
                        },
                        label = { Text(type.label) }
                    )
                }
            }

            Text("Tema del cielo y suelo:", style = MaterialTheme.typography.titleSmall)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(SkyTheme.entries) { theme ->
                    FilterChip(
                        selected = selectedSky == theme,
                        onClick = { selectedSky = theme },
                        label = { Text(theme.label) }
                    )
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Nombre de tu juego 3D") },
                placeholder = { Text("Ej: Mi Parkour de Ladrillos") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("world_title_input")
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                placeholder = { Text("Cuenta de qué trata tu mundo 3D...") },
                maxLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("Sandbox", "Obby", "Ciudad", "Aventura", "Castillo")) { cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick = { category = cat },
                        label = { Text(cat) }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        onPublishWorld(title, description, category, selectedSky, blocks.toList(), false)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Publicar")
                }
                Button(
                    onClick = {
                        onPublishWorld(title, description, category, selectedSky, blocks.toList(), true)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RotoxGreen),
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("publish_and_play_world_button")
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Publicar y Jugar")
                }
            }
        }
    }
}

@Composable
private fun AccessoryCrafterStudio(
    baseAppearance: AvatarAppearance,
    onCreateAccessory: (String, AccessorySlot, Int, List<AccessoryPart>, Boolean, () -> Unit) -> Unit
) {
    var slot by remember { mutableStateOf(AccessorySlot.HAT) }
    var name by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("60") }
    var putForSale by remember { mutableStateOf(true) }

    val parts = remember {
        mutableStateListOf(
            AccessoryPart(0f, 0.30f, 0f, 0.72f, 0.22f, 0.72f, 0xFFD92B2B),
            AccessoryPart(0f, 0.48f, 0f, 0.32f, 0.20f, 0.32f, 0xFFF5C518)
        )
    }
    var selectedPartIndex by remember { mutableIntStateOf(0) }
    var partColorHex by remember { mutableStateOf(0xFFD92B2BL) }
    var offsetY by remember { mutableFloatStateOf(0.30f) }
    var offsetZ by remember { mutableFloatStateOf(0f) }
    var sizeScale by remember { mutableFloatStateOf(0.45f) }

    val previewAppearance = remember(baseAppearance, slot, parts.toList()) {
        when (slot) {
            AccessorySlot.HAT -> baseAppearance.copy(hatParts = parts.toList())
            AccessorySlot.FACE -> baseAppearance.copy(faceParts = parts.toList())
            AccessorySlot.BACK -> baseAppearance.copy(backParts = parts.toList())
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(Color(0xFF73C2FB))
        ) {
            Rotox3DView(
                mode = RenderMode.ACCESSORY_PREVIEW,
                avatarAppearance = previewAppearance,
                modifier = Modifier.fillMaxSize()
            )
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xCCFFFFFF)
            ) {
                Text(
                    text = "Vista previa 3D en tu avatar • ${parts.size} piezas",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF1A2230),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("¿Dónde va el accesorio?", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AccessorySlot.entries.forEach { s ->
                    FilterChip(
                        selected = slot == s,
                        onClick = {
                            slot = s
                            parts.clear()
                            when (s) {
                                AccessorySlot.HAT -> {
                                    parts += AccessoryPart(0f, 0.30f, 0f, 0.72f, 0.22f, 0.72f, partColorHex)
                                    offsetY = 0.30f
                                    offsetZ = 0f
                                }
                                AccessorySlot.FACE -> {
                                    parts += AccessoryPart(0f, 0.06f, 0.36f, 0.56f, 0.16f, 0.10f, partColorHex)
                                    offsetY = 0.06f
                                    offsetZ = 0.36f
                                }
                                AccessorySlot.BACK -> {
                                    parts += AccessoryPart(0f, 0.05f, -0.34f, 0.72f, 0.80f, 0.28f, partColorHex)
                                    offsetY = 0.05f
                                    offsetZ = -0.34f
                                }
                            }
                            selectedPartIndex = 0
                        },
                        label = { Text(s.label) }
                    )
                }
            }

            // Manage 3D parts
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Piezas 3D (${parts.size}/6)", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            if (parts.size < 6) {
                                val newPart = when (slot) {
                                    AccessorySlot.HAT -> AccessoryPart(0f, 0.45f, 0f, 0.35f, 0.22f, 0.35f, partColorHex)
                                    AccessorySlot.FACE -> AccessoryPart(0f, 0.10f, 0.40f, 0.22f, 0.16f, 0.12f, partColorHex)
                                    AccessorySlot.BACK -> AccessoryPart(0f, 0.22f, -0.45f, 0.48f, 0.34f, 0.22f, partColorHex)
                                }
                                parts.add(newPart)
                                selectedPartIndex = parts.lastIndex
                                offsetY = newPart.oy
                                offsetZ = newPart.oz
                                sizeScale = newPart.sx
                            }
                        },
                        enabled = parts.size < 6,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Añadir pieza")
                    }
                    if (parts.size > 1) {
                        IconButton(
                            onClick = {
                                val idx = selectedPartIndex.coerceIn(0, parts.lastIndex)
                                parts.removeAt(idx)
                                selectedPartIndex = (idx - 1).coerceAtLeast(0)
                            }
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = "Quitar pieza", tint = RotoxRed)
                        }
                    }
                }
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(parts.indices.toList()) { idx ->
                    FilterChip(
                        selected = selectedPartIndex == idx,
                        onClick = {
                            selectedPartIndex = idx
                            val p = parts[idx]
                            partColorHex = p.colorHex
                            offsetY = p.oy
                            offsetZ = p.oz
                            sizeScale = p.sx
                        },
                        label = { Text("Pieza ${idx + 1}") }
                    )
                }
            }

            // Color picker for selected part
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ClassicBrickColors) { (_, hex) ->
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(hex or 0xFF000000L))
                            .border(
                                width = if (partColorHex == hex) 3.dp else 1.dp,
                                color = if (partColorHex == hex) RotoxBlue else Color(0x44000000),
                                shape = CircleShape
                            )
                            .clickable {
                                partColorHex = hex
                                val idx = selectedPartIndex.coerceIn(0, parts.lastIndex)
                                parts[idx] = parts[idx].copy(colorHex = hex)
                            }
                    )
                }
            }

            // Live 3D sliders for selected part
            Column {
                Text("Altura de pieza: ${"%.2f".format(offsetY)}", style = MaterialTheme.typography.labelMedium)
                Slider(
                    value = offsetY,
                    onValueChange = { v ->
                        offsetY = v
                        val idx = selectedPartIndex.coerceIn(0, parts.lastIndex)
                        parts[idx] = parts[idx].copy(oy = v)
                    },
                    valueRange = -0.35f..0.75f
                )
                Text("Profundidad (Adelante/Atrás): ${"%.2f".format(offsetZ)}", style = MaterialTheme.typography.labelMedium)
                Slider(
                    value = offsetZ,
                    onValueChange = { v ->
                        offsetZ = v
                        val idx = selectedPartIndex.coerceIn(0, parts.lastIndex)
                        parts[idx] = parts[idx].copy(oz = v)
                    },
                    valueRange = -0.60f..0.55f
                )
                Text("Tamaño de pieza: ${"%.2f".format(sizeScale)}", style = MaterialTheme.typography.labelMedium)
                Slider(
                    value = sizeScale,
                    onValueChange = { v ->
                        sizeScale = v
                        val idx = selectedPartIndex.coerceIn(0, parts.lastIndex)
                        parts[idx] = parts[idx].copy(sx = v, sy = (v * 0.45f).coerceAtLeast(0.10f), sz = v)
                    },
                    valueRange = 0.14f..0.95f
                )
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre del accesorio 3D") },
                placeholder = { Text("Ej: Corona Ladrillo Pro") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("accessory_name_input")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it.filter { c -> c.isDigit() } },
                    label = { Text("Precio (R$)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Vender", style = MaterialTheme.typography.titleSmall)
                    Switch(
                        checked = putForSale,
                        onCheckedChange = { putForSale = it }
                    )
                }
            }

            Button(
                onClick = {
                    val price = priceText.toIntOrNull() ?: 50
                    onCreateAccessory(name, slot, price, parts.toList(), putForSale) {
                        name = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = RotoxGreen),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("create_sell_accessory_button")
            ) {
                Icon(Icons.Filled.Sell, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text(
                    text = if (putForSale) "Crear y Vender en Tienda 3D" else "Crear y Equipar en Avatar",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

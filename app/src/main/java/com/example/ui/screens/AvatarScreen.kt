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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.AccessoryEntity
import com.example.data.AccessorySlot
import com.example.data.AvatarAppearance
import com.example.data.BlockSerializer
import com.example.data.BodyPart
import com.example.data.ClassicBrickColors
import com.example.data.UserEntity
import com.example.engine3d.RenderMode
import com.example.engine3d.Rotox3DView
import com.example.ui.theme.RotoxBlue
import com.example.ui.theme.RotoxGold
import com.example.ui.theme.RotoxGreen
import com.example.ui.theme.RotoxRed

@Composable
fun AvatarScreen(
    user: UserEntity,
    appearance: AvatarAppearance,
    accessories: List<AccessoryEntity>,
    ownedIds: Set<String>,
    onBodyColorChange: (BodyPart, Long) -> Unit,
    onEquipAccessory: (AccessoryEntity) -> Unit,
    onBuyAccessory: (AccessoryEntity) -> Unit,
    onToggleForSale: (AccessoryEntity, Boolean, Int) -> Unit
) {
    var subTab by remember { mutableIntStateOf(0) } // 0 = Colores, 1 = Mis Accesorios, 2 = Tienda
    var selectedBodyPart by remember { mutableStateOf(BodyPart.TORSO) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Interactive 3D Avatar Orbit Preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(235.dp)
                .background(Color(0xFF73C2FB))
        ) {
            Rotox3DView(
                mode = RenderMode.SHOWCASE,
                avatarAppearance = appearance,
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
                    text = "${user.username} • Vista 3D",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF1A2230),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xBB1A2230)
            ) {
                Text(
                    text = "Gira 360° con el dedo",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                val tabs = listOf("Colores", "Equipar", "Tienda 3D")
                tabs.forEachIndexed { idx, title ->
                    SegmentedButton(
                        selected = subTab == idx,
                        onClick = { subTab = idx },
                        shape = SegmentedButtonDefaults.itemShape(index = idx, count = tabs.size),
                        modifier = Modifier.testTag("avatar_subtab_$idx")
                    ) {
                        Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            when (subTab) {
                0 -> {
                    Text(
                        text = "Elige una parte del cuerpo:",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(BodyPart.entries) { part ->
                            FilterChip(
                                selected = selectedBodyPart == part,
                                onClick = { selectedBodyPart = part },
                                label = { Text(part.label) }
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Color para ${selectedBodyPart.label}:",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(Modifier.height(8.dp))
                    val activeHex = when (selectedBodyPart) {
                        BodyPart.HEAD -> user.headColorHex
                        BodyPart.TORSO -> user.torsoColorHex
                        BodyPart.ARMS -> user.leftArmColorHex
                        BodyPart.LEGS -> user.leftLegColorHex
                    }
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 12.dp)
                    ) {
                        items(ClassicBrickColors.chunked(4)) { rowColors ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                rowColors.forEach { (label, hex) ->
                                    val selected = activeHex == hex
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { onBodyColorChange(selectedBodyPart, hex) }
                                            .testTag("color_swatch_${hex.toString(16)}"),
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(
                                            width = if (selected) 2.5.dp else 1.dp,
                                            color = if (selected) RotoxBlue else MaterialTheme.colorScheme.outline
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(hex or 0xFF000000L))
                                                    .border(1.dp, Color(0x44000000), CircleShape)
                                            )
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelSmall,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    val myItems = accessories.filter { ownedIds.contains(it.id) || it.creatorUsername == user.username }
                    if (myItems.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Aún no tienes accesorios. Compra uno en Tienda 3D o crea el tuyo en Crear 3D.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(myItems, key = { it.id }) { item ->
                                val isEquipped = user.equippedHatId == item.id ||
                                    user.equippedFaceId == item.id ||
                                    user.equippedBackId == item.id
                                val isMine = item.creatorUsername == user.username
                                AccessoryRowCard(
                                    item = item,
                                    isOwned = true,
                                    isEquipped = isEquipped,
                                    isCreatedByMe = isMine,
                                    onEquip = { onEquipAccessory(item) },
                                    onBuy = {},
                                    onToggleSale = { forSale ->
                                        onToggleForSale(item, forSale, item.price)
                                    }
                                )
                            }
                        }
                    }
                }

                2 -> {
                    val shopItems = accessories.filter { it.isForSale }
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(shopItems, key = { it.id }) { item ->
                            val isOwned = ownedIds.contains(item.id)
                            val isEquipped = user.equippedHatId == item.id ||
                                user.equippedFaceId == item.id ||
                                user.equippedBackId == item.id
                            val isMine = item.creatorUsername == user.username
                            AccessoryRowCard(
                                item = item,
                                isOwned = isOwned,
                                isEquipped = isEquipped,
                                isCreatedByMe = isMine,
                                onEquip = { onEquipAccessory(item) },
                                onBuy = { onBuyAccessory(item) },
                                onToggleSale = { forSale ->
                                    onToggleForSale(item, forSale, item.price)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AccessoryRowCard(
    item: AccessoryEntity,
    isOwned: Boolean,
    isEquipped: Boolean,
    isCreatedByMe: Boolean,
    onEquip: () -> Unit,
    onBuy: () -> Unit,
    onToggleSale: (Boolean) -> Unit
) {
    val parts = BlockSerializer.decodeParts(item.partsJson)
    val previewHex = parts.firstOrNull()?.colorHex ?: 0xFFD92B2BL
    val slotLabel = AccessorySlot.fromId(item.slot).label

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (isEquipped) 2.dp else 1.5.dp,
            color = if (isEquipped) RotoxGreen else MaterialTheme.colorScheme.outline
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(previewHex or 0xFF000000L))
                    .border(1.5.dp, Color(0x44000000), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${parts.size}p",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$slotLabel • Por ${item.creatorUsername}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (item.isForSale) {
                    Text(
                        text = "${item.price} R$ • ${item.salesCount} ventas",
                        style = MaterialTheme.typography.labelMedium,
                        color = RotoxGold
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (isOwned) {
                    Button(
                        onClick = onEquip,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isEquipped) RotoxGreen else RotoxBlue
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        if (isEquipped) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Puesto")
                        } else {
                            Text("Equipar")
                        }
                    }
                } else {
                    Button(
                        onClick = onBuy,
                        colors = ButtonDefaults.buttonColors(containerColor = RotoxGreen),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("buy_acc_${item.id}")
                    ) {
                        Icon(Icons.Filled.ShoppingBag, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("${item.price} R$")
                    }
                }

                if (isCreatedByMe) {
                    OutlinedButton(
                        onClick = { onToggleSale(!item.isForSale) },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        border = BorderStroke(1.dp, if (item.isForSale) RotoxRed else RotoxBlue)
                    ) {
                        Icon(Icons.Filled.Sell, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (item.isForSale) "Quitar venta" else "Vender",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}

package com.example.data

enum class BlockType(val label: String, val defaultColorHex: Long, val isSolid: Boolean) {
    GRASS("Pasto", 0xFF2EA043, true),
    BRICK("Ladrillo", 0xFFD92B2B, true),
    WOOD("Madera", 0xFF9C6635, true),
    STONE("Piedra", 0xFF8C96A4, true),
    GOLD("Oro", 0xFFF5C518, true),
    BLUE_BLOCK("Bloque Azul", 0xFF1E6BE6, true),
    WATER("Agua", 0xFF2B95E6, false),
    LAVA("Lava", 0xFFFF5A1F, false),
    COIN("Moneda R$", 0xFFFFD52E, false),
    SPAWN("Inicio", 0xFF39C5BB, true),
    GOAL("Meta", 0xFFFFD700, true),
    TREE_TRUNK("Tronco", 0xFF6E4726, true),
    TREE_LEAVES("Hojas", 0xFF238636, true)
}

enum class SkyTheme(
    val id: String,
    val label: String,
    val skyRed: Float,
    val skyGreen: Float,
    val skyBlue: Float,
    val groundColorHex: Long
) {
    DAY("DAY", "Día Soleado", 0.45f, 0.76f, 0.98f, 0xFF38A849),
    SUNSET("SUNSET", "Atardecer", 0.95f, 0.64f, 0.44f, 0xFF558B2F),
    ISLAND("ISLAND", "Isla Tropical", 0.35f, 0.80f, 0.96f, 0xFF1E88E5),
    CASTLE("CASTLE", "Tarde Medieval", 0.54f, 0.72f, 0.90f, 0xFF4A7C3F);

    companion object {
        fun fromId(id: String): SkyTheme = entries.firstOrNull { it.id == id } ?: DAY
    }
}

enum class AccessorySlot(val id: String, val label: String) {
    HAT("HAT", "Cabeza"),
    FACE("FACE", "Cara"),
    BACK("BACK", "Espalda");

    companion object {
        fun fromId(id: String): AccessorySlot = entries.firstOrNull { it.id == id } ?: HAT
    }
}

enum class BodyPart(val label: String) {
    HEAD("Cabeza"),
    TORSO("Torso"),
    ARMS("Brazos"),
    LEGS("Piernas")
}

data class WorldBlock(
    val x: Float,
    val y: Float,
    val z: Float,
    val w: Float = 1f,
    val h: Float = 1f,
    val d: Float = 1f,
    val colorHex: Long,
    val type: BlockType = BlockType.BRICK
)

data class AccessoryPart(
    val ox: Float,
    val oy: Float,
    val oz: Float,
    val sx: Float,
    val sy: Float,
    val sz: Float,
    val colorHex: Long
)

data class AvatarAppearance(
    val headColorHex: Long = 0xFFF5CD30,
    val torsoColorHex: Long = 0xFF1E6BE6,
    val leftArmColorHex: Long = 0xFFF5CD30,
    val rightArmColorHex: Long = 0xFFF5CD30,
    val leftLegColorHex: Long = 0xFF2EA043,
    val rightLegColorHex: Long = 0xFF2EA043,
    val hatParts: List<AccessoryPart> = emptyList(),
    val faceParts: List<AccessoryPart> = emptyList(),
    val backParts: List<AccessoryPart> = emptyList()
)

data class NpcAvatarState(
    val userId: Long,
    val username: String,
    val appearance: AvatarAppearance,
    val x: Float,
    val y: Float,
    val z: Float,
    val yawDeg: Float,
    val isFriend: Boolean,
    val chatLine: String = ""
)

object BlockSerializer {
    fun encodeBlocks(blocks: List<WorldBlock>): String =
        blocks.joinToString(";") { b ->
            "${b.x},${b.y},${b.z},${b.w},${b.h},${b.d},${b.colorHex.toString(16)},${b.type.name}"
        }

    fun decodeBlocks(raw: String): List<WorldBlock> {
        if (raw.isBlank()) return emptyList()
        return raw.split(";").mapNotNull { entry ->
            val p = entry.split(",")
            if (p.size < 8) return@mapNotNull null
            val x = p[0].toFloatOrNull() ?: return@mapNotNull null
            val y = p[1].toFloatOrNull() ?: return@mapNotNull null
            val z = p[2].toFloatOrNull() ?: return@mapNotNull null
            val w = p[3].toFloatOrNull() ?: 1f
            val h = p[4].toFloatOrNull() ?: 1f
            val d = p[5].toFloatOrNull() ?: 1f
            val color = p[6].toLongOrNull(16) ?: 0xFFD92B2BL
            val type = BlockType.entries.firstOrNull { it.name == p[7] } ?: BlockType.BRICK
            WorldBlock(x, y, z, w, h, d, color, type)
        }
    }

    fun encodeParts(parts: List<AccessoryPart>): String =
        parts.joinToString(";") { p ->
            "${p.ox},${p.oy},${p.oz},${p.sx},${p.sy},${p.sz},${p.colorHex.toString(16)}"
        }

    fun decodeParts(raw: String): List<AccessoryPart> {
        if (raw.isBlank()) return emptyList()
        return raw.split(";").mapNotNull { entry ->
            val p = entry.split(",")
            if (p.size < 7) return@mapNotNull null
            val ox = p[0].toFloatOrNull() ?: 0f
            val oy = p[1].toFloatOrNull() ?: 0f
            val oz = p[2].toFloatOrNull() ?: 0f
            val sx = p[3].toFloatOrNull() ?: 0.2f
            val sy = p[4].toFloatOrNull() ?: 0.2f
            val sz = p[5].toFloatOrNull() ?: 0.2f
            val color = p[6].toLongOrNull(16) ?: 0xFFD92B2BL
            AccessoryPart(ox, oy, oz, sx, sy, sz, color)
        }
    }
}

val ClassicBrickColors: List<Pair<String, Long>> = listOf(
    "Amarillo Clásico" to 0xFFF5CD30,
    "Rojo Ladrillo" to 0xFFD92B2B,
    "Azul Real" to 0xFF1E6BE6,
    "Verde Pasto" to 0xFF2EA043,
    "Naranja" to 0xFFF27D24,
    "Blanco" to 0xFFF4F6F9,
    "Gris Piedra" to 0xFF8C96A4,
    "Café Madera" to 0xFF8D5B34,
    "Carbón" to 0xFF2D333B,
    "Morado" to 0xFF7B42BC,
    "Celeste" to 0xFF39A2DB,
    "Arena" to 0xFFE5C487
)

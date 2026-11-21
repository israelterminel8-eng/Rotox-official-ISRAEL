package com.example.data

object DatabaseSeeder {

    fun defaultAccessories(): List<AccessoryEntity> = listOf(
        AccessoryEntity(
            id = "hat_red_cap",
            name = "Gorra Roja Clásica",
            slot = AccessorySlot.HAT.id,
            price = 40,
            creatorUsername = "ROTOX_Oficial",
            isForSale = true,
            partsJson = BlockSerializer.encodeParts(
                listOf(
                    AccessoryPart(0f, 0.28f, 0f, 0.72f, 0.22f, 0.72f, 0xFFD92B2B),
                    AccessoryPart(0f, 0.21f, 0.44f, 0.68f, 0.08f, 0.34f, 0xFFB01E1E),
                    AccessoryPart(0f, 0.36f, 0.37f, 0.24f, 0.12f, 0.05f, 0xFFF4F6F9)
                )
            )
        ),
        AccessoryEntity(
            id = "hat_top_hat",
            name = "Sombrero de Copa",
            slot = AccessorySlot.HAT.id,
            price = 85,
            creatorUsername = "BuilderAlex",
            isForSale = true,
            partsJson = BlockSerializer.encodeParts(
                listOf(
                    AccessoryPart(0f, 0.26f, 0f, 0.92f, 0.08f, 0.92f, 0xFF23272E),
                    AccessoryPart(0f, 0.36f, 0f, 0.64f, 0.14f, 0.64f, 0xFFD92B2B),
                    AccessoryPart(0f, 0.60f, 0f, 0.62f, 0.42f, 0.62f, 0xFF23272E)
                )
            )
        ),
        AccessoryEntity(
            id = "hat_golden_crown",
            name = "Corona Dorada Real",
            slot = AccessorySlot.HAT.id,
            price = 160,
            creatorUsername = "CastilloKing",
            isForSale = true,
            partsJson = BlockSerializer.encodeParts(
                listOf(
                    AccessoryPart(0f, 0.28f, 0f, 0.72f, 0.14f, 0.72f, 0xFFF5C518),
                    AccessoryPart(-0.26f, 0.42f, 0.26f, 0.14f, 0.18f, 0.14f, 0xFFFFD84D),
                    AccessoryPart(0.26f, 0.42f, 0.26f, 0.14f, 0.18f, 0.14f, 0xFFFFD84D),
                    AccessoryPart(-0.26f, 0.42f, -0.26f, 0.14f, 0.18f, 0.14f, 0xFFFFD84D),
                    AccessoryPart(0.26f, 0.42f, -0.26f, 0.14f, 0.18f, 0.14f, 0xFFFFD84D),
                    AccessoryPart(0f, 0.30f, 0.37f, 0.14f, 0.10f, 0.06f, 0xFFD92B2B)
                )
            )
        ),
        AccessoryEntity(
            id = "hat_knight_helm",
            name = "Yelmo de Caballero",
            slot = AccessorySlot.HAT.id,
            price = 120,
            creatorUsername = "CastilloKing",
            isForSale = true,
            partsJson = BlockSerializer.encodeParts(
                listOf(
                    AccessoryPart(0f, 0.26f, 0f, 0.76f, 0.28f, 0.76f, 0xFF8C96A4),
                    AccessoryPart(0f, 0.48f, -0.05f, 0.18f, 0.24f, 0.62f, 0xFFD92B2B),
                    AccessoryPart(-0.34f, 0.05f, 0f, 0.12f, 0.46f, 0.68f, 0xFF727C8A),
                    AccessoryPart(0.34f, 0.05f, 0f, 0.12f, 0.46f, 0.68f, 0xFF727C8A)
                )
            )
        ),
        AccessoryEntity(
            id = "hat_pirate_bandana",
            name = "Pañuelo Pirata",
            slot = AccessorySlot.HAT.id,
            price = 65,
            creatorUsername = "PirataRojo",
            isForSale = true,
            partsJson = BlockSerializer.encodeParts(
                listOf(
                    AccessoryPart(0f, 0.25f, 0f, 0.72f, 0.24f, 0.72f, 0xFFD92B2B),
                    AccessoryPart(0f, 0.38f, 0f, 0.56f, 0.12f, 0.56f, 0xFFA61C1C),
                    AccessoryPart(0.28f, 0.18f, -0.38f, 0.16f, 0.22f, 0.20f, 0xFFD92B2B)
                )
            )
        ),
        AccessoryEntity(
            id = "face_cool_shades",
            name = "Lentes Clásicos",
            slot = AccessorySlot.FACE.id,
            price = 55,
            creatorUsername = "Obbysta_XD",
            isForSale = true,
            partsJson = BlockSerializer.encodeParts(
                listOf(
                    AccessoryPart(-0.16f, 0.06f, 0.36f, 0.24f, 0.14f, 0.08f, 0xFF1F242D),
                    AccessoryPart(0.16f, 0.06f, 0.36f, 0.24f, 0.14f, 0.08f, 0xFF1F242D),
                    AccessoryPart(0f, 0.08f, 0.36f, 0.14f, 0.06f, 0.08f, 0xFF1F242D)
                )
            )
        ),
        AccessoryEntity(
            id = "back_builder_pack",
            name = "Mochila Constructor",
            slot = AccessorySlot.BACK.id,
            price = 75,
            creatorUsername = "BrickMaster99",
            isForSale = true,
            partsJson = BlockSerializer.encodeParts(
                listOf(
                    AccessoryPart(0f, 0.02f, -0.34f, 0.72f, 0.84f, 0.30f, 0xFF8D5B34),
                    AccessoryPart(0f, 0.20f, -0.44f, 0.56f, 0.34f, 0.16f, 0xFFF5C518),
                    AccessoryPart(0f, -0.22f, -0.44f, 0.54f, 0.28f, 0.16f, 0xFF6E4726)
                )
            )
        ),
        AccessoryEntity(
            id = "back_royal_cape",
            name = "Capa Roja Real",
            slot = AccessorySlot.BACK.id,
            price = 110,
            creatorUsername = "CastilloKing",
            isForSale = true,
            partsJson = BlockSerializer.encodeParts(
                listOf(
                    AccessoryPart(0f, -0.15f, -0.30f, 0.86f, 1.35f, 0.10f, 0xFFD92B2B),
                    AccessoryPart(-0.28f, 0.45f, -0.24f, 0.26f, 0.16f, 0.22f, 0xFFF5C518),
                    AccessoryPart(0.28f, 0.45f, -0.24f, 0.26f, 0.16f, 0.22f, 0xFFF5C518)
                )
            )
        )
    )

    fun defaultWorlds(): List<WorldEntity> = listOf(
        WorldEntity(
            id = 1L,
            title = "Torre Obby Clásica",
            description = "Salta por los bloques de colores sobre la placa verde, evita la lava y llega a la meta dorada.",
            creatorUsername = "Obbysta_XD",
            category = "Obby",
            skyTheme = SkyTheme.DAY.id,
            blocksJson = BlockSerializer.encodeBlocks(buildObbyWorld()),
            likesCount = 148,
            activePlayersCount = 12,
            thumbnailKey = "obby",
            isBuiltIn = true
        ),
        WorldEntity(
            id = 2L,
            title = "Ciudad Ladrillo",
            description = "Explora las casitas de ladrillo rojo, la fuente central y junta monedas por todo el pueblo.",
            creatorUsername = "BuilderAlex",
            category = "Ciudad",
            skyTheme = SkyTheme.DAY.id,
            blocksJson = BlockSerializer.encodeBlocks(buildCityWorld()),
            likesCount = 96,
            activePlayersCount = 8,
            thumbnailKey = "city",
            isBuiltIn = true
        ),
        WorldEntity(
            id = 3L,
            title = "Isla Pirata 3D",
            description = "Camina por el muelle de madera, sube a las colinas de arena y encuentra el tesoro entre palmeras.",
            creatorUsername = "PirataRojo",
            category = "Aventura",
            skyTheme = SkyTheme.ISLAND.id,
            blocksJson = BlockSerializer.encodeBlocks(buildPirateWorld()),
            likesCount = 114,
            activePlayersCount = 9,
            thumbnailKey = "pirate",
            isBuiltIn = true
        ),
        WorldEntity(
            id = 4L,
            title = "Castillo Medieval",
            description = "Cruza el puente levadizo de madera, sube las murallas de piedra y conquista la torre del rey.",
            creatorUsername = "CastilloKing",
            category = "Castillo",
            skyTheme = SkyTheme.CASTLE.id,
            blocksJson = BlockSerializer.encodeBlocks(buildCastleWorld()),
            likesCount = 83,
            activePlayersCount = 6,
            thumbnailKey = "castle",
            isBuiltIn = true
        )
    )

    fun defaultCommunityUsers(): List<UserEntity> {
        val dummyHash = SecurityUtils.hashPassword("rotox_community_bot_2026")
        return listOf(
            UserEntity(
                username = "NoobClasico",
                passwordHash = dummyHash,
                rotoxCoins = 420,
                headColorHex = 0xFFF5CD30,
                torsoColorHex = 0xFF1E6BE6,
                leftArmColorHex = 0xFFF5CD30,
                rightArmColorHex = 0xFFF5CD30,
                leftLegColorHex = 0xFF2EA043,
                rightLegColorHex = 0xFF2EA043,
                equippedHatId = null,
                equippedFaceId = null,
                equippedBackId = null,
                currentWorldId = 1L,
                isCommunityBot = true,
                onlineStatus = "Jugando"
            ),
            UserEntity(
                username = "BuilderAlex",
                passwordHash = dummyHash,
                rotoxCoins = 780,
                headColorHex = 0xFFE5C487,
                torsoColorHex = 0xFFD92B2B,
                leftArmColorHex = 0xFFE5C487,
                rightArmColorHex = 0xFFE5C487,
                leftLegColorHex = 0xFF2D333B,
                rightLegColorHex = 0xFF2D333B,
                equippedHatId = "hat_red_cap",
                equippedFaceId = null,
                equippedBackId = "back_builder_pack",
                currentWorldId = 2L,
                isCommunityBot = true,
                onlineStatus = "Jugando"
            ),
            UserEntity(
                username = "BrickMaster99",
                passwordHash = dummyHash,
                rotoxCoins = 950,
                headColorHex = 0xFFF5CD30,
                torsoColorHex = 0xFFF27D24,
                leftArmColorHex = 0xFFF5CD30,
                rightArmColorHex = 0xFFF5CD30,
                leftLegColorHex = 0xFF1E6BE6,
                rightLegColorHex = 0xFF1E6BE6,
                equippedHatId = "hat_top_hat",
                equippedFaceId = "face_cool_shades",
                equippedBackId = "back_builder_pack",
                currentWorldId = 2L,
                isCommunityBot = true,
                onlineStatus = "Jugando"
            ),
            UserEntity(
                username = "PirataRojo",
                passwordHash = dummyHash,
                rotoxCoins = 640,
                headColorHex = 0xFFE5C487,
                torsoColorHex = 0xFF2D333B,
                leftArmColorHex = 0xFFE5C487,
                rightArmColorHex = 0xFFE5C487,
                leftLegColorHex = 0xFF8D5B34,
                rightLegColorHex = 0xFF8D5B34,
                equippedHatId = "hat_pirate_bandana",
                equippedFaceId = "face_cool_shades",
                equippedBackId = null,
                currentWorldId = 3L,
                isCommunityBot = true,
                onlineStatus = "Jugando"
            ),
            UserEntity(
                username = "Obbysta_XD",
                passwordHash = dummyHash,
                rotoxCoins = 510,
                headColorHex = 0xFFF5CD30,
                torsoColorHex = 0xFF2EA043,
                leftArmColorHex = 0xFFF5CD30,
                rightArmColorHex = 0xFFF5CD30,
                leftLegColorHex = 0xFF1E6BE6,
                rightLegColorHex = 0xFF1E6BE6,
                equippedHatId = "hat_red_cap",
                equippedFaceId = "face_cool_shades",
                equippedBackId = null,
                currentWorldId = 1L,
                isCommunityBot = true,
                onlineStatus = "Jugando"
            ),
            UserEntity(
                username = "CastilloKing",
                passwordHash = dummyHash,
                rotoxCoins = 1200,
                headColorHex = 0xFFE5C487,
                torsoColorHex = 0xFF8C96A4,
                leftArmColorHex = 0xFF8C96A4,
                rightArmColorHex = 0xFF8C96A4,
                leftLegColorHex = 0xFF2D333B,
                rightLegColorHex = 0xFF2D333B,
                equippedHatId = "hat_golden_crown",
                equippedFaceId = null,
                equippedBackId = "back_royal_cape",
                currentWorldId = 4L,
                isCommunityBot = true,
                onlineStatus = "Jugando"
            )
        )
    }

    private fun buildObbyWorld(): List<WorldBlock> {
        val list = mutableListOf<WorldBlock>()
        // Spawn platform
        list += WorldBlock(0f, 0f, 0f, 4f, 1f, 4f, 0xFF39C5BB, BlockType.SPAWN)
        // Lava floor strips
        for (i in -6..6) {
            list += WorldBlock(i * 2f, -0.35f, -6f, 2f, 0.3f, 2f, 0xFFFF5A1F, BlockType.LAVA)
            list += WorldBlock(i * 2f, -0.35f, -10f, 2f, 0.3f, 2f, 0xFFFF5A1F, BlockType.LAVA)
        }
        // Ascending Obby platforms
        val steps = listOf(
            Triple(0f, 0.8f, -3.5f) to 0xFFD92B2B,
            Triple(2.2f, 1.4f, -5.5f) to 0xFFF5C518,
            Triple(-0.2f, 2.0f, -7.5f) to 0xFF1E6BE6,
            Triple(-2.6f, 2.6f, -9.5f) to 0xFF2EA043,
            Triple(0f, 3.2f, -11.8f) to 0xFFF27D24,
            Triple(2.4f, 3.8f, -13.8f) to 0xFFD92B2B,
            Triple(0f, 4.4f, -16.0f) to 0xFF1E6BE6
        )
        steps.forEachIndexed { idx, (pos, color) ->
            list += WorldBlock(pos.first, pos.second, pos.third, 2.2f, 0.7f, 2.2f, color, BlockType.BRICK)
            if (idx % 2 == 0) {
                list += WorldBlock(pos.first, pos.second + 1.1f, pos.third, 0.6f, 0.6f, 0.25f, 0xFFFFD52E, BlockType.COIN)
            }
        }
        // Goal platform + arch
        list += WorldBlock(0f, 4.9f, -19.5f, 4f, 0.8f, 4f, 0xFFFFD700, BlockType.GOAL)
        list += WorldBlock(-1.4f, 6.2f, -19.5f, 0.5f, 2.2f, 0.5f, 0xFFD92B2B, BlockType.BRICK)
        list += WorldBlock(1.4f, 6.2f, -19.5f, 0.5f, 2.2f, 0.5f, 0xFFD92B2B, BlockType.BRICK)
        list += WorldBlock(0f, 7.3f, -19.5f, 3.3f, 0.5f, 0.5f, 0xFFF5C518, BlockType.GOLD)
        // Side trees for classic sandbox vibe
        list += addTree(-6f, 0f, -1f)
        list += addTree(6f, 0f, -2f)
        list += addTree(6f, 0f, 3f)
        return list
    }

    private fun buildCityWorld(): List<WorldBlock> {
        val list = mutableListOf<WorldBlock>()
        list += WorldBlock(0f, 0f, 0f, 3.5f, 0.5f, 3.5f, 0xFF39C5BB, BlockType.SPAWN)
        // Plaza fountain
        list += WorldBlock(0f, 0.35f, -7f, 4.4f, 0.7f, 4.4f, 0xFF8C96A4, BlockType.STONE)
        list += WorldBlock(0f, 0.75f, -7f, 3.2f, 0.4f, 3.2f, 0xFF2B95E6, BlockType.WATER)
        list += WorldBlock(0f, 1.4f, -7f, 1.0f, 1.6f, 1.0f, 0xFF8C96A4, BlockType.STONE)
        list += WorldBlock(0f, 2.4f, -7f, 0.6f, 0.6f, 0.25f, 0xFFFFD52E, BlockType.COIN)

        // Red brick house on left
        list += WorldBlock(-6.5f, 1.5f, -7f, 4f, 3f, 4f, 0xFFD92B2B, BlockType.BRICK)
        list += WorldBlock(-6.5f, 3.3f, -7f, 4.6f, 0.7f, 4.6f, 0xFFF5C518, BlockType.GOLD)
        list += WorldBlock(-6.5f, 3.9f, -7f, 3.2f, 0.6f, 3.2f, 0xFFF5C518, BlockType.GOLD)

        // Blue brick house on right
        list += WorldBlock(6.5f, 1.5f, -7f, 4f, 3f, 4f, 0xFF1E6BE6, BlockType.BLUE_BLOCK)
        list += WorldBlock(6.5f, 3.3f, -7f, 4.6f, 0.7f, 4.6f, 0xFFD92B2B, BlockType.BRICK)
        list += WorldBlock(6.5f, 3.9f, -7f, 3.2f, 0.6f, 3.2f, 0xFFD92B2B, BlockType.BRICK)

        // Back tower & goal
        list += WorldBlock(0f, 2.0f, -15f, 4.5f, 4f, 4.5f, 0xFF9C6635, BlockType.WOOD)
        list += WorldBlock(0f, 4.3f, -15f, 3.2f, 0.7f, 3.2f, 0xFFFFD700, BlockType.GOAL)

        // Stepping stairs to tower roof
        list += WorldBlock(-3.2f, 0.8f, -12.5f, 2f, 0.8f, 2f, 0xFF8C96A4, BlockType.STONE)
        list += WorldBlock(-3.2f, 1.8f, -14.5f, 2f, 0.8f, 2f, 0xFF8C96A4, BlockType.STONE)
        list += WorldBlock(-2.5f, 3.0f, -15f, 2f, 0.8f, 2f, 0xFF8C96A4, BlockType.STONE)

        // Coins & Trees
        list += WorldBlock(-3.5f, 1.1f, -3.5f, 0.6f, 0.6f, 0.25f, 0xFFFFD52E, BlockType.COIN)
        list += WorldBlock(3.5f, 1.1f, -3.5f, 0.6f, 0.6f, 0.25f, 0xFFFFD52E, BlockType.COIN)
        list += WorldBlock(0f, 1.1f, -11.5f, 0.6f, 0.6f, 0.25f, 0xFFFFD52E, BlockType.COIN)
        list += addTree(-6.5f, 0f, -1.5f)
        list += addTree(6.5f, 0f, -1.5f)
        list += addTree(6.5f, 0f, -13f)
        return list
    }

    private fun buildPirateWorld(): List<WorldBlock> {
        val list = mutableListOf<WorldBlock>()
        // Wooden dock
        list += WorldBlock(0f, 0.2f, 0f, 3.5f, 0.6f, 3.5f, 0xFF39C5BB, BlockType.SPAWN)
        list += WorldBlock(0f, 0.2f, -4f, 2.6f, 0.5f, 6f, 0xFF9C6635, BlockType.WOOD)
        // Sandy island
        list += WorldBlock(0f, 0.4f, -10.5f, 11f, 0.9f, 9f, 0xFFE5C487, BlockType.GRASS)
        list += WorldBlock(2.5f, 1.2f, -11.5f, 5f, 1.0f, 5f, 0xFFE5C487, BlockType.GRASS)
        list += WorldBlock(3.0f, 2.0f, -12.0f, 3f, 0.9f, 3f, 0xFFFFD700, BlockType.GOAL)
        // Palm trees
        list += addTree(-4f, 0.8f, -8.5f)
        list += addTree(4.5f, 0.8f, -8.0f)
        list += addTree(-3.5f, 0.8f, -13.5f)
        // Pirate crates & coins
        list += WorldBlock(-2f, 1.3f, -10f, 1.2f, 1.2f, 1.2f, 0xFF8D5B34, BlockType.WOOD)
        list += WorldBlock(-2f, 2.4f, -10f, 0.6f, 0.6f, 0.25f, 0xFFFFD52E, BlockType.COIN)
        list += WorldBlock(0f, 1.3f, -6f, 0.6f, 0.6f, 0.25f, 0xFFFFD52E, BlockType.COIN)
        list += WorldBlock(2.5f, 2.2f, -9.8f, 0.6f, 0.6f, 0.25f, 0xFFFFD52E, BlockType.COIN)
        return list
    }

    private fun buildCastleWorld(): List<WorldBlock> {
        val list = mutableListOf<WorldBlock>()
        list += WorldBlock(0f, 0.2f, 1f, 3.5f, 0.6f, 3.5f, 0xFF39C5BB, BlockType.SPAWN)
        // Drawbridge
        list += WorldBlock(0f, 0.3f, -2.5f, 2.8f, 0.5f, 4.5f, 0xFF9C6635, BlockType.WOOD)
        // Castle courtyard & walls
        list += WorldBlock(0f, 0.2f, -10.5f, 12f, 0.6f, 12f, 0xFF4A7C3F, BlockType.GRASS)
        // Left & right walls
        list += WorldBlock(-5.5f, 1.8f, -10.5f, 1.2f, 3.0f, 11f, 0xFF8C96A4, BlockType.STONE)
        list += WorldBlock(5.5f, 1.8f, -10.5f, 1.2f, 3.0f, 11f, 0xFF8C96A4, BlockType.STONE)
        list += WorldBlock(0f, 1.8f, -16.0f, 12f, 3.0f, 1.2f, 0xFF8C96A4, BlockType.STONE)
        // Four corner towers
        val corners = listOf(-5.5f to -5.5f, 5.5f to -5.5f, -5.5f to -15.5f, 5.5f to -15.5f)
        corners.forEach { (cx, cz) ->
            list += WorldBlock(cx, 2.8f, cz, 2.2f, 5.0f, 2.2f, 0xFF727C8A, BlockType.STONE)
            list += WorldBlock(cx, 5.6f, cz, 0.8f, 0.8f, 0.8f, 0xFFD92B2B, BlockType.BRICK)
        }
        // Central stairs + King's keep goal
        list += WorldBlock(0f, 0.9f, -9f, 2.8f, 0.8f, 2.0f, 0xFF8C96A4, BlockType.STONE)
        list += WorldBlock(0f, 1.7f, -11f, 2.8f, 0.8f, 2.0f, 0xFF8C96A4, BlockType.STONE)
        list += WorldBlock(0f, 2.5f, -13f, 3.6f, 0.9f, 3.0f, 0xFFFFD700, BlockType.GOAL)
        list += WorldBlock(-2.5f, 1.3f, -8f, 0.6f, 0.6f, 0.25f, 0xFFFFD52E, BlockType.COIN)
        list += WorldBlock(2.5f, 1.3f, -8f, 0.6f, 0.6f, 0.25f, 0xFFFFD52E, BlockType.COIN)
        return list
    }

    private fun addTree(x: Float, baseY: Float, z: Float): List<WorldBlock> = listOf(
        WorldBlock(x, baseY + 1.2f, z, 0.8f, 2.4f, 0.8f, 0xFF6E4726, BlockType.TREE_TRUNK),
        WorldBlock(x, baseY + 2.9f, z, 2.4f, 1.8f, 2.4f, 0xFF238636, BlockType.TREE_LEAVES)
    )
}

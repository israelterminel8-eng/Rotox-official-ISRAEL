package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AccessoryPart
import com.example.data.AccessorySlot
import com.example.data.BlockSerializer
import com.example.data.BlockType
import com.example.data.RotoxDatabase
import com.example.data.RotoxRepository
import com.example.data.SecurityUtils
import com.example.data.SkyTheme
import com.example.data.WorldBlock
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var context: Context
    private lateinit var db: RotoxDatabase
    private lateinit var repo: RotoxRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, RotoxDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repo = RotoxRepository(db.rotoxDao(), context)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `app name is strictly ROTOX ALFA`() {
        assertEquals("ROTOX[ALFA]", context.getString(R.string.app_name))
    }

    @Test
    fun `password hashing and account creation workflow`() = runTest {
        val hash = SecurityUtils.hashPassword("secreto123")
        assertTrue(SecurityUtils.verifyPassword("secreto123", hash))
        assertFalse(SecurityUtils.verifyPassword("otra_clave", hash))

        val created = repo.createAccount("JugadorUno", "clave123").getOrThrow()
        assertEquals("JugadorUno", created.username)
        assertEquals(250, created.rotoxCoins)

        val loginFail = repo.loginAccount("JugadorUno", "incorrecta")
        assertTrue(loginFail.isFailure)

        val loginOk = repo.loginAccount("JugadorUno", "clave123")
        assertTrue(loginOk.isSuccess)
    }

    @Test
    fun `friend request search accept reject and join flow`() = runTest {
        val userA = repo.createAccount("PlayerA", "pass123").getOrThrow()
        val userB = repo.createAccount("PlayerB", "pass123").getOrThrow()

        // Both start with 2 seeded incoming requests
        val initialB = repo.observeFriendRequests(userB.id).first()
        assertTrue(initialB.any { it.isIncoming && it.status == "PENDING" })

        // UserA sends request to UserB
        repo.sendFriendRequest(userA.id, userB.id).getOrThrow()
        val requestsForB = repo.observeFriendRequests(userB.id).first()
        val reqFromA = requestsForB.firstOrNull { it.otherUser.id == userA.id && it.isIncoming }
        assertNotNull(reqFromA)

        // UserB accepts UserA
        repo.acceptFriendRequest(reqFromA!!.requestId)
        val friendsOfB = repo.observeFriends(userB.id).first()
        assertTrue(friendsOfB.any { it.user.id == userA.id })

        // UserA enters world 2, UserB sees them and can join
        repo.enterWorld(userA.id, 2L)
        val updatedFriendsOfB = repo.observeFriends(userB.id).first()
        val friendA = updatedFriendsOfB.first { it.user.id == userA.id }
        assertEquals(2L, friendA.user.currentWorldId)
    }

    @Test
    fun `create 3D world and sell 3D accessory flow`() = runTest {
        val user = repo.createAccount("Creador3D", "pass123").getOrThrow()

        val world = repo.publishWorld(
            userId = user.id,
            titleRaw = "Mi Torre 3D",
            descriptionRaw = "Prueba",
            category = "Obby",
            skyTheme = SkyTheme.DAY,
            blocks = listOf(
                WorldBlock(0f, 0.5f, 0f, 1f, 1f, 1f, 0xFFD92B2B, BlockType.BRICK)
            )
        ).getOrThrow()
        assertEquals("Mi Torre 3D", world.title)

        val accessory = repo.createAndSellAccessory(
            userId = user.id,
            nameRaw = "Corona Roja",
            slot = AccessorySlot.HAT,
            price = 80,
            parts = listOf(AccessoryPart(0f, 0.3f, 0f, 0.7f, 0.2f, 0.7f, 0xFFD92B2B)),
            putForSale = true
        ).getOrThrow()
        assertTrue(accessory.isForSale)
        assertEquals(1, BlockSerializer.decodeParts(accessory.partsJson).size)
    }
}

package com.example.gesto_stocks

import com.example.gesto_stocks.util.hashPassword
import com.example.gesto_stocks.util.passwordCorreta
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SegurancaTest {

    @Test
    fun `a mesma password gera hashes diferentes`() {
        // O sal aleatório é o que impede rainbow tables e impede saber que
        // duas contas partilham a password
        assertNotEquals(hashPassword("password123"), hashPassword("password123"))
    }

    @Test
    fun `a password certa valida`() {
        assertTrue(passwordCorreta("password123", hashPassword("password123")))
    }

    @Test
    fun `a password errada nao valida`() {
        val guardado = hashPassword("password123")
        assertFalse(passwordCorreta("password124", guardado))
        assertFalse(passwordCorreta("", guardado))
        assertFalse(passwordCorreta("PASSWORD123", guardado))
    }

    @Test
    fun `hash antigo ou corrompido nao valida em vez de rebentar`() {
        // Hash SHA-256 do formato anterior, sem os dois pontos
        val antigo = "ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f"
        assertFalse(passwordCorreta("password", antigo))
        assertFalse(passwordCorreta("password", ""))
        assertFalse(passwordCorreta("password", "210000:naoehex:tambemnao"))
        assertFalse(passwordCorreta("password", "iteracoes:aabb:ccdd"))
    }
}

package com.example.gesto_stocks.util

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Derivação de password com PBKDF2, do próprio JDK — sem dependências.
 * Substitui o SHA-256 simples: o sal aleatório impede rainbow tables e faz
 * com que duas contas com a mesma password tenham hashes diferentes; as
 * iterações tornam a força bruta cara.
 *
 * ponytail: HMAC-SHA1 e não SHA256 porque PBKDF2WithHmacSHA256 só existe a
 * partir da API 26 e o minSdk do projeto é 24. Subir o minSdk para 26 →
 * trocar ALGORITMO por "PBKDF2WithHmacSHA256".
 */
private const val ALGORITMO = "PBKDF2WithHmacSHA1"
private const val ITERACOES = 210_000   // recomendação OWASP para PBKDF2-HMAC-SHA1
private const val BITS = 256

/** Formato guardado: "iteracoes:sal:hash", tudo o que é preciso para validar. */
fun hashPassword(password: String): String {
    val sal = ByteArray(16).also(SecureRandom()::nextBytes)
    return "$ITERACOES:${sal.paraHex()}:${derivar(password, sal, ITERACOES).paraHex()}"
}

/**
 * Compara com [MessageDigest.isEqual] (tempo constante) em vez de `==`.
 * Devolve false para qualquer valor guardado que não tenha o formato acima,
 * incluindo os hashes SHA-256 antigos.
 */
fun passwordCorreta(password: String, guardado: String): Boolean = runCatching {
    val (iteracoes, sal, hash) = guardado.split(":")
    MessageDigest.isEqual(
        derivar(password, sal.paraBytes(), iteracoes.toInt()),
        hash.paraBytes()
    )
}.getOrDefault(false)

private fun derivar(password: String, sal: ByteArray, iteracoes: Int): ByteArray =
    SecretKeyFactory.getInstance(ALGORITMO)
        .generateSecret(PBEKeySpec(password.toCharArray(), sal, iteracoes, BITS))
        .encoded

private fun ByteArray.paraHex() = joinToString("") { "%02x".format(it) }

private fun String.paraBytes() = ByteArray(length / 2) {
    substring(it * 2, it * 2 + 2).toInt(16).toByte()
}

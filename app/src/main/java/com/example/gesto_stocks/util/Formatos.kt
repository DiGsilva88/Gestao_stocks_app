package com.example.gesto_stocks.util

/**
 * Iniciais do utilizador para o avatar: a primeira letra do primeiro e do
 * último nome. Estava copiada em cada ecrã que mostra o avatar.
 */
fun iniciais(nome: String): String {
    val partes = nome.trim().split(" ").filter { it.isNotBlank() }
    return when {
        partes.isEmpty() -> "?"
        partes.size == 1 -> partes[0].take(1).uppercase()
        else -> (partes.first().take(1) + partes.last().take(1)).uppercase()
    }
}

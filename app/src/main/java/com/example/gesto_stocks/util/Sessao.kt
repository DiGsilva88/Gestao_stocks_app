package com.example.gesto_stocks.util

import android.content.Context

class Sessao(context: Context) {

    private val prefs = context.getSharedPreferences("sessao", Context.MODE_PRIVATE)

    fun guardarUtilizador(id: Int) {
        prefs.edit().putInt("utilizadorId", id).apply()
    }

    fun utilizadorId(): Int = prefs.getInt("utilizadorId", -1)

    fun temSessao(): Boolean = utilizadorId() != -1

    fun terminar() {
        prefs.edit().clear().apply()
    }
}
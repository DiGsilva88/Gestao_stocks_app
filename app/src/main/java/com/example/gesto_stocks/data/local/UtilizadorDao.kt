package com.example.gesto_stocks.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.gesto_stocks.data.model.Utilizador

@Dao
interface UtilizadorDao {


    @Query("SELECT * FROM utilizadores WHERE email = :email LIMIT 1")
    suspend fun buscarPorEmail(email: String): Utilizador?

    @Query("SELECT * FROM utilizadores WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Int): Utilizador?

    @Insert
    suspend fun inserir(utilizador: Utilizador): Long

    @Update
    suspend fun atualizar(utilizador: Utilizador)

    @Delete
    suspend fun eliminar(utilizador: Utilizador)
}


package com.example.gesto_stocks.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.gesto_stocks.data.model.Produto

@Dao
interface ProdutoDao {

    @Query("SELECT * FROM produtos ORDER BY nome ASC")
    fun listarTodos(): LiveData<List<Produto>>

    @Query("SELECT * FROM produtos WHERE quantidade < stockMinimo ORDER BY quantidade ASC")
    fun listarEmAlerta(): LiveData<List<Produto>>

    @Query("SELECT * FROM produtos WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Int): Produto?

    /** Serve para avisar no formulário antes de o índice único recusar. */
    @Query("SELECT id FROM produtos WHERE sku = :sku LIMIT 1")
    suspend fun idPorSku(sku: String): Int?

    /**
     * Soma [delta] ao stock (negativo numa saída) numa única instrução, com o
     * limite de zero dentro do WHERE para que a verificação e a escrita não
     * possam ficar desfasadas.
     *
     * @return número de linhas alteradas: 0 quando não há stock suficiente.
     */
    @Query("UPDATE produtos SET quantidade = quantidade + :delta " +
            "WHERE id = :id AND quantidade + :delta >= 0")
    suspend fun ajustarQuantidade(id: Int, delta: Int): Int

    @Insert
    suspend fun inserir(produto: Produto)

    @Insert
    suspend fun inserirTodos(produtos: List<Produto>)

    @Update
    suspend fun atualizar(produto: Produto)

    @Delete
    suspend fun eliminar(produto: Produto)
}



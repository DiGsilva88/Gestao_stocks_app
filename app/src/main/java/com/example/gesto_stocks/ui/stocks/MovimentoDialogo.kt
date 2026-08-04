package com.example.gesto_stocks.ui.stocks

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Toast
import com.example.gesto_stocks.data.local.StockifyDatabase
import com.example.gesto_stocks.data.model.Produto
import com.example.gesto_stocks.util.Sessao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.gesto_stocks.data.model.Movimento
import com.example.gesto_stocks.databinding.DialogMovimentoBinding

/**
 * Diálogo para registar entradas e saídas de stock.
 * Atualiza a quantidade do produto e grava o movimento na base de dados.
 */
class MovimentoDialogo(
    private val context: Context,
    private val produto: Produto,
    private val onConcluido: () -> Unit
) {

    fun mostrar() {
        val binding = DialogMovimentoBinding.inflate(LayoutInflater.from(context))

        // Preenche os dados do produto
        binding.txtProduto.text = "${produto.nome}  (${produto.sku})"
        binding.txtStockAtual.text = "Stock atual: ${produto.quantidade} unidades"

        val dialogo = AlertDialog.Builder(context)
            .setView(binding.root)
            .setPositiveButton("Confirmar", null)  // null para controlar o fecho
            .setNegativeButton("Cancelar", null)
            .create()

        dialogo.show()

        // Override do botão para impedir que feche com dados inválidos
        dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val qtd = binding.editQuantidade.text.toString().toIntOrNull()

            if (qtd == null || qtd <= 0) {
                binding.editQuantidade.error = "Introduz uma quantidade válida"
                return@setOnClickListener
            }

            val entrada = binding.chipEntrada.isChecked
            val novaQuantidade = if (entrada) {
                produto.quantidade + qtd
            } else {
                produto.quantidade - qtd
            }

            // Impede stock negativo
            if (novaQuantidade < 0) {
                binding.editQuantidade.error =
                    "Não há stock suficiente (apenas ${produto.quantidade})"
                return@setOnClickListener
            }

            val tipo = if (entrada) "ENTRADA" else "SAIDA"
            val motivo = binding.editMotivo.text.toString().trim()

            // Obtém o nome do utilizador com sessão para registar quem fez
            val sessao = Sessao(context)
            val db = StockifyDatabase.obter(context)

            CoroutineScope(Dispatchers.IO).launch {
                // Buscar o nome do utilizador
                val utilizador = db.utilizadorDao()
                    .buscarPorId(sessao.utilizadorId())?.nome ?: "Desconhecido"

                // Atualizar a quantidade do produto
                db.produtoDao().atualizar(
                    produto.copy(quantidade = novaQuantidade)
                )

                // Registar o movimento
                db.movimentoDao().inserir(
                    Movimento(
                        produtoId = produto.id,
                        nomeProduto = produto.nome,
                        tipo = tipo,
                        quantidade = qtd,
                        motivo = motivo,
                        utilizador = utilizador
                    )
                )

                withContext(Dispatchers.Main) {
                    val acao = if (entrada) "Entrada" else "Saída"
                    Toast.makeText(context,
                        "$acao de $qtd registada", Toast.LENGTH_SHORT).show()
                    dialogo.dismiss()
                    onConcluido()
                }
            }
        }
    }
}
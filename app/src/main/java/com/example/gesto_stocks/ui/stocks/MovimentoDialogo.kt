package com.example.gesto_stocks.ui.stocks

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Toast
import com.example.gesto_stocks.R
import com.example.gesto_stocks.data.local.StockifyDatabase
import com.example.gesto_stocks.data.model.Produto
import com.example.gesto_stocks.util.Sessao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.example.gesto_stocks.data.model.Movimento
import com.example.gesto_stocks.databinding.DialogMovimentoBinding

/**
 * Diálogo para registar entradas e saídas de stock.
 * Atualiza a quantidade do produto e grava o movimento na base de dados.
 *
 * O [scope] vem de quem abre o diálogo (o lifecycleScope do Fragment): se o
 * ecrã desaparecer a meio da gravação, a coroutine é cancelada em vez de
 * continuar a tocar num diálogo e num contexto já mortos.
 */
class MovimentoDialogo(
    private val context: Context,
    private val produto: Produto,
    private val scope: CoroutineScope
) {

    fun mostrar() {
        val binding = DialogMovimentoBinding.inflate(LayoutInflater.from(context))

        // Preenche os dados do produto
        binding.txtProduto.text =
            context.getString(R.string.movimento_produto, produto.nome, produto.sku)
        binding.txtStockAtual.text = context.resources.getQuantityString(
            R.plurals.movimento_stock_atual, produto.quantidade, produto.quantidade)

        val dialogo = AlertDialog.Builder(context)
            .setView(binding.root)
            .setPositiveButton(R.string.confirmar, null)  // null para controlar o fecho
            .setNegativeButton(R.string.cancelar, null)
            .create()

        dialogo.show()

        // Override do botão para impedir que feche com dados inválidos
        dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val qtd = binding.editQuantidade.text.toString().toIntOrNull()

            if (qtd == null || qtd <= 0) {
                binding.editQuantidade.error =
                    context.getString(R.string.erro_quantidade_invalida)
                return@setOnClickListener
            }

            val entrada = binding.chipEntrada.isChecked
            val delta = if (entrada) qtd else -qtd
            val motivo = binding.editMotivo.text.toString().trim()

            val sessao = Sessao(context)
            val db = StockifyDatabase.obter(context)

            scope.launch {
                // Regista quem fez o movimento
                val utilizador = db.utilizadorDao()
                    .buscarPorId(sessao.utilizadorId())?.nome ?: "Desconhecido"

                val gravado = db.registarMovimento(
                    Movimento(
                        produtoId = produto.id,
                        nomeProduto = produto.nome,
                        tipo = if (entrada) "ENTRADA" else "SAIDA",
                        quantidade = qtd,
                        motivo = motivo,
                        utilizador = utilizador
                    ),
                    delta
                )

                if (!gravado) {
                    // O stock pode ter mudado desde que o diálogo abriu, por
                    // isso a quantidade da mensagem é lida agora, não a do ecrã
                    val atual = db.produtoDao().buscarPorId(produto.id)?.quantidade ?: 0
                    binding.editQuantidade.error =
                        context.getString(R.string.erro_stock_insuficiente, atual)
                    return@launch
                }

                val mensagem = context.getString(
                    if (entrada) R.string.movimento_entrada_registada
                    else R.string.movimento_saida_registada,
                    qtd
                )
                Toast.makeText(context, mensagem, Toast.LENGTH_SHORT).show()
                dialogo.dismiss()
            }
        }
    }
}

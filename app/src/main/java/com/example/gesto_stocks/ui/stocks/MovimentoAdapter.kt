package com.example.gesto_stocks.ui.stocks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gesto_stocks.R
import com.example.gesto_stocks.data.model.Movimento
import com.example.gesto_stocks.databinding.ItemMovimentoBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Adapter para a lista de movimentos de stock.
 * Mostra o tipo (entrada/saída), o produto, a quantidade e a data.
 */
class MovimentoAdapter :
    ListAdapter<Movimento, MovimentoAdapter.MovimentoVH>(DIFF) {

    // Formato de data: "04 ago 2026"
    private val formatoData = SimpleDateFormat("dd MMM yyyy", Locale("pt", "PT"))

    inner class MovimentoVH(private val b: ItemMovimentoBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(m: Movimento) {
            val ctx = b.root.context
            val entrada = m.tipo == "ENTRADA"

            // Ícone e cor consoante o tipo
            b.imgTipo.setImageResource(
                if (entrada) android.R.drawable.arrow_down_float
                else android.R.drawable.arrow_up_float
            )
            b.imgTipo.setColorFilter(
                ctx.getColor(if (entrada) R.color.accent else R.color.critico)
            )

            b.txtProduto.text = m.nomeProduto

            // Linha de detalhes: motivo · utilizador · data
            val partes = mutableListOf<String>()
            if (m.motivo.isNotBlank()) partes.add(m.motivo)
            if (m.utilizador.isNotBlank()) partes.add(m.utilizador)
            partes.add(formatoData.format(Date(m.dataHora)))
            b.txtDetalhes.text = partes.joinToString(" · ")

            // Quantidade com sinal e cor. A descrição diz "Entrada"/"Saída"
            // por extenso: o sinal e a cor sozinhos não chegam ao leitor de ecrã.
            b.txtQuantidade.text = ctx.getString(
                if (entrada) R.string.movimento_qtd_entrada
                else R.string.movimento_qtd_saida,
                m.quantidade
            )
            b.txtQuantidade.contentDescription = ctx.getString(
                if (entrada) R.string.desc_movimento_entrada
                else R.string.desc_movimento_saida,
                m.quantidade
            )
            b.txtQuantidade.setTextColor(
                ctx.getColor(if (entrada) R.color.accent else R.color.critico)
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        MovimentoVH(ItemMovimentoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: MovimentoVH, position: Int) =
        holder.bind(getItem(position))

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Movimento>() {
            override fun areItemsTheSame(a: Movimento, b: Movimento) = a.id == b.id
            override fun areContentsTheSame(a: Movimento, b: Movimento) = a == b
        }
    }
}
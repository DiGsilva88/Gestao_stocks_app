package com.example.gesto_stocks.ui.inicio

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.graphics.drawable.GradientDrawable
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gesto_stocks.R
import com.example.gesto_stocks.data.local.StockifyDatabase
import com.example.gesto_stocks.databinding.FragmentInicioBinding
import com.example.gesto_stocks.databinding.ItemLegendaBinding
import com.example.gesto_stocks.databinding.ItemTopProdutoBinding
import com.example.gesto_stocks.ui.stocks.MovimentoAdapter
import com.example.gesto_stocks.util.Sessao
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

class InicioFragment : Fragment() {

    private var _binding: FragmentInicioBinding? = null
    private val binding get() = _binding!!

    private val viewModel: InicioViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInicioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        carregarUtilizador()

        val moeda = NumberFormat.getCurrencyInstance(Locale("pt", "PT"))

        viewModel.produtos.observe(viewLifecycleOwner) {
            binding.txtVazio.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.stockTotal.observe(viewLifecycleOwner) {
            binding.txtStockTotal.text = it.toString()
        }

        viewModel.emAlerta.observe(viewLifecycleOwner) {
            binding.txtEmAlerta.text = it.toString()
            binding.txtBadge.text = it.toString()
            binding.txtBadge.visibility = if (it == 0) View.GONE else View.VISIBLE
            binding.blocoNotificacoes.contentDescription = if (it == 0) {
                getString(R.string.desc_sem_alertas)
            } else {
                resources.getQuantityString(R.plurals.desc_alertas_stock, it, it)
            }
        }

        viewModel.receitaTotal.observe(viewLifecycleOwner) {
            binding.txtReceitaTotal.text = moeda.format(it)
        }

        viewModel.lucroTotal.observe(viewLifecycleOwner) {
            binding.txtLucroTotal.text = moeda.format(it)
        }

        val cores = listOf(
            R.color.accent, R.color.aviso, R.color.critico,
            R.color.text_muted, R.color.text_primary
        ).map { requireContext().getColor(it) }

        val movimentoAdapter = MovimentoAdapter()
        binding.recyclerMovimentos.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerMovimentos.adapter = movimentoAdapter

        viewModel.movimentosRecentes.observe(viewLifecycleOwner) { lista ->
            val temMovimentos = lista.isNotEmpty()
            binding.txtTituloMovimentos.visibility = if (temMovimentos) View.VISIBLE else View.GONE
            binding.recyclerMovimentos.visibility = if (temMovimentos) View.VISIBLE else View.GONE
            movimentoAdapter.submitList(lista)
        }

        viewModel.vendasPorCategoria.observe(viewLifecycleOwner) { lista ->
            binding.cartaoCategorias.visibility = if (lista.isEmpty()) View.GONE else View.VISIBLE

            binding.graficoDonut.mostrar(lista.map { it.second }, cores)

            binding.legendaCategorias.removeAllViews()
            val total = lista.sumOf { it.second }
            lista.forEachIndexed { i, (categoria, valor) ->
                val item = ItemLegendaBinding.inflate(
                    layoutInflater, binding.legendaCategorias, false)
                item.pontoCor.background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(cores[i % cores.size])
                }
                item.txtCategoria.text = categoria
                item.txtPercent.text = getString(
                    R.string.percentagem, (valor / total * 100).roundToInt())
                binding.legendaCategorias.addView(item.root)
            }
        }

        viewModel.topProdutos.observe(viewLifecycleOwner) { lista ->
            binding.txtTituloTop.visibility = if (lista.isEmpty()) View.GONE else View.VISIBLE

            binding.containerTopProdutos.removeAllViews()
            val maior = lista.firstOrNull()?.let { it.preco * it.quantidade } ?: 0.0
            lista.forEach { p ->
                val receita = p.preco * p.quantidade
                val item = ItemTopProdutoBinding.inflate(
                    layoutInflater, binding.containerTopProdutos, false)
                item.txtNome.text = p.nome
                item.txtReceita.text = moeda.format(receita)
                item.barraReceita.progress =
                    if (maior > 0) (receita / maior * 100).toInt() else 0
                binding.containerTopProdutos.addView(item.root)
            }
        }
    }

    private fun carregarUtilizador() {
        binding.txtSaudacao.text = saudacao()

        val sessao = Sessao(requireContext())
        val dao = StockifyDatabase.obter(requireContext()).utilizadorDao()

        lifecycleScope.launch {
            val u = dao.buscarPorId(sessao.utilizadorId()) ?: return@launch
            binding.txtNomeUtilizador.text = u.nome.trim().split(" ").first()
            binding.txtIniciais.text = iniciais(u.nome)
        }
    }

    private fun saudacao(): String {
        val hora = java.util.Calendar.getInstance()
            .get(java.util.Calendar.HOUR_OF_DAY)
        return getString(
            when {
                hora < 12 -> R.string.saudacao_bom_dia
                hora < 20 -> R.string.saudacao_boa_tarde
                else -> R.string.saudacao_boa_noite
            }
        )
    }
    private fun iniciais(nome: String): String {
        val partes = nome.trim().split(" ").filter { it.isNotBlank() }
        return when {
            partes.isEmpty() -> "?"
            partes.size == 1 -> partes[0].take(1).uppercase()
            else -> (partes.first().take(1) + partes.last().take(1)).uppercase()
        }
    }
}
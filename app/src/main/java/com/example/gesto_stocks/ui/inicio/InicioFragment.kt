package com.example.gesto_stocks.ui.inicio

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.gesto_stocks.data.local.StockifyDatabase
import com.example.gesto_stocks.databinding.FragmentInicioBinding
import com.example.gesto_stocks.util.Sessao
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

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

        viewModel.stockTotal.observe(viewLifecycleOwner) {
            binding.txtStockTotal.text = it.toString()
        }

        viewModel.numProdutos.observe(viewLifecycleOwner) {
            binding.txtNumProdutos.text = it.toString()
            binding.txtVazio.visibility = if (it == 0) View.VISIBLE else View.GONE
        }

        viewModel.emAlerta.observe(viewLifecycleOwner) {
            binding.txtEmAlerta.text = it.toString()
            binding.txtBadge.text = it.toString()
            binding.txtBadge.visibility = if (it == 0) View.GONE else View.VISIBLE
        }

        viewModel.valorTotal.observe(viewLifecycleOwner) {
            binding.txtValorTotal.text = moeda.format(it)
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
        return when {
            hora < 12 -> "Bom dia"
            hora < 20 -> "Boa tarde"
            else -> "Boa noite"
        }
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
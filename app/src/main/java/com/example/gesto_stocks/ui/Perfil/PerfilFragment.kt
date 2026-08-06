package com.example.gesto_stocks.ui.perfil

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.gesto_stocks.LoginActivity
import com.example.gesto_stocks.data.local.StockifyDatabase
import com.example.gesto_stocks.databinding.FragmentPerfilBinding
import com.example.gesto_stocks.ui.stocks.Rascunho
import com.example.gesto_stocks.util.Sessao
import kotlinx.coroutines.launch
import androidx.navigation.fragment.findNavController
import com.example.gesto_stocks.R

class PerfilFragment : Fragment() {

    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        carregarUtilizador()
        observarProdutos()

        binding.btnTerminarSessao.setOnClickListener { confirmarSaida() }
        binding.btnEditarPerfil.setOnClickListener {
            findNavController().navigate(R.id.acaoPerfilParaEditar)
        }
    }

    private fun carregarUtilizador() {
        val sessao = Sessao(requireContext())
        val dao = StockifyDatabase.obter(requireContext()).utilizadorDao()

        lifecycleScope.launch {
            val u = dao.buscarPorId(sessao.utilizadorId()) ?: return@launch
            binding.txtNome.text = u.nome
            binding.txtEmail.text = u.email
            binding.txtIniciais.text = iniciais(u.nome)
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

    private fun observarProdutos() {
        val dao = StockifyDatabase.obter(requireContext()).produtoDao()

        dao.listarTodos().observe(viewLifecycleOwner) { lista ->
            binding.txtTotalProdutos.text = lista.size.toString()
            binding.txtTotalAlertas.text =
                lista.count { it.quantidade < it.stockMinimo }.toString()
            binding.txtCategorias.text =
                lista.map { it.categoria }.distinct().size.toString()
        }
    }

    private fun confirmarSaida() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.perfil_terminar_sessao)
            .setMessage(R.string.sair_mensagem)
            .setPositiveButton(R.string.sair_confirmar) { _, _ ->
                Sessao(requireContext()).terminar()
                // O rascunho vive no processo: sem isto, o formulário por
                // gravar de um utilizador reaparecia ao próximo que entrasse
                Rascunho.limpar()
                startActivity(Intent(requireContext(), LoginActivity::class.java))
                requireActivity().finish()
            }
            .setNegativeButton(R.string.cancelar, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
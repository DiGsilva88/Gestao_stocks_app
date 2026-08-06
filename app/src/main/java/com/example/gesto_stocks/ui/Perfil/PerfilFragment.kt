package com.example.gesto_stocks.ui.perfil

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.gesto_stocks.MainActivity
import com.example.gesto_stocks.data.local.StockifyDatabase
import com.example.gesto_stocks.databinding.FragmentPerfilBinding
import com.example.gesto_stocks.ui.stocks.Rascunho
import com.example.gesto_stocks.util.Sessao
import com.example.gesto_stocks.util.iniciais
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

        binding.btnMenu.setOnClickListener { (requireActivity() as MainActivity).abrirMenu() }

        // O mesmo diálogo que a entrada do menu lateral usa
        binding.btnTerminarSessao.setOnClickListener {
            (requireActivity() as MainActivity).confirmarTerminarSessao()
        }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
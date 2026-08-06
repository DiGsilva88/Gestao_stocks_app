package com.example.gesto_stocks.ui.movimentos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gesto_stocks.data.local.StockifyDatabase
import com.example.gesto_stocks.databinding.FragmentMovimentosBinding
import com.example.gesto_stocks.ui.stocks.MovimentoAdapter

/**
 * Histórico completo de entradas e saídas, aberto pelo menu lateral.
 * O painel só mostra os dez últimos.
 */
class MovimentosFragment : Fragment() {

    private var _binding: FragmentMovimentosBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMovimentosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = MovimentoAdapter()
        binding.recyclerMovimentos.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerMovimentos.adapter = adapter

        val dao = StockifyDatabase.obter(requireContext()).movimentoDao()
        dao.listarTodos().observe(viewLifecycleOwner) { lista ->
            adapter.submitList(lista)

            // Uma lista vazia sem explicação parece uma avaria
            val vazio = lista.isEmpty()
            binding.txtVazio.visibility = if (vazio) View.VISIBLE else View.GONE
            binding.recyclerMovimentos.visibility = if (vazio) View.GONE else View.VISIBLE
        }

        binding.btnFechar.setOnClickListener { findNavController().navigateUp() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

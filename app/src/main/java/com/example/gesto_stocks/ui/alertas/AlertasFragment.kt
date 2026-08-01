package com.example.gesto_stocks.ui.alertas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gesto_stocks.R
import com.example.gesto_stocks.data.local.StockifyDatabase
import com.example.gesto_stocks.databinding.FragmentAlertasBinding

class AlertasFragment : Fragment() {

    private var _binding: FragmentAlertasBinding? = null
    private val binding get() = _binding!!

    private val adapter = AlertaAdapter { produto ->
        val args = Bundle().apply { putInt("produtoId", produto.id) }
        findNavController().navigate(R.id.acaoStockParaForm, args)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlertasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerAlertas.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerAlertas.adapter = adapter

        val dao = StockifyDatabase.obter(requireContext()).produtoDao()

        dao.listarEmAlerta().observe(viewLifecycleOwner) { lista ->
            adapter.submitList(lista)

            binding.txtContador.text =
                if (lista.size == 1) "1 item" else "${lista.size} itens"

            val vazio = lista.isEmpty()
            binding.txtVazio.visibility = if (vazio) View.VISIBLE else View.GONE
            binding.recyclerAlertas.visibility = if (vazio) View.GONE else View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
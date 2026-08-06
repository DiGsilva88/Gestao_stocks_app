package com.example.gesto_stocks.ui.stocks

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gesto_stocks.MainActivity
import com.example.gesto_stocks.R
import com.example.gesto_stocks.data.local.StockifyDatabase
import com.example.gesto_stocks.databinding.FragmentStockBinding
import com.example.gesto_stocks.ui.alertas.AlertaAdapter
import com.example.gesto_stocks.util.produtosParaCsv
import com.example.gesto_stocks.util.produtosParaExcel
import com.google.android.material.chip.Chip
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Ecrã de listagem de produtos.
 * Permite pesquisar por nome ou SKU, filtrar por categoria, ordenar a lista,
 * exportar os dados e abrir o formulário para criar ou editar um produto.
 */
class StockFragment : Fragment() {

    // O binding é anulável porque a vista de um Fragment pode ser
    // destruída e recriada várias vezes durante o seu ciclo de vida
    private var _binding: FragmentStockBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StockViewModel by viewModels()

    // Guarda o texto do ficheiro entre a escolha do formato e a
    // devolução do seletor do sistema, que só chega mais tarde
    private var conteudoPendente: String? = null

    // Tem de ser registado enquanto o Fragment está a ser criado.
    // Dentro do onViewCreated ou de um listener a app fecha
    private val criarFicheiro = registerForActivityResult(
        ActivityResultContracts.CreateDocument("*/*")
    ) { uri: Uri? ->
        val conteudo = conteudoPendente
        conteudoPendente = null
        if (uri != null && conteudo != null) escrever(uri, conteudo)
    }

    // O clique numa linha leva ao formulário, enviando o id do produto
    // para que o mesmo ecrã saiba que está em modo de edição
    private val adapter = ProdutoAdapter(
        onClick = { produto ->
            val args = Bundle().apply { putInt("produtoId", produto.id) }
            findNavController().navigate(R.id.acaoStockParaForm, args)
        },
        onMovimento = { produto ->
            // O LiveData atualiza a lista sozinho depois da gravação
            MovimentoDialogo(
                requireContext(), produto, viewLifecycleOwner.lifecycleScope
            ).mostrar()
        }
    )

    private val adapterAlertas = AlertaAdapter(
        onClick = { produto ->
            val args = Bundle().apply { putInt("produtoId", produto.id) }
            findNavController().navigate(R.id.acaoStockParaForm, args)
        },
        onMovimento = { produto ->
            MovimentoDialogo(
                requireContext(), produto, viewLifecycleOwner.lifecycleScope
            ).mostrar()
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStockBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnMenu.setOnClickListener {
            (requireActivity() as MainActivity).abrirMenu()
        }

        // O layoutManager tem de ser definido antes do adapter,
        // caso contrário a lista aparece vazia
        binding.recyclerProdutos.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerProdutos.adapter = adapter

        // A lista vem do Room através de LiveData: qualquer alteração
        // na base de dados atualiza o ecrã automaticamente
        viewModel.produtos.observe(viewLifecycleOwner) { lista ->
            adapter.submitList(lista)

            // Uma lista vazia sem explicação parece uma avaria
            val vazio = lista.isEmpty()
            binding.txtVazio.visibility = if (vazio) View.VISIBLE else View.GONE
            binding.recyclerProdutos.visibility = if (vazio) View.GONE else View.VISIBLE
        }

        binding.editPesquisa.doAfterTextChanged { texto ->
            viewModel.pesquisar(texto.toString())
        }

        // O ChipGroup devolve os ids selecionados; como só permite um,
        // basta ler o primeiro. Sem seleção, mostra todas as categorias
        binding.chipCategorias.setOnCheckedStateChangeListener { grupo, ids ->
            val categoria = if (ids.isEmpty()) {
                getString(R.string.categoria_todos)
            } else {
                grupo.findViewById<Chip>(ids.first()).text.toString()
            }
            viewModel.filtrarCategoria(categoria)
        }

        binding.btnOrdenar.setOnClickListener { mostrarMenuOrdenacao(it) }

        binding.btnExportar.setOnClickListener { escolherFormato() }

        binding.fabAdicionar.setOnClickListener {
            findNavController().navigate(R.id.acaoStockParaForm)
        }

        binding.recyclerAlertas.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerAlertas.adapter = adapterAlertas

        val daoAlertas = StockifyDatabase.obter(requireContext()).produtoDao()
        daoAlertas.listarEmAlerta().observe(viewLifecycleOwner) { lista ->
            adapterAlertas.submitList(lista)

            binding.txtContadorAlertas.text = resources.getQuantityString(
                R.plurals.stock_itens, lista.size, lista.size
            )

            val vazio = lista.isEmpty()
            binding.txtVazioAlertas.visibility = if (vazio) View.VISIBLE else View.GONE
            binding.recyclerAlertas.visibility = if (vazio) View.GONE else View.VISIBLE
        }

        binding.tabsStock.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val mostrarProdutos = tab.position == 0
                binding.conteudoProdutos.visibility =
                    if (mostrarProdutos) View.VISIBLE else View.GONE
                binding.conteudoAlertas.visibility =
                    if (mostrarProdutos) View.GONE else View.VISIBLE
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    /** Mostra as opções de ordenação da lista junto ao botão. */
    private fun mostrarMenuOrdenacao(ancora: View) {
        val ctx = android.view.ContextThemeWrapper(requireContext(), R.style.MenuPopup)
        val menu = PopupMenu(ctx, ancora)
        menu.menu.add(0, 0, 0, R.string.ordenar_nome)
        menu.menu.add(0, 1, 1, R.string.ordenar_quantidade)
        menu.menu.add(0, 2, 2, R.string.ordenar_valor)

        menu.setOnMenuItemClickListener { item ->
            viewModel.ordenar(item.itemId)
            true
        }
        menu.show()
    }

    /** Pergunta o formato antes de abrir o seletor de ficheiros. */
    private fun escolherFormato() {
        val formatos = arrayOf(
            getString(R.string.exportar_csv),
            getString(R.string.exportar_excel)
        )
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.exportar_titulo)
            .setItems(formatos) { _, indice -> exportar(csv = indice == 0) }
            .setNegativeButton(R.string.cancelar, null)
            .show()
    }

    /** Gera o texto e pede ao sistema onde o gravar. */
    private fun exportar(csv: Boolean) {
        viewLifecycleOwner.lifecycleScope.launch {
            // Exporta o que está visível, respeitando pesquisa e filtros
            val lista = viewModel.listaVisivel()

            if (lista.isEmpty()) {
                aviso(getString(R.string.exportar_sem_dados))
                return@launch
            }

            // Com muitos produtos, gerar o texto no ecrã principal
            // provocaria o aviso de aplicação sem resposta
            conteudoPendente = withContext(Dispatchers.IO) {
                if (csv) produtosParaCsv(lista) else produtosParaExcel(lista)
            }

            val data = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            criarFicheiro.launch("stockify_$data.${if (csv) "csv" else "xls"}")
        }
    }

    /** Escreve no destino escolhido pelo utilizador. */
    private fun escrever(uri: Uri, conteudo: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val sucesso = withContext(Dispatchers.IO) {
                try {
                    requireContext().contentResolver
                        .openOutputStream(uri)
                        ?.use { it.write(conteudo.toByteArray(Charsets.UTF_8)) }
                    true
                } catch (e: Exception) {
                    false
                }
            }
            aviso(getString(
                if (sucesso) R.string.exportar_guardado else R.string.exportar_falhou
            ))
        }
    }

    private fun aviso(texto: String) {
        Toast.makeText(requireContext(), texto, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Sem esta linha o Fragment segura a vista antiga em memória.
        // É a causa mais comum de fugas de memória em aplicações com Fragments
        _binding = null
    }
}
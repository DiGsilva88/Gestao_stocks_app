package com.example.gesto_stocks

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.gesto_stocks.data.local.StockifyDatabase
import com.example.gesto_stocks.databinding.ActivityMainBinding
import com.example.gesto_stocks.util.Sessao
import com.example.gesto_stocks.util.iniciais
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Com edge-to-edge (targetSdk 35+) o adjustResize do manifesto deixa
        // de funcionar sozinho: sem isto o teclado tapa o fundo dos ecrãs e
        // os botões (ex.: Guardar/Eliminar no formulário) ficam inacessíveis.
        //
        // O padding vai no conteúdo e não na raiz, para a gaveta continuar a
        // ocupar o ecrã todo. Em troca, o cabeçalho da gaveta tem de receber
        // aqui o seu próprio topo: o fitsSystemWindows do NavigationView só
        // acolchoa a lista de opções, não o cabeçalho, que ficava por baixo
        // das horas.
        val cabecalho = binding.navigationView.getHeaderView(0)
        val topoCabecalho = cabecalho.paddingTop

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val barras = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
            )
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom)
            cabecalho.updatePadding(top = topoCabecalho + barras.top)
            WindowInsetsCompat.CONSUMED
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNavigation.setupWithNavController(navController)

        binding.navigationView.setNavigationItemSelectedListener { item ->
            binding.drawer.closeDrawer(GravityCompat.START)
            when (item.itemId) {
                R.id.menuEditarPerfil -> navController.navigate(R.id.editarPerfilFragment)
                R.id.menuMovimentos -> navController.navigate(R.id.movimentosFragment)
                R.id.menuSobre -> mostrarSobre()
                R.id.menuTerminarSessao -> confirmarTerminarSessao()
            }
            true
        }

        // Com a gaveta aberta, o botão de voltar fecha-a em vez de sair do ecrã
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() = binding.drawer.closeDrawer(GravityCompat.START)
        }.also { callback ->
            binding.drawer.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
                override fun onDrawerOpened(drawerView: android.view.View) {
                    callback.isEnabled = true
                }

                override fun onDrawerClosed(drawerView: android.view.View) {
                    callback.isEnabled = false
                }
            })
        })
    }

    /** Chamado pelo botão ☰ do cabeçalho de cada ecrã principal. */
    fun abrirMenu() {
        preencherCabecalho()
        binding.drawer.openDrawer(GravityCompat.START)
    }

    /** Nome, email e iniciais do utilizador com sessão, no topo da gaveta. */
    private fun preencherCabecalho() {
        val cabecalho = binding.navigationView.getHeaderView(0) ?: return
        val sessao = Sessao(this)
        val dao = StockifyDatabase.obter(this).utilizadorDao()

        lifecycleScope.launch {
            val u = dao.buscarPorId(sessao.utilizadorId()) ?: return@launch
            cabecalho.findViewById<android.widget.TextView>(R.id.txtNomeMenu).text = u.nome
            cabecalho.findViewById<android.widget.TextView>(R.id.txtEmailMenu).text = u.email
            cabecalho.findViewById<android.widget.TextView>(R.id.txtIniciaisMenu).text =
                iniciais(u.nome)
        }
    }

    private fun mostrarSobre() {
        AlertDialog.Builder(this)
            .setTitle(R.string.sobre_titulo)
            .setMessage(getString(R.string.sobre_mensagem, BuildConfig.VERSION_NAME))
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    /**
     * Termina a sessão depois de confirmar. Vive aqui e não no PerfilFragment
     * porque agora há dois caminhos para a mesma acção: o botão do perfil e a
     * entrada do menu lateral.
     */
    fun confirmarTerminarSessao() {
        AlertDialog.Builder(this)
            .setTitle(R.string.perfil_terminar_sessao)
            .setMessage(R.string.sair_mensagem)
            .setPositiveButton(R.string.sair_confirmar) { _, _ ->
                Sessao(this).terminar()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .setNegativeButton(R.string.cancelar, null)
            .show()
    }
}

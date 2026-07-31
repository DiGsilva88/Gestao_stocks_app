package com.example.gesto_stocks

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.gesto_stocks.data.local.StockifyDatabase
import com.example.gesto_stocks.data.model.Utilizador
import com.example.gesto_stocks.databinding.ActivityRegistoBinding
import com.example.gesto_stocks.util.Sessao
import com.example.gesto_stocks.util.hashPassword
import kotlinx.coroutines.launch

class RegistoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dao = StockifyDatabase.obter(this).utilizadorDao()
        val sessao = Sessao(this)

        binding.btnRegistar.setOnClickListener {
            if (!validar()) return@setOnClickListener

            val nome = binding.editNome.text.toString().trim()
            val email = binding.editEmail.text.toString().trim().lowercase()
            val password = binding.editPassword.text.toString()

            lifecycleScope.launch {
                if (dao.buscarPorEmail(email) != null) {
                    aviso("Já existe uma conta com este email")
                    return@launch
                }

                val id = dao.inserir(
                    Utilizador(
                        nome = nome,
                        email = email,
                        passwordHash = hashPassword(password)
                    )
                )

                sessao.guardarUtilizador(id.toInt())
                startActivity(Intent(this@RegistoActivity, MainActivity::class.java))
                finishAffinity()
            }
        }

        binding.txtIrLogin.setOnClickListener { finish() }
    }

    private fun validar(): Boolean {
        val nome = binding.editNome.text.toString().trim()
        val email = binding.editEmail.text.toString().trim()
        val password = binding.editPassword.text.toString()
        val confirmar = binding.editConfirmar.text.toString()

        if (nome.isEmpty() || email.isEmpty() || password.isEmpty()) {
            aviso("Preenche todos os campos")
            return false
        }
        if (!email.contains("@") || !email.contains(".")) {
            aviso("Email inválido")
            return false
        }
        if (password.length < 6) {
            aviso("A password precisa de pelo menos 6 caracteres")
            return false
        }
        if (password != confirmar) {
            aviso("As passwords não coincidem")
            return false
        }
        return true
    }

    private fun aviso(texto: String) {
        Toast.makeText(this, texto, Toast.LENGTH_SHORT).show()
    }
}
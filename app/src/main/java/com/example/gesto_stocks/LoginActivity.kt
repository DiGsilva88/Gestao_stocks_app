package com.example.gesto_stocks

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.gesto_stocks.data.local.StockifyDatabase
import com.example.gesto_stocks.databinding.ActivityLoginBinding
import com.example.gesto_stocks.util.Sessao
import com.example.gesto_stocks.util.hashPassword
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dao = StockifyDatabase.obter(this).utilizadorDao()
        val sessao = Sessao(this)

        binding.btnEntrar.setOnClickListener {
            val email = binding.editEmail.text.toString().trim().lowercase()
            val password = binding.editPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                aviso("Preenche os dois campos")
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val utilizador = dao.buscarPorEmail(email)

                if (utilizador == null ||
                    utilizador.passwordHash != hashPassword(password)) {
                    aviso("Email ou password incorretos")
                    return@launch
                }

                sessao.guardarUtilizador(utilizador.id)
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
            }
        }

        binding.txtIrRegisto.setOnClickListener {
            startActivity(Intent(this, RegistoActivity::class.java))
        }
    }

    private fun aviso(texto: String) {
        Toast.makeText(this, texto, Toast.LENGTH_SHORT).show()
    }
}
package com.example.gesto_stocks

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.gesto_stocks.databinding.ActivitySplashBinding
import com.example.gesto_stocks.util.Sessao

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = getColor(R.color.splash_fundo)

        if (Sessao(this).temSessao()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        val binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnEntrarSplash.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
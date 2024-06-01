package com.willow.quiz

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.willow.quiz.Data.SharedPrefManager
import com.willow.quiz.databinding.ActivityAboutBinding

@Suppress("DEPRECATION")
class AboutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding
    private val isLoggedIn: Boolean
        get() {
            val token = SharedPrefManager.getToken(this)
            return token != null
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setOnClickMenu()
    }
    private fun setOnClickMenu() {
        binding.bottomNavigationView.setItemSelected(R.id.about, true)
        binding.bottomNavigationView.setOnItemSelectedListener {
            if (it == R.id.home) {
                updateUI(this, MainActivity::class.java)
                finish()
            } else if (it == R.id.me) {
                if (isLoggedIn) {
                    updateUI(this, MeActivity::class.java)
                    finish()
                } else {
                    updateUI(this, LoginActivity::class.java)
                    finish()
                }
            }
        }
    }

    private fun updateUI(context: Context, targetActivity: Class<*>) {
        val intent = Intent(context, targetActivity)
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        context.startActivity(intent)
    }
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        updateUI(this, MainActivity::class.java)
        finish()
    }

}
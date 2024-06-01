package com.willow.quiz


import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.PorterDuff
import android.graphics.Shader
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.willow.quiz.Data.SharedPrefManager
import com.willow.quiz.Data.UserPreferences
import com.willow.quiz.Models.LoginResponse
import com.willow.quiz.Sever.ApiClient
import com.willow.quiz.Sever.ApiSevices
import com.willow.quiz.databinding.ActivityLoginBinding

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.regex.Pattern


@Suppress("DEPRECATION", "NAME_SHADOWING")
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)



        binding.logInButton.setOnClickListener {
            val email = binding.emailLogIn.text.toString()
            val password = binding.etpasswordLogin.text.toString()

            if (TextUtils.isEmpty(email)) {
                binding.emailLogIn.error = "Email is required"
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(password)) {
                binding.etpasswordLogin.error = "Password is required"
                return@setOnClickListener
            }
            if (isValidEmail(email)){
                login(email, password)
                showProgressBar()
                hideProgressBar()
            }else{
                binding.emailLogIn.error = "Invalid email format"
                return@setOnClickListener
            }
        }

        binding.register.setOnClickListener {
            updateUI(this, SignupActivity::class.java)
        }
        //Change text color gradient
        textColor()

    }

    private fun login(email: String, password: String) {
        val userPrefs = UserPreferences(this)

        ApiClient.getRetrofitInstance().create(ApiSevices::class.java).login(email, password)
            .enqueue(object : Callback<LoginResponse> {
                override fun onResponse(
                    call: Call<LoginResponse>, response: Response<LoginResponse>
                ) {
                    if (response.isSuccessful) {
                        val data = response.body()

                        if (data?.token == null) {
                            customToast(this@LoginActivity,
                                data?.message.toString(),
                                Color.parseColor("#FDD2B5"),
                                Color.parseColor("#322A24")
                            )
                        } else {
                            val data = response.body()

                            SharedPrefManager.saveToken(
                                this@LoginActivity, data?.token.toString()
                            )
                            userPrefs.saveUserInfo(
                                data?.user?.name.toString(),
                                data?.user?.email.toString(),
                                data?.user?.details?.dateOfBirth.toString(),
                                data?.user?.details?.phone.toString(),
                                data?.user?.details?.gender.toString()
                            )
                            customToast(this@LoginActivity,
                                "Login successful!",
                                Color.parseColor("#ACDBC9"),
                                Color.parseColor("#33413C")
                            )
                            Log.e(TAG, data?.token.toString())

                            updateUI(this@LoginActivity, MeActivity::class.java)
                        }


                    } else {
                        customToast(this@LoginActivity,
                            "Login failed!",
                            Color.parseColor("#FDD2B5"),
                            Color.parseColor("#322A24")
                        )
                        Log.e(TAG, response.code().toString())
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, t.message, Toast.LENGTH_LONG).show()
                    Log.e(TAG, "onFailure: ${t.message}")
                }

            })
    }

    fun textColor() {
        val txtLogin = findViewById<TextView>(R.id.txtLogin)
        val paint = txtLogin.paint
        val width = paint.measureText(txtLogin.text.toString())

        txtLogin.paint.shader = LinearGradient(
            0f, 0f, width, txtLogin.textSize, intArrayOf(
                Color.parseColor("#0068ff"),
                Color.parseColor("#02c166"),
            ), null, Shader.TileMode.REPEAT
        )

        val textView = findViewById<TextView>(R.id.register)
        val text = textView.text.toString()
        val spannableBuilder = SpannableString(text)
        spannableBuilder.setSpan(
            ForegroundColorSpan(Color.BLACK), 0, 24, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        textView.text = spannableBuilder
    }

    private fun showProgressBar() {
        binding.login.visibility = View.GONE
        binding.progressbarLogin.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.login.visibility = View.VISIBLE
        binding.progressbarLogin.visibility = View.GONE
    }

    private fun updateUI(context: Context, targetActivity: Class<*>) {
        val intent = Intent(context, targetActivity)
        intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION or Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_TASK_ON_HOME
        context.startActivity(intent)
        finish()
    }

    //Custom Toast
    @SuppressLint("InflateParams")
    fun customToast(context: Context, message: String, backgroundColor: Int, textColor: Int) {
        val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout = layoutInflater.inflate(R.layout.custom_toast_layout, null)

        val toastTextView = layout.findViewById<TextView>(R.id.toast_text)
        toastTextView.text = message
        toastTextView.setTextColor(textColor)

        val toast = Toast(context)
        toast.duration = Toast.LENGTH_LONG
        toast.view = layout

        // Set background color for the toast
        val toastView = toast.view
        toastView?.background?.setColorFilter(backgroundColor, PorterDuff.Mode.SRC_IN)

        toast.show()
    }

    // Check format email
    private fun isValidEmail(email: String): Boolean {
        val pattern: Pattern = Patterns.EMAIL_ADDRESS
        return pattern.matcher(email).matches()
    }
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        updateUI(this, MainActivity::class.java)
        finish()
    }
}




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
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.willow.quiz.Data.SharedPrefManager
import com.willow.quiz.Models.LoginResponse
import com.willow.quiz.Sever.ApiClient
import com.willow.quiz.Sever.ApiSevices
import com.willow.quiz.databinding.ActivitySignupBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.regex.Pattern

@Suppress("DEPRECATION", "NAME_SHADOWING")
class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        changeTextColor()

        binding.signUpButton.setOnClickListener {

            val name = binding.editName.text.toString()
            val email = binding.emailSignUp.text.toString()
            val password = binding.etpasswordSignUp.text.toString()

            if (TextUtils.isEmpty(email)) {
                binding.emailSignUp.error = "Email is required"
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(password)) {
                binding.etpasswordSignUp.error = "Password is required"
                return@setOnClickListener
            }
            if (isValidEmail(email)){
                signup()
            }else{
                binding.emailSignUp.error = "Invalid email format"
                return@setOnClickListener
            }
        }

        binding.login.setOnClickListener{
            updateUI(this, LoginActivity::class.java)
        }
    }

    private fun signup() {
        val name = binding.editName.text.toString()
        val email = binding.emailSignUp.text.toString()
        val password = binding.etpasswordSignUp.text.toString()
        val passwordConf = binding.etConfirmPassword.text.toString()

        ApiClient.getRetrofitInstance().create(ApiSevices::class.java)
            .create(email, name, password, passwordConf).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful) {
                        val data = response.body()

                        if (data?.token == null) {
                            customToast(this@SignupActivity,
                                data?.message.toString(),
                                Color.parseColor("#FDD2B5"),
                                Color.parseColor("#322A24")
                            )
                        } else {
                            val data = response.body()

                            SharedPrefManager.saveToken(
                                this@SignupActivity, data?.token.toString()
                            )
                            customToast(this@SignupActivity,
                                "Login successful!",
                                Color.parseColor("#ACDBC9"),
                                Color.parseColor("#33413C")
                            )
                            Log.e(TAG, data?.token.toString())

                            updateUI(this@SignupActivity, MeActivity::class.java)
                        }


                    } else {
                        customToast(this@SignupActivity,
                            "Sign up failed.",
                            Color.parseColor("#FDD2B5"),
                            Color.parseColor("#322A24")
                        )
                        Log.e(TAG, response.body()?.message.toString())
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, throwable: Throwable) {
                    Log.e(TAG, "onFailure: $throwable")
                    Toast.makeText(this@SignupActivity, "An error occurred", Toast.LENGTH_SHORT)
                        .show()
                }

            })
    }
    //Change text color
    private fun changeTextColor(){
        //Change text color gradient
        val txtSignup = findViewById<TextView>(R.id.txtSignup)
        val paint = txtSignup.paint
        val width = paint.measureText(txtSignup.text.toString())

        txtSignup.paint.shader = LinearGradient(
            0f, 0f, width, txtSignup.textSize , intArrayOf(
                Color.parseColor("#0068ff"),
                Color.parseColor("#02c166"),
            ),null, Shader.TileMode.REPEAT
        )
        //change multicolor
        val textView = findViewById<TextView>(R.id.login)
        val text = textView.text.toString()
        val spannableBuilder = SpannableString(text)
        spannableBuilder.setSpan(ForegroundColorSpan(Color.BLACK), 0, 24, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        textView.text = spannableBuilder
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
    private fun updateUI(context: Context, targetActivity: Class<*>) {
        val intent = Intent(context, targetActivity)
        intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION or Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_TASK_ON_HOME
        context.startActivity(intent)
        finish()
    }
}
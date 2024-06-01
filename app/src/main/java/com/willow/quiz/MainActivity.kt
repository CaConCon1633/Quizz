package com.willow.quiz

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.view.Window
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.willow.quiz.Data.SharedPrefManager
import com.willow.quiz.Data.UserPreferences
import com.willow.quiz.Models.ShortId
import com.willow.quiz.Sever.ApiClient
import com.willow.quiz.Sever.ApiSevices
import com.willow.quiz.databinding.ActivityMainBinding
import com.willow.quiz.databinding.InforExamLayoutBinding
import com.willow.quiz.databinding.ResultExamLayoutBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    var examId = ""
    private val isLoggedIn: Boolean
        get() {
            val token = SharedPrefManager.getToken(this)
            return token != null
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //change style textview
        val benefit1 = findViewById<TextView>(R.id.benefit_1)
        makeBold(benefit1, 0, 47)
        val benefit2 = findViewById<TextView>(R.id.benefit_2)
        makeBold(benefit2, 0, 27)
        val benefit3 = findViewById<TextView>(R.id.benefit_3)
        makeBold(benefit3, 0, 28)
        val benefit4 = findViewById<TextView>(R.id.benefit_4)
        makeBold(benefit4, 0, 21)

        setOnClickMenu()
        getExam()


    }

    private fun getExam() {

        binding.btnJoin.setOnClickListener {

            binding.codeExam.onEditorAction(EditorInfo.IME_ACTION_DONE)

            val shortId = binding.codeExam.text.toString().uppercase().trim()


            Log.e(TAG, "joinExam: $shortId")

            if (TextUtils.isEmpty(shortId)) {
                binding.codeExam.error = "Code is required"
                return@setOnClickListener
            }

            binding.progressBar.visibility = View.VISIBLE

            val token= SharedPrefManager.getToken(this)

            ApiClient.getRetrofitInstance().create(ApiSevices::class.java).getExamId("Bearer $token",shortId)
                .enqueue(object : Callback<ShortId> {
                    @SuppressLint("SetTextI18n")
                    override fun onResponse(p0: Call<ShortId>, p1: Response<ShortId>) {
                        if (p1.isSuccessful) {
                            binding.progressBar.visibility = View.GONE
                            binding.scrollView2.visibility = View.VISIBLE
                            val data = p1.body()
                            examId = data?.examId.toString()
                            Log.d(TAG, data.toString())
                            if(data?.result != null){

                                //If the user participates in the exam, the exam results will be displayed
                                val builder = Dialog(this@MainActivity, android.R.style.Theme_DeviceDefault_NoActionBar_Fullscreen )
                                builder.requestWindowFeature(Window.FEATURE_NO_TITLE)
                                val binding = ResultExamLayoutBinding.inflate(layoutInflater)
                                builder.setContentView(binding.root)

                                binding.txtCountQuestions.text = "You get: " + data.countCorrect.toString() +"/" +data.countQuestions.toString()
                                binding.txtResult.text = "Score: " + data.result.toString()

                                binding.btnCancel.setOnClickListener {
                                    builder.dismiss()
                                }
                                builder.show()
                            }
                            if (data?.examId != null) {

                                val totalSecs = data.duration!!.toInt()
                                val hours = totalSecs / 3600
                                val minutes = (totalSecs % 3600) / 60
                                val seconds = totalSecs % 60

                                val builder = Dialog(this@MainActivity, android.R.style.Theme_DeviceDefault_NoActionBar_Fullscreen )

                                builder.requestWindowFeature(Window.FEATURE_NO_TITLE)
                                val time = hours.toString() + "h " + minutes.toString() + "m " + seconds.toString() + "s"

                                val binding: InforExamLayoutBinding = InforExamLayoutBinding.inflate(layoutInflater)
                                builder.setContentView(binding.root)

                                binding.txtExam.text = "Exam: " + data.title.toString()
                                if (data.description == null) {
                                    binding.txtDescription.text = "Description: "
                                } else {
                                    binding.txtDescription.text =
                                        "Description: " + data.description.toString()
                                }
                                binding.txtDuration.text = "Duration: $time"
                                binding.txtQuestion.text =
                                    "Question: " + data.questionsCount.toString()
                                if(SharedPrefManager.getToken(this@MainActivity) == null){
                                    binding.txtExaminee.text = "The result only saves if you logged"
                                    binding.txtExaminee.setTextColor(getColor(R.color.dark_red))
                                }else {
                                    binding.txtExaminee.text = "Examinee: " + UserPreferences(this@MainActivity).getUserInfo()["name"]
                                }

                                binding.btnJoin.setOnClickListener {
                                    val intent = Intent(this@MainActivity, ExamActivity::class.java)
                                    intent.putExtra("getExamId", examId)
                                    intent.putExtra("getQuestionsCount", data.questionsCount)
                                    intent.putExtra("getDuration", data.duration)
                                    intent.flags = FLAG_ACTIVITY_NO_ANIMATION or Intent.FLAG_ACTIVITY_NO_HISTORY
                                    startActivity(intent)
                                }

                                binding.btnCancel.setOnClickListener {
                                    builder.dismiss()
                                }

                                builder.show()
                            }
                        } else {
                            binding.progressBar.visibility = View.GONE
                            binding.scrollView2.visibility = View.VISIBLE
                            Toast.makeText(this@MainActivity, "Exam not found", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }

                    override fun onFailure(p0: Call<ShortId>, p1: Throwable) {
                        Log.e(TAG, "onFailure: $p1")
                        binding.progressBar.visibility = View.GONE
                        binding.scrollView2.visibility = View.VISIBLE
                        Toast.makeText(this@MainActivity, "An error occurred", Toast.LENGTH_SHORT)
                            .show()
                    }
                })

        }
    }

    private fun setOnClickMenu() {
        binding.bottomNavigationView.setItemSelected(R.id.home, true)
        binding.bottomNavigationView.setOnItemSelectedListener {
            if (it == R.id.about) {
                updateUI(this, AboutActivity::class.java)
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
        intent.setFlags(FLAG_ACTIVITY_NO_ANIMATION)
        context.startActivity(intent)
    }

    private fun makeBold(textView: TextView, begin: Int, end: Int) {
        val text = textView.text.toString()
        val spannableBuilder = SpannableString(text)
        val style = Typeface.BOLD
        spannableBuilder.setSpan(StyleSpan(style), begin, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        textView.text = spannableBuilder
    }

}
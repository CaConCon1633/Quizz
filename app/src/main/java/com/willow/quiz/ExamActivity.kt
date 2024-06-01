package com.willow.quiz

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.willow.quiz.Adapters.AnswerAdapter
import com.willow.quiz.Data.SharedPrefManager
import com.willow.quiz.Models.Answers
import com.willow.quiz.Models.JoinExam
import com.willow.quiz.Models.JoinExam.Question
import com.willow.quiz.Models.ResultExam
import com.willow.quiz.Sever.ApiClient
import com.willow.quiz.Sever.ApiSevices
import com.willow.quiz.databinding.ActivityExamBinding
import com.willow.quiz.databinding.ResultExamLayoutBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Suppress("UNCHECKED_CAST", "NAME_SHADOWING")
class ExamActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExamBinding
    private lateinit var answerAdapter: AnswerAdapter
    private val handler = Handler(Looper.getMainLooper())
    var number = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExamBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val token = SharedPrefManager.getToken(this@ExamActivity)
        val examId = intent.getStringExtra("getExamId").toString()
        val questionCount = intent.getIntExtra("getQuestionsCount", 0)
        val duration = intent.getIntExtra("getDuration", 0)

        val userAnswers: MutableList<String> = MutableList(questionCount) { ""}

        binding.btnExit.setOnClickListener {
            val builder = AlertDialog.Builder(this)

            builder.setTitle("Are you sure you want to exit?")

            builder.setNegativeButton("Yes") { _, _ ->
                val intent = Intent(this@ExamActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION or Intent.FLAG_ACTIVITY_NO_HISTORY
                startActivity(intent)
            }
            builder.setPositiveButton("No") { _, _ ->
            }

            builder.create()
            builder.show()
        }

        startCountdown(duration, examId, token.toString(), userAnswers)

        ApiClient.getRetrofitInstance().create(ApiSevices::class.java).getJoinExam("Bearer $token",examId)
            .enqueue(object : Callback<JoinExam> {
                @SuppressLint("SetTextI18n")
                override fun onResponse(p0: Call<JoinExam>, p1: Response<JoinExam>) {
                    if (p1.isSuccessful) {
                        val data = p1.body()
                        val questions = data?.questions
                        if (questions != null) {
                            if (number == questions.size - 1) {

                                //For the exam there is only 1 question
                                binding.btnNext.visibility = View.GONE
                                binding.btnSubmit.visibility = View.VISIBLE

                                binding.numberQuestion.text = "Question ${number + 1}: "
                                binding.numberQuestion.setTypeface(Typeface.DEFAULT_BOLD)
                                binding.txtQuestion.text = questions[number]!!.question.toString()

                                val answers = questions[number]!!.answers
                                answerAdapter = AnswerAdapter(
                                    answers as List<Question.Answer>,
                                    userAnswers,
                                    number,
                                    this@ExamActivity
                                )
                                binding.recyclerView.layoutManager =
                                    LinearLayoutManager(this@ExamActivity)
                                binding.recyclerView.adapter = answerAdapter

                            } else {
                                //The test only has many questions
                                binding.numberQuestion.text = "Question ${number + 1}: "
                                binding.numberQuestion.setTypeface(Typeface.DEFAULT_BOLD)
                                binding.txtQuestion.text = questions[number]!!.question.toString()

                                val answers = questions[number]!!.answers
                                answerAdapter = AnswerAdapter(
                                    answers as List<Question.Answer>,
                                    userAnswers,
                                    number,
                                    this@ExamActivity
                                )
                                binding.recyclerView.layoutManager =
                                    LinearLayoutManager(this@ExamActivity)
                                binding.recyclerView.adapter = answerAdapter

                                binding.btnNext.setOnClickListener {

                                    number++
                                    binding.numberQuestion.text = "Question ${number + 1}: "
                                    binding.numberQuestion.setTypeface(Typeface.DEFAULT_BOLD)
                                    binding.txtQuestion.text =
                                        questions[number]!!.question.toString()


                                    val answers = questions[number]!!.answers
                                    answerAdapter = AnswerAdapter(
                                        answers as List<Question.Answer>,
                                        userAnswers,
                                        number,
                                        this@ExamActivity
                                    )

                                    binding.recyclerView.layoutManager =
                                        LinearLayoutManager(this@ExamActivity)
                                    binding.recyclerView.adapter = answerAdapter
                                    if (number == questions.size - 1) {
                                        binding.btnSubmit.visibility = View.VISIBLE
                                    }
                                }
                                binding.btnBack.setOnClickListener {
                                    if (number != 0) {
                                        number--
                                        binding.numberQuestion.text = "Question ${number + 1}: "
                                        binding.numberQuestion.setTypeface(Typeface.DEFAULT_BOLD)
                                        binding.txtQuestion.text =
                                            questions[number]!!.question.toString()


                                        val answers = questions[number]!!.answers
                                        answerAdapter = AnswerAdapter(
                                            answers as List<Question.Answer>,
                                            userAnswers,
                                            number,
                                            this@ExamActivity
                                        )

                                        binding.recyclerView.layoutManager =
                                            LinearLayoutManager(this@ExamActivity)
                                        binding.recyclerView.adapter = answerAdapter
                                        if (number < questions.size - 1) {
                                            binding.btnNext.visibility = View.VISIBLE
                                            binding.btnSubmit.visibility = View.GONE
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                override fun onFailure(p0: Call<JoinExam>, p1: Throwable) {
                    Log.e(TAG, "onFailure: $p1")
                    Toast.makeText(this@ExamActivity, "An error occurred", Toast.LENGTH_SHORT)
                        .show()
                }
            })

        binding.btnSubmit.setOnClickListener {
            val builder = AlertDialog.Builder(this)

            builder.setTitle("Do you want to submit the exam?")

            builder.setNegativeButton("Yes") { _, _ ->

                val answers = Answers(userAnswers)
                ApiClient.getRetrofitInstance().create(ApiSevices::class.java)
                    .postSubmit("Bearer $token", examId, answers).enqueue(object : Callback<ResultExam> {
                        @SuppressLint("SetTextI18n")
                        override fun onResponse(p0: Call<ResultExam>, p1: Response<ResultExam>) {
                            if (p1.isSuccessful) {
                                val data = p1.body()
//                                Log.e(TAG, userAnswers.toString() )
                                Log.e(TAG, "$userAnswers")
                                Log.e(TAG, "onResponse: " + data?.result)

                                val dialog = Dialog(
                                    this@ExamActivity,
                                    android.R.style.Theme_DeviceDefault_NoActionBar_Fullscreen
                                )

                                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

                                val binding = ResultExamLayoutBinding.inflate(layoutInflater)
                                dialog.setContentView(binding.root)

                                binding.txtCountQuestions.text =
                                    "You get: " + data?.countCorrect.toString() + "/" + data?.countQuestions.toString()
                                binding.txtResult.text = "Score: " + data?.result.toString()

                                binding.btnCancel.setOnClickListener {
                                    updateUI(this@ExamActivity, MainActivity::class.java)
                                    dialog.dismiss()
                                }
                                dialog.show()
                            }
                        }

                        override fun onFailure(p0: Call<ResultExam>, p1: Throwable) {
                            Log.e(TAG, "onFailure: submitExam")
                            Toast.makeText(
                                this@ExamActivity, "An error occurred", Toast.LENGTH_SHORT
                            ).show()
                        }

                    })
            }
            builder.setPositiveButton("No") { _, _ ->
            }

            builder.create()
            builder.show()

        }
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {

        val builder = AlertDialog.Builder(this)

        builder.setTitle("Are you sure you want to exit?")

        builder.setNegativeButton("Yes") { _, _ ->
            val intent = Intent(this@ExamActivity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION or Intent.FLAG_ACTIVITY_NO_HISTORY
            startActivity(intent)
        }
        builder.setPositiveButton("No") { _, _ ->
        }

        builder.create()
        builder.show()
    }

    private fun startCountdown(duration1:Int, examId: String, token: String, userAnswers: List<String>) {

        var duration = duration1
        val runnable = object : Runnable {
            @SuppressLint("SetTextI18n")
            override fun run() {
                val hours = duration / 3600
                val minutes = (duration % 3600) / 60
                val seconds = duration % 60
                val timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds)

                binding.duration.text = timeString

                if(duration <= 30){
                    binding.duration.setTextColor(getColor(R.color.red))
                }
                if (duration > 0) {
                    duration--
                    handler.postDelayed(this, 1000)
                } else {
                    binding.duration.text = "Time out"

                    val answers = Answers(userAnswers)

                    ApiClient.getRetrofitInstance().create(ApiSevices::class.java)
                        .postSubmit("Bearer $token", examId, answers).enqueue(object : Callback<ResultExam> {
                            @SuppressLint("SetTextI18n")
                            override fun onResponse(p0: Call<ResultExam>, p1: Response<ResultExam>) {
                                if (p1.isSuccessful) {
                                    val data = p1.body()
                                    Log.e(TAG, "$userAnswers")
                                    Log.e(TAG, "onResponse: " + data?.result)

                                    val dialog = Dialog(
                                        this@ExamActivity,
                                        android.R.style.Theme_DeviceDefault_NoActionBar_Fullscreen
                                    )

                                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

                                    val binding = ResultExamLayoutBinding.inflate(layoutInflater)
                                    dialog.setContentView(binding.root)

                                    binding.txtCountQuestions.text =
                                        "You get: " + data?.countCorrect.toString() + "/" + data?.countQuestions.toString()
                                    binding.txtResult.text = "Score: " + data?.result.toString()

                                    binding.btnCancel.setOnClickListener {
                                        updateUI(this@ExamActivity, MainActivity::class.java)
                                        dialog.dismiss()
                                    }
                                    dialog.show()
                                }
                            }

                            override fun onFailure(p0: Call<ResultExam>, p1: Throwable) {
                                Log.e(TAG, "onFailure: submitExam")
                                Toast.makeText(
                                    this@ExamActivity, "An error occurred", Toast.LENGTH_SHORT
                                ).show()
                            }

                        })

                }
            }
        }

        handler.post(runnable)
    }

    private fun updateUI(context: Context, targetActivity: Class<*>) {
        val intent = Intent(context, targetActivity)
        intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION or Intent.FLAG_ACTIVITY_TASK_ON_HOME
        context.startActivity(intent)
    }
}
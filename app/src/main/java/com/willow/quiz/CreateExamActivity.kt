package com.willow.quiz

import Questions
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.GsonBuilder
import com.willow.quiz.Adapters.ChoiceAdapter
import com.willow.quiz.Data.SharedPrefManager
import com.willow.quiz.Models.Exam
import com.willow.quiz.Models.UpdateExam
import com.willow.quiz.Sever.ApiClient
import com.willow.quiz.Sever.ApiSevices
import com.willow.quiz.databinding.ActivityCreateExamBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Suppress("NAME_SHADOWING")
class CreateExamActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateExamBinding
    //var examId = ""
    private lateinit var db : Questions
    private lateinit var choiceAdapter: ChoiceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateExamBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val token = SharedPrefManager.getToken(this)

        this.db = Questions(this@CreateExamActivity)

        binding.question.setOnClickListener {
            binding.createExam.visibility = View.VISIBLE
            binding.configExam.visibility = View.GONE
        }
        binding.config.setOnClickListener {
            binding.createExam.visibility = View.GONE
            binding.configExam.visibility = View.VISIBLE
        }
        binding.cancelButton.setOnClickListener {
            updateUI(this, MeActivity::class.java)
            db.deleteAllDataFromTable()
        }

        configExam()

        var examId = intent.getStringExtra("examId").toString()



        if (examId == "null") {
            ApiClient.getRetrofitInstance().create(ApiSevices::class.java)
                .postCreate("Bearer $token")
                .enqueue(object : Callback<Exam> {
                    @SuppressLint("SetTextI18n")
                    override fun onResponse(call: Call<Exam>, response: Response<Exam>) {
                        if (response.isSuccessful) {
                            val data = response.body()

                            examId = data?.examId.toString()
                            getExamId(examId, token.toString())
                            binding.title.setText(data?.title)
                        } else {
                            Toast.makeText(this@CreateExamActivity, "Failed to create exam", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Exam>, t: Throwable) {
                        Log.e(TAG, "onFailure: $t")
                        Toast.makeText(this@CreateExamActivity, "An error occurred", Toast.LENGTH_SHORT).show()
                    }
                })
        }else{
            getExamId(examId, token.toString())
        }


        updateExam(examId)

        binding.saveButton.setOnClickListener {

            val questions = db.getAllQuestions()
            val title = binding.title.text.toString()
            val decription = binding.decription.text.toString()
            val duration = binding.duration.text.toString().toInt()
            val status: String = if (binding.status.isChecked) {
                "publish"
            } else {
                "draft"
            }

            val exam = UpdateExam(decription,duration,questions,status, title)

            ApiClient.getRetrofitInstance().create(ApiSevices::class.java).upadteExam(examId, "Bearer $token", exam)
                .enqueue(object : Callback<Exam>{
                    override fun onResponse(p0: Call<Exam>, p1: Response<Exam>) {
                        if (p1.isSuccessful){
                            Log.e(TAG, p1.body()?.shortId.toString() )
                            Toast.makeText(this@CreateExamActivity, "Update successfull.", Toast.LENGTH_SHORT).show()
                            if (p1.body()?.shortId != null){
                                binding.codeExam.text = p1.body()?.shortId.toString()
                            }
                            binding.createExam.visibility = View.GONE
                            binding.configExam.visibility = View.VISIBLE
                        }
                    }

                    override fun onFailure(p0: Call<Exam>, p1: Throwable) {
                        Log.e(TAG, "onFailure: $p1")
                        Toast.makeText(this@CreateExamActivity, "An error occurred", Toast.LENGTH_SHORT)
                            .show()
                    }
                })

        }

    }

    fun convertQuestionsToRequiredFormat(questions: List<Exam.Question>): List<Map<String, Any?>> {
        return questions.map { question ->
            val answersList = question.answers?.map { mapOf("answer" to it?.answer.toString()) }
            mapOf(
                "question" to question.question,
                "correct" to question.correct,
                "answers" to answersList
            )
        }
    }

    private fun configExam() {

        updateStatus(false)
        binding.status.setOnCheckedChangeListener { _, isChecked ->
            updateStatus(isChecked)
        }
    }

    fun getExamId(examId: String, token: String){

        binding.progressBar.visibility = View.VISIBLE

        ApiClient.getRetrofitInstance().create(ApiSevices::class.java).getExam(examId, "Bearer $token")
            .enqueue(object : Callback<Exam>{
                override fun onResponse(p0: Call<Exam>, p1: Response<Exam>) {
                    if (p1.isSuccessful){
                        db = Questions(this@CreateExamActivity)
                        db.deleteAllDataFromTable()
                        binding.progressBar.visibility = View.GONE
                        val data = p1.body()
                        binding.title.setText(   data?.title )
                        if (data?.description != null) {
                            binding.decription.setText(data.description.toString())
                        }
                        if (data?.status == "publish"){
                            binding.status.setChecked(true)
                        }
                        binding.duration.setText(data?.duration.toString())
                        if(data?.shortId != null){
                            binding.codeExam.text = data.shortId
                            binding.codeExam.setOnClickListener {
                                copyTextToClipboard(binding.codeExam.text.toString())
                            }
                        }

                        val questions: List<Exam.Question> = data?.questions as List<Exam.Question>
                        for(question in questions){
                            db.addQuestion(question)
                        }

                    }
                }

                override fun onFailure(p0: Call<Exam>, p1: Throwable) {
                    Log.e(TAG, "onFailure: $p1")
                    Toast.makeText(this@CreateExamActivity, "An error occurred", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    fun updateExam(examId: String){
        var userAnswers = mutableListOf<String>()
        db = Questions(this)
        db.getAllQuestions()
        var id = db.getFirstQuestionId()


        userAnswers = db.getAnswersForQuestion(id).toMutableList()
        var correct = db.getCorrectAnswerForQuestion(id)
        binding.txtQuestion.setText(db.getQuestionById(id).toString())

        choiceAdapter = ChoiceAdapter(userAnswers,correct , this)
        binding.recyclerView.layoutManager =
            LinearLayoutManager(this)
        binding.recyclerView.adapter = choiceAdapter

        binding.btnNext.setOnClickListener{
            if (id < db.getLastQuestionId()){
                id++
                userAnswers = db.getAnswersForQuestion(id).toMutableList()
                correct = db.getCorrectAnswerForQuestion(id)
                binding.txtQuestion.setText(db.getQuestionById(id).toString())

                choiceAdapter = ChoiceAdapter(userAnswers,correct , this)
                binding.recyclerView.layoutManager =
                    LinearLayoutManager(this)
                binding.recyclerView.adapter = choiceAdapter
            }
        }
        binding.btnBack.setOnClickListener {
            if (id > 1){
                id--
                userAnswers = db.getAnswersForQuestion(id).toMutableList()
                correct = db.getCorrectAnswerForQuestion(id)

                binding.txtQuestion.setText(db.getQuestionById(id).toString())

                choiceAdapter = ChoiceAdapter(userAnswers,correct , this)
                binding.recyclerView.layoutManager =
                    LinearLayoutManager(this)
                binding.recyclerView.adapter = choiceAdapter
            }
        }

        binding.addChoice.setOnClickListener {
            var newAnswer = "Option"
            userAnswers.add(newAnswer)
            choiceAdapter = ChoiceAdapter(userAnswers,correct , this)
            binding.recyclerView.layoutManager =
                LinearLayoutManager(this)
            binding.recyclerView.adapter = choiceAdapter

        }

        binding.newQuestion.setOnClickListener {

            if (binding.txtQuestion.text.toString() == "null" ||binding.txtQuestion.text == null ){
                userAnswers.clear()
                correct = -1
                binding.txtQuestion.setText("")

                choiceAdapter = ChoiceAdapter(userAnswers,correct , this)
                binding.recyclerView.layoutManager =
                    LinearLayoutManager(this)
                binding.recyclerView.adapter = choiceAdapter

            }else{
                val txtQuestion = binding.txtQuestion.text.toString()
                val correct = choiceAdapter.getCheck()
                val answersList = userAnswers

                val answers = answersList.map { Exam.Question.Answer(it) }

                val question = Exam.Question(answers, correct, txtQuestion)

                db.addQuestion(question)

                binding.txtQuestion.setText("")
                userAnswers.clear()
                choiceAdapter.refreshData(userAnswers)
            }
        }

    }

    private fun updateUI(context: Context, targetActivity: Class<*>) {
        val intent = Intent(context, targetActivity)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME
        context.startActivity(intent)
        finish()
    }

    private fun updateStatus(isChecked: Boolean) {
        if (isChecked) {
            binding.status.paintFlags =
                binding.status.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        } else {
            binding.status.paintFlags = binding.status.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        }
    }
    private fun copyTextToClipboard(text: String) {
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("text", text)
        clipboardManager.setPrimaryClip(clipData)
    }
}
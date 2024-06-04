package com.willow.quiz

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.willow.quiz.Adapters.ReportAdapter
import com.willow.quiz.Data.SharedPrefManager
import com.willow.quiz.Models.Dashboard
import com.willow.quiz.Models.Report
import com.willow.quiz.Sever.ApiClient
import com.willow.quiz.Sever.ApiSevices
import com.willow.quiz.databinding.ActivityDashBoardBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DashBoardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashBoardBinding
    private lateinit var reportAdapter: ReportAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDashBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            updateUI(this, MeActivity::class.java)
            finish()
        }

        val token = SharedPrefManager.getToken(this)

        ApiClient.getRetrofitInstance().create(ApiSevices::class.java).getDashBoard("Bearer $token")
            .enqueue(object : Callback<Report>{
                override fun onResponse(p0: Call<Report>, p1: Response<Report>) {
                    if (p1.isSuccessful){
                        binding.progressBar2.visibility = View.GONE
                        binding.dashboard.visibility = View.VISIBLE
                        binding.exams.text = "Exams: ${p1.body()?.examsCount}"
                        binding.number.text = "Number of who joined your exams: ${p1.body()?.resultsCount} \n(not count guest)"
                        binding.joinedExams.text = "Joined exams: ${p1.body()?.joinedExamsCount}"

                        if (p1.body()?.exams != null){
                            val exams = p1.body()?.exams
                            reportAdapter = ReportAdapter(exams as List<Report.Exam>, this@DashBoardActivity)
                            binding.recyclerView.layoutManager =
                                LinearLayoutManager(this@DashBoardActivity)
                            binding.recyclerView.adapter = reportAdapter
                        }
                    }
                }

                override fun onFailure(p0: Call<Report>, p1: Throwable) {
                    Log.e(ContentValues.TAG, "onFailure: $p1")
                    Toast.makeText(this@DashBoardActivity, "An error occurred", Toast.LENGTH_SHORT).show()
                }
            })
    }
    private fun updateUI(context: Context, targetActivity: Class<*>) {
        val intent = Intent(context, targetActivity)
        intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION or Intent.FLAG_ACTIVITY_TASK_ON_HOME
        context.startActivity(intent)
    }
}
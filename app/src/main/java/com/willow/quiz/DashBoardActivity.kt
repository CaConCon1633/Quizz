package com.willow.quiz

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.willow.quiz.Data.SharedPrefManager
import com.willow.quiz.Models.Dashboard
import com.willow.quiz.Sever.ApiClient
import com.willow.quiz.Sever.ApiSevices
import com.willow.quiz.databinding.ActivityDashBoardBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DashBoardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashBoardBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDashBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val token = SharedPrefManager.getToken(this)

        ApiClient.getRetrofitInstance().create(ApiSevices::class.java).getDashBoard("Bearer $token")
            .enqueue(object : Callback<Dashboard>{
                override fun onResponse(p0: Call<Dashboard>, p1: Response<Dashboard>) {
                    if (p1.isSuccessful){
                        binding.progressBar2.visibility = View.GONE
                        binding.dashboard.visibility = View.VISIBLE
                        binding.exams.text = "Exams: ${p1.body()?.examsCount}"
                        binding.number.text = "Number of who joined your exams: ${p1.body()?.resultsCount} \n(not count guest)"
                        binding.joinedExams.text = "Joined exams: ${p1.body()?.joinedExamsCount}"
                    }
                }

                override fun onFailure(p0: Call<Dashboard>, p1: Throwable) {
                    Log.e(ContentValues.TAG, "onFailure: $p1")
                    Toast.makeText(this@DashBoardActivity, "An error occurred", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }
}
package com.willow.quiz

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.willow.quiz.Adapters.ExamAdapter
import com.willow.quiz.Data.SharedPrefManager
import com.willow.quiz.Models.Exam
import com.willow.quiz.Sever.ApiClient
import com.willow.quiz.Sever.ApiSevices
import com.willow.quiz.databinding.ActivityExamsBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Suppress("DEPRECATION")
class ExamsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityExamsBinding
    private lateinit var examAdapter: ExamAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExamsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getAllExam()
        binding.btnBack.setOnClickListener {
            updateUI(this, MeActivity::class.java)
            finish()
        }



    }
    private fun getAllExam(){

        binding.progressBar2.visibility = View.VISIBLE
        val token = SharedPrefManager.getToken(this)

        ApiClient.getRetrofitInstance().create(ApiSevices::class.java).getAllExam("Bearer $token")
            .enqueue(object : Callback<List<Exam>>{
                override fun onResponse(p0: Call<List<Exam>>, p1: Response<List<Exam>>) {
                    if (p1.isSuccessful){
                        binding.progressBar2.visibility = View.GONE
                        val exams = p1.body()
                        if (exams != null){
                            examAdapter = ExamAdapter(exams, this@ExamsActivity)
                            binding.exams.layoutManager = LinearLayoutManager(this@ExamsActivity)
                            binding.exams.adapter = examAdapter

                            val simpleCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
                                override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                                    return false
                                }

                                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                                    val position = viewHolder.adapterPosition
                                    examAdapter.deleteItem(position, this@ExamsActivity)
                                }
                            }
                            val itemTouchHelper = ItemTouchHelper(simpleCallback)
                            itemTouchHelper.attachToRecyclerView(binding.exams)
                        }
                    }
                }

                override fun onFailure(p0: Call<List<Exam>>, p1: Throwable) {
                    Log.e(ContentValues.TAG, "onFailure: $p1")
                    Toast.makeText(this@ExamsActivity, "An error occurred", Toast.LENGTH_SHORT)
                        .show()
                }

            })
    }
    private fun updateUI(context: Context, targetActivity: Class<*>) {
        val intent = Intent(context, targetActivity)
        intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION or Intent.FLAG_ACTIVITY_TASK_ON_HOME
        context.startActivity(intent)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        updateUI(this, MeActivity::class.java)
        finish()
    }
}
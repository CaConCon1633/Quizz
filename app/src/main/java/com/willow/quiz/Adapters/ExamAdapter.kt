package com.willow.quiz.Adapters

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.willow.quiz.CreateExamActivity
import com.willow.quiz.Data.SharedPrefManager
import com.willow.quiz.Models.Exam
import com.willow.quiz.Models.Message
import com.willow.quiz.R
import com.willow.quiz.Sever.ApiClient
import com.willow.quiz.Sever.ApiSevices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ExamAdapter(private var exams: List<Exam>, context: Context) :
    RecyclerView.Adapter<ExamAdapter.ExamViewHolder>() {
    class ExamViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.titleExam)
        val description: TextView = itemView.findViewById(R.id.decriptionExam)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExamAdapter.ExamViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.exam_item, parent, false)
        return ExamAdapter.ExamViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExamAdapter.ExamViewHolder, position: Int) {
        val exam = exams[position]
        holder.title.text = exam.title
        if (exam.description == null) {
            holder.description.text = ""
        } else {
            holder.description.text = exam.description.toString()
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, CreateExamActivity::class.java).apply {
                putExtra("examId", exam.examId)
            }
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = exams.size

    fun deleteItem(position: Int, context: Context) {
        val token = SharedPrefManager.getToken(context)
        ApiClient.getRetrofitInstance().create(ApiSevices::class.java)
            .deleteExam(exams[position].examId.toString(), "Bearer $token")
            .enqueue(object : Callback<Message> {
                override fun onResponse(p0: Call<Message>, p1: Response<Message>) {
                    if (p1.isSuccessful) {
                        Toast.makeText(context, p1.body()?.message, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                override fun onFailure(p0: Call<Message>, p1: Throwable) {
                    Log.e(ContentValues.TAG, "onFailure: $p1")
                    Toast.makeText(context, "An error occurred", Toast.LENGTH_SHORT)
                        .show()
                }
            })

    }

}
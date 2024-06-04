package com.willow.quiz.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.willow.quiz.Models.Report
import com.willow.quiz.R

class ReportAdapter(private var exams: List<Report.Exam>, context: Context) :
    RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {
    class ReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val shortId: TextView = itemView.findViewById(R.id.txtShortId)
        val title: TextView = itemView.findViewById(R.id.txtTitle)
        val attendance: TextView = itemView.findViewById(R.id.txtNumber)
        val over5: TextView = itemView.findViewById(R.id.txtJoinExam)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ReportAdapter.ReportViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.exam_iteam_layout, parent, false)
        return ReportAdapter.ReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportAdapter.ReportViewHolder, position: Int) {
        val exam = exams.get(position)
        holder.shortId.text = exam.shortId
        holder.title.text = exam.title
        holder.attendance.text = exam.doneCount.toString()
        holder.over5.text = exam.greater5.toString()
    }

    override fun getItemCount(): Int = exams.size
}
package com.willow.quiz.Adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.recyclerview.widget.RecyclerView
import com.willow.quiz.Models.JoinExam.Question.Answer
import com.willow.quiz.R


class AnswerAdapter(private var answers: List<Answer>, var userAnswer: MutableList<String>, val number: Int, context: Context) :
    RecyclerView.Adapter<AnswerAdapter.AnswerViewHolder>() {

    var lastSelectedPosition = -1

    class AnswerViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView) {
        val answer : RadioButton = itemView.findViewById(R.id.txtAnwer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnswerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.anwer_item, parent, false)
        return AnswerViewHolder(view)
    }

    override fun getItemCount(): Int = answers.size

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: AnswerViewHolder, position: Int) {
        val answer = answers[position]
        holder.answer.text = answer.answer

        holder.answer.setOnClickListener {
            lastSelectedPosition = position
            notifyDataSetChanged()
            userAnswer[number] = position.toString()
        }

        holder.answer.isChecked = position == lastSelectedPosition || position.toString() == userAnswer[number]
    }


}



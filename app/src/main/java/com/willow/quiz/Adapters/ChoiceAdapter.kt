package com.willow.quiz.Adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.willow.quiz.R

class ChoiceAdapter(private var options: MutableList<String>,
                    var correct: Int,
                    private val context: Context) :
    RecyclerView.Adapter<ChoiceAdapter.AnswerViewHolder>() {

    var lastSelectedPosition = -1

    class AnswerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val option: TextView = itemView.findViewById(R.id.txtOption)
        val check: RadioButton = itemView.findViewById(R.id.btn_check)
        val delete: ImageView = itemView.findViewById(R.id.deleteChoice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnswerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.choice_item, parent, false)
        return AnswerViewHolder(view)
    }

    override fun getItemCount(): Int = options.size

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: AnswerViewHolder, position: Int) {
        val answer = options[position]
        holder.option.text = answer

        holder.option.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Type a option")

            val changeOption = EditText(context)
            changeOption.setText(holder.option.text)
            builder.setView(changeOption)

            builder.setPositiveButton("OK") { dialog, which ->
                val newText = changeOption.text.toString()
                holder.option.text = newText
                options[position] = newText
            }

            builder.setNegativeButton("Cancel") { dialog, which ->

            }
            builder.show()

        }

        holder.check.setOnClickListener {
            lastSelectedPosition = position
            notifyDataSetChanged()
            correct = position


        }
        holder.check.isChecked = position == lastSelectedPosition || position == correct

        holder.delete.setOnClickListener {
            options.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, options.size)
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    fun refreshData(newOptions: MutableList<String>) {
        notifyDataSetChanged()
    }
    fun getCheck(): Int {
        return correct
    }

}
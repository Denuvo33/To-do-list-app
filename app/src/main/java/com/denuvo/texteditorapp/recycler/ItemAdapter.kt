package com.denuvo.texteditorapp.recycler

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.denuvo.texteditorapp.Interface.CarryValueFromAdapter
import com.denuvo.texteditorapp.MainActivity
import com.denuvo.texteditorapp.R
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.collections.ArrayList

class ItemAdapter (var activity: MainActivity, val data : ArrayList<TaskData>) : RecyclerView.Adapter<ViewHolder>() {

    var persentase : Float = 100f
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
        val textView: TextView = activity.findViewById(R.id.txt_persentase)
        val circularProgressBar : CircularProgressBar = activity.findViewById(R.id.circularBar)
        holder.checkDone.setOnCheckedChangeListener { buttonView, isChecked: Boolean ->
            if (isChecked){
                CarryValueFromAdapter.check = isChecked
                CarryValueFromAdapter.position = position
                holder.card.setCardBackgroundColor(Color.GRAY)
                activity.persentase += 5f
                holder.checkDone.apply {
                    paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                }
                holder.textView.apply {
                    paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    setTextColor(ContextCompat.getColor(context,R.color.black))
                }
                holder.textDesc.apply {
                    paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                }
                holder.checkDone.isEnabled = false
                circularProgressBar.apply {
                    setProgressWithAnimation(activity.persentase,1000)
                    textView.text = "${activity.persentase.toInt()}%"
                }
            }else{
                holder.card.setCardBackgroundColor(Color.WHITE)
            }
        }
    }

    override fun getItemCount(): Int {
        return data.count()
    }

}
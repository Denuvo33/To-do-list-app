package com.denuvo.texteditorapp.recycler

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.denuvo.texteditorapp.R
import java.util.*

class ViewHolder(inflater: LayoutInflater,parent: ViewGroup) : RecyclerView.ViewHolder(inflater.inflate(
    R.layout.item_list,parent,false)) {

        val textView : TextView = itemView.findViewById(R.id.txt_result)
        val textDesc : TextView = itemView.findViewById(R.id.txt_desc)
        private val c = Calendar.getInstance()
        val checkDone : CheckBox =  itemView.findViewById(R.id.btn_done)
        val time : TextView = itemView.findViewById(R.id.txt_time)
        val date : TextView = itemView.findViewById(R.id.txt_date)
        var card : CardView = itemView.findViewById(R.id.item_card)




    fun bind(data : TaskData){

        if (checkDone.isChecked){

        }else {
            checkDone.isEnabled = true
            checkDone.paintFlags= 0
            textView.paintFlags = 0
            textDesc.paintFlags = 0
            textView.setTextColor(Color.parseColor("#ef5350"))
        }
        textView.text = data.task
        textDesc.text = data.desc
        time.text = "${c.get(Calendar.HOUR_OF_DAY)}:${c.get(Calendar.MINUTE)}"
        date.text = "${c.get(Calendar.DAY_OF_MONTH)},${c.get(Calendar.MONTH)},${c.get(Calendar.YEAR)}"
    }


}
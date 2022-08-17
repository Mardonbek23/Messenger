package com.example.messenger.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.messenger.R
import com.example.messenger.databinding.CallItemBinding
import com.example.messenger.models.Call
import java.text.SimpleDateFormat

class CallAdapter(var list: ArrayList<Call>, var number: String) :
    RecyclerView.Adapter<CallAdapter.Vh>() {
    inner class Vh(var itemRvBinding: CallItemBinding) :
        RecyclerView.ViewHolder(itemRvBinding.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        return Vh(CallItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.itemRvBinding.apply {
            if (list[position].from == number) {
                text.setText("Chiquvchi")
                image.setImageResource(R.drawable.ic_send_call)
            } else {
                text.setText("Kiruvchi")
                image.setImageResource(R.drawable.ic_prinyatiy)
            }
            var simle = SimpleDateFormat("HH:mm dd MMMM")
            date.setText(simle.format(list[position].date))
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }


}
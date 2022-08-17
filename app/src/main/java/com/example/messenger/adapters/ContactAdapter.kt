package com.example.messenger.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.messenger.R
import com.example.messenger.databinding.AccountItemBinding
import com.example.messenger.databinding.ContactItemBinding
import com.example.messenger.models.Account
import com.example.messenger.models.Message
import com.example.messenger.services.DefNameService
import com.google.firebase.database.*
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat

class ContactAdapter(
    var list: ArrayList<Account>,
    var listener: OnItemClickListener, var phone: String?
) : RecyclerView.Adapter<ContactAdapter.Vh>() {

    inner class Vh(var itemRvBinding: ContactItemBinding) :
        RecyclerView.ViewHolder(itemRvBinding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        return Vh(ContactItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    interface OnItemClickListener {
        fun onClick(account: Account)
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        if (list[position].image != null) {
            Picasso.get().load(list[position].image)
                .placeholder(
                    R.drawable.ic_account).into(holder.itemRvBinding.image)
        }
        else {
            holder.itemRvBinding.defText.visibility = View.VISIBLE
            var txt = DefNameService().getFirstChar(list[position].surname, list[position].name)
            holder.itemRvBinding.defText.setText(txt)
            holder.itemRvBinding.image.setImageResource(list[position].account_color)
        }
        holder.itemRvBinding.apply {
            name.setText("${list[position].surname} ${list[position].name}")
        }
        holder.itemView.setOnClickListener {
            listener.onClick(list[position])
        }
        if (phone!=null){
            if (list[position].phone == phone) {
                holder.itemRvBinding.admin.visibility=View.VISIBLE
            }
        }
        if (list[position].isOnline == 1L) {
           holder.itemRvBinding.lastText.setText("Online")
            holder.itemRvBinding.lastText.setTextColor(Color.parseColor("#3B773D"))
        } else {
            val simplehour = SimpleDateFormat("HH:mm EE")
            holder.itemRvBinding.lastText.setText("Oxirgi faol vaqti: " + simplehour.format(list[position].isOnline))
        }
    }
}
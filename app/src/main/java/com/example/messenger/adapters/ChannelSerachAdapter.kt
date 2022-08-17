package com.example.messenger.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.messenger.R
import com.example.messenger.databinding.ContactItemBinding
import com.example.messenger.models.Group
import com.example.messenger.services.DefNameService
import com.squareup.picasso.Picasso

class ChannelSerachAdapter(
    var list: ArrayList<Group>,
    var listener: OnItemClickListener, var phone: String?,
) : RecyclerView.Adapter<ChannelSerachAdapter.Vh>() {

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
        fun onClick(account: Group)
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        if (list[position].image != null) {
            Picasso.get().load(list[position].image)
                .placeholder(
                    R.drawable.ic_account).into(holder.itemRvBinding.image)
        }
        else {
            holder.itemRvBinding.defText.visibility = View.VISIBLE
            var txt = DefNameService().getFirstChar(null, list[position].name)
            holder.itemRvBinding.defText.setText(txt)
            holder.itemRvBinding.image.setImageResource(list[position].account_color)
        }
        holder.itemRvBinding.apply {
            name.setText("${list[position].name}")
        }
        holder.itemView.setOnClickListener {
            listener.onClick(list[position])
        }
        holder.itemRvBinding.lastText.setText("" +
                "${list[position].accounts!!.size} ta a\'zo")

    }
}
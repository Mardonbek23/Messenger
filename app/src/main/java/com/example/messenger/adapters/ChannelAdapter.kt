package com.example.messenger.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.messenger.R
import com.example.messenger.databinding.AccountItemBinding
import com.example.messenger.models.Group
import com.example.messenger.models.Message
import com.example.messenger.services.DefNameService
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import java.sql.Date
import java.text.SimpleDateFormat

class ChannelAdapter(
    var list: ArrayList<Group>,
    var listener: OnItemClickListener,
    var phone: String,
) : RecyclerView.Adapter<ChannelAdapter.Vh>() {
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var reference: DatabaseReference

    inner class Vh(var itemRvBinding: AccountItemBinding) :
        RecyclerView.ViewHolder(itemRvBinding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        return Vh(AccountItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    interface OnItemClickListener {
        fun onClick(group: Group)
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.itemRvBinding.image.setImageResource(R.drawable.ic_def_image)
        if (list[position].image != null) {
            Picasso.get().load(list[position].image)
                .placeholder(
                    R.drawable.ic_def_image).into(holder.itemRvBinding.image)
        } else {
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
        firebaseDatabase = FirebaseDatabase.getInstance()
        reference = firebaseDatabase.getReference("channel_message")
        reference.child("${list[position].key}").limitToLast(1)
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val value = snapshot.getValue(Message::class.java)
                    if (value != null) {
                        holder.itemRvBinding.lastText.setText("${value!!.text}")
                        if (value.from == phone) {
                            if (value.check == 1) {
                                holder.itemRvBinding.check.setImageResource(R.drawable.ic_double_tick)
                            } else {
                                holder.itemRvBinding.check.setImageResource(R.drawable.ic_tick)
                            }
                        }

                        if (value.from == phone) {
                            holder.itemRvBinding.check.visibility = View.VISIBLE
                            holder.itemRvBinding.circle.visibility = View.INVISIBLE
                        } else {
                            holder.itemRvBinding.check.visibility = View.INVISIBLE
                            holder.itemRvBinding.circle.visibility = View.VISIBLE
                        }
                        if (value.image != null) {
                            holder.itemRvBinding.lastImage.visibility = View.VISIBLE
                            Picasso.get().load(value.image).placeholder(R.color.def_siyoh)
                                .into(holder.itemRvBinding.lastImage)
                            if (value.text == null || value.text == "") {
                                holder.itemRvBinding.lastText.setText("Rasmli xabar")
                            }
                        }
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

                }

                override fun onChildRemoved(snapshot: DataSnapshot) {

                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

                }

                override fun onCancelled(error: DatabaseError) {

                }


            })
        reference.child("${list[position].key}").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.children
                var list1 = ArrayList<Message>()
                for (child in value) {
                    val value1 = child.getValue(Message::class.java)
                    list1.add(value1!!)
                }
                var count = 0
                for (message1 in list1) {
                    if (message1.check == 0 && message1.to == phone) {
                        count++
                    }
                }
                if (count == 0) {
                    holder.itemRvBinding.circle.visibility = View.INVISIBLE
                } else {
                    holder.itemRvBinding.circle.visibility = View.VISIBLE
                }
                holder.itemRvBinding.count.setText(count.toString())

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }
}
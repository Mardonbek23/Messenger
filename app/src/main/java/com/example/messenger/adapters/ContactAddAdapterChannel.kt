package com.example.messenger.adapters

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.messenger.R
import com.example.messenger.databinding.ContactAddItemBinding
import com.example.messenger.models.Account
import com.example.messenger.services.DefNameService
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat

class ContactAddAdapterChannel(
    var list: ArrayList<Account>,
    var listener: OnItemClickListener, var key: String,
) : RecyclerView.Adapter<ContactAddAdapterChannel.Vh>() {
    lateinit var addList: ArrayList<Account>
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var reference1: DatabaseReference
    lateinit var allList: ArrayList<Account>

    inner class Vh(var itemRvBinding: ContactAddItemBinding) :
        RecyclerView.ViewHolder(itemRvBinding.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        addList = ArrayList()
        allList = ArrayList()
        firebaseDatabase = FirebaseDatabase.getInstance()
        reference1 = firebaseDatabase.getReference("channel/$key")
        reference1.child("accounts").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val children = snapshot.children
                for (child in children) {
                    val value = child.getValue(Account::class.java)
                    if (value != null) {
                        allList.add(value)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
        return Vh(ContactAddItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun addItem() {
        Log.d("asas", "addItem: ------tushdi")
        for (account in addList) {
            var ishave=true
            for (account2 in allList) {
                if (account2.phone==account.phone){
                    Log.d("asas", "addItem: ------tushdi minus")
                    ishave=false
                    break
                }
            }
            if (ishave){
                allList.add(account)
            }
        }
        reference1.child("accounts").setValue(allList)
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
            var txt = DefNameService().getFirstChar(null, list[position].name)
            holder.itemRvBinding.defText.setText(txt)
            holder.itemRvBinding.image.setImageResource(list[position].account_color)
        }
        holder.itemRvBinding.apply {
            name.setText("${list[position].surname} ${list[position].name}")
        }
        holder.itemView.setOnClickListener {
            listener.onClick(list[position])
        }
        holder.itemView.setOnClickListener {
            if (holder.itemRvBinding.check.visibility == View.VISIBLE) {
                addList.remove(list[position])
                holder.itemRvBinding.check.visibility = View.INVISIBLE
            } else {
                addList.add(list[position])
                holder.itemRvBinding.check.visibility = View.VISIBLE
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
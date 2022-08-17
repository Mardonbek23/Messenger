package com.example.messenger.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.messenger.R
import com.example.messenger.databinding.ItemFromBinding
import com.example.messenger.databinding.ItemToGroupBinding
import com.example.messenger.models.Account
import com.example.messenger.models.Message
import com.example.messenger.services.DefNameService
import com.google.firebase.database.*
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.ArrayList

class MessageGroupAdapter(var list: ArrayList<Message>,var group_key:String, var currentUid: String,  var listener:OnItemClickListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var simpleDateFormat = SimpleDateFormat("HH:mm")
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var reference: DatabaseReference

    inner class FromVh(var itemFromBinding: ItemFromBinding) :
        RecyclerView.ViewHolder(itemFromBinding.root) {

        fun onBind(message: Message) {
            itemFromBinding.text.text = message.text
            if (message.image == null) {
                itemFromBinding.image.visibility = View.GONE
            } else {
                itemFromBinding.image.visibility = View.VISIBLE
                Picasso.get().load(message.image)
                    .into(itemFromBinding.imageView, object : Callback {
                        override fun onSuccess() {
                            itemFromBinding.progress.visibility = View.GONE
                            itemFromBinding.imageView.setOnClickListener {
                                listener.onImageClick(message.image.toString())
                            }
                        }

                        override fun onError(e: Exception?) {
                            itemFromBinding.error.visibility = View.VISIBLE
                        }
                    })
            }
            if (message.check == 1) {
                itemFromBinding.check.setImageResource(R.drawable.ic_double_tick)
            } else if (message.check == 0) {
                itemFromBinding.check.setImageResource(R.drawable.ic_tick)
            }
            itemFromBinding.time.setText(simpleDateFormat.format(message.date))
            itemFromBinding.imageView.setOnLongClickListener {
                listener.onImageSave(list[position], itemFromBinding.image)
                true
            }
            itemFromBinding.container.setOnClickListener {
                listener.onItemMessageClicke(list[position], itemFromBinding.container)
            }
        }

    }

    inner class ToVh(var itemToBinding: ItemToGroupBinding) :
        RecyclerView.ViewHolder(itemToBinding.root) {
        fun onBind(message: Message) {
            itemToBinding.text.text = message.text
            if (message.image == null) {
                itemToBinding.image.visibility = View.GONE
            } else {
                itemToBinding.image.visibility = View.VISIBLE
                Picasso.get().load(message.image)
                    .into(itemToBinding.imageView, object : Callback {
                        override fun onSuccess() {
                            itemToBinding.progress.visibility = View.GONE
                            itemToBinding.imageView.setOnClickListener {
                                listener.onImageClick(message.image.toString())
                            }
                        }

                        override fun onError(e: Exception?) {

                        }
                    })
            }
            firebaseDatabase = FirebaseDatabase.getInstance()
            reference =
                firebaseDatabase.getReference("group_message/${group_key}/${message.key}")
            reference.child("check").setValue(1)
            itemToBinding.time.setText(simpleDateFormat.format(message.date))
            var reference1=firebaseDatabase.getReference("users")
            reference1.child(message.from!!).addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    var value = snapshot.getValue(Account::class.java)
                    if (value!=null){
                        itemToBinding.name.setText(value.name)
                        if (value.image!=null){
                            Picasso.get().load(value.image).placeholder(R.drawable.ic_account).into(itemToBinding.accountImage)
                        }
                        else{
                            itemToBinding.defText.visibility=View.VISIBLE
                            var txt= DefNameService().getFirstChar(value.surname,value.name)
                            itemToBinding.defText.setText(txt)
                            itemToBinding.accountImage.setImageResource(value.account_color)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
            itemToBinding.imageView.setOnLongClickListener {
                listener.onImageSave(list[position], itemToBinding.image)
                true
            }
            itemToBinding.container.setOnClickListener {
                listener.onItemMessageClicke(list[position], itemToBinding.container)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == 0) {
            return FromVh(
                ItemFromBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        } else {
            return ToVh(
                ItemToGroupBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is FromVh) {
            holder.onBind(list[position])
        } else {
            val toVh = holder as ToVh
            toVh.onBind(list[position])
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (list[position].from == currentUid) {
            0
        } else 1
    }

    override fun getItemCount(): Int {
        return list.size
    }
    interface OnItemClickListener{
        fun onImageClick(url: String)
        fun onItemLongClick(message: Message)
        fun onImageSave(message: Message, view: View)
        fun onItemMessageClicke(message: Message, view: View)
    }
}
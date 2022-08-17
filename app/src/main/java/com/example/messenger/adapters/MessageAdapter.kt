package com.example.messenger.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.messenger.R
import com.example.messenger.databinding.ItemFromBinding
import com.example.messenger.databinding.ItemToBinding
import com.example.messenger.models.Message
import com.google.firebase.database.*
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.ArrayList

class MessageAdapter(
    var list: ArrayList<Message>,
    var currentUid: String,
    var uid: String?,
    var listener: OnItemClickListener,
) :
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
            itemFromBinding.imageView.setOnLongClickListener {
                listener.onImageSave(list[position], itemFromBinding.image)
                true
            }
            itemFromBinding.message.setOnLongClickListener {
                listener.onItemLongClick(list[position])
                true
            }
            itemFromBinding.container.setOnClickListener {
                listener.onItemMessageClicke(list[position], itemFromBinding.container)
            }
            if (message.check == 1) {
                itemFromBinding.check.setImageResource(R.drawable.ic_double_tick)
            } else if (message.check == 0) {
                itemFromBinding.check.setImageResource(R.drawable.ic_tick)
            }
            if (message.date != null) {
                itemFromBinding.time.setText(simpleDateFormat.format(message.date))
            }

        }

    }

    inner class ToVh(var itemToBinding: ItemToBinding) :
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
                firebaseDatabase.getReference("messages/${uid}/${currentUid}/${message.key}")
            reference.child("check").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var value = snapshot.getValue(Int::class.java)
                    if (value != null) {
                        reference.child("check").setValue(1)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

            if (message.date != null) {
                itemToBinding.time.setText(simpleDateFormat.format(message.date))
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
                ItemToBinding.inflate(
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

    interface OnItemClickListener {
        fun onImageClick(url: String)
        fun onItemLongClick(message: Message)
        fun onImageSave(message: Message, view: View)
        fun onItemMessageClicke(message: Message, view: View)
    }
}
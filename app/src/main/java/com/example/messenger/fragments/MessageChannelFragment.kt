package com.example.messenger.fragments

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.navigation.Navigation
import com.example.messenger.R
import com.example.messenger.adapters.MessageAdapter
import com.example.messenger.adapters.MessageGroupAdapter
import com.example.messenger.databinding.CustomDeleteItemBinding
import com.example.messenger.databinding.CustomDialogBinding
import com.example.messenger.databinding.FragmentMessageChannelBinding
import com.example.messenger.models.Account
import com.example.messenger.models.Group
import com.example.messenger.models.Message
import com.example.messenger.services.DefNameService
import com.example.messenger.services.Service
import com.example.messenger.shared.SharedPreference
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.vanniktech.emoji.EmojiPopup

private const val ARG_PARAM1 = "key"
private const val ARG_PARAM2 = "param2"

class MessageChannelFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    lateinit var binding: FragmentMessageChannelBinding
    lateinit var my_number: String
    lateinit var group_key: String
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var reference1: DatabaseReference
    lateinit var handler: Handler
    lateinit var messageAdapter: MessageAdapter
    lateinit var list: ArrayList<Message>
    lateinit var accounts:ArrayList<Account>
    var file_uri: String? = null
    var image: String? = null
    var checer=false
    var check_edit = false

    //storage
    lateinit var firebaseStorage: FirebaseStorage
    lateinit var reference_storage: StorageReference
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentMessageChannelBinding.inflate(inflater, container, false)
        group_key = param1!!
        accounts= ArrayList()
        my_number = SharedPreference().getSomeStringValue(requireContext()).toString()
        firebaseDatabase = FirebaseDatabase.getInstance()
        list = ArrayList()
        firebaseStorage = FirebaseStorage.getInstance()
        reference_storage = firebaseStorage.getReference("images/$group_key")
        reference1 = firebaseDatabase.getReference("channel_message/$group_key")
        setBack()
        setBtns()
        setSendButton()
        setRv()
        settool()
        setScript()
        setOtmen()
        setSmile()
        handler = Handler(Looper.getMainLooper())
        handler.postDelayed(runnable, 100)
        return binding.root
    }

    private fun setSmile() {
        var emoji_popup= EmojiPopup.Builder.fromRootView(binding.root).build(binding.edit)
        binding.smile.setOnClickListener(object :View.OnClickListener{
            override fun onClick(v: View?) {
                if (checer){
                    checer=false
                    emoji_popup.dismiss()
                    binding.smile.setImageResource(R.drawable.ic_smile)
                }
                else{
                    checer=true
                    emoji_popup.toggle()
                    binding.smile.setImageResource(R.drawable.keyboard)
                }
            }
        })
        binding.edit.setOnClickListener {
            if (checer){
                emoji_popup.toggle()
            }
            else{
                emoji_popup.dismiss()
            }
        }
    }

    private fun setBack() {
        binding.back.setOnClickListener {
            Navigation.findNavController(binding.root).popBackStack()
        }
    }

    private fun setOtmen() {
        binding.otmen.setOnClickListener {
            image = null
            binding.otmen.visibility = View.GONE
            if (binding.edit.text.toString() == "") {
                binding.script.visibility = View.VISIBLE
                binding.voice.visibility = View.VISIBLE
                binding.send.visibility = View.GONE
            }
        }
    }


    private fun setScript() {
        binding.script.setOnClickListener {
            binding.progress.visibility = View.VISIBLE
            binding.script.visibility = View.GONE
            binding.voice.visibility = View.GONE
            getFile.launch("image/*")
        }
    }

    private var getFile = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) {
            binding.progress.visibility = View.GONE
            binding.script.visibility = View.VISIBLE
            binding.voice.visibility = View.VISIBLE
            return@registerForActivityResult
        }
        reference_storage.child(System.currentTimeMillis().toString()).child("image").putFile(uri)
            .addOnSuccessListener {
                if (it.task.isSuccessful) {
                    it.storage.downloadUrl.addOnSuccessListener { url ->
                        image = url.toString()
                        binding.script.visibility = View.GONE
                        binding.voice.visibility = View.GONE
                        binding.send.visibility = View.VISIBLE
                        binding.progress.visibility = View.GONE
                    }
                } else {
                    Toast.makeText(requireContext(), "not downloaded", Toast.LENGTH_SHORT).show()
                }
            }
    }

    @SuppressLint("NewApi")
    private fun settool() {
        var reference3 = firebaseDatabase.getReference("channel")
        reference3.child(group_key).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.getValue(Group::class.java)
                if (value != null) {
                    if (value!!.image != null) {
                        Picasso.get().load(value!!.image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.ic_def_image_gr)
                            .into(binding.image)
                    }
                    else{
                        binding.defText.visibility=View.VISIBLE
                        var txt= DefNameService().getFirstChar(null,value.name)
                        binding.defText.setText(txt)
                        binding.image.setImageResource(value.account_color)
                    }
                    accounts=value.accounts!!
                    binding.name.setText(value.name)
                    binding.checkOnline.setText("Obunachilar:${value.accounts!!.size} ta")
                    if (value.admin == my_number) {
                        binding.keyboard.visibility = View.VISIBLE
                        binding.follow.visibility = View.GONE
                    } else {
                        binding.keyboard.visibility = View.GONE
                        binding.follow.visibility = View.VISIBLE
                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
        binding.name.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("key", group_key)
            Navigation.findNavController(binding.root).navigate(R.id.showChannelFragment, bundle)
        }
        binding.image.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("key", group_key)
            Navigation.findNavController(binding.root).navigate(R.id.showChannelFragment, bundle)
        }
        binding.follow.setOnClickListener {
            if (binding.follow.text.toString()=="Obunani bekor qilish"){
                binding.follow.setText("Obuna bo\'lish")
                accounts.removeIf {
                    it.phone==my_number
                }
                var reference5=firebaseDatabase.getReference("channel")
                reference5.child(group_key).child("accounts").setValue(accounts)
            }else{
                binding.follow.setText("Obunani bekor qilish")
                var reference4=firebaseDatabase.getReference("users")
                reference4.child("$my_number").addValueEventListener(object :ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val value = snapshot.getValue(Account::class.java)
                        if (value!=null){
                            accounts.add(value)
                            var reference5=firebaseDatabase.getReference("channel")
                            reference5.child(group_key).child("accounts").setValue(accounts)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
            }
        }
    }

    private fun setBtns() {
        binding.edit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (binding.edit.text.toString().isEmpty()) {
                    if (!check_edit){
                        binding.script.visibility = View.VISIBLE
                        binding.voice.visibility = View.VISIBLE
                        binding.send.visibility = View.GONE
                    }
                } else {
                    if (!check_edit){
                        binding.script.visibility = View.GONE
                        binding.voice.visibility = View.GONE
                        binding.send.visibility = View.VISIBLE
                    }
                }
                if(binding.progress.visibility==View.VISIBLE){
                    binding.script.visibility = View.GONE
                    binding.voice.visibility = View.GONE
                    binding.send.visibility = View.GONE
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
    }

    private fun setRv() {
        list = ArrayList()
        messageAdapter = MessageAdapter(list,
            my_number,
            null,
            object : MessageAdapter.OnItemClickListener {
                override fun onImageClick(url: String) {
                    val builder = AlertDialog.Builder(requireContext())
                    builder.setCancelable(true)
                    val customdialogBinding: CustomDialogBinding =
                        CustomDialogBinding.inflate(layoutInflater)
                    Picasso.get().load(url).into(customdialogBinding.image)
                    builder.setView(customdialogBinding.getRoot())
                    var alertDialog = builder.create();
                    customdialogBinding.exit.setOnClickListener {
                        alertDialog.dismiss()
                    }
                    alertDialog.show()
                }

                override fun onItemLongClick(message: Message) {

                }

                override fun onImageSave(message: Message,view: View) {
                    var popup = PopupMenu(requireContext(), view)
                    popup.inflate(R.menu.image_popup_menu)
                    popup.setOnMenuItemClickListener {
                        when (it.itemId) {
                            R.id.save_image -> {
                                Service().saveImage(message.image.toString(),requireContext())
                            }
                        }
                        true
                    }
                    popup.show()
                }

                override fun onItemMessageClicke(message: Message,view: View) {
                    var popup = PopupMenu(requireContext(), view)
                    if (message.from==my_number){
                        popup.inflate(R.menu.message_popup_menu)
                    }
                    else{
                        popup.inflate(R.menu.message_popup_menu_channel)
                    }
                    popup.setOnMenuItemClickListener {
                        when (it.itemId) {
                            R.id.delete_message -> {
                                val builder =
                                    AlertDialog.Builder(requireContext(), R.style.AlertDialogStyle)
                                builder.setTitle("Custom Dialog")
                                builder.setCancelable(false)
                                val customdialogBinding: CustomDeleteItemBinding =
                                    CustomDeleteItemBinding.inflate(layoutInflater)
                                builder.setTitle("Xabarni o'chirish")
                                builder.setMessage("Siz haqiqatdan ma\'lumotni o\'chirmoqchimsiz?")
                                customdialogBinding.checkbox.visibility=View.GONE
                                builder.setNegativeButton("Bekor qilish",
                                    object : DialogInterface.OnClickListener {
                                        override fun onClick(dialog: DialogInterface?, which: Int) {

                                        }
                                    })
                                builder.setPositiveButton("O\'chirish",
                                    object : DialogInterface.OnClickListener {
                                        override fun onClick(dialog: DialogInterface?, which: Int) {
                                            var reference_message =
                                                firebaseDatabase.getReference("channel_message")
                                            reference_message.child(group_key)
                                                .child(message.key!!).removeValue()
                                        }
                                    })
                                builder.setView(customdialogBinding.getRoot())
                                builder.show()

                            }
                            R.id.edit_message -> {
                                setEdit(message)
                                check_edit=true
                            }
                            R.id.copy -> {
                                val clipboardManager: ClipboardManager =
                                    requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val data =
                                    ClipData.newPlainText("text", message.text) as ClipData
                                clipboardManager.setPrimaryClip(data)
                                Toast.makeText(requireContext(),
                                    "Buferga saqlandi!",
                                    Toast.LENGTH_SHORT).show()

                            }
                        }
                        true
                    }
                    popup.show()
                }
            })
        binding.rv.adapter = messageAdapter
        reference1.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                list.clear()
                val children = snapshot.children
                for (child in children) {
                    val value = child.getValue(Message::class.java)
                    if (value != null) {
                        list.add(value)
                    }
                }
                messageAdapter.list = list
                messageAdapter.notifyDataSetChanged()
                if (::messageAdapter.isInitialized) {
                    binding.rv.smoothScrollToPosition(messageAdapter.itemCount)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
    private fun setEdit(message: Message) {
        binding.editLine.visibility = View.VISIBLE
        binding.editText.setText(message.text)
        binding.edit.setText(message.text)
        binding.editDone.visibility = View.VISIBLE
        binding.send.visibility = View.INVISIBLE
        binding.script.visibility = View.GONE
        binding.voice.visibility = View.GONE
        binding.editCancel.setOnClickListener {
            binding.editLine.visibility = View.GONE
            binding.editText.setText("")
            binding.edit.setText("")
            binding.editDone.visibility = View.GONE
            binding.send.visibility = View.GONE
            binding.script.visibility = View.VISIBLE
            binding.voice.visibility = View.VISIBLE
            check_edit=false
        }
        binding.editDone.setOnClickListener {
            var reference_ms=firebaseDatabase.getReference("channel_message")
            var text=binding.edit.text.toString()
            reference_ms.child(group_key).child(message.key!!).child("text").setValue(text)
            binding.editLine.visibility = View.GONE
            binding.editText.setText("")
            binding.edit.setText("")
            binding.editDone.visibility = View.GONE
            binding.send.visibility = View.GONE
            binding.script.visibility = View.VISIBLE
            binding.voice.visibility = View.VISIBLE
            check_edit=false
        }
    }
    private fun setSendButton() {
        binding.send.setOnClickListener {
            val text = binding.edit.text.toString()
            val key = reference1.push().key
            var message = Message(text,
                my_number,
                group_key,
                System.currentTimeMillis(),
                0,
                key!!,
                null,
                image,
                null,
                file_uri)
            reference1.child(key).setValue(message)
            binding.edit.text = null
            binding.rv.smoothScrollToPosition(messageAdapter.itemCount)
            image = null
            file_uri = null
            binding.otmen.visibility = View.GONE
        }
    }

    private var runnable = object : Runnable {
        override fun run() {
            if (image != null) {
                binding.otmen.visibility = View.VISIBLE
                binding.script.visibility = View.GONE
                binding.voice.visibility = View.GONE
                binding.send.visibility = View.VISIBLE
            }
            handler.postDelayed(this, 10)
        }
    }


}
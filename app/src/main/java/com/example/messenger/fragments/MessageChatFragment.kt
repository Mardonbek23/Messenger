package com.example.messenger.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.media.MediaRecorder
import android.os.*
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.messenger.R
import com.example.messenger.adapters.MessageAdapter
import com.example.messenger.databinding.CustomDialogBinding
import com.example.messenger.databinding.FragmentMessageChatBinding
import com.example.messenger.models.Account
import com.example.messenger.models.Message
import com.example.messenger.shared.SharedPreference
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import com.vanniktech.emoji.EmojiPopup
import android.os.Environment
import com.example.messenger.services.Service
import android.content.ClipData
import android.content.ClipboardManager
import android.content.DialogInterface
import com.example.messenger.databinding.CustomDeleteItemBinding
import com.example.messenger.retrofit.ApiClient
import com.example.messenger.retrofit.Data
import com.example.messenger.retrofit.Notification
import com.example.messenger.retrofit.RetrofitObject
import com.example.messenger.services.DefNameService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


private const val ARG_PARAM1 = "key"
private const val ARG_PARAM2 = "param2"

class MessageChatFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    lateinit var binding: FragmentMessageChatBinding
    lateinit var my_number: String
    lateinit var your_number: String
    lateinit var list: ArrayList<Message>
    lateinit var messageAdapter: MessageAdapter
    var image_uri: String? = null
    var file_uri: String? = null
    lateinit var handler: Handler

    //Firebase
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var reference1: DatabaseReference
    lateinit var reference2: DatabaseReference

    //storage
    lateinit var firebaseStorage: FirebaseStorage
    lateinit var reference_storage: StorageReference

    //media
    var mediaRecorder: MediaRecorder? = null
    lateinit var audioPath: String

    var checer = false
    var check_edit = false
    var voice_check = true
    lateinit var your_account: Account
    lateinit var mediarecorder: MediaRecorder
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentMessageChatBinding.inflate(inflater, container, false)
        your_number = param1!!
        my_number = SharedPreference().getSomeStringValue(requireContext())!!
        binding.back.setOnClickListener {
            Navigation.findNavController(binding.root).popBackStack()
        }

        firebaseDatabase = FirebaseDatabase.getInstance()
        reference1 = firebaseDatabase.getReference("messages/$my_number")
        reference2 = firebaseDatabase.getReference("messages/$your_number")

        firebaseStorage = FirebaseStorage.getInstance()
        reference_storage = firebaseStorage.getReference("images/$my_number")

        setBtns()
        setSendButton()
        setRv()
        setTool()
        setScript()
        setOtmen()
        setPerexod()
        handler = Handler(Looper.getMainLooper())
        handler.postDelayed(runnable, 100)
        setVoice()
        setSmile()
//        recordAudio()
        return binding.root
    }


    private fun setVoice() {
        audioPath = Environment.getExternalStorageDirectory().absolutePath + "/voice.mp3"
        binding.voice.setOnClickListener {
            if (voice_check) {
                voice_check = false
                startrecord()
                Toast.makeText(requireContext(), "Started REcord", Toast.LENGTH_SHORT).show()
            } else {
                voice_check = true
                stopRecord()
                Toast.makeText(requireContext(), "Stopped record", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startrecord() {
        try {
            mediaRecorder = MediaRecorder()
            mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT)
            mediaRecorder!!.setOutputFile(audioPath)
            mediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            mediaRecorder!!.prepare()
            mediaRecorder!!.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun stopRecord() {

        try {
            mediaRecorder!!.stop()
            mediaRecorder!!.release()
            mediaRecorder = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("CheckResult")
    private fun setSmile() {
        var emoji_popup = EmojiPopup.Builder.fromRootView(binding.root).build(binding.edit)

        binding.smile.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if (checer) {
                    checer = false
                    emoji_popup.dismiss()
                    binding.smile.setImageResource(R.drawable.ic_smile)
                } else {
                    checer = true
                    emoji_popup.toggle()
                    binding.smile.setImageResource(R.drawable.keyboard)
                }
            }
        })
        binding.edit.setOnClickListener {
            if (checer) {
                emoji_popup.toggle()
            } else {
                emoji_popup.dismiss()
            }
        }
    }

    private fun setPerexod() {
        binding.name.setOnClickListener {
            var bundle = Bundle()
            bundle.putString("key", your_number)
            Navigation.findNavController(binding.root).navigate(R.id.showChatFragment, bundle)
        }
        binding.image.setOnClickListener {
            var bundle = Bundle()
            bundle.putString("key", your_number)
            Navigation.findNavController(binding.root).navigate(R.id.showChatFragment, bundle)
        }
    }

    private fun setOtmen() {
        binding.otmen.setOnClickListener {
            image_uri = null
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
                        image_uri = url.toString()
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

    private fun setTool() {
        var reference3 = firebaseDatabase.getReference("users")
        reference3.child(your_number).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.getValue(Account::class.java)
                if (value != null) {
                    your_account = value
                }
                if (value!!.image != null) {
                    Picasso.get().load(value!!.image).networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.ic_account)
                        .into(binding.image)
                } else {
                    binding.defText.visibility = View.VISIBLE
                    var txt = DefNameService().getFirstChar(value.surname, value.name)
                    binding.defText.setText(txt)
                    binding.image.setImageResource(value.account_color)
                }
                binding.name.setText(value.surname + " " + value.name)
                Log.d("asss", "onDataChange: ----------${value.isOnline}")
                if (value.isOnline == 1L) {
                    binding.checkOnline.setText("Online")
                    binding.checkOnline.setTextColor(Color.parseColor("#3B773D"))
                } else {
                    val simplehour = SimpleDateFormat("HH:mm EE")
                    binding.checkOnline.setText("Oxirgi faol vaqti: " + simplehour.format(value.isOnline))
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun setBtns() {
        binding.edit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (binding.edit.text.toString().isEmpty()) {
                    if (!check_edit) {
                        binding.script.visibility = View.VISIBLE
                        binding.voice.visibility = View.VISIBLE
                        binding.send.visibility = View.GONE
                    }
                } else {
                    if (!check_edit) {
                        binding.script.visibility = View.GONE
                        binding.voice.visibility = View.GONE
                        binding.send.visibility = View.VISIBLE
                    }
                }
                if (binding.progress.visibility == View.VISIBLE) {
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
            your_number,
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

                override fun onImageSave(message: Message, view: View) {
                    var popup = PopupMenu(requireContext(), view)
                    popup.inflate(R.menu.image_popup_menu)
                    popup.setOnMenuItemClickListener {
                        when (it.itemId) {
                            R.id.save_image -> {
                                Service().saveImage(message.image.toString(), requireContext())
                            }
                        }
                        true
                    }
                    popup.show()
                }

                override fun onItemMessageClicke(message: Message, view: View) {
                    var popup = PopupMenu(requireContext(), view)
                    if (message.from == my_number) {
                        popup.inflate(R.menu.message_popup_menu)
                    } else {
                        popup.inflate(R.menu.message_popup_menu_to)
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
                                customdialogBinding.checkbox.setText("Xabarni ${binding.name.text.toString()} dan ham o\'chirmoqchimisiz?")
                                builder.setNegativeButton("Bekor qilish",
                                    object : DialogInterface.OnClickListener {
                                        override fun onClick(dialog: DialogInterface?, which: Int) {

                                        }
                                    })
                                builder.setPositiveButton("O\'chirish",
                                    object : DialogInterface.OnClickListener {
                                        override fun onClick(dialog: DialogInterface?, which: Int) {
                                            var reference_message =
                                                firebaseDatabase.getReference("messages")
                                            if (customdialogBinding.checkbox.isChecked) {
                                                reference_message.child(your_number)
                                                    .child(my_number).child(message.key!!)
                                                    .removeValue()
                                            }
                                            reference_message.child(my_number).child(your_number)
                                                .child(message.key!!).removeValue()
                                        }
                                    })
                                builder.setView(customdialogBinding.getRoot())
                                builder.show()

                            }
                            R.id.edit_message -> {
                                setEdit(message)
                                check_edit = true
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
                            R.id.copy_message -> {
                                firebaseDatabase.getReference("saved_messages").child(my_number)
                                    .child(message.key!!).setValue(message,
                                    object : DatabaseReference.CompletionListener {
                                        override fun onComplete(
                                            error: DatabaseError?,
                                            ref: DatabaseReference,
                                        ) {
                                            if (error == null) {
                                                Toast.makeText(requireContext(),
                                                    "Saqlandi!",
                                                    Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    })
                            }
                        }
                        true
                    }
                    popup.show()
                }
            })
        binding.rv.scrollToPosition(messageAdapter.itemCount)
        binding.rv.adapter = messageAdapter
        reference1.child(your_number).addValueEventListener(object : ValueEventListener {
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
            check_edit = false
        }
        binding.editDone.setOnClickListener {
            var reference_ms = firebaseDatabase.getReference("messages")
            var text = binding.edit.text.toString()
            reference_ms.child(message.to!!).child(message.from!!).child(message.key!!)
                .child("text").setValue(text)
            reference_ms.child(message.from!!).child(message.to!!).child(message.key!!)
                .child("text").setValue(text)
            binding.editLine.visibility = View.GONE
            binding.editText.setText("")
            binding.edit.setText("")
            binding.editDone.visibility = View.GONE
            binding.send.visibility = View.GONE
            binding.script.visibility = View.VISIBLE
            binding.voice.visibility = View.VISIBLE
            check_edit = false
        }
    }

    private fun setSendButton() {
        binding.send.setOnClickListener {
            val text = binding.edit.text.toString()
            val key = reference1.push().key
            var message = Message(text,
                my_number,
                your_number,
                System.currentTimeMillis(),
                0,
                key!!,
                null,
                image_uri,
                null,
                file_uri)
            reference1.child(your_number).child(key).setValue(message)
            reference2.child(my_number).child(key).setValue(message)
            var reference_m = firebaseDatabase.getReference("messaging_contacts")
            reference_m.child(my_number).child(your_number).setValue(your_number)
            reference_m.child(your_number).child(my_number).setValue(my_number)
            binding.edit.text = null
            binding.rv.smoothScrollToPosition(messageAdapter.itemCount)
            image_uri = null
            file_uri = null
            binding.otmen.visibility = View.GONE
            if (::your_account.isInitialized){
                Toast.makeText(requireContext(), "ass", Toast.LENGTH_SHORT).show()
                var retrofitObject=RetrofitObject("type_a", Data("${your_account.phone}","number","3","4"), Notification("$text","${your_account.name}"),your_account.notification_key!!)
                ApiClient.apiService(requireContext()).getNotify(retrofitObject).enqueue(object :Callback<RetrofitObject>{
                    override fun onResponse(
                        call: Call<RetrofitObject>,
                        response: Response<RetrofitObject>,
                    ) {
                        Toast.makeText(requireContext(), "${response.code()}", Toast.LENGTH_SHORT).show()
                    }

                    override fun onFailure(call: Call<RetrofitObject>, t: Throwable) {
                        Toast.makeText(requireContext(), "${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }

    private var runnable = object : Runnable {
        override fun run() {
            if (image_uri != null) {
                binding.otmen.visibility = View.VISIBLE
                binding.script.visibility = View.GONE
                binding.voice.visibility = View.GONE
                binding.send.visibility = View.VISIBLE
            }
            handler.postDelayed(this, 10)
        }
    }
//
//    private fun recordAudio() {
//
//        binding.recordbtn.setRecordView(binding.recordview)
//        binding.recordbtn.isListenForRecord=false
//        binding.recordbtn.setOnClickListener{
//            binding.recordbtn.isListenForRecord=true
//        }
//        binding.recordview.setOnRecordListener(object :OnRecordListener{
//            override fun onStart() {
//
//                setuprecording()
//                mediarecorder.prepare()
//                mediarecorder.start()
//                binding.recordview.visibility=View.VISIBLE
//
//            }
//
//            override fun onCancel() {
//
//                mediarecorder.reset()
//                mediarecorder.release()
//                var file= File(audioPath)
//                if(!file.exists())
//                    file.delete()
//                binding.recordview.visibility=View.GONE
//
//
//
//            }
//
//            override fun onFinish(recordTime: Long, limitReached: Boolean) {
//                mediarecorder.stop()
//                mediarecorder.release()
//                binding.recordview.visibility=View.GONE
//                sendrecordingMessage(audioPath)
//            }
//
//            override fun onLessThanSecond() {
//                mediarecorder.reset()
//                mediarecorder.release()
//                var file= File(audioPath)
//                if(!file.exists())
//                    file.delete()
//                binding.recordview.visibility=View.GONE
//
//            }
//
//        })
//    }
//    fun setuprecording(){
//        mediarecorder=MediaRecorder()
//        mediarecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
//        mediarecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
//        mediarecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB)
//        var file = File("${requireActivity().externalCacheDir?.absolutePath}/${System.currentTimeMillis()}.3gp")
//        audioPath = Environment.getExternalStorageDirectory().absolutePath + "/recording.mp3"
//        if(!file.exists()){
//            file.mkdirs()
//
//            mediarecorder.setOutputFile(audioPath)
//        }
//    }
//    fun sendrecordingMessage(audio:String){
//        var storagereference=FirebaseStorage.getInstance().getReference("RecordingVoiceMessages/${my_number}/${System.currentTimeMillis()}")
//        var uri= Uri.fromFile(File(audio))
//        storagereference.putFile(uri).addOnSuccessListener{
//            var audiourl=it.storage.downloadUrl
//            audiourl.addOnCompleteListener{ i->
//                if(i.isSuccessful){
//                    var url=i.result.toString()
//                    val text = binding.edit.text.toString()
//                    val key = reference1.push().key
//                    var message = Message(text,
//                        my_number,
//                        your_number,
//                        System.currentTimeMillis(),
//                        0,
//                        key!!,
//                        url,
//                        image_uri,
//                        null,
//                        file_uri)
//                    reference1.child(your_number).child(key).setValue(message)
//                    reference2.child(my_number).child(key).setValue(message)
//                    var reference_m = firebaseDatabase.getReference("messaging_contacts")
//                    reference_m.child(my_number).child(your_number).setValue(your_number)
//                    reference_m.child(your_number).child(my_number).setValue(my_number)
//                    binding.edit.text = null
//                    binding.rv.smoothScrollToPosition(messageAdapter.itemCount)
//                }
//            }
//        }
//    }
}
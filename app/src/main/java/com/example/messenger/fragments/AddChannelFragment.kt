package com.example.messenger.fragments

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.Navigation
import com.example.messenger.R
import com.example.messenger.databinding.FragmentAddChannelBinding
import com.example.messenger.models.Account
import com.example.messenger.models.Group
import com.example.messenger.services.DefNameService
import com.example.messenger.shared.SharedPreference
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.lang.Exception

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class AddChannelFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    lateinit var binding: FragmentAddChannelBinding
    var image: String? = null
    var description: String? = null
    lateinit var list: ArrayList<Group>
    lateinit var account: Account
    lateinit var number: String

    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var reference: DatabaseReference
    lateinit var reference2: DatabaseReference

    lateinit var firebaseStorage: FirebaseStorage
    lateinit var reference_storage: StorageReference
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentAddChannelBinding.inflate(inflater, container, false)
        number = SharedPreference().getSomeStringValue(requireContext()).toString()
        list = ArrayList()
        firebaseStorage = FirebaseStorage.getInstance()
        reference_storage = firebaseStorage.getReference("images/channel")
        firebaseDatabase = FirebaseDatabase.getInstance()
        reference = firebaseDatabase.getReference("channel")
        reference2 = firebaseDatabase.getReference("users")
        setAccount()
        setList()
        setImage()
        setCancel()
        setSave()
        setEdit()
        setBack()
        return binding.root
    }

    private fun setAccount() {
        reference2.child(number).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.getValue(Account::class.java)
                if (value != null) {
                    account = value
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun setBack() {
        binding.cancel.setOnClickListener {
            Navigation.findNavController(binding.root).popBackStack()
        }
    }

    private fun setEdit() {
        binding.link.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = s.toString()
                val link = binding.link.text.toString()
                var index = -1
                for (i in 0 until  list.size) {
                    if (list[i].key.toString() == text) {
                        index =1
                        break
                    }
                }
                if (link.length > 4 && index == -1) {
                    binding.link.setTextColor(Color.parseColor("#6AD66D"))
                } else {
                    binding.link.setTextColor(Color.parseColor("#F44336"))
                }

            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
    }

    private fun setList() {
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val children = snapshot.children
                list.clear()
                for (child in children) {
                    val value = child.getValue(Group::class.java)
                    if (value != null) {
                        list.add(value)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun setSave() {
        binding.save.setOnClickListener {
            val name = binding.name.text.toString()
            val link = binding.link.text.toString()
            description = binding.description.text.toString()
            var index = -1
            for (group in list) {
                if (group.key.toString() == link) {
                    index =1
                    break
                }
            }
            if (link.length > 4 && index == -1) {
                if (name.length > 0) {
                    var listx = ArrayList<Account>()

                    if (::account.isInitialized) {
                        listx.add(account)
                        var group = Group(name, description, image, listx, number, link,DefNameService().getRandomColor())
                        reference.child(link).setValue(group,object :DatabaseReference.CompletionListener{
                            override fun onComplete(error: DatabaseError?, ref: DatabaseReference) {
                                if (error==null){
                                    Navigation.findNavController(binding.root).popBackStack()
                                    Toast.makeText(requireContext(),
                                        "Muvofaqiyatli qo\'shildi!",
                                        Toast.LENGTH_SHORT).show()
                                }
                            }
                        })
                    }

                } else {
                    Toast.makeText(requireContext(), "Kanal nomini kiriting", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                Toast.makeText(requireContext(),
                    "Link 4ta belgidan ortiq unikal bo\'lishi kerak",
                    Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun setImage() {
        binding.image.setOnClickListener {
            getImage.launch("image/*")
            binding.progress.visibility = View.VISIBLE
        }
    }

    private var getImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) {
            return@registerForActivityResult
            binding.progress.visibility = View.GONE
        }
        reference_storage.child(System.currentTimeMillis().toString()).putFile(uri)
            .addOnSuccessListener {
                if (it.task.isSuccessful) {
                    it.storage.downloadUrl.addOnSuccessListener { x ->
                        Picasso.get().load(x).placeholder(R.drawable.ic_def_image)
                            .into(binding.image, object : Callback {
                                override fun onSuccess() {
                                    binding.progress.visibility = View.GONE
                                }

                                override fun onError(e: Exception?) {

                                }
                            })
                        image = x.toString()

                    }
                } else {
                    Toast.makeText(requireContext(), "Rasm yuklanmadi", Toast.LENGTH_SHORT).show()
                }
            }

    }

    private fun setCancel() {
        binding.cancel.setOnClickListener {
            Navigation.findNavController(binding.root).popBackStack()
        }
    }


}
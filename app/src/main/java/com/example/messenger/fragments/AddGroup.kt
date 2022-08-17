package com.example.messenger.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.Navigation
import com.example.messenger.R
import com.example.messenger.databinding.FragmentAddGroupBinding
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

class AddGroup : Fragment() {
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

    lateinit var binding: FragmentAddGroupBinding
    var image_uri: String? = null
    lateinit var number: String
    lateinit var account: Account
    lateinit var list: ArrayList<Account>
    lateinit var key: String

    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var reference: DatabaseReference
    lateinit var reference2: DatabaseReference

    lateinit var firebaseStorage: FirebaseStorage
    lateinit var reference_storage: StorageReference
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        list = ArrayList()
        number = SharedPreference().getSomeStringValue(requireContext()).toString()
        firebaseStorage = FirebaseStorage.getInstance()
        reference_storage = firebaseStorage.getReference("images/group")
        firebaseDatabase = FirebaseDatabase.getInstance()
        reference = firebaseDatabase.getReference("groups")
        reference2 = firebaseDatabase.getReference("users")
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
        binding = FragmentAddGroupBinding.inflate(inflater, container, false)
        setCancel()
        setSave()
        setImage()
        return binding.root
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
                        image_uri = x.toString()

                    }
                } else {
                    Toast.makeText(requireContext(), "Rasm yuklanmadi", Toast.LENGTH_SHORT).show()
                }
            }

    }

    private fun setSave() {
        binding.save.setOnClickListener {
            val name = binding.name.text.toString()
            val description = binding.description.text.toString()
            if (name.isEmpty()) {
                Toast.makeText(requireContext(),
                    "Guruh nomini kiritishingiz kerak!",
                    Toast.LENGTH_SHORT).show()
            } else {
                if (::account.isInitialized) {
                    list.add(account)
                }
                key = reference2.push().key.toString()

                var group = Group(name, description, image_uri, list, number, key, DefNameService().getRandomColor())
                reference.child(key.toString())
                    .setValue(group, object : DatabaseReference.CompletionListener {
                        override fun onComplete(error: DatabaseError?, ref: DatabaseReference) {
                            if (error == null) {
                                Toast.makeText(requireContext(),
                                    "Guruh muvofaqiyatli qo\'shildi",
                                    Toast.LENGTH_SHORT).show()
                                Navigation.findNavController(binding.root).popBackStack()
                            }
                        }
                    })

            }
        }
    }

    private fun setCancel() {
        binding.cancel.setOnClickListener {
            Navigation.findNavController(binding.root).popBackStack()
        }
    }

}
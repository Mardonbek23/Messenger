package com.example.messenger.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.Navigation
import com.example.messenger.R
import com.example.messenger.databinding.FragmentDesBinding
import com.example.messenger.services.DefNameService
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.lang.Exception

private const val ARG_PARAM1 = "key"
private const val ARG_PARAM2 = "key2"

class DesFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    lateinit var binding: FragmentDesBinding
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var reference: DatabaseReference

    //storage
    lateinit var firebaseStorage: FirebaseStorage
    lateinit var reference_storage: StorageReference
    var image: String? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentDesBinding.inflate(inflater, container, false)
        firebaseDatabase = FirebaseDatabase.getInstance()
        reference = firebaseDatabase.getReference("users")
        firebaseStorage = FirebaseStorage.getInstance()
        reference_storage = firebaseStorage.getReference("images/avatar/$param1")
        binding.save.setOnClickListener {
            val name = binding.name.text.toString()
            val surname = binding.surname.text.toString()
            val description = binding.description.text.toString()
            if (name.isNotEmpty()) {
                Navigation.findNavController(binding.root).popBackStack()
                Navigation.findNavController(binding.root).navigate(R.id.headFragment)
                reference.child(param1!!).child("name").setValue(name)
                if (surname.isNotEmpty()) {
                    reference.child(param1!!).child("surname").setValue(surname)
                }
                if (description.isNotEmpty()) {
                    reference.child(param1!!).child("description").setValue(description)
                }
                reference.child(param1!!).child("image").setValue(image)
                reference.child(param1!!).child("account_color").setValue(DefNameService().getRandomColor())
                setNotificxationkey()
            } else {
                Toast.makeText(requireContext(), "Ismni to\'ldirish majburiy!", Toast.LENGTH_SHORT)
                    .show()
            }

        }
        binding.back.setOnClickListener {

        }
        binding.image.setOnClickListener {
            getImage.launch("image/*")
        }
        return binding.root
    }

    private fun setNotificxationkey() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("notification", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            reference.child(param1!!).child("notification_key").setValue(token.toString())
        })
    }

    private var getImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
      binding.progress.visibility=View.VISIBLE
        if (uri == null) {
            return@registerForActivityResult
            binding.progress.visibility=View.GONE
        }
        reference_storage.child("image").putFile(uri).addOnSuccessListener {
            if (it.task.isSuccessful) {
                it.storage.downloadUrl.addOnSuccessListener { x ->
                    Picasso.get().load(x).placeholder(R.drawable.ic_def_image).into(binding.image,object :Callback{
                        override fun onSuccess() {
                            binding.progress.visibility=View.GONE
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

}
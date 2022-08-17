package com.example.messenger.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.messenger.R
import com.example.messenger.databinding.FragmentMenuBinding
import com.example.messenger.models.Account
import com.example.messenger.shared.SharedPreference
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class MenuFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    lateinit var binding: FragmentMenuBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var reference: DatabaseReference
    lateinit var number: String
    lateinit var group_reference: DatabaseReference

    //storage
    lateinit var firebaseStorage: FirebaseStorage
    lateinit var reference_storage: StorageReference
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentMenuBinding.inflate(inflater, container, false)

        firebaseDatabase = FirebaseDatabase.getInstance()
        reference = firebaseDatabase.getReference("users")
        group_reference = firebaseDatabase.getReference("groups")
        number = SharedPreference().getSomeStringValue(requireContext()).toString()
        firebaseStorage = FirebaseStorage.getInstance()
        reference_storage = firebaseStorage.getReference("images/avatar/$number")
        reference.child(number).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.getValue(Account::class.java)
                if (value!!.image != null) {
                    Picasso.get().load(value!!.image).placeholder(R.drawable.ic_account)
                        .into(binding.image)
                }
                binding.name.setText("${value.surname} ${value.name}")
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
        val gso = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
        setProfile()
        setLogout()
        setBack()
        setGroups()
        setChannels()
        setcontacts()
        setSaved()
        return binding.root
    }

    private fun setSaved() {
        binding.saved1.setOnClickListener {
            Toast.makeText(requireContext(), "toast", Toast.LENGTH_SHORT).show()
            Navigation.findNavController(binding.root).navigate(R.id.savedFragment)
        }

    }

    private fun setcontacts() {
        binding.contacts.setOnClickListener {
            Navigation.findNavController(binding.root).navigate(R.id.contactsFragment)
        }
    }

    private fun setChannels() {
        binding.addChanel.setOnClickListener {
            Navigation.findNavController(binding.root).navigate(R.id.addChannelFragment)
        }
    }

    private fun setGroups() {
        binding.addgroup.setOnClickListener {
            Navigation.findNavController(binding.root).navigate(R.id.addGroup)
        }
    }

    private fun setProfile() {
        binding.number.setText(number)
        binding.image.setOnClickListener {
            getImage.launch("image/*")
        }
    }

    private var getImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        binding.progress.visibility = View.VISIBLE
        if (uri == null) {
            return@registerForActivityResult
            binding.progress.visibility = View.GONE
        }
        reference_storage.child("image").putFile(uri).addOnSuccessListener {
            if (it.task.isSuccessful) {
                it.storage.downloadUrl.addOnSuccessListener { x ->
                    Picasso.get().load(x).placeholder(R.drawable.ic_account)
                        .into(binding.image, object : Callback {
                            override fun onSuccess() {
                                binding.progress.visibility = View.GONE
                            }

                            override fun onError(e: Exception?) {
                                binding.progress.visibility = View.GONE
                            }
                        })
                    binding.progress.visibility = View.GONE
                    reference.child(number).child("image").setValue(x.toString())
                }
            } else {
                Toast.makeText(requireContext(), "Rasm yuklanmadi", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun setBack() {
        binding.back.setOnClickListener {
            Navigation.findNavController(binding.root).popBackStack()
        }
    }

    private fun setLogout() {
        binding.logout.setOnClickListener {
            var builder = AlertDialog.Builder(requireContext(), R.style.AlertDialogStyle);
            builder.setMessage("Siz ushbu profildan chiqmoqchimisiz?");
            builder.setCancelable(true)
            builder.setPositiveButton("Ha", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    googleSignInClient.signOut()
                    SharedPreference().setSomeStringValue(requireContext(), null)
                    Navigation.findNavController(binding.root).popBackStack()
                    Navigation.findNavController(binding.root).navigate(R.id.registrFragment)
                }
            })
            builder.setNegativeButton("Yo\'q", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {

                }
            })
            builder.show()


        }
    }

}
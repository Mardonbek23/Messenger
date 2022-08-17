package com.example.messenger.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.example.messenger.R
import com.example.messenger.adapters.MessageAdapter
import com.example.messenger.databinding.FragmentSavedBinding
import com.example.messenger.models.Message
import com.example.messenger.shared.SharedPreference
import com.google.firebase.database.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class SavedFragment : Fragment() {
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

    lateinit var binding: FragmentSavedBinding
    lateinit var number: String
    lateinit var list: ArrayList<Message>
    lateinit var messageAdapter: MessageAdapter
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var reference1: DatabaseReference
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentSavedBinding.inflate(inflater, container, false)
        number = SharedPreference().getSomeStringValue(requireContext()).toString()
        list = ArrayList()
        setBack()
        setRv()
        return binding.root
    }

    private fun setRv() {
        messageAdapter =
            MessageAdapter(list, number, null, object : MessageAdapter.OnItemClickListener {
                override fun onImageClick(url: String) {

                }

                override fun onItemLongClick(message: Message) {

                }

                override fun onImageSave(message: Message, view: View) {

                }

                override fun onItemMessageClicke(message: Message, view: View) {

                }
            })
        firebaseDatabase = FirebaseDatabase.getInstance()
        reference1 = firebaseDatabase.getReference("saved_messages")
        reference1.child(number).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    var value = child.getValue(Message::class.java)
                    if (value != null) {
                        list.add(value)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun setBack() {
        binding.back.setOnClickListener {
            Navigation.findNavController(binding.root).popBackStack()
        }
    }

}
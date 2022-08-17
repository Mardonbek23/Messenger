package com.example.messenger.fragments

import android.Manifest.permission.CALL_PHONE
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.messenger.databinding.FragmentShowChatBinding
import com.example.messenger.models.Account
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.example.messenger.R
import com.example.messenger.adapters.CallAdapter
import com.example.messenger.models.Call
import com.example.messenger.services.DefNameService
import com.example.messenger.shared.SharedPreference
import java.text.SimpleDateFormat


private const val ARG_PARAM1 = "key"
private const val ARG_PARAM2 = "param2"

class ShowChatFragment : Fragment() {
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

    lateinit var binding: FragmentShowChatBinding
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var reference1: DatabaseReference
    lateinit var reference2: DatabaseReference
    lateinit var reference3: DatabaseReference
    lateinit var account: Account
    lateinit var number: String
    lateinit var my_number: String
    lateinit var list: ArrayList<Call>
    lateinit var callAdapter: CallAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentShowChatBinding.inflate(inflater, container, false)
        number = param1.toString()
        my_number = SharedPreference().getSomeStringValue(requireContext()).toString()
        list = ArrayList()
        firebaseDatabase = FirebaseDatabase.getInstance()
        reference1 = firebaseDatabase.getReference("users")
        reference2 = firebaseDatabase.getReference("calls/$my_number/$number")
        reference3 = firebaseDatabase.getReference("calls/$number/$my_number")
        setBack()
        setData()
        setCall()
        setRv()
        return binding.root
    }

    private fun setRv() {
        callAdapter = CallAdapter(list, my_number)
        reference2.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val children = snapshot.children
                list.clear()
                for (child in children) {
                    val value = child.getValue(Call::class.java)
                    if (value != null) {
                        list.add(value)
                    }
                }
                if (callAdapter.itemCount > 0) {
                    binding.nocall.visibility = View.GONE
                } else {
                    binding.nocall.visibility = View.VISIBLE
                }
                list.reverse()
                binding.rv.adapter = callAdapter
                callAdapter.list = list
                callAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun setCall() {
        binding.call.setOnClickListener {
            val i = Intent(Intent.ACTION_CALL)
            i.data = Uri.parse("tel:$number")
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    CALL_PHONE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                startActivity(i)
                val key = reference2.push().key
                var call = Call(my_number, number, System.currentTimeMillis(), key)
                reference2.child(key!!).setValue(call)
                reference3.child(key!!).setValue(call)
            } else {
                requestPermissions(arrayOf(CALL_PHONE), 1)
            }

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val i = Intent(Intent.ACTION_CALL)
            i.data = Uri.parse("tel:$number")
            startActivity(i)
            val key = reference2.push().key
            var call = Call(my_number, number, System.currentTimeMillis().toLong(), key)
            reference2.child(key!!).setValue(call)
            reference3.child(key!!).setValue(call)
        } else {
            requestPermissions(arrayOf(CALL_PHONE), 1)
        }
    }

    private fun setData() {
        reference1.child(number).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.getValue(Account::class.java)
                if (value != null) {
                    account = value
                    if (value.image != null) {
                        Picasso.get().load(account.image).placeholder(R.drawable.ic_account)
                            .into(binding.image)
                    } else {
                        binding.defText.visibility = View.VISIBLE
                        var txt = DefNameService().getFirstChar(value.surname, value.name)
                        binding.defText.setText(txt)
                        binding.image.setImageResource(value.account_color)
                    }
                    binding.name.setText(account.name)
                    binding.number.setText(number)
                    if (value.isOnline == 1L) {
                        binding.isOnline.setText("Online")
                        binding.isOnline.setTextColor(Color.parseColor("#3B773D"))
                    } else {
                        val simplehour = SimpleDateFormat("HH:mm EE")
                        binding.isOnline.setText("Oxirgi faol vaqti: " + simplehour.format(value.isOnline))
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
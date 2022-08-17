package com.example.messenger.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.messenger.R
import com.example.messenger.adapters.ContactAdapter
import com.example.messenger.adapters.ContactAddAdapter
import com.example.messenger.databinding.BottomDialogBinding
import com.example.messenger.databinding.FragmentShowGroupBinding
import com.example.messenger.models.Account
import com.example.messenger.models.Group
import com.example.messenger.services.DefNameService
import com.example.messenger.shared.SharedPreference
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

private const val ARG_PARAM1 = "key"
private const val ARG_PARAM2 = "param2"

class ShowGroupFragment : Fragment() {
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

    lateinit var binding: FragmentShowGroupBinding
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var reference1: DatabaseReference
    lateinit var contactAdapter: ContactAdapter
    lateinit var contacts: ArrayList<Account>
    lateinit var contactAddAdapter: ContactAddAdapter
    lateinit var lists: ArrayList<Account>
    lateinit var xlist: ArrayList<Account>
    var admin: String? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentShowGroupBinding.inflate(inflater, container, false)
        firebaseDatabase = FirebaseDatabase.getInstance()
        reference1 = firebaseDatabase.getReference("groups")
        lists = ArrayList()
        xlist = ArrayList()
        contacts = ArrayList()
        setRv()
        setBack()
        settool()
        setLists()
        setAdd()

        return binding.root
    }

    private fun setLists() {
        reference1.child(param1!!).child("accounts")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val children = snapshot.children
                    xlist.clear()
                    for (child in children) {
                        val value = child.getValue(Account::class.java)
                        if (value != null) {
                            xlist.add(value)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun setAdd() {

        contactAddAdapter =
            ContactAddAdapter(lists, object : ContactAddAdapter.OnItemClickListener {
                override fun onClick(account: Account) {

                }
            }, param1!!)
        var reference2 = firebaseDatabase.getReference("contacts")
        reference2.child(SharedPreference().getSomeStringValue(requireContext()).toString())
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    lists.clear()
                    val children = snapshot.children
                    for (child in children) {
                        val value = child.getValue(Account::class.java)
                        if (value != null) {
                            var index = -1
                            for (account in xlist) {
                                if (account.phone == value.phone) {
                                    index = 1
                                    break
                                }
                            }
                            if (index == -1) {
                                lists.add(value)
                            }
                        }
                    }
                    contactAddAdapter.list = lists
                    contactAddAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        binding.add.setOnClickListener {
            val bottomSheetDialog = BottomSheetDialog(requireContext())
            val customdialogBinding: BottomDialogBinding =
                BottomDialogBinding.inflate(layoutInflater)
            customdialogBinding.rv.adapter = contactAddAdapter
            customdialogBinding.add.setOnClickListener {
                contactAddAdapter.addItem()
                bottomSheetDialog.dismiss()
            }
            bottomSheetDialog.setContentView(customdialogBinding.getRoot())
            bottomSheetDialog.show()
        }
    }

    private fun setRv() {

            contactAdapter = ContactAdapter(contacts, object : ContactAdapter.OnItemClickListener {
                override fun onClick(account: Account) {
                    val bundle = Bundle()
                    bundle.putString("key", account.phone)
                    if (account.phone != SharedPreference().getSomeStringValue(requireContext())
                            .toString()
                    ) {
                        Navigation.findNavController(binding.root)
                            .navigate(R.id.messageChatFragment, bundle)
                    }
                }
            }, admin)

            contactAdapter.notifyDataSetChanged()

    }

    private fun settool() {

        reference1.child(param1!!).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.getValue(Group::class.java)
                contacts.clear()
                if (value != null) {
                    if (value.image != null) {
                        Picasso.get().load(value.image).into(binding.image)
                    }
                    else{
                        binding.defText.visibility=View.VISIBLE
                        var txt= DefNameService().getFirstChar(null,value.name)
                        binding.defText.setText(txt)
                        binding.image.setImageResource(value.account_color)
                    }
                    binding.name.setText(value.name)
                    binding.count.setText("${value.accounts!!.size}ta a\'zo")
                    for (i in 0..value.accounts!!.size) {
                        if (value.accounts!![i].phone == value.admin) {
                            binding.admin.setText("Rahbar:${value.accounts!![i].name}")
                            break
                        }
                    }

                    admin = value.admin
                    contactAdapter.phone=admin!!
                    contacts = value.accounts!!

                    binding.rv.adapter = contactAdapter
                    contactAdapter.notifyDataSetChanged()
                    contactAdapter.list = contacts
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
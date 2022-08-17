package com.example.messenger.fragments

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.messenger.adapters.ContactAdapter
import com.example.messenger.databinding.ContactDialogBinding
import com.example.messenger.R
import com.example.messenger.databinding.FragmentContactsBinding
import com.example.messenger.models.Account
import com.example.messenger.models.Contact
import com.example.messenger.shared.SharedPreference
import com.google.firebase.database.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ContactsFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    lateinit var binding: FragmentContactsBinding
    lateinit var contactAdapter: ContactAdapter
    lateinit var list: ArrayList<Account>
    lateinit var accounts: ArrayList<Account>
    lateinit var contactList: ArrayList<Contact>
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var reference: DatabaseReference
    lateinit var reference2: DatabaseReference
    lateinit var number: String
    var count = 0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentContactsBinding.inflate(inflater, container, false)
        number = SharedPreference().getSomeStringValue(requireContext()).toString()
        firebaseDatabase = FirebaseDatabase.getInstance()
        reference = firebaseDatabase.getReference("users")
        reference2 = firebaseDatabase.getReference("contacts/$number")
        accounts = ArrayList()
        contactList = ArrayList()
        setContacts()
        setBack()
        setRv()
        setAdd()
        return binding.root
    }

    private fun setAdd() {
        binding.add.setOnClickListener {
            val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Kontakt qo\'shish")
            builder.setCancelable(true)
            val customdialogBinding: ContactDialogBinding =
                ContactDialogBinding.inflate(layoutInflater)
            builder.setView(customdialogBinding.getRoot())
            var alertDialog = builder.create()
            customdialogBinding.add.setOnClickListener {
                val text = customdialogBinding.edit.text.toString()
                if (text.length != 13) {
                    Toast.makeText(requireContext(),
                        "Iltimos raqamni to\'g\'ri kiriting!",
                        Toast.LENGTH_SHORT).show()
                } else {
                    var index = -1
                    accounts.forEach {
                        if (it.phone == text) {
                            index = 1
                        }
                    }
                    if (index == -1) {
                        Toast.makeText(requireContext(),
                            "Bunday raqam mavjud emas!",
                            Toast.LENGTH_SHORT).show()
                    } else {
                        var x = -1
                        for (contact in contactList) {
                            if (contact.number.replace(" ", "") == text) {
                                x = 1
                            }
                        }
                        if (x == 1 || text == number) {
                            Toast.makeText(requireContext(),
                                "Bu raqam kontaktda mavjud!",
                                Toast.LENGTH_SHORT).show()
                        } else {
                            val key = reference2.push().key
                            for (account in accounts) {
                                if (account.phone == text) {
                                    reference2.child("${account.phone}").setValue(account)
                                }
                            }
                            Toast.makeText(requireContext(),
                                "Muvofaqiyatli saqlandi!",
                                Toast.LENGTH_SHORT).show()
                            alertDialog.dismiss()
                        }
                    }
                }
            }
            alertDialog.show()

        }
        binding.rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    // Scroll Down
                    if (binding.add.isShown()) {
                        binding.add.hide();
                    }
                } else if (dy < 0) {
                    // Scroll Up
                    if (!binding.add.isShown()) {
                        binding.add.show();
                    }
                }
            }
        })
    }


    private fun setContacts() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            contactList = ArrayList()
            val contacts = requireContext().contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                null
            )
            while (contacts!!.moveToNext()) {
                val name =
                    contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val number =
                    contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                val contact = Contact(name, number)
                contactList.add(contact)
            }
            contacts.close()

        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.READ_CONTACTS),
                100
            )
        }
    }

    private fun setRv() {
        list = ArrayList()
        contactAdapter = ContactAdapter(list, object : ContactAdapter.OnItemClickListener {
            override fun onClick(account: Account) {
                if (number != account.phone) {
                    val bundle = Bundle()
                    bundle.putString("key", account.phone)
                    Navigation.findNavController(binding.root)
                        .navigate(R.id.messageChatFragment, bundle)
                }
            }
        }, number)

        reference.addValueEventListener(object : ValueEventListener {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onDataChange(snapshot: DataSnapshot) {
                val children = snapshot.children
//                list.clear()
                accounts.clear()
                for (child in children) {
                    val value = child.getValue(Account::class.java)
                    if (value != null) {
                        accounts.add(value)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
        reference2.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                list.clear()
                val children = snapshot.children
                for (child in children) {
                    val value = child.getValue(Account::class.java)
                    if (value != null) {
                        list.add(value)
                    }
                }
                contactAdapter.notifyDataSetChanged()
                contactAdapter.list = list
                binding.rv.adapter = contactAdapter
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })


    }

    private fun setBack() {
        binding.back.setOnClickListener {
            Navigation.findNavController(binding.root).popBackStack()
        }
        binding.search.setOnClickListener {
            Navigation.findNavController(binding.root).navigate(R.id.searchFragment)
        }
    }

}
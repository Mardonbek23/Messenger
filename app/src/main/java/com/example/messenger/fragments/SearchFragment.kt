package com.example.messenger.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.example.messenger.R
import com.example.messenger.adapters.ChannelSerachAdapter
import com.example.messenger.adapters.ContactAdapter
import com.example.messenger.databinding.FragmentSearchBinding
import com.example.messenger.models.Account
import com.example.messenger.models.Group
import com.example.messenger.shared.SharedPreference
import com.google.firebase.database.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class SearchFragment : Fragment() {
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

    lateinit var binding: FragmentSearchBinding
    lateinit var accountList_base: ArrayList<Account>
    lateinit var accountList: ArrayList<Account>
    lateinit var channelList: ArrayList<Group>
    lateinit var channelList_base: ArrayList<Group>
    lateinit var accountAdapter: ContactAdapter
    lateinit var channelAdapter: ChannelSerachAdapter

    //Firebase database
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var reference_user: DatabaseReference
    lateinit var reference_channel: DatabaseReference

    lateinit var number: String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        accountList_base = ArrayList()
        accountList = ArrayList()
        channelList = ArrayList()
        channelList_base = ArrayList()
        number = SharedPreference().getSomeStringValue(requireContext()).toString()
        firebaseDatabase = FirebaseDatabase.getInstance()
        reference_user = firebaseDatabase.getReference("users")
        reference_channel = firebaseDatabase.getReference("channel")

        setBack()
        setAccountList()
        setChannelList()
        return binding.root
    }

    private fun setChannelList() {
        channelAdapter = ChannelSerachAdapter(channelList, object : ChannelSerachAdapter.OnItemClickListener {
            override fun onClick(group: Group) {
                var bundle = Bundle()
                bundle.putString("key", group.key)
                Navigation.findNavController(binding.root)
                    .navigate(R.id.messageChannelFragment, bundle)
            }
        }, number)
        reference_channel.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                channelList_base.clear()
                for (child in snapshot.children) {
                    var value = child.getValue(Group::class.java)
                    if (value != null) {
                        channelList_base.add(value)
                    }
                }
                binding.rvChannel.adapter = channelAdapter
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }

    private fun setAccountList() {
        accountAdapter = ContactAdapter(accountList, object : ContactAdapter.OnItemClickListener {
            override fun onClick(account: Account) {
                var bundle = Bundle()
                bundle.putString("key", account.phone)
                Navigation.findNavController(binding.root)
                    .navigate(R.id.messageChatFragment, bundle)
            }
        }, number)
        reference_user.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                accountList_base.clear()
                var children = snapshot.children
                for (child in children) {
                    var value = child.getValue(Account::class.java)
                    if (value != null) {
                        accountList_base.add(value)
                    }
                }

                binding.rvAccount.adapter = accountAdapter
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
        binding.edit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                accountList.clear()
                var text = binding.edit.text.toString()
                for (account in accountList_base) {
                    if (account.name.toString().contains(text) || account.surname.toString()
                            .contains(text)
                    ) {
                        if (account.phone != number) {
                            accountList.add(account)
                        }
                    }
                }
                accountAdapter.list = accountList
                binding.rvAccount.adapter = accountAdapter

                channelList.clear()
                for (group in channelList_base) {
                    if (group.name!!.contains(text) || group.key!!.contains(text)) {
                        channelList.add(group)
                    }
                }
                channelAdapter.list = channelList
                binding.rvChannel.adapter = channelAdapter
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                accountList.clear()
                var text = binding.edit.text.toString()
                for (account in accountList_base) {
                    if (account.name.toString().contains(text) || account.surname.toString()
                            .contains(text)
                    ) {
                        if (account.phone != number) {
                            accountList.add(account)
                        }
                    }
                }
                accountAdapter.list = accountList
                binding.rvAccount.adapter = accountAdapter

                channelList.clear()
                for (group in channelList_base) {
                    if (group.name!!.contains(text) || group.key!!.contains(text)) {
                        channelList.add(group)
                    }
                }
                channelAdapter.list = channelList
                binding.rvChannel.adapter = channelAdapter
            }

            override fun afterTextChanged(s: Editable?) {
                accountList.clear()
                var text = binding.edit.text.toString()
                for (account in accountList_base) {
                    if (account.name.toString().contains(text) || account.surname.toString()
                            .contains(text)
                    ) {
                        if (account.phone != number) {
                            accountList.add(account)
                        }
                    }
                }
                accountAdapter.list = accountList
                binding.rvAccount.adapter = accountAdapter

                channelList.clear()
                for (group in channelList_base) {
                    if (group.name!!.contains(text) || group.key!!.contains(text)) {
                        channelList.add(group)
                    }
                }
                channelAdapter.list = channelList
                binding.rvChannel.adapter = channelAdapter
            }
        })
    }


    private fun setBack() {
        binding.back.setOnClickListener {
            Navigation.findNavController(binding.root).popBackStack()
        }

    }

}
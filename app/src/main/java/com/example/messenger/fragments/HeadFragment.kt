package com.example.messenger.fragments

import android.content.DialogInterface
import android.content.pm.PackageManager
import com.example.messenger.models.Account
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.messenger.R
import com.example.messenger.adapters.AccountAdapter
import com.example.messenger.adapters.ChannelAdapter
import com.example.messenger.adapters.GroupAdapter
import com.example.messenger.databinding.FragmentHeadBinding
import com.example.messenger.models.Group
import com.example.messenger.models.Message
import com.example.messenger.shared.SharedPreference
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class HeadFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    lateinit var binding: FragmentHeadBinding
    lateinit var list: ArrayList<Account>
    lateinit var groups: ArrayList<Group>
    lateinit var channels: ArrayList<Group>
    lateinit var groupAdapter: GroupAdapter
    lateinit var channelAdapter: ChannelAdapter
    lateinit var message_list:ArrayList<String>

    //Firebase
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var reference: DatabaseReference
    lateinit var reference_menu: DatabaseReference
    lateinit var reference_group: DatabaseReference
    lateinit var reference_channel: DatabaseReference
    lateinit var accountAdapter: AccountAdapter
    lateinit var number: String
    lateinit var sharedPreference: SharedPreference


    //storage
    lateinit var firebaseStorage: FirebaseStorage
    lateinit var reference_storage: StorageReference
    private lateinit var googleSignInClient: GoogleSignInClient
    lateinit var group_reference: DatabaseReference
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentHeadBinding.inflate(inflater, container, false)
        sharedPreference = SharedPreference()
        val someStringValue = sharedPreference.getSomeStringValue(requireContext())
        number = someStringValue!!
        firebaseDatabase = FirebaseDatabase.getInstance()
        reference = firebaseDatabase.getReference("users")
        reference_group = firebaseDatabase.getReference("groups")
        reference_channel = firebaseDatabase.getReference("channel")
        message_list= ArrayList()
        set_adapter()
        val enabledItem = SharedPreference().getEnabledItem(requireContext())
        setBottomNavigation()
        setFloatBtn()
        setMessagintContacts()
        binding.bottomNavigation.selectedItemId = enabledItem!!
        return binding.root
    }


    private fun setMessagintContacts() {
        var reference_txt=firebaseDatabase.getReference("messaging_contacts")
        reference_txt.child("$number").addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                message_list.clear()
                for (child in snapshot.children) {
                    var value=child.getValue(String::class.java)
                    if (value!=null){
                        message_list.add(value.toString())
                    }
                }
                if (binding.bottomNavigation.selectedItemId==R.id.chats){
                    setChats()
                }
                accountAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (binding.bottomNavigation.selectedItemId==R.id.chats){
            setChats()
        }
    }

    private fun setFloatBtn() {
        binding.search.setOnClickListener {
            Navigation.findNavController(binding.root).navigate(R.id.searchFragment)
        }
        binding.add.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.READ_CONTACTS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Navigation.findNavController(binding.root).navigate(R.id.contactsFragment)
            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(android.Manifest.permission.READ_CONTACTS),
                    100
                )
            }
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Navigation.findNavController(binding.root).navigate(R.id.contactsFragment)
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.READ_CONTACTS),
                100
            )
        }
    }

    private fun set_adapter() {
        list = ArrayList()
        groups = ArrayList()
        channels = ArrayList()
        accountAdapter = AccountAdapter(list = list, object : AccountAdapter.OnItemClickListener {
            override fun onClick(account: Account) {
                val bundle = Bundle()
                bundle.putString("key", account.phone)
                Navigation.findNavController(binding.root)
                    .navigate(R.id.messageChatFragment, bundle)
            }
        }, number)
        groupAdapter = GroupAdapter(groups, object : GroupAdapter.OnItemClickListener {
            override fun onClick(group: Group) {
                val bundle = Bundle()
                bundle.putString("key", group.key)
                Navigation.findNavController(binding.root)
                    .navigate(R.id.messageGroupFragment, bundle)
            }
        }, number)
        channelAdapter = ChannelAdapter(channels, object : ChannelAdapter.OnItemClickListener {
            override fun onClick(group: Group) {
                val bundle = Bundle()
                bundle.putString("key", group.key)
                Navigation.findNavController(binding.root)
                    .navigate(R.id.messageChannelFragment, bundle)
            }
        }, number)
        binding.rv.adapter = accountAdapter
    }

    private fun setBottomNavigation() {
        val menu1 = binding.bottomNavigation.menu.getItem(0)
        val menu2 = binding.bottomNavigation.menu.getItem(1)
        val menu3 = binding.bottomNavigation.menu.getItem(2)
        val menu4 = binding.bottomNavigation.menu.getItem(3)
        binding.bottomNavigation.setOnNavigationItemSelectedListener(object :
            BottomNavigationView.OnNavigationItemSelectedListener {
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                when (item.itemId) {
                    R.id.chats -> {
                        binding.rv.visibility = View.VISIBLE
                        binding.menuFragment.visibility = View.GONE
                        SharedPreference().setEnabledItem(requireContext(), R.id.chats)
                        binding.toolText.setText("Do\'stlar")
                        menu1.setIcon(R.drawable.ic_selected_chat)
                        menu2.setIcon(R.drawable.ic_u_group)
                        menu3.setIcon(R.drawable.ic_u_chanel)
                        menu4.setIcon(R.drawable.ic_u_menu)
                        setChats()
                    }
                    R.id.groups -> {
                        binding.rv.visibility = View.VISIBLE
                        binding.menuFragment.visibility = View.GONE
                        SharedPreference().setEnabledItem(requireContext(), R.id.groups)
                        binding.toolText.setText("Guruhlar")
                        menu1.setIcon(R.drawable.ic_chatlar)
                        menu2.setIcon(R.drawable.ic_selected_group)
                        menu3.setIcon(R.drawable.ic_u_chanel)
                        menu4.setIcon(R.drawable.ic_u_menu)
                        setGroups()
                    }
                    R.id.chanels -> {
                        binding.rv.visibility = View.VISIBLE
                        binding.menuFragment.visibility = View.GONE
                        SharedPreference().setEnabledItem(requireContext(), R.id.chanels)
                        binding.toolText.setText("Kanallar")
                        menu1.setIcon(R.drawable.ic_chatlar)
                        menu2.setIcon(R.drawable.ic_u_group)
                        menu3.setIcon(R.drawable.ic_s_chanel)
                        menu4.setIcon(R.drawable.ic_u_menu)
                        setChanels()
                    }
                    R.id.menu -> {
                        binding.rv.visibility = View.GONE
                        binding.menuFragment.visibility = View.VISIBLE
                        SharedPreference().setEnabledItem(requireContext(), R.id.menu)
                        binding.toolText.setText("Menu")
                        menu1.setIcon(R.drawable.ic_chatlar)
                        menu2.setIcon(R.drawable.ic_u_group)
                        menu3.setIcon(R.drawable.ic_u_chanel)
                        menu4.setIcon(R.drawable.ic_selected_menu)
                        setMenu()
                    }
                }
                return true
            }


        })
    }

    private fun setMenu() {
        binding.add.visibility = View.GONE
        binding.progress.visibility = View.GONE
        reference_menu = firebaseDatabase.getReference("users")
        group_reference = firebaseDatabase.getReference("groups")
        number = SharedPreference().getSomeStringValue(requireContext()).toString()
        firebaseStorage = FirebaseStorage.getInstance()
        reference_storage = firebaseStorage.getReference("images/avatar/$number")
        reference_menu.child(number).addValueEventListener(object : ValueEventListener {
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
        setGroupsm()
        setChannels()
        setcontacts()
    }

    //Menu Settings starts
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

    private fun setGroupsm() {
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
        binding.progress1.visibility = View.VISIBLE
        if (uri == null) {
            return@registerForActivityResult
            binding.progress1.visibility = View.GONE
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
                    binding.progress1.visibility = View.GONE
                    reference_menu.child(number).child("image").setValue(x.toString())
                }
            } else {
                Toast.makeText(requireContext(), "Rasm yuklanmadi", Toast.LENGTH_SHORT).show()
            }
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
    //Menu Settings end

    private fun setGroups() {
        binding.add.visibility = View.GONE
        binding.rv.adapter = groupAdapter
        binding.progress.visibility = View.VISIBLE
        reference_group.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val children = snapshot.children
                groups.clear()
                for (child in children) {
                    val value = child.getValue(Group::class.java)
                    if (value != null) {
                        var index = -1
                        value.accounts!!.forEach {
                            if (it.phone == number) {
                                index = 1
                            }
                        }
                        if (index == 1) {
                            groups.add(value)
                        }
                    }
                }
                groupAdapter.notifyDataSetChanged()
                binding.progress.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun setChanels() {
        binding.add.visibility = View.GONE
        binding.rv.adapter = channelAdapter
        binding.progress.visibility = View.VISIBLE
        reference_channel.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val children = snapshot.children
                channels.clear()
                for (child in children) {
                    val value = child.getValue(Group::class.java)
                    if (value != null) {
                        var index = -1
                        value.accounts!!.forEach {
                            if (it.phone == number) {
                                index = 1
                            }
                        }
                        if (index == 1) {
                            channels.add(value)
                        }
                    }
                }
                channelAdapter.notifyDataSetChanged()
                binding.progress.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun setChats() {
        binding.add.visibility = View.VISIBLE
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val children = snapshot.children
                list.clear()
                for (child in children) {
                    val value = child.getValue(Account::class.java)
                    if (value!!.phone != number) {
                        for (s in message_list) {
                            if (value.phone==s){
                                list.add(value!!)
                                break
                            }
                        }
                    }
                }

                binding.progress.visibility = View.GONE
                accountAdapter.list = list
                accountAdapter.notifyDataSetChanged()
                binding.rv.adapter = accountAdapter
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
//        reference.child(number).addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                val children = snapshot.children
//                list.clear()
//                for (child in children) {
//                    val value = child.getValue(Account::class.java)
//
//                    if (value!!.phone != number) {
//                        list.add(value!!)
//                    }
//                }
//
//                binding.progress.visibility = View.GONE
//                accountAdapter.list = list
//                accountAdapter.notifyDataSetChanged()
//                binding.rv.adapter = accountAdapter
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//
//            }
//        })
    }

}
package com.example.messenger

import android.app.DownloadManager
import android.net.ConnectivityManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.navigation.Navigation
import com.example.messenger.databinding.ActivityMainBinding
import com.example.messenger.shared.SharedPreference
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.google.GoogleEmojiProvider
import java.io.File

class MainActivity : AppCompatActivity() {
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var reference: DatabaseReference
    lateinit var number: String
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        number = SharedPreference().getSomeStringValue(this).toString()
        firebaseDatabase = FirebaseDatabase.getInstance()
        val chats = R.id.chats
        SharedPreference().setEnabledItem(this, chats)
        reference = firebaseDatabase.getReference("users")

    }

    override fun onSupportNavigateUp(): Boolean {
        return Navigation.findNavController(this, R.id.my_nav_host_fragment).navigateUp()
    }


    override fun onStart() {
        super.onStart()

        if (number != null&&number.length>6) {
            reference.child(number).child("online").setValue(1L)
        }
    }
    override fun onStop() {
        super.onStop()
        if (number != null&&number.length>6) {
            reference.child(number).child("online").setValue(System.currentTimeMillis().toLong())
        }
    }
}
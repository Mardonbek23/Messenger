package com.example.messenger.fragments

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.example.messenger.R
import com.example.messenger.databinding.FragmentSmsBinding
import com.example.messenger.models.Account
import com.example.messenger.services.DefNameService
import com.example.messenger.shared.SharedPreference
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import java.text.SimpleDateFormat

private const val ARG_PARAM1 = "key"
private const val ARG_PARAM2 = "param2"

class SmsFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    lateinit var binding: FragmentSmsBinding
    lateinit var number: String
    lateinit var sharedPreference: SharedPreference
    lateinit var list: ArrayList<Account>

    //Firebase
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var storedVerificationId: String
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    //Google sign
    private lateinit var googleSignInClient: GoogleSignInClient
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var reference: DatabaseReference
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentSmsBinding.inflate(inflater, container, false)
        sharedPreference = SharedPreference()
        number = param1!!
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        reference = firebaseDatabase.getReference("users")
        list = ArrayList()
        setList()
        settime()
        return binding.root
    }

    private fun setList() {
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                list.clear()
                val children = snapshot.children
                for (child in children) {
                    val value = child.getValue(Account::class.java)
                    if (value != null) {
                        list.add(value)
                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    override fun onResume() {
        super.onResume()
        setGoogleSign()
        setData()
        setResend()
        setVerificationCode(number)
        checkCode()
    }

    private fun settime() {
        object : CountDownTimer(45000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                var simple = SimpleDateFormat("ss")
                binding.time.setText("00:${simple.format(millisUntilFinished)}")
            }

            override fun onFinish() {
                binding.or.visibility = View.VISIBLE
                binding.googleSign.visibility = View.VISIBLE
                binding.time.visibility = View.GONE
                binding.resend.visibility = View.VISIBLE
            }
        }.start()
    }

    private fun setGoogleSign() {
        val gso = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.googleSign.setOnClickListener {
            signIn()
        }
    }

    private fun checkCode() {
            binding.next.setOnClickListener {
                val code = binding.code.text.toString()
                if (code.length == 6) {
                    val credential =
                        PhoneAuthProvider.getCredential(storedVerificationId, code)
                    signInWithPhoneAuthCredential(credential)
                } else {
                    Toast.makeText(requireContext(),
                        "Kod 6ta raqamdan iborat bo\'lishi kerak!",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun setResend() {
        binding.resend.setOnClickListener {
            binding.resend.visibility = View.INVISIBLE
            binding.time.visibility = View.VISIBLE
            binding.or.visibility=View.INVISIBLE
            binding.googleSign.visibility=View.INVISIBLE
            resendCode(number)
            settime()
        }
    }

    private fun setData() {
        binding.tr.setText("Sms ${number.substring(0, 9)}-**-** raqamiga yuborildi")
    }


    fun setVerificationCode(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(param1!!)       // Phone number to verify
            .setTimeout(45L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(requireActivity())                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun resendCode(phoneNumber: String) {
        if (::resendToken.isInitialized) {
            val options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phoneNumber)       // Phone number to verify
                .setTimeout(45L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(requireActivity())                 // Activity (for callback binding)
                .setCallbacks(callbacks)       // OnVerificationStateChangedCallbacks
                .setForceResendingToken(resendToken)
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = firebaseAuth.currentUser
                var ishave = true
                for (account in list) {
                    if (account.phone == number) {
                        ishave = false
                        break
                    }
                }
                var a = "1"
                val bundle = Bundle()
                bundle.putString("key", number)
                if (ishave) {
                    var token:String?=null
                    FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            Log.w("notification", "Fetching FCM registration token failed", task.exception)
                            return@OnCompleteListener
                        }

                        // Get new FCM registration token
                        token = task.result
                    })
                    var account =
                        Account(number, null, null, null, user!!.email, null, null, 1,
                            DefNameService().getRandomColor(),token,null)
                    reference.child(number).setValue(account)
                    a = "2"
                    bundle.putString("key2", a)
                    Navigation.findNavController(binding.root).popBackStack()
                    Navigation.findNavController(binding.root)
                        .navigate(R.id.desFragment, bundle)
                } else {
                    bundle.putString("key2", a)
                    Navigation.findNavController(binding.root).popBackStack()
                    Navigation.findNavController(binding.root)
                        .navigate(R.id.headFragment, bundle)
                }
                sharedPreference.setSomeStringValue(requireContext(), number)
            } else {
                Log.w(ContentValues.TAG, "signInWithCredential:failure", task.exception)
                if (task.exception is FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(requireContext(), "kod notug'ri kiritildi!", Toast.LENGTH_SHORT)
                        .show()
                }
                // Update UI
            }
        }
    }

    val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
            }

        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken,
        ) {
            Log.d("Kod yuborildi-->", "onCodeSent:$verificationId")
            storedVerificationId = verificationId
            resendToken = token
        }
    }


    //Gooodle Sign

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                var a = "1"
                var ishave = true
                val user = firebaseAuth.currentUser
                for (account in list) {
                    if (account.phone == number) {
                        ishave = false
                        break
                    }
                }
                val bundle = Bundle()
                bundle.putString("key", number)
                if (ishave) {
                    var account =
                        Account(number, null, null, null, user!!.email, null, null, 1)
                    reference.child(number).setValue(account)
                    a = "2"
                    bundle.putString("key2", a)
                    Navigation.findNavController(binding.root).popBackStack()
                    Navigation.findNavController(binding.root)
                        .navigate(R.id.desFragment, bundle)
                } else {
                    bundle.putString("key2", a)
                    Navigation.findNavController(binding.root).popBackStack()
                    Navigation.findNavController(binding.root)
                        .navigate(R.id.headFragment, bundle)
                }
                sharedPreference.setSomeStringValue(requireContext(), number)


            }
        }

    }

    fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount = completedTask.getResult(ApiException::class.java)!!
            firebaseAuthWithGoogle(account.idToken!!)

        } catch (e: ApiException) {
            Log.d("0123456789", "handleSignInResult: ${e.statusCode}")
        }
    }
}
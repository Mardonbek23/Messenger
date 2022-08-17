package com.example.messenger.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.example.messenger.R
import com.example.messenger.databinding.FragmentRegistrBinding
import com.example.messenger.shared.SharedPreference

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class RegistrFragment : Fragment() {
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

    lateinit var binding: FragmentRegistrBinding
    lateinit var sharedPreference: SharedPreference
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding =FragmentRegistrBinding.inflate(inflater, container, false)
        sharedPreference= SharedPreference()


        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val str = sharedPreference.getSomeStringValue(requireContext())
        if (str!=null){
            Navigation.findNavController(binding.root).popBackStack()
            Navigation.findNavController(binding.root).navigate(R.id.headFragment)
        }
        binding.next.setOnClickListener {
            val number = binding.telNumber.text.toString()
            if (number.length == 9) {
                val bundle = Bundle()
                bundle.putString("key", "+998$number")
                Navigation.findNavController(binding.root).popBackStack()
                Navigation.findNavController(binding.root).navigate(R.id.smsFragment,bundle)
            }
            else{
                Toast.makeText(requireContext(), "Telefon raqamni to'ldiring!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
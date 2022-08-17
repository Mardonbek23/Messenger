package com.example.messenger.services

import android.content.Context
import com.example.messenger.R
import com.example.messenger.retrofit.ApiClient
import com.example.messenger.retrofit.RetrofitObject
import java.util.*

class DefNameService {
    fun getFirstChar(str1: String?, str2: String?): String {
        var a2 = str2!!.replace(" ", "").substring(0, 1)
        if (str1 != null&&str1.length>0) {
            var a1 = str1.replace(" ", "")
            if(a1.length>0){
                a1=a1.substring(0,1)
            }
            return "${a1.toUpperCase()} ${a2.toUpperCase()}"
        } else {
            return a2.toUpperCase()
        }
    }
    fun getRandomColor():Int{
        var a=Random().nextInt(6)
        var color=R.color.def_blue
        when(a){
            0->return R.color.def_blue
            1->return R.color.def_green
            2->return R.color.def_orange
            3->return R.color.def_pink
            4->return R.color.def_yellow
            5->return R.color.def_siyoh
        }
        return color
    }
}
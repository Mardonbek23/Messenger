package com.example.messenger.retrofit

import com.google.android.gms.common.api.Response
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiService {
    @Headers(
        "Content-Type:application/json",
        "Authorization:key=AAAA00FZ3eM:APA91bFoz_XrH9ae_BXXzFkdQ-aMEyyLsm1JGW9Bn3WsPkreSzsrSirxh49UKO_mU4DK-ZiKws-NtMtLOXLrei1j49vGmW8KKFUhPymoKqWfvPV5jxNV52d5ZaJe5Mtx98WOhohyViRB"
    )
    @POST("fcm/send")
    fun getNotify(
       @Body retrofitObject: RetrofitObject
    ): Call<RetrofitObject>
}
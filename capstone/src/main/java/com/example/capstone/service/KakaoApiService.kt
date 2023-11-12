package com.example.capstone.service

import com.example.capstone.data.Address
import com.example.capstone.data.AddressResponse
import com.google.gson.GsonBuilder
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
import java.net.CookieManager
import java.net.CookiePolicy
import java.util.concurrent.TimeUnit

interface KakaoApiService {

    companion object {
        val instance = KakaoApiServiceGenerator.createService(KakaoApiService::class.java)
    }

    // REST API KEY 수정 필요 (b5821f9c1bc1acf58f6f7aa5e17d8b94 부분)
    @Headers("Authorization: KakaoAK 9ae432b0db81d7a5de8096a2cb932231")
    @GET("coord2address.json")
    suspend fun coordToAddress(
        @Query("x") longitude: String,
        @Query("y") latitude: String
    ) : Response<AddressResponse>

}

object KakaoApiServiceGenerator {

    private const val BASE_URL = "https://dapi.kakao.com/v2/local/geo/"

    fun <S> createService(serviceClass: Class<S>): S {
        val cookieManager = CookieManager()
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL)
        val myCookieJar = JavaNetCookieJar(cookieManager)

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .cookieJar(myCookieJar)
            .retryOnConnectionFailure(true)
            .build()

        val gson = GsonBuilder().setLenient().create()

        val builder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl(BASE_URL)
            .client(okHttpClient)
        val retrofit = builder.build()
        return retrofit.create(serviceClass)
    }

}
package com.example.capstone.service

import com.google.gson.GsonBuilder
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.net.CookieManager
import java.net.CookiePolicy
import java.util.concurrent.TimeUnit

interface RestApiService {

    companion object {
        val instance = RestApiServiceGenerator.createService(RestApiService::class.java)
        val serviceKey = "bSNLput17htg30aKizDy8BQFXWSJUiXgwRvYfF2ivmyXih4YUfPubY/uxRsVj4B1tdokVI+M3tjKDqNsxW3ErQ=="
    }

    @GET("getHsptlMdcncListInfoInqire")
    fun getHospitals(
        @Query("serviceKey") serviceKey: String,
        @Query("Q0") address1: String,
        @Query("Q1") address2: String,
        @Query("QZ") dutyDiv: String,
        @Query("numOfRows") rows: Int
    ) : Call<ResponseBody>

    @GET("getHsptlMdcncLcinfoInqire")
    fun getHospitalsByWGS84(
        @Query("serviceKey") serviceKey: String,
        @Query("WGS84_LON") lon: Double,
        @Query("WGS84_LAT") lat: Double,
        @Query("numOfRows") rows: Int
    ) : Call<ResponseBody>

}

object RestApiServiceGenerator {

    private const val BASE_URL = "http://apis.data.go.kr/B552657/HsptlAsembySearchService/"

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
            //.addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl(BASE_URL)
            .client(okHttpClient)
        val retrofit = builder.build()
        return retrofit.create(serviceClass)
    }

}
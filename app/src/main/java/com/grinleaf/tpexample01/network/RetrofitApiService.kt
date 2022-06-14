package com.grinleaf.tpexample01.network

import com.grinleaf.tpexample01.model.KakaoSearchPlaceResponse
import com.grinleaf.tpexample01.model.NaverUserInfoResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Query

interface RetrofitApiService {

    //네아로 사용자정보 API
    // @Headers("Authorization: Bearer AAAA...", "Authorization: Bearer BBBB", ...) --> 접근 토큰을 정적인 형태로, 고정하여 사용할 때
    @GET("/v1/nid/me")
    fun getNidUserInfo(@Header("Authorization") authorization:String): Call<NaverUserInfoResponse>  //--> 접근 토큰을 동적으로 사용할 때

    //카카오 키워드 장소검색 API : 결과값 String 반환
    @Headers("Authorization: KakaoAK c2eb139bc820badb8f58d4e0b2a1a0a4")
    @GET("/v2/local/search/keyword.json")
    fun searchPlacesToString(@Query("query")query:String, @Query("x")longitude:String, @Query("y")latitude:String):Call<String>

    //카카오 키워드 장소검색 API : 결과값 json 파싱한 KakaoSearchPlaceResponse 로
    @Headers("Authorization: KakaoAK c2eb139bc820badb8f58d4e0b2a1a0a4")
    @GET("/v2/local/search/keyword.json")
    fun searchPlaces(@Query("query")query:String, @Query("x")longitude:String, @Query("y")latitude:String):Call<KakaoSearchPlaceResponse>

}
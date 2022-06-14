package com.grinleaf.tpexample01.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import com.grinleaf.tpexample01.R
import com.grinleaf.tpexample01.databinding.ActivityPlaceUrlBinding

class PlaceUrlActivity : AppCompatActivity() {
    val binding by lazy { ActivityPlaceUrlBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        
        //** 웹뷰 사용 시 필수적으로 쓰는 코드
        binding.wv.webViewClient= WebViewClient()       //디바이스 내 웹브라우저 새창x 기존 앱 내에서 실행 
        binding.wv.webChromeClient= WebChromeClient()   //팝업관리자
        binding.wv.settings.javaScriptEnabled= true     //js 문서 사용 가능
        
        //intent 에서 Url 데이터 받기
        val placeUrl:String= intent.getStringExtra("place_url") ?:""    //placeUrl 이 null 일 경우 "" 보여줌
        binding.wv.loadUrl(placeUrl)
    }

    override fun onBackPressed() {
        if(binding.wv.canGoBack()) binding.wv.goBack()  //웹뷰에서 웹페이지 이동하여 뒤로 돌아갈 페이지가 쌓여있을 경우, 이전 페이지로 이동
        else super.onBackPressed()                      //이전페이지가 없으면 액티비티 종료
    }
}
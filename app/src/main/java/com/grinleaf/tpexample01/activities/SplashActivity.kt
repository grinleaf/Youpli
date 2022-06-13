package com.grinleaf.tpexample01.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView 대신 Theme 를 이용한 화면 구성! --> Splash 화면에서 자주 쓰임(클릭 이벤트 같은 거 없이 모양 출력만 하는 화면의 경우)

        //1.5초 후에 자동으로 LoginActivity 로 이동 : Handler 이용 (Runnable == Thread 의 인터페이스 객체)
//        Handler(Looper.getMainLooper()).postDelayed(object:Runnable{
//            override fun run() {
//                TODO("Not yet implemented")
//            }
//        },1500)
        //Handler 람다 표기법으로 사용하기
        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            startActivity(Intent(this,LoginActivity::class.java))
            finish()
        },1500)
    }
}
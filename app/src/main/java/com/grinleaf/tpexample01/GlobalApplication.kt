package com.grinleaf.tpexample01

import android.app.Application
import com.kakao.sdk.common.KakaoSdk

class GlobalApplication:Application() {
    override fun onCreate() {
        super.onCreate()

        // Kakao SDK 초기화 (필수!)
        KakaoSdk.init(this, "83a761736b854fe5e13b7ba44d54f4af")
    }
}
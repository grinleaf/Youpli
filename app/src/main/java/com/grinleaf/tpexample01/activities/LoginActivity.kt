package com.grinleaf.tpexample01.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.grinleaf.tpexample01.G
import com.grinleaf.tpexample01.databinding.ActivityLoginBinding
import com.grinleaf.tpexample01.model.NaverUserInfoResponse
import com.grinleaf.tpexample01.model.UserAccount
import com.grinleaf.tpexample01.network.RetrofitApiService
import com.grinleaf.tpexample01.network.RetrofitHelper
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.util.Utility
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.OAuthLoginCallback
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //둘러보기 글씨 클릭으로 로그인없이 Main 화면 실행
        binding.tvGo.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        //회원가입 버튼 클릭
        binding.tvSignup.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java)) //회원가입 화면으로 전환
        }

        //이메일 로그인 버튼 클릭
        binding.layoutLoginEmail.setOnClickListener {
            startActivity(Intent(this, EmailSignInActivity::class.java)) //이메일 로그인 화면으로 전환
        }

        //간편로그인 - Kakao/google/naver
        binding.btnLoginKakao.setOnClickListener { clickLoginKakao() }
        binding.btnLoginGoogle.setOnClickListener { clickLoginGoogle() }
        binding.btnLoginNaver.setOnClickListener { clickLoginNaver() }

        //카카오로그인 키해시 값 얻어오기
        val keyHash:String= Utility.getKeyHash(this)
        Log.i("keyHash",keyHash)

    }

    private fun clickLoginKakao() {
        //카카오 로그인 SDK 설정 후,
        //카카오 로그인 성공 시 반응하는 callback 객체 생성
        //--> 자료형이 함수(==OAuthToken? 과 Throwable 을 멤버로 가진 / return 이 없는(==Unit))인 변수 callback
        //val callback: (OAuthToken?, Throwable?)->Unit = fun(token:OAuthToken?, error:Throwable?){ }
        //람다식으로~
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if(error!=null) Toast.makeText(this, "카카오로그인 실패", Toast.LENGTH_SHORT).show()
            else Toast.makeText(this, "카카오로그인 성공", Toast.LENGTH_SHORT).show()

            //사용자 정보 요청
            UserApiClient.instance.me { user, error ->
                if(user!=null){
                    var id:String= user.id.toString()
                    var email:String= user.kakaoAccount?.email ?: ""
                    //if(email==null) email=""
                    G.userAccount= UserAccount(id,email)

                    //MainActivity 로 이동
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
        }
        //카카오톡이 설치되어 있을 경우, 카카오톡 로그인 / 설치되지 않았을 경우 카카오계정 로그인
        if(UserApiClient.instance.isKakaoTalkLoginAvailable(this)){
            UserApiClient.instance.loginWithKakaoTalk(this,callback = callback)
            //★ 두번째 파라미터부터 변수명=null : default value 설정되어 있는 것 (==파라미터 입력되지 않았을 때 null 을 디폴트로 가짐) 읽을 줄 알아야해~!
            //★ 중간 파라미터들 순서를 무시하고 뒷 쪽 파라미터 값 설정하고 싶을 때, 파라미터 변수명 = '넣을 값'
        }else{
            UserApiClient.instance.loginWithKakaoAccount(this,callback = callback)
        }
    }

    private fun clickLoginGoogle(){
        //Firebase Authentication - ID 공급업체 (Google)
        //* google login 관련 가이드 문서에서는 firebase auth 제품과 연동하여 만들도록 권장함
        //* 구글 로그인만 사용할 거면(이메일 로그인, 페이스북 로그인 등 다중 로그인 없이) 파이어베이스 개발 가이드 대신 구글 개발 가이드를 참고하기~

        //구글 로그인 옵션객체 생성 : Builder 이용
        val gso:GoogleSignInOptions= GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)   //id 만 요청됨 (==식별자. 알 수 없는 랜덤 값)
            .requestEmail() //이메일 요청
            .build()

        //구글 로그인 화면 액티비티를 실행시켜주는 Intent 객체 얻어오기!
        val intent:Intent= GoogleSignIn.getClient(this, gso).signInIntent
        resultLauncher.launch(intent)
    }

    //새로운 액티비티를 실행하고 해당 결과를 받아오는 객체 등록
    val resultLauncher:ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult(), object: ActivityResultCallback<ActivityResult>{
            override fun onActivityResult(result: ActivityResult?) {
                if(result?.resultCode== RESULT_CANCELED) return
                //로그인 객체를 가져온 Intent 객체 소환
                val intent:Intent?= result?.data
                //Intent 로부터 구글 계정 정보를 가져오는 작업 객체 생성 --> 결과데이터 받기
                val account:GoogleSignInAccount= GoogleSignIn.getSignedInAccountFromIntent(intent).result
                var id:String= account.id.toString()
                var email:String= account.email ?: ""   //가져온 email 값이 null 값이면 email=""

//                Toast.makeText(this@LoginActivity, "$email", Toast.LENGTH_SHORT).show()
                G.userAccount= UserAccount(id, email)

                //작업이 끝났으니 Main 화면으로 이동~
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
            }
        })


    private fun clickLoginNaver(){
        //네이버 아이디 로그인 [네아로] : 사용자 정보를 REST API 로 받아오는 방식! - 네이버개발자센터의 가이드문서 참고
        //Retrofit 네트워크 라이브러리 사용할 것

        //SDK 추가 후 로그인 초기화
        NaverIdLoginSDK.initialize(this,"sQKPFalquBqTuoG8FVlx","rpEDdsWWXS","TPExample01Grinleaf")

        //로그인 인증하기 : 로그인 정보를 받는 것이 아닌, 로그인 정보를 받기위한 REST API 에 접근하는 '키'를 받는 것! (== 토큰)
        //토큰으로 네트워크 API 를 통해 json 형식의 정보 데이터를 받아옴
        NaverIdLoginSDK.authenticate(this, object: OAuthLoginCallback{
            override fun onError(errorCode: Int, message: String) {
                //네이버 서버에 문제있을 경우
                Toast.makeText(this@LoginActivity, "server error : $message", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(httpStatus: Int, message: String) {
                Toast.makeText(this@LoginActivity, "로그인 실패 : $message", Toast.LENGTH_SHORT).show()
            }

            override fun onSuccess() {
                Toast.makeText(this@LoginActivity, "로그인 성공", Toast.LENGTH_SHORT).show()

                //사용자 정보를 가져올 REST API 의 접속토큰(Access Token) 받아오기
                val accessToken:String? = NaverIdLoginSDK.getAccessToken()
//                Toast.makeText(this@LoginActivity, "token : $accessToken", Toast.LENGTH_SHORT).show()

                //06.09 - 사용자 정보 가져오는 네트워크 작업 수행(Retrofit library 이용)
                val retrofit= RetrofitHelper.getRetrofitInstance("https://openapi.naver.com/")
                retrofit.create(RetrofitApiService::class.java).getNidUserInfo("Bearer $accessToken").enqueue(object: Callback<NaverUserInfoResponse>{
                    override fun onResponse(
                        call: Call<NaverUserInfoResponse>,
                        response: Response<NaverUserInfoResponse>
                    ) {
                        val userInfo:NaverUserInfoResponse?= response.body()
                        val id:String= userInfo?.response?.id ?: ""
                        val email:String= userInfo?.response?.email ?: ""

                        Toast.makeText(this@LoginActivity, "$email", Toast.LENGTH_SHORT).show()
                        G.userAccount= UserAccount(id,email)

                        //main 화면으로 이동
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    }
                    override fun onFailure(call: Call<NaverUserInfoResponse>, t: Throwable) {
                        Toast.makeText(this@LoginActivity, "회원정보 로드 실패", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        })
    }
}
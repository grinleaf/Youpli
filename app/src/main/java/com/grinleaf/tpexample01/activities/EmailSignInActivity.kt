package com.grinleaf.tpexample01.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore
import com.grinleaf.tpexample01.G
import com.grinleaf.tpexample01.R
import com.grinleaf.tpexample01.databinding.ActivityEmailSignInBinding
import com.grinleaf.tpexample01.model.UserAccount

class EmailSignInActivity : AppCompatActivity() {
    val binding by lazy { ActivityEmailSignInBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //툴바를 제목줄로 설정
        setSupportActionBar(binding.toolbar)

        supportActionBar?.setDisplayShowTitleEnabled(false)  //액션바 기본타이틀 표시 false
        supportActionBar?.setDisplayHomeAsUpEnabled(true)    //홈버튼 위치에 있는 버튼을 Up 버튼으로 사용 true
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)    //홈버튼(현재 Up 버튼) 이미지 설정

        binding.btnSignin.setOnClickListener { clickSignIn() }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    private fun clickSignIn(){
        var email:String= binding.etEmail.text.toString()
        var password:String= binding.etPassword.text.toString()
        
        //Firebase Firestore DB 에서 이메일 로그인 확인 ( email, password 값 검증 )
        val db:FirebaseFirestore= FirebaseFirestore.getInstance()
        db.collection("emailUsers")
            .whereEqualTo("email",email)    //첫번째 파라미터가 필드의 식별자값. 쿼리문의 where 키워드 역할임~ 입력값과 같은 값이 있는지 검증
            .whereEqualTo("password",password)
            .get().addOnSuccessListener {
                if(it.documents.size>0){
                    //1) 조건에 맞는 snapshot 을 하나 이상 찾았을 경우
                    //firestore DB 에서 랜덤으로 부여된 document 명을 id(계정 식별자)로 사용
                    var id:String= it.documents[0].id
                    G.userAccount= UserAccount(id,email)
                    val intent:Intent= Intent(this, MainActivity::class.java)
                    //★ 기존 task 내의 모든 Activity 를 종료하고 새로운 task 를 시작하기 (== 백스택 Activity 들 전부 종료) ★
                    //** 액티비티가 Activity Back Stack 메모리에 쌓이는 순간을 task 하나가 시작되었다고 지칭함
                    //** task 하나당 새로운 Activity Back Stack 공간을 가짐
                    //--> Intent.FLAG_ACTIVITY_NEW_TASK (새로운 task 생성)
                    //--> Intent.FLAG_ACTIVITY_REORDER_TO_FRONT (MainAc 가 stack 된 상태에서 SecondAc 에서 새로운 Intent 로 MainAc 으로 startActivity 했을 때, 기존에 스택되어 있던 MainAc 를 위로 올림)
                    //--> Intent.FLAG_ACTIVITY_SINGLE_TOP (MainAc 에서 다시 MainAc 을 또 stack 하는 상황일 경우, 새로운 MainAc 을 스택하지 않고, 하나의 MainAc 만 둠)
                    //*** 이름은 Activity Back Stack 이지만, 사실 실행되어 있는 화면 자체도 Back Stack 에 쌓여있는 상태임. MainActivity 만 실행되어 보여지고 있을 때도 백스택에 MainAc 이 쌓여있는 상태인 것. (이건 manifest 의 intent-filter 가 있는 Ac만 가능함)
                    //*** 참고로 Service(푸시알림 등)처럼 Activity 가 스택되지 않은 상태에서 pendingIntent 로 Activity 실행해야할 때, 스택에 안쌓여있으면 startAc가 안됨! --> 이 때 NEW_TASK 를 사용한다!
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }else{
                    //2) 조건에 맞는 snapshot 이 하나도 없을 경우 : 로그인 실패
                    AlertDialog.Builder(this).setMessage("이메일과 비밀번호를 다시 확인해주세요.").create().show()
                    binding.etEmail.requestFocus()  //입력 포커스를 email 박스로 보내기
                    binding.etEmail.selectAll()     //입력된 값 전체 블럭처리
                }
            }

    }
}
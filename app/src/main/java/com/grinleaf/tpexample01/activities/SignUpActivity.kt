package com.grinleaf.tpexample01.activities

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore
import com.grinleaf.tpexample01.R
import com.grinleaf.tpexample01.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {
    val binding by lazy { ActivitySignUpBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        //툴바를 액션바로 대체하기
        setSupportActionBar(binding.toolbar)
        //액션바에 제목글씨가 표시되는 상태가 됨 : setDisplayShowTitleEnabled 를 false 로 설정해야 제목글씨 표시 X
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)   //업버튼 표시 여부
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)    //업버튼으로 사용할 리소스 지정
        
        binding.btnSignup.setOnClickListener { clickSignup() }   //버튼 클릭시 Firebase 에 데이터 저장하기
    }

    //업버튼 클릭 이벤트
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    private fun clickSignup(){
        //Firebase FireStore DB 에 사용자 정보 저장하기
        var email:String= binding.etEmail.text.toString()
        var password:String= binding.etPassword.text.toString()
        var passwordConfirm= binding.etPasswordConfirm.text.toString()

        //유효성 검사 : 패스워드와 패스워드확인이 맞는지 검사
        //kotlin 에서는 문자열 비교 시 equals 쓰지 않고 == 사용하는 것을 권장함!
        if(password!=passwordConfirm){
            AlertDialog.Builder(this).setMessage("패스워드 확인에 문제가 있습니다. 다시 확인하여 입력해주시기 바랍니다.").create().show()
            binding.etPasswordConfirm.selectAll()   //커서 위치 지정(모든 글자 블럭 선택) / setSelection(index)하면 문자열 인덱스에 커서 놓는 선택도 가능
            return
        }

        //Firebase Firestore 관리 객체 얻어오기
        val db= FirebaseFirestore.getInstance()

        //이미 가입된 이메일일 경우 회원가입을 막아야함! : .whereXxxx() 사용
        //원래 SQL 쿼리문으로는 SELECT * from aa where email='aa@aa.com' 이겠지만, firestore 는 No-SQL!
        //(*참고로 NoSQL != SQL 문을 쓰지 않는 다는 뜻이 아님. Not Only SQL)
        db.collection("emailUsers")
            .whereEqualTo("email",email)    //emailUsers 컬렉션 안의 email 필드에 etText 로 받은 email 값이 있으면.. (여러 개의 결과 가져올 수 O)
            .get().addOnSuccessListener { 
                //같은 값을 가진 Document 가 여러 개일 수도 있으므로
                if(it.documents.size>0){    //snapshot 의 결과값이 있을 경우 (이미 firestore 에 해당 이메일이 있을 경우)
                    AlertDialog.Builder(this).setMessage("중복된 이메일이 있습니다. 다시 확인하여 입력해주시기 바랍니다.").show()  //create()는 Builder 가 자동으로 해줌
                    binding.etEmail.requestFocus()  //passwordConfirm et 박스에 있던 커서 포커스 가져오기
                    binding.etEmail.selectAll() //얘는 해당 et 박스에 focus 가 존재할 때만 정상적으로 사용되기 때문에, 윗줄 코드 적어서 미리 포커스를 가져온 후 호출해야함
                }else{
                    //저장할 값(이메일, 비밀번호)을 저장하기 위해 HashMap 으로 만들기
                    val user:MutableMap<String, String> = mutableMapOf()
                    user.put("email", email)  //MutableList 의 add 역할
                    user.put("password", password)

                    //collection 명은 "emailUsers"로 지정 (No-SQL 에서 RDBMS 의 테이블명과 같은 역할!)
                    db.collection("emailUsers").add(user).addOnSuccessListener { //.add(user) == .document().set(user) --> 둘다 document 값이 랜덤하게 부여됨
                        AlertDialog.Builder(this).setMessage("축하합니다.\n회원가입이 완료되었습니다.").setPositiveButton("확인",object:DialogInterface.OnClickListener{
                            override fun onClick(p0: DialogInterface?, p1: Int) {
                                finish()
                            }
                        }).create().show()
                    }.addOnFailureListener {
                        Toast.makeText(this, "회원가입에 오류가 발생했습니다.\n다시 시도해주시기 바랍니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }.addOnFailureListener {
                Toast.makeText(this, "서버상태가 불안정합니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
            }
    }
}
package com.grinleaf.tpexample01.model

data class NaverUserInfoResponse(var resultcode:String, var message:String, var response: NidUser)  //API 명세의 출력 결과대로 멤버변수 작성(이름 다르면 파싱 안 됨!)

data class NidUser(var email:String, var id:String) //받아서 사용할 사용자 정보만 멤버로 두자! (로그인 시 사용자한테 동의받은 항목)

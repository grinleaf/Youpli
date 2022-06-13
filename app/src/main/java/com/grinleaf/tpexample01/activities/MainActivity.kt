package com.grinleaf.tpexample01.activities

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.graphics.ColorFilter
import android.location.Location
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.view.Menu
import android.view.View
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.material.tabs.TabLayout
import com.grinleaf.tpexample01.R
import com.grinleaf.tpexample01.databinding.ActivityMainBinding
import com.grinleaf.tpexample01.fragments.PlaceListFragment
import com.grinleaf.tpexample01.fragments.PlaceMapFragment
import com.grinleaf.tpexample01.network.RetrofitApiService
import com.grinleaf.tpexample01.network.RetrofitHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    
    //1. 검색 장소 키워드
    var searchQuery:String= "화장실"   //앱 초기 검색어 - 내 주변 개방된 화장실

    //2. 현재 내 위치 정보를 가진 객체 (위도, 경도 정보를 멤버로 가짐)
    var myLocation: Location? = null
    //[ Google Fused Location API 사용 : play-services-location ] --> Location API 객체들 사용 시 android.gms 용으로 import 해야함 주의~
    val providerClient: FusedLocationProviderClient by lazy { LocationServices.getFusedLocationProviderClient(this) }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //툴바를 제목줄로 설정하기
        setSupportActionBar(binding.toolbar)

        //첫 실행될 프래그먼트를 동적으로 추가하기 : 첫 시작은 무조건 List 형식으로 붙도록
        supportFragmentManager.beginTransaction().add(R.id.container_fragment,PlaceListFragment()).commit()

        //탭 레이아의 탭 버튼 클릭했을 때, 화면에 보여줄 프래그먼트를 변경
        binding.layoutTab.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if(tab?.text == "LIST"){
                    supportFragmentManager.beginTransaction().replace(R.id.container_fragment,PlaceListFragment()).commit()
                }else if(tab?.text == "MAP"){
                    supportFragmentManager.beginTransaction().replace(R.id.container_fragment,PlaceMapFragment()).commit()
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) { }
            override fun onTabReselected(tab: TabLayout.Tab?) { }
        })

        //검색어 입력에 따라 장소 검색요청하기
        binding.etSearch.setOnEditorActionListener { textView, i, keyEvent ->
            searchQuery= binding.etSearch.text.toString()
            //카카오 장소검색 API 작업 요청
            searchPlace()

            false   //true 리턴 시 키패드 처리작업을 하지 않음 / false 리턴 시 설정한 키패드 작업을 실행함 + 센변환 시 return 키워드도 생략
        }

        //특정 키워드 단축 choice 버튼에 리스너 처리하는 함수 호출
        setChoiceButtonsListener()

        //내 위치 정보제공은 동적 퍼미션 필요
        val permissions:Array<String> = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)   //Fine 요청시 Coarse 는 자동으로 같이 됨
        if (checkSelfPermission(permissions[0])==PackageManager.PERMISSION_DENIED){
            requestPermissions(permissions, 10)
        }else{
            //내 위치탐색을 요청하는 기능 함수 호출
            requestMyLocation()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode==10 && grantResults[0]==PackageManager.PERMISSION_GRANTED) requestMyLocation()
        else Toast.makeText(this, "내 위치정보를 제공하지 않아 검색기능을 사용할 수 없습니다.", Toast.LENGTH_SHORT).show()
    }
    
    private fun requestMyLocation(){
        //* 내 위치 정보 얻어오기
        //위치 검색 기준 설정값 객체
        val request: LocationRequest= LocationRequest.create()
        request.interval= 1000  //1초마다 갱신
        //request.priority= LocationRequest.PRIORITY_HIGH_ACCURACY    //요거 Deprecate 되고 Priority 클래스가 따로 생김!
        request.priority= Priority.PRIORITY_HIGH_ACCURACY

        //providerClient.lastLocation  //요거는 마지막으로 인식된 location. 실시간 갱신x 이므로 요거 말고 실시간 위치정보 갱신 요청 권장
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        providerClient.requestLocationUpdates(request,locationCallback, Looper.getMainLooper())
        //첫번째 파라미터 : 특정 시간마다 위치를 갱신하는 변수 설정
        //두번째 파라미터 : callback 객체를 전역에서 만들어 쓰자!
        //세번째 파라미터 : 별도 스레드로 작업하기 때문에 메인 스레드의 루퍼 클래스 필요 --> 작성 다하면 윗쪽에 퍼미션 요청문 자동완성 빨간전구 뜸 ㅇ_ㅇ!
    }
    
    //위치정보 검색결과 콜백 객체
    private val locationCallback: LocationCallback= object: LocationCallback(){
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)

            //갱신된 위치정보결과 객체에게 위치정보 얻어오기
            myLocation= p0.lastLocation
            
            //내 위치정보가 있으므로, 카카오 검색을 시작하기
            searchPlace()

            //검색 후 내 위치 정보 업데이트를 정지하기
            providerClient.removeLocationUpdates(this)  //this == callback 객체 (본인 지칭 키워드)
        }
    }

    //옵션메뉴가 만들어질 때 실행되는 메소드
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.actionbar_main,menu)
        return super.onCreateOptionsMenu(menu)
    }

    //카카오 키워드 장소검색 API 작업을 수행하는 기능 메소드
    private fun searchPlace(){
        Toast.makeText(this, "$searchQuery : ${myLocation?.latitude},${myLocation?.longitude}", Toast.LENGTH_SHORT).show()

        //Retrofit 을 이용하여 카카오 키워드 장소검색 API 파싱하기
        val retrofit: Retrofit = RetrofitHelper.getRetrofitInstance("https://dapi.kakao.com")
        retrofit.create(RetrofitApiService::class.java).searchPlacesToString(searchQuery,myLocation?.latitude.toString(), myLocation?.longitude.toString())
            .enqueue(object: Callback<String>{
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    var s= response.body()
                    AlertDialog.Builder(this@MainActivity).setMessage(s.toString()).create().show()
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Toast.makeText(this@MainActivity, "서버 오류가 있습니다.\n잠시 후 다시 시도해 주세요.", Toast.LENGTH_SHORT).show()
                }

            })
    }

    private fun setChoiceButtonsListener(){

        binding.layoutChoice.choiceWc.setOnClickListener { clickChoice(it) }    //it == 클릭된 View
        binding.layoutChoice.choiceGas.setOnClickListener { clickChoice(it) }
        binding.layoutChoice.choiceDining.setOnClickListener { clickChoice(it) }
        binding.layoutChoice.choiceEv.setOnClickListener { clickChoice(it) }
        binding.layoutChoice.choiceMovie.setOnClickListener { clickChoice(it) }
        binding.layoutChoice.choicePark.setOnClickListener { clickChoice(it) }
        binding.layoutChoice.choiceWc2.setOnClickListener { clickChoice(it) }
        binding.layoutChoice.choiceDining2.setOnClickListener { clickChoice(it) }
        binding.layoutChoice.choiceEv2.setOnClickListener { clickChoice(it) }
        binding.layoutChoice.choiceGas2.setOnClickListener { clickChoice(it) }
        binding.layoutChoice.choiceMovie2.setOnClickListener { clickChoice(it) }
        binding.layoutChoice.choicePark2.setOnClickListener { clickChoice(it) }
    }

    //멤버변수로 기존 선택된 뷰의 id 를 저장하는 변수 두기
    var choiceID= R.id.choice_wc    //초기값

    private fun clickChoice(view: View){

        //기존에 선택되었던 뷰의 배경 이미지를 원래 상태로 돌리기
        findViewById<ImageView>(choiceID).setBackgroundResource(R.drawable.bg_choice)
        findViewById<ImageView>(choiceID).setColorFilter(R.color.main_color)

        //현재 선택된 뷰의 배경 이미지를 변경하기
        view.setBackgroundResource(R.drawable.bg_choice_selected)
//        findViewById<ImageView>(view.id).setColorFilter(R.color.white)
        findViewById<ImageView>(view.id).setColorFilter(ContextCompat.getColor(this,R.color.white))

        //현재 선택한 뷰의 id 를 멤버변수에 저장하기
        choiceID= view.id

        //초이스할 버튼에 따라 검색장소 키워드를 변경하여 다시 검색을 요청해야함!
        when(view.id){
            R.id.choice_wc -> searchQuery= "화장실"
            R.id.choice_gas -> searchQuery= "주유소"
            R.id.choice_dining -> searchQuery= "식당"
            R.id.choice_ev -> searchQuery= "전기차충전소"
            R.id.choice_movie -> searchQuery= "영화관"
            R.id.choice_park -> searchQuery= "공원"
            R.id.choice_wc2 -> searchQuery= "화장실2"
            R.id.choice_dining2 -> searchQuery= "식당2"
            R.id.choice_ev2 -> searchQuery= "전기차충전소2"
            R.id.choice_gas2 -> searchQuery= "주유소2"
            R.id.choice_movie2 -> searchQuery= "영화관2"
            R.id.choice_park2 -> searchQuery= "공원2"
        }
        searchPlace()

        //새로운 검색 요청
        binding.etSearch.text.clear()
        binding.etSearch.clearFocus()   //윗줄 코드 실행하면 포커스가 검색창으로 이동되므로, 포커스 지워주기
    }
}
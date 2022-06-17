package com.grinleaf.tpexample01.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.grinleaf.tpexample01.R
import com.grinleaf.tpexample01.activities.MainActivity
import com.grinleaf.tpexample01.activities.PlaceUrlActivity
import com.grinleaf.tpexample01.databinding.FragmentPlaceListBinding
import com.grinleaf.tpexample01.databinding.FragmentPlaceMapBinding
import com.grinleaf.tpexample01.model.Place
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView

class PlaceMapFragment:Fragment() {
    val binding by lazy { FragmentPlaceMapBinding.inflate(layoutInflater) }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }

    val mapView: MapView by lazy { MapView(context) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //MapView container 에 MapView 객체 추가
        binding.containerMapview.addView(mapView)

        //실제 디바이스에서 테스트 가능. 애뮬레이터에서 지도 안 뜸
        //지도 관련 설정 (지도위치, 마커 추가 등)
        setMapAndMarkers()
    }

    private fun setMapAndMarkers(){
        //★★ 마커 클릭 이벤트에 반응하는 리스너 등록 : 반드시 마커 추가하기 이전에 등록되어있어야만 동작이 됨!! ★★
        mapView.setPOIItemEventListener( markerEventListener )

        //지도 중심좌표 설정
        //현재 내 위치로 이동 : MainActivity 가 이미 내 위치 값을 가지고 있음! MainActivity 의 멤버변수 사용하는 방법 (액티비티 호출+형변환) ★★
        var lat: Double= (activity as MainActivity).myLocation?.latitude ?: 37.5666805  //맵뷰에 null 값을 줄 수 없으므로, 기본값 설정
        var lng: Double= (activity as MainActivity).myLocation?.longitude ?: 126.9784147
        var myMapPoint: MapPoint= MapPoint.mapPointWithGeoCoord(lat,lng)    //내 위도와 경도를 카카오 지도에서 사용할 수 있는 맵 좌표 객체로 만듦
        mapView.setMapCenterPointAndZoomLevel(myMapPoint,3,true)
        mapView.zoomIn(true)
        mapView.zoomOut(true)

        //내 위치 마커 추가하기
        val marker= MapPOIItem()    //카카오 맵 마커 객체

        //마커 설정들 : 객체가 여러 개일 때 헷갈리지 않도록 '스코프 함수' 사용! : .apply{ }
        marker.apply {
            //this 생략이 가능함!
            itemName= "ME"
            mapPoint= myMapPoint
            markerType= MapPOIItem.MarkerType.BluePin           //기본 마커 모양
            selectedMarkerType= MapPOIItem.MarkerType.YellowPin //선택된 마커 모양
        }
        mapView.addPOIItem(marker)

        //검색결과 장소들 마커 추가
        val documents:MutableList<Place>? = (activity as MainActivity).searchPlaceResponse?.documents
        documents?.forEach {
            //배열 크기만큼 반복문을 돌리고자 할 때, + 해당 배열이 가진 객체들이 nullable 일 때 기존 for 문을 돌리지 않고 forEach 를 사용함
            //세이프티 연산자는 null 값이면 해당 코드를 무시하는 특징이 있음!
            val point: MapPoint = MapPoint.mapPointWithGeoCoord(it.y.toDouble(), it.x.toDouble())
            //마커객체 생성
            var marker: MapPOIItem= MapPOIItem().apply { //apply 시 리턴은 apply 실행결과를 적용한 MapPOIItem()의 결과값임
                itemName= it.place_name
                mapPoint= point
                markerType= MapPOIItem.MarkerType.RedPin
                selectedMarkerType= MapPOIItem.MarkerType.YellowPin
                
                //★ 해당 마커마다 관련된 place 객체 정보를 저장하는 주머니 ★ --> 아래 말풍선 클릭 리스너에서 사용하기 위함
                userObject= it
            }
            mapView.addPOIItem(marker)
        }
    }

    //마커나 말풍선이 클릭되는 이벤트에 반응하는 리스너 객체를 멤버로 만들기
    private val markerEventListener:MapView.POIItemEventListener= object: MapView.POIItemEventListener{
        //마커 클릭 시 발동하는 메소드
        override fun onPOIItemSelected(p0: MapView?, p1: MapPOIItem?) {
            //p1 : 해당 마커 객체

        }

        //마커에 달린 말풍선 클릭 시 발동하는 메소드
        override fun onCalloutBalloonOfPOIItemTouched(p0: MapView?, p1: MapPOIItem?) {
            //세번째 메소드에 의해 오버로딩됨 (이전버전. deprecated 됨!)
        }
        override fun onCalloutBalloonOfPOIItemTouched(
            p0: MapView?,
            p1: MapPOIItem?,
            p2: MapPOIItem.CalloutBalloonButtonType?
        ) {
            //두번째 메소드를 오버로딩한 메소드 (최신버전)
            if(p1?.userObject==null) return
            val place: Place= p1?.userObject as Place   //userObject 타입이 Any 이므로, 형변환해주기
            
            //말풍선 클릭 시 말풍선에 적힌 장소의 상세정보 화면으로 전환하기
//            val intent= Intent(context, PlaceUrlActivity::class.java)
//            intent.putExtra("place_url", place.place_url)
//            startActivity(intent)
        }

        override fun onDraggablePOIItemMoved(p0: MapView?, p1: MapPOIItem?, p2: MapPoint?) {
            //마커를 드래그하여 움직였을 때 발동하는 메소드

        }

    }

}
package com.grinleaf.tpexample01.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.grinleaf.tpexample01.R
import com.grinleaf.tpexample01.activities.MainActivity
import com.grinleaf.tpexample01.adapters.PlaceListRecyclerAdapter
import com.grinleaf.tpexample01.databinding.FragmentPlaceListBinding

class PlaceListFragment:Fragment() {
    val binding by lazy { FragmentPlaceListBinding.inflate(layoutInflater) }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setPlaceListRecyclerAdapter()
    }

    private fun setPlaceListRecyclerAdapter(){
        //MainActivity 에 만들어둔 멤버변수 가져오기
        val ma:MainActivity= activity as MainActivity

        //아직 MainActivity 의 파싱작업이 완료되지 않았을 수 있음! (데이터가 null 상태)
        if(ma.searchPlaceResponse==null) return
        //adapter 의 멤버변수에 documents 가 nullable 변수가 아니므로 + 위의 if 문을 통해 null 이 아님을 검증했으므로! 이럴때는 !! 사용 가능
        binding.recyclerview.adapter= PlaceListRecyclerAdapter(requireContext(),ma.searchPlaceResponse!!.documents)


    }
}
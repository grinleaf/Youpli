package com.grinleaf.tpexample01.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.grinleaf.tpexample01.R
import com.grinleaf.tpexample01.databinding.FragmentPlaceListBinding
import com.grinleaf.tpexample01.databinding.FragmentPlaceMapBinding

class PlaceMapFragment:Fragment() {
    val binding by lazy { FragmentPlaceMapBinding.inflate(layoutInflater) }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

}
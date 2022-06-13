package com.grinleaf.tpexample01.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.grinleaf.tpexample01.R
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
}
package com.nolamarel.onlinelibrary.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.nolamarel.onlinelibrary.databinding.FragmentAboutAppBinding

class AboutAppFragment : Fragment() {
    private var binding: FragmentAboutAppBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAboutAppBinding.inflate(inflater, container, false)

        binding!!.arrowBack.setOnClickListener { parentFragmentManager.popBackStack() }

        return binding!!.root
    }
}
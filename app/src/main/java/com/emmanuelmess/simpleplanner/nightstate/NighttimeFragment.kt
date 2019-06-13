package com.emmanuelmess.simpleplanner.nightstate

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.emmanuelmess.simpleplanner.R

class NighttimeFragment : Fragment() {
    companion object {
        val TAG = "nightime_fragment"

        @JvmStatic
        fun newInstance() = NighttimeFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_nighttime, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
    }
}

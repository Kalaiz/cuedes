package com.kalai.cuedes.entry

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.leanback.app.OnboardingSupportFragment
import com.kalai.cuedes.R
import com.kalai.cuedes.databinding.ViewBackgroundOnboardBinding
import com.kalai.cuedes.databinding.ViewOnboardBinding
import com.kalai.cuedes.hide
import com.kalai.cuedes.show

class OnBoardFragment :OnboardingSupportFragment()  {

    private lateinit var contentBinding: ViewOnboardBinding
    private lateinit var backgroundBinding:ViewBackgroundOnboardBinding

    private lateinit var titles:Array<String>
    private lateinit var descriptions:Array<String>

    override fun getPageCount(): Int = titles.size

    override fun getPageTitle(pageIndex: Int): CharSequence = titles[pageIndex]

    override fun getPageDescription(pageIndex: Int): CharSequence = descriptions[pageIndex]

    override fun onCreateBackgroundView(inflater: LayoutInflater?, container: ViewGroup?): View? {
        if(inflater!=null){
            backgroundBinding =  ViewBackgroundOnboardBinding.inflate(inflater,container,false)
            backgroundBinding.backImageView.setOnClickListener { moveToPreviousPage() }
        }
        return if(this::backgroundBinding.isInitialized) backgroundBinding.root else null
    }



    override fun onPageChanged(newPage: Int, previousPage: Int) {
        super.onPageChanged(newPage, previousPage)
        with(backgroundBinding.backImageView) {
            if(previousPage < newPage && visibility==View.INVISIBLE){
                show()
            }
            else if(newPage==0){
                hide()
                val view = rootView?.findViewById<View>(androidx.leanback.R.id.page_indicator)

            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        titles = inflater.context?.resources?.getStringArray(R.array.onboard_titles)?: arrayOf()
        descriptions = inflater.context?.resources?.getStringArray(R.array.onboard_description) ?: arrayOf()
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreateContentView(inflater: LayoutInflater?, container: ViewGroup?): View? {
        if(inflater!=null){
            contentBinding =  ViewOnboardBinding.inflate(inflater,container,false)
        }
        return if(this::contentBinding.isInitialized) contentBinding.root else null
    }

    override fun onCreateForegroundView(inflater: LayoutInflater?, container: ViewGroup?): View? {
        return null
    }


    override fun onProvideTheme(): Int = R.style.ThemeOverlay_OnBoardFragment
}


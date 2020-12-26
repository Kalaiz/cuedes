package com.kalai.cuedes.entry.onboard


import android.animation.Animator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesKey
import androidx.datastore.preferences.createDataStore
import androidx.fragment.app.activityViewModels
import androidx.leanback.app.OnboardingSupportFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.kalai.cuedes.*
import com.kalai.cuedes.databinding.ViewBackgroundOnboardBinding
import com.kalai.cuedes.databinding.ViewPagerOnboardBinding
import com.kalai.cuedes.entry.onboard.OnBoardPagerAdapter.Companion.PAGE
import com.kalai.cuedes.entry.onboard.PageContent.*
import kotlinx.coroutines.launch

/*TODO need to follow MVVM */
class OnBoardFragment :OnboardingSupportFragment()  {

    companion object{ private const val TAG = "OnBoardFragment" }

    private lateinit var contentBinding: ViewPagerOnboardBinding
    private lateinit var backgroundBinding:ViewBackgroundOnboardBinding
    private lateinit var titles:Array<String>
    private lateinit var viewPager: ViewPager2
    private lateinit var onBoardPagerAdapter: OnBoardPagerAdapter
    private lateinit var descriptions:Array<String>
    private lateinit var pageNavigatorView: View
    private lateinit var getStartedButton: View
    private lateinit var viewPagerAnimator: Animator

    private val onBoardViewModel: OnBoardViewModel by activityViewModels()

    override fun getPageCount(): Int = titles.size

    override fun getPageTitle(pageIndex: Int): CharSequence = titles[pageIndex]

    override fun getPageDescription(pageIndex: Int): CharSequence = descriptions[pageIndex]

    override fun onProvideTheme(): Int = R.style.ThemeOverlay_OnBoardFragment

    override fun onCreateBackgroundView(inflater: LayoutInflater?, container: ViewGroup?): View? {
        if(inflater!=null){
            backgroundBinding =  ViewBackgroundOnboardBinding.inflate(inflater, container, false)

            backgroundBinding.backImageView.setOnClickListener {

                if(!viewPagerAnimator.isRunning){
                    Log.d(TAG,"Not Dragging ,backImageView Selected")
                    moveToPreviousPage()
                }
                else{

                    moveToPreviousPage()
                    viewPagerAnimator.end()
                }
            }
        }
        return if(this::backgroundBinding.isInitialized) backgroundBinding.root else null
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(TAG,"onCreateView")
        titles = inflater.context?.resources?.getStringArray(R.array.onboard_titles)?: arrayOf()
        descriptions = inflater.context?.resources?.getStringArray(R.array.onboard_description) ?: arrayOf()
        return super.onCreateView(inflater, container, savedInstanceState)
    }


    override fun onCreateContentView(inflater: LayoutInflater, container: ViewGroup?): View? {
        Log.d(TAG,"onCreateContentView")

        contentBinding =  ViewPagerOnboardBinding.inflate(inflater, container, false).apply {
            viewPager = onboardViewPager }

        activity?.let { onBoardPagerAdapter = OnBoardPagerAdapter(it,numOfTabs = pageCount) }
        viewPager.adapter = onBoardPagerAdapter

        /*Disabling Swipes*/
        viewPager.isUserInputEnabled = false

        viewPager.setPageTransformer(fadeInFadeOutViewPagerTransformation)


        return if(this::contentBinding.isInitialized) contentBinding.root else null
    }


    override fun onCreateForegroundView(inflater: LayoutInflater?, container: ViewGroup?): View? {
        Log.d(TAG,"onCreateForegroundView")
        return null
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG,"onViewCreated")
        with(view.rootView){
            pageNavigatorView = findViewById<View>(androidx.leanback.R.id.page_indicator)
            getStartedButton =  findViewById<View>(androidx.leanback.R.id.button_start) }

        getStartedButton.setOnClickListener {
            lifecycleScope.launch { setOnBoardCompleted() }
            startMainActivity() }

        onBoardViewModel.isPageNavigationViewable.observe(viewLifecycleOwner, Observer { updatedMap ->
            PAGE[currentPageIndex]
            updatedMap?.run {
                Log.d(TAG, PAGE[currentPageIndex].toString())
                Log.d(TAG, toString())
                if (containsKey(PAGE[currentPageIndex])) {
                    if (get(PAGE[currentPageIndex]) == true) {
                        pageNavigatorView.show()
                    } else {
                        pageNavigatorView.hide()
                    }
                }
            }
        })
    }


    private fun startMainActivity(){
        val intent = Intent(activity, MainActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }


    override fun onResume() {
        super.onResume()
        Log.d(TAG,"onResume")
    }


    override fun onPageChanged(newPage: Int, previousPage: Int) {
        super.onPageChanged(newPage, previousPage)
        Log.d(TAG, "newpage $newPage currentPage$previousPage " )
        if(this::viewPagerAnimator.isInitialized && viewPagerAnimator.isRunning){
            viewPagerAnimator.end()}
            viewPagerAnimator =  viewPager.setCurrentItem(newPage,725)

        viewPagerAnimator.doOnStart {
            pageNavigatorView.setOnClickListener {
                Log.d(TAG,"While animating, page Nav Selected")
                viewPagerAnimator.doOnEnd { moveToNextPage() }
            }
        }
        if(newPage == 0){ backgroundBinding.backImageView.hide() }
        else{ backgroundBinding.backImageView.show() }

        val isPageNavigatorNeeded = onBoardViewModel.isPageNavigationViewable.value?.get(PAGE[newPage])

        if(isPageNavigatorNeeded == true || isPageNavigatorNeeded == null){// null for pages like intro
            pageNavigatorView.show() }
        else{ pageNavigatorView.hide() }

        if(PAGE[newPage+1] == GOOGLE_LOCATION_SERVICE_PERMISSION){
            viewPager.offscreenPageLimit = 1
        }
    }


    private suspend fun setOnBoardCompleted(){
        val dataStore = context?.createDataStore("settings")
        val isOnBoardingKey = preferencesKey<Boolean>("isOnBoard")
        dataStore?.edit { settings->
            settings[isOnBoardingKey]= false
        }
    }

}


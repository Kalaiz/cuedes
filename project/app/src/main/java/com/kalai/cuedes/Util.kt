package com.kalai.cuedes

import android.animation.Animator
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.app.ActivityCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import kotlin.math.abs

val TAG = "Util"
fun View.hide() {
    if (visibility == View.VISIBLE || isEnabled) {
        visibility = View.INVISIBLE
        isEnabled = false
    }
}

fun View.show(){
    if(visibility==View.INVISIBLE || !isEnabled){
        visibility= View.VISIBLE
        isEnabled = true
    }
}

fun Context?.isDevicePermissionGranted(permissionCodes: Array<String>):Boolean = if(this!=null)(permissionCodes.fold(true,
    { acc, it ->
        acc && ActivityCompat.checkSelfPermission(
            this,
            it
        ) == PackageManager.PERMISSION_GRANTED
    })) else false

fun GoogleApiAvailability.isGooglePlayServicesCompatible(context: Context?) = if (context!=null) isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS else false



val fadeInFadeOutViewPagerTransformation =
    ViewPager2.PageTransformer { page, position ->
        if(position <-1 || position > 1){
            page.alpha = 0f }
        else{ page.alpha = 1.0f - abs(position) * 2.5f} }

/*https://stackoverflow.com/a/59235979/11200630*/
fun ViewPager2.setCurrentItem(
    item: Int,
    duration: Long,
    interpolator: TimeInterpolator = AccelerateDecelerateInterpolator(),
    pagePxWidth: Int = width
) :Animator{
    val pxToDrag: Int = pagePxWidth * (item - currentItem)
    val animator = ValueAnimator.ofInt(0, pxToDrag)
    var previousValue = 0
    animator.addUpdateListener { valueAnimator ->
        val currentValue = valueAnimator.animatedValue as Int
        val currentPxToDrag = (currentValue - previousValue).toFloat()
        fakeDragBy(-currentPxToDrag)
        previousValue = currentValue
    }
    animator.addListener(object : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator?) { beginFakeDrag() }
        override fun onAnimationEnd(animation: Animator?) { endFakeDrag() }
        override fun onAnimationCancel(animation: Animator?) { }
        override fun onAnimationRepeat(animation: Animator?) {  }
    })
    animator.interpolator = interpolator
    animator.duration = duration
    animator.start()
return  animator}



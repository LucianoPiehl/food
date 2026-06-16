package com.example.food.util

import android.view.View
import android.view.animation.AnimationUtils
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import com.example.food.R

fun View.playEntranceMotion(delayMillis: Long = 0L, offsetDp: Float = 28f) {
    val offsetPx = offsetDp * resources.displayMetrics.density
    alpha = 0f
    translationY = offsetPx
    scaleX = 0.985f
    scaleY = 0.985f

    animate()
        .alpha(1f)
        .translationY(0f)
        .scaleX(1f)
        .scaleY(1f)
        .setStartDelay(delayMillis)
        .setDuration(420L)
        .setInterpolator(FastOutSlowInInterpolator())
        .start()
}

fun RecyclerView.configureFeedMotion() {
    layoutAnimation = AnimationUtils.loadLayoutAnimation(context, R.anim.feed_layout_animation)
    itemAnimator = DefaultItemAnimator().apply {
        addDuration = 220L
        moveDuration = 180L
        changeDuration = 140L
        removeDuration = 160L
    }
}

fun RecyclerView.playFeedRefreshMotion() {
    post { scheduleLayoutAnimation() }
}

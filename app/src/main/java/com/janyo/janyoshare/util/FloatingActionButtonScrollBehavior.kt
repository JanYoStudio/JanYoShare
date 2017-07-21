package com.janyo.janyoshare.util

import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.util.AttributeSet
import android.view.View
import android.support.v4.view.ViewCompat
import android.support.v4.view.ViewPropertyAnimatorListener
import android.support.v4.view.animation.LinearOutSlowInInterpolator

class FloatingActionButtonScrollBehavior(context: Context,
										 attrs: AttributeSet) : FloatingActionButton.Behavior()
{
	private var mIsAnimatingOut = false
	private val SHOW_HIDE_ANIM_DURATION = 160L

	override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout,
									 child: FloatingActionButton, directTargetChild: View,
									 target: View, nestedScrollAxes: Int, type: Int): Boolean
	{
		// 确保是竖直判断的滚动
		return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL || super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, nestedScrollAxes, type)
	}

	override fun onNestedScroll(coordinatorLayout: CoordinatorLayout, child: FloatingActionButton,
								target: View, dxConsumed: Int, dyConsumed: Int,
								dxUnconsumed: Int, dyUnconsumed: Int, type: Int)
	{
		super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed,
				dxUnconsumed, dyUnconsumed, type)
		if (dyConsumed > 0 && !this.mIsAnimatingOut && child.visibility == View.VISIBLE)
		{
			animateOut(child)
		}
		else if (dyConsumed < 0 && child.visibility != View.VISIBLE)
		{
			animateIn(child)
		}
	}

	private fun animateOut(button: FloatingActionButton)
	{
		ViewCompat.animate(button)
				.scaleX(0f)
				.scaleY(0f)
				.withLayer()
				.setDuration(SHOW_HIDE_ANIM_DURATION)
				.setInterpolator(LinearOutSlowInInterpolator())
				.setListener(object : ViewPropertyAnimatorListener
				{
					override fun onAnimationStart(view: View)
					{
						mIsAnimatingOut = true
					}

					override fun onAnimationCancel(view: View)
					{
						mIsAnimatingOut = false
					}

					override fun onAnimationEnd(view: View)
					{
						mIsAnimatingOut = false
						view.visibility = View.INVISIBLE
					}
				})
				.start()
	}

	private fun animateIn(button: FloatingActionButton)
	{
		button.visibility = View.VISIBLE
		ViewCompat.animate(button)
				.scaleX(1f)
				.scaleY(1f)
				.withLayer()
				.setDuration(SHOW_HIDE_ANIM_DURATION)
				.setInterpolator(LinearOutSlowInInterpolator())
				.setListener(null)
				.start()
	}
}
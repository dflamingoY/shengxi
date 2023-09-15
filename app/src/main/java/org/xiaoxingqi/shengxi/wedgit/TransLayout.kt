package org.xiaoxingqi.shengxi.wedgit

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

/**
 * Created by yzm on 2018/4/20.
 */
class TransLayout : StatefulLayout, View.OnClickListener {

    private var mTouchEnable = true

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onSetState(state: State?) {
        when (state) {
            State.EMPTY -> mTouchEnable = true
            State.PROGRESS -> mTouchEnable = false
            State.OFFLINE -> {
                mTouchEnable = true
                this.setOnClickListener(this)
//                getOfflineLayout().findViewById(R.id.text_retry).setOnClickListener(this)
            }
            else -> mTouchEnable = true
        }
        this.setOnClickListener(this)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return try {
            if (mTouchEnable)
                super.dispatchTouchEvent(ev)
            else
                true
        } catch (e: Exception) {
            true
        }
    }

    override fun onClick(v: View?) {


    }
}
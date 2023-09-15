package org.xiaoxingqi.shengxi.impl

import com.tencent.tauth.IUiListener
import com.tencent.tauth.UiError
import org.json.JSONObject

open class QQLoginUI : IUiListener {
    override fun onComplete(p0: Any?) {
        paserObj(p0 as JSONObject)
    }

    protected open fun paserObj(`object`: JSONObject) {

    }

    override fun onCancel() {

    }

    override fun onError(p0: UiError?) {
    }

}
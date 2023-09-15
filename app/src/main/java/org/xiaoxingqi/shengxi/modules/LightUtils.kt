package org.xiaoxingqi.shengxi.modules

import android.content.Context
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools
import org.xiaoxingqi.shengxi.utils.SPUtils
import kotlin.collections.HashSet

class LightUtils private constructor() {

    companion object {
        private val list = HashSet<String>()
        private var context: Context? = null
        private var login: LoginData.LoginBean? = null

        fun initArray(context: Context) {
            list.clear()
            login = PreferenceTools.getObj(context, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
            SPUtils.getString(context, IConstant.LIGHT_CACHE + login!!.user_id, "").let {
                it.split(",").filter { child ->
                    !child.isNullOrEmpty() && child != ","
                }.let { data ->
                    list.addAll(data)
                }
            }
        }

        fun addItem(key: String?) {
            try {
                key?.let {
                    if (list.add(key)) {
                        SPUtils.setString(context, IConstant.LIGHT_CACHE + login?.user_id, list.joinToString(","))
                    }
                }
            } catch (e: Exception) {
            }
        }

        /**
         * 是否包含该voiceId
         */
        fun contains(key: String): Boolean {
            return try {
                !list.contains(key)
            } catch (e: Exception) {
                false
            }
        }
    }

}
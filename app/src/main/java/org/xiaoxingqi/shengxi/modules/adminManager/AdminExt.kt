package org.xiaoxingqi.shengxi.modules.adminManager

import android.app.Activity
import okhttp3.FormBody
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.UserInfoData
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.SPUtils

//操作审核中心的数据类型 2 忽略  6：只替换元数据；
fun Activity.operator(id: String, type: Int, function: (BaseRepData?) -> Unit) {
    OkClientHelper.patch(this, "admin/suspiciousDatas/$id", FormBody.Builder()
            .add("token", SPUtils.getString(this, IConstant.ADMINTOKEN, ""))
            .add("operationType", "$type").build(), BaseRepData::class.java, object : OkResponse {
        override fun onFailure(any: Any?) {
            function(null)
        }

        override fun success(result: Any?) {
            result as BaseRepData
            function(result)
        }
    })
}

//仙人掌或者解封号操作
fun Activity.operatorUser(userId: String, formBody: FormBody, function: (BaseRepData?) -> Unit) {
    OkClientHelper.patch(this, "admin/users/$userId", formBody, BaseRepData::class.java, object : OkResponse {
        override fun success(result: Any?) {
            function(result as BaseRepData)
        }

        override fun onFailure(any: Any?) {
            function(null)
        }
    })
}

fun Activity.getUserInfo(id: String, function: (UserInfoData) -> Unit) {
    OkClientHelper.get(this, "users/$id", UserInfoData::class.java, object : OkResponse {
        override fun onFailure(any: Any?) {

        }

        override fun success(result: Any?) {
            result as UserInfoData
            function(result)
        }
    })
}

fun Activity.operatorVoice(url: String, formBody: FormBody, function: (BaseRepData?) -> Unit) {
    OkClientHelper.patch(this, url, formBody, BaseRepData::class.java, object : OkResponse {
        override fun onFailure(any: Any?) {
            function(null)
        }

        override fun success(result: Any?) {
            function(result as BaseRepData)
        }
    })
}

fun Activity.deleteVoice(url: String, formBody: FormBody, function: (BaseRepData?) -> Unit) {
    OkClientHelper.delete(this, url, formBody, BaseRepData::class.java, object : OkResponse {
        override fun onFailure(any: Any?) {
            function(null)
        }

        override fun success(result: Any?) {
            function(result as BaseRepData)
        }
    })
}

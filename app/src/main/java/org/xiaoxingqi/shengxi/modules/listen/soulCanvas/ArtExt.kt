package org.xiaoxingqi.shengxi.modules.listen.soulCanvas

import android.app.Activity
import okhttp3.FormBody
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.model.*
import java.util.*


/**
 * 添加收藏
 */
fun Activity.artAddCollection(bean: PaintData.PaintBean, function: (IntegerRespData?) -> Unit) {
    OkClientHelper.post(this, "artworkCollection", FormBody.Builder().add("artworkId", "${bean.id}").build(), IntegerRespData::class.java, object : OkResponse {
        override fun onFailure(any: Any?) {
            function(null)
        }

        override fun success(result: Any?) {
            function(result as IntegerRespData)
        }
    }, "V4.3")
}

//删除收藏
fun Activity.artDeleteCollection(bean: PaintData.PaintBean, function: (BaseRepData?) -> Unit) {
    OkClientHelper.delete(this, "artworkCollection/${bean.collection_id}", FormBody.Builder().build(), BaseRepData::class.java, object : OkResponse {
        override fun onFailure(any: Any?) {
            function(null)
        }

        override fun success(result: Any?) {
            function(result as BaseRepData)
        }
    }, "V4.3")
}

fun PaintData.PaintBean.checkUserInfo(userInfo: UserInfoData): PaintData.PaintBean {
    if (user == null) {
        if (user_id == userInfo.data.user_id) {
            isSelf = 1
            user = BaseUserBean().apply {
                this.avatar_url = userInfo.data.avatar_url
                this.nick_name = userInfo.data.nick_name
                this.identity_type = userInfo.data.identity_type
            }
        }
    }
    return this
}

//查询用户拉黑关系之类的
fun Activity.artRelation(uid: String, function: (RelationData?) -> Unit) {
    OkClientHelper.get(this, "relations/$uid", RelationData::class.java, object : OkResponse {
        override fun onFailure(any: Any?) {
            function(null)
        }

        override fun success(result: Any?) {
            result as RelationData
            if (result.code == 0) {
                ArtUserRelationDelegate.getInstance().plus(uid, result.data.friend_status)
                function(result)
            } else {
                function(null)
            }
        }
    })

}
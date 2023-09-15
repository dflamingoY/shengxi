package org.xiaoxingqi.shengxi.modules.listen.soulCanvas

import java.util.*

//用户关系代理
class ArtUserRelationDelegate private constructor() {
    ////用户关系，0=陌生人，1=待验证，2=已是好友，3：被对方拉黑，4：自己拉黑对方,5=自己
    private val relationMap: WeakHashMap<String, Int> = WeakHashMap<String, Int>()

    companion object {
        private var instances: ArtUserRelationDelegate? = null
        fun getInstance(): ArtUserRelationDelegate {
            if (instances == null) {
                instances = ArtUserRelationDelegate()
            }
            return instances!!
        }
    }

    /**
     * return -1 无数据, 0 表示不存在拉黑, 1表示存在拉黑或者被拉黑
     */
    fun getRelationById(uid: String): Int? {
        val result = relationMap[uid] ?: -1
        return if (result != -1) {
            if (result == 3 || result == 4) {
                1
            } else {
                0
            }
        } else {
            -1
        }
    }

    fun plus(uid: String, value: Int) {
        relationMap[uid] = value
    }

    fun close() {
        relationMap.clear()
    }
}




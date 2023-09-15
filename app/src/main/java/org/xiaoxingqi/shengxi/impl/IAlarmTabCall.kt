package org.xiaoxingqi.shengxi.impl

interface IAlarmTabCall : ITabAlarmClickCall {
    /**
     * @param isReverse 是否是非界面请求, false请求, true 不请求
     */
    fun itemTabSelected(page: Int, isReverse: Boolean, tabType: Int)

}
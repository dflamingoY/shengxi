package org.xiaoxingqi.shengxi.impl

interface OnAlarmItemClickListener {
    /**
     * @param type 0:all  1:求交配 2:freestyle
     */
    fun itemClick(type: Int)
}
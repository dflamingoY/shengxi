package org.xiaoxingqi.shengxi.impl

data class SendMsgEvent(var msg: String, var isCircle: Boolean = true)
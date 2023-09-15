package org.xiaoxingqi.shengxi.impl.event

data class AdminReportEvent(val id: String, val hideAt: Int = 0, val status: Int = 1)
package org.xiaoxingqi.shengxi.utils.download

import android.os.Environment
import okhttp3.Request
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.impl.DownProxyListener
import org.xiaoxingqi.shengxi.model.BaseAnimBean
import org.xiaoxingqi.shengxi.utils.AppTools
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.LocalLogUtils
import java.io.File
import java.io.FileOutputStream
import kotlin.collections.ArrayList

/**
 * 下载音频管理
 * 1.音频下载,
 * 2.维护下载池,点击加入pool,不允许重复添加;
 * 3.在pool真该下载中,再次点击取消该下载回调UI
 * 4.栈顶的下载都要回调UI,压栈之后上次的回调取消,回调和下载并发时,不做处理
 * 5.刷新列表之后回调UI 不处理
 *
 *
 * 使用OKHttp同步回调下载,线程池维护,最大5条下载记录
 *
 */
class DownVoiceProxy private constructor() {

    companion object {
        @JvmStatic
        val downProxy: DownVoiceProxy by lazy {
            DownVoiceProxy()
        }
    }

    val array by lazy { ArrayList<BaseAnimBean>() }

    private fun getExist(bean: BaseAnimBean): BaseAnimBean? {
        if (array.size > 0) {
            array.forEach {
                if (it.voicePath == bean.voicePath) {
                    return it
                }
            }
        }
        return null
    }

    fun addPool(bean: BaseAnimBean, downProxyListener: DownProxyListener) {
        var result: BaseAnimBean? = null
        when {
            array.contains(bean) -> bean.isHeapTop = false
            getExist(bean)?.apply { result = this } != null -> {
                result?.isHeapTop = false
            }
            else -> {
                array.forEach { it.isHeapTop = false }
                bean.isHeapTop = true
                array.add(bean)//添加到顶部
                Thread(DownThread(bean, downProxyListener)).start()
            }
        }
    }

    /**
     * 清除所有栈顶焦点
     */
    fun clearFocus() {
        array.forEach {
            it.isHeapTop = false
        }
    }

    private inner class DownThread(val bean: BaseAnimBean, val downProxyListener: DownProxyListener) : Runnable {

        override fun run() {
            //开始下载
            val request = Request.Builder()
                    .url(bean.voicePath)
                    .build()
            val call = OkClientHelper.getClient().newCall(request)
            try {
                val execute = call.execute()//同步请求
                if (execute.code() == 200) {
                    //写入本地数据
                    val bootFile = File(Environment.getExternalStorageDirectory(),
                            IConstant.DOCNAME + "/" + IConstant.CACHE_NAME + "/" + IConstant.VOICENAME)
                    if (!bootFile.exists()) {
                        bootFile.mkdirs()
                    }
                    FileOutputStream(File(bootFile, AppTools.getSuffix(bean.voicePath)), false).apply {
                        write(execute.body()!!.bytes())
                        close()
                    }
                    if (bean.isHeapTop) {//栈顶回调UI
                        downProxyListener.success(bootFile.absolutePath + "/${AppTools.getSuffix(bean.voicePath)}")
                    }
                    //本地下载完成 移除此条记录
                } else {
                    if (bean.isHeapTop) {
                        downProxyListener.fail("资源未找到 ")
                    }
                }
                array.remove(bean)
            } catch (e: Exception) {
                e.printStackTrace()
                //fail 前台统一弹网络不佳
                LocalLogUtils.writeLog("DownVoiceProxy : $e : " + e.message, System.currentTimeMillis())
                if (bean.isHeapTop) {
                    downProxyListener.fail("语音正在加载中，请耐心等一下哦~")
                }
                array.remove(bean)
            }
        }
    }
}
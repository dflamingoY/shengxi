package org.xiaoxingqi.shengxi.modules.adminManager

import android.os.Environment
import android.text.TextUtils
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.frag_sensitive.*
import kotlinx.android.synthetic.main.frag_sensitive.view.*
import okhttp3.FormBody
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseFragment
import org.xiaoxingqi.shengxi.core.http.AliLoadFactory
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.impl.ITabClickCall
import org.xiaoxingqi.shengxi.impl.LoadStateListener
import org.xiaoxingqi.shengxi.model.IntegerRespData
import org.xiaoxingqi.shengxi.model.QiniuStringData
import org.xiaoxingqi.shengxi.model.qiniu.UploadData
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.LocalLogUtils
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter

/**
 * 敏感词管理
 */
class SensitiveFragment : BaseFragment(), ITabClickCall {
    private val mData by lazy { ArrayList<String>() }
    private var longClickTag: String? = null
    private var longClickPosition = -1
    override fun tabClick(isVisible: Boolean) {

    }

    override fun doubleClickRefresh() {

    }

    override fun getLayoutId(): Int {
        return R.layout.frag_sensitive
    }

    override fun initView(view: View?) {
        registerForContextMenu(mView!!.flow_tables)
    }

    override fun initData() {
        request(0)
    }

    override fun initEvent() {
        mView!!.tvSave.setOnClickListener {
            //解析数据  本地json格式
            transLayout.showProgress()
            saveSensitive()
        }
        mView!!.tvAdd.setOnClickListener {
            //添加, 空格解析
            mView!!.etSensitive.text.toString().trim().replace("，", ",").split(",").filter {
                !TextUtils.isEmpty(it)
            }.forEach { tag ->
                if (!mData.contains(tag)) {
                    mData.add(tag)
                    mView!!.flow_tables.addView(View.inflate(activity, R.layout.item_sensitive_tag, null).also { view ->
                        view.findViewById<TextView>(R.id.text_inf).apply {
                            text = tag
                            setOnLongClickListener {
                                longClickPosition = mView!!.flow_tables.indexOfChild(view)
                                longClickTag = tag
                                false
                            }
                        }
                    })
                }
            }
            mView!!.etSensitive.setText("")
        }
    }

    private fun saveSensitive() {
        var result = ""
        mData.forEach {
            result += "$it,"
        }
        File(Environment.getExternalStorageDirectory(), "${IConstant.DOCNAME}/${IConstant.CACHE_NAME}").apply {
            if (!exists()) {
                mkdirs()
            }
        }
        OutputStreamWriter(FileOutputStream(File(Environment.getExternalStorageDirectory(), "${IConstant.DOCNAME}/${IConstant.CACHE_NAME}/${IConstant.LOCALSENSITIVE}"), false)).apply {
            write(result)
            flush()
            close()
        }
        request(1)
    }

    private var bucketId: String? = "0"

    override fun request(flag: Int) {
        when (flag) {
            0 -> OkClientHelper.get(activity, "admin/lines/sensitive", IntegerRespData::class.java, object : OkResponse {
                override fun success(result: Any?) {
                    result as IntegerRespData
                    //下载文件
                    if (result.code == 0) {
                        OkClientHelper.downByPath(activity, result.data.sensitive_url, File(Environment.getExternalStorageDirectory(), "${IConstant.DOCNAME}/${IConstant.CACHE_NAME}/${IConstant.LOCALSENSITIVE}").absolutePath, {
                            //读取数据
                            File(Environment.getExternalStorageDirectory(), "${IConstant.DOCNAME}/${IConstant.CACHE_NAME}/${IConstant.LOCALSENSITIVE}").apply {
                                if (exists()) {
                                    readText().replace("，", ",").split(",").filter {
                                        it != "," && it != "，" && !TextUtils.isEmpty(it)
                                    }.forEach { tag ->
                                        mData.add(tag)
                                        mView!!.flow_tables.addView(View.inflate(activity, R.layout.item_sensitive_tag, null).also { view ->
                                            view.findViewById<TextView>(R.id.text_inf).apply {
                                                text = tag
                                                setOnLongClickListener {
                                                    longClickPosition = mView!!.flow_tables.indexOfChild(view)
                                                    longClickTag = tag
                                                    false
                                                }
                                            }
                                        })
                                    }
                                }
                            }
                        }, {
                        })
                    }
                }

                override fun onFailure(any: Any?) {

                }
            })
            1 -> {
                OkClientHelper.post(activity, "resource", FormBody.Builder()
                        .add("resourceContent", "line-sensitive.txt")
                        .add("resourceType", "21").build(), QiniuStringData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as QiniuStringData
                        if (result.code == 0) {
                            result.data.bucket_id?.let {
                                bucketId = it
                            }
                            LocalLogUtils.writeLog("Sensitive:上传声昔敏感词汇:${result.data}", System.currentTimeMillis())
                            AliLoadFactory(activity, result.data.end_point, result.data.bucket, result.data.oss, object : LoadStateListener {
                                override fun progress(current: Long) {

                                }

                                override fun success() {
                                    activity!!.runOnUiThread {
                                        showToast("保存成功")
                                        transLayout.showContent()
                                    }
                                }

                                override fun fail() {//oss 异常
                                    LocalLogUtils.writeLog("Sensitive:上传声昔敏感词汇 oss error", System.currentTimeMillis())
                                    activity!!.runOnUiThread {
                                        transLayout.showContent()
                                    }
                                }

                                override fun oneFinish(endTag: String?, position: Int) {

                                }
                            }, UploadData(result.data.resource_content, File(Environment.getExternalStorageDirectory(), "${IConstant.DOCNAME}/${IConstant.CACHE_NAME}/${IConstant.LOCALSENSITIVE}").absolutePath))
                        }
                    }

                    override fun onFailure(any: Any?) {

                    }
                })
            }
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        activity!!.menuInflater.inflate(R.menu.menu_delete_tag, menu)
    }

    override fun onContextItemSelected(item: MenuItem?): Boolean {
        //删除item  刷新View
        try {
            if (mData.contains(longClickTag)) {
                mView!!.flow_tables.removeViewAt(longClickPosition)
                mData.removeAt(longClickPosition)
            }
        } catch (e: Exception) {
        }
        return super.onContextItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterForContextMenu(mView!!.flow_tables)
    }

}
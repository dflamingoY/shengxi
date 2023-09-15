package org.xiaoxingqi.shengxi.modules.listen.alarm

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.media.AudioManager
import android.os.AsyncTask
import android.os.Environment
import android.preference.PreferenceManager
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import kotlinx.android.synthetic.main.activity_select_alarm.*
import org.greenrobot.eventbus.EventBus
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.impl.AlarmUpdateEvent
import org.xiaoxingqi.shengxi.impl.OnPlayListenAdapter
import org.xiaoxingqi.shengxi.model.FilePath
import org.xiaoxingqi.shengxi.utils.AlarmPlayer
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.TimeUtils
import org.xiaoxingqi.shengxi.wedgit.swiprecycler.SwipeMenuLayout
import java.io.File
import java.io.FileFilter

class SelectAlarmActivity : BaseAct() {
    private lateinit var adapter: QuickAdapter<FilePath>

    private lateinit var audioPlayer: AlarmPlayer
    private val mData by lazy {
        ArrayList<FilePath>().apply {
            add(FilePath().apply {
                name = resources.getString(R.string.string_alarm_18)
                isSelected = false
                path = IConstant.LOCAL_DEFAULT_ALARM//默认音频的地址
            })
        }
    }

    private lateinit var localPath: String
    private var playBean: FilePath? = null
    private var isModify = false//已选中的文件被删除
    //最多允许插入5条数据
    private val selectedList by lazy {
        ArrayList<String>()
    }

    override fun onResume() {
        super.onResume()
        volumeControlStream = AudioManager.STREAM_ALARM
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_select_alarm
    }

    override fun initView() {
        toggleVibrate.isSelected = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("vibrate", true)
    }

    override fun initData() {
        audioPlayer = AlarmPlayer(this)
        localPath = intent.getStringExtra("alarmTone")
        //记录本地文件的地址
        if (!TextUtils.isEmpty(localPath)) {
            localPath.split(",").filter {
                !TextUtils.isEmpty(it) && it != ","
            }.forEach {
                //编辑文件
                if (!it.startsWith("assets://")) {
                    if (File(it).exists())
                        selectedList.add(it)
                    else {
                        isModify = true
                    }
                } else {
                    selectedList.add(it)
                }
            }
        }
        tvSelectedCount.text = String.format(resources.getString(R.string.string_alarm_selected_count), selectedList.size)
        adapter = object : QuickAdapter<FilePath>(this, R.layout.item_alarm_cache, mData) {
            override fun convert(helper: BaseAdapterHelper?, item: FilePath?) {
                val menuView = (helper!!.getView(R.id.swipeMenu) as SwipeMenuLayout)
                menuView.isSwipeEnable = item!!.name != "默认"
                helper.getTextView(R.id.tv_title).text = item.name
                helper.getView(R.id.iv_isSelected).visibility = if (selectedList.contains(item.path)) View.VISIBLE else View.INVISIBLE
                helper.getView(R.id.tv_stop).visibility = if (item.isPlaying) View.VISIBLE else View.GONE
                helper.getView(R.id.tv_stop).setOnClickListener {
                    //停止播放
                    audioPlayer.stop()
                    item.isPlaying = false
                    it.visibility = View.GONE
                }
                helper.getView(R.id.tv_delete).setOnClickListener {
                    if (File(item.path).delete()) {
                        transLayout.showProgress()
                        recyclerView.smoothCloseMenu()
                        val removeIndex = mData.indexOf(item)
                        mData.remove(item)
                        adapter.notifyItemRemoved(removeIndex)
                        helper.getView(R.id.swipeMenu)
                        if (selectedList.contains(item.path)) {
                            selectedList.remove(item.path)
                            tvSelectedCount.text = String.format(resources.getString(R.string.string_alarm_selected_count), selectedList.size)
                        }
                        if (localPath.contains(item.path)) {
                            isModify = true
                        }
                        //发送通知其他界面已经删除
                        EventBus.getDefault().post(AlarmUpdateEvent(3, 3).apply {
                            deletePath = item.path.substring(item.path.lastIndexOf("/") + 1, item.path.length)
                        })
                        transLayout.postDelayed({
                            adapter.notifyDataSetChanged()
                            transLayout.showContent()
                        }, 300)
                    }
                }
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        getLocalAudio()
    }

    override fun initEvent() {
        adapter.setOnItemClickListener { _, position ->
            if (mData.size == 1) {//只有默认音频 不允许取消默认音频
                if (audioPlayer.isPlaying) {
                    mData[0].isPlaying = false
                    audioPlayer.stop()
                } else {
                    audioPlayer.setResource(mData[position].path)
                    audioPlayer.start(AudioManager.STREAM_ALARM)
                    mData[position].isPlaying = true
                    playBean = mData[position]
                }
                recyclerView.smoothCloseMenu()
                adapter.notifyDataSetChanged()
                return@setOnItemClickListener
            }
            if (selectedList.contains(mData[position].path)) {
                if (mData[position].isPlaying) {
                    if (audioPlayer.isPlaying) {
                        audioPlayer.stop()
                    }
                    mData[position].isPlaying = false
                    adapter.notifyItemChanged(position)
                    recyclerView.smoothCloseMenu()
                }
                selectedList.remove(mData[position].path)
            } else {
                if (audioPlayer.isPlaying) {
                    audioPlayer.stop()
                }
                playBean?.let {
                    it.isPlaying = false
                }
                audioPlayer.setResource(mData[position].path)
                audioPlayer.start(AudioManager.STREAM_ALARM)
                playBean = mData[position]
                playBean?.let {
                    it.isPlaying = true
                }
                if (selectedList.size < 5) {//最大允许设置5个
                    selectedList.add(mData[position].path)
                }
            }
            tvSelectedCount.text = String.format(resources.getString(R.string.string_alarm_selected_count), selectedList.size)
            recyclerView.smoothCloseMenu()
            adapter.notifyDataSetChanged()
        }
        tvDismiss.setOnClickListener {
            if (isModify) {//本地数据被改变,确认保存本地数据
                setResult(Activity.RESULT_OK, parseIntent())
            }
            finish()
        }
        toggleVibrate.setOnClickListener {
            toggleVibrate.isSelected = !it.isSelected
        }
        tvSave.setOnClickListener {
            setResult(Activity.RESULT_OK, Intent()
                    .putExtra("path", parsePlayPath())
                    .putExtra("vibrate", toggleVibrate.isSelected))
            finish()
        }
        audioPlayer.setOnPlayListener(object : OnPlayListenAdapter() {
            override fun onCompletion() {
                //hide button
                recyclerView.smoothCloseMenu()
                playBean?.let {
                    it.isPlaying = false
                    adapter.notifyDataSetChanged()
                }
            }
        })
    }

    private fun parsePlayPath(): String {
        var result = ""
        selectedList.forEach {
            result += "$it,"
        }
        return if (TextUtils.isEmpty(result)) {
            IConstant.LOCAL_DEFAULT_ALARM
        } else
            result
    }

    @SuppressLint("StaticFieldLeak")
    private fun getLocalAudio() {
        object : AsyncTask<Void, Void, Array<File>?>() {
            override fun doInBackground(vararg params: Void?): Array<File>? {
                val file = File(Environment.getExternalStorageDirectory(), IConstant.DOCNAME + "/" + IConstant.CACHE_NAME + "/" + IConstant.DOWNAUDIO)
                return if (file.exists()) {
                    file.listFiles(FileFilter {
                        !it.endsWith(".aac1")
                    })
                } else
                    null
            }

            override fun onPostExecute(result: Array<File>?) {
                transLayout.showContent()
                if (result != null && result.isNotEmpty()) {
                    result.forEach {
                        val path = FilePath()
                        path.path = it.absolutePath
                        path.name = it.name.replace(".aac1", "").split(" ")[0] + " " + TimeUtils.getInstance().parseFileTime(it.lastModified()).replace("_", "")
                        path.name = path.name.substring(0, path.name.length - 2)
                        mData.add(path)
                    }
                }
                if (mData.size == 1) {
                    mData[0].isSelected = true
                    if (selectedList.size == 0)
                        selectedList.add(mData[0].path)
                }
                adapter.notifyDataSetChanged()
            }
        }.execute()
    }

    private fun parseIntent() = Intent().apply {
        putExtra("path", parsePlayPath())
        putExtra("vibrate", PreferenceManager.getDefaultSharedPreferences(this@SelectAlarmActivity).getBoolean("vibrate", true))
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isModify) {//本地数据被改变,确认保存本地数据
                setResult(Activity.RESULT_OK, parseIntent())
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
        audioPlayer.stop()
        audioPlayer.onDestory()
    }

}
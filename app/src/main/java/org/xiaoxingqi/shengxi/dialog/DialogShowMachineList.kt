package org.xiaoxingqi.shengxi.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.dialog_show_machine_list.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.impl.DialogLoadMoreListener
import org.xiaoxingqi.shengxi.model.BaseBean
import org.xiaoxingqi.shengxi.modules.publicmoudle.SendAct
import org.xiaoxingqi.shengxi.utils.AppTools
import org.xiaoxingqi.shengxi.utils.TimeUtils
import org.xiaoxingqi.shengxi.wedgit.discretescrollview.DSVOrientation
import org.xiaoxingqi.shengxi.wedgit.discretescrollview.DiscreteScrollView
import org.xiaoxingqi.shengxi.wedgit.discretescrollview.InfiniteScrollAdapter
import org.xiaoxingqi.shengxi.wedgit.discretescrollview.transform.ScaleTransformer

class DialogShowMachineList : Dialog {
    private val mData by lazy { ArrayList<BaseBean>() }
    private lateinit var adapter: QuickAdapter<BaseBean>
    private lateinit var discreteAdapter: QuickAdapter<String>
    private val keyData by lazy { ArrayList<String>() }
    private var current = 0
    private var mContext: Context
    private var isSelf = true
    private var map: HashMap<String, ArrayList<BaseBean>>? = null
    private var currentKey = 0
    private lateinit var recyclerView: DiscreteScrollView
    private lateinit var scrollAdapter: InfiniteScrollAdapter<BaseAdapterHelper>
    private var strangeView = -1//全部

    constructor(context: Context) : super(context, R.style.FullDialogTheme) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        mContext = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_show_machine_list)
        if (strangeView != -1 && !isSelf) {
            tvRelation.visibility = View.VISIBLE
            tvRelation.text = String.format(context.resources.getString(R.string.string_machine_dialog_show_permission), if (strangeView == 7) "七" else if (strangeView == 30) "三十" else "")
        } else {
            tvRelation.visibility = View.GONE
        }
        val recycler = findViewById<RecyclerView>(R.id.recyclerPlayList)
        recyclerView = findViewById(R.id.discreteView)
        discreteAdapter = object : QuickAdapter<String>(context, R.layout.item_scroll_text, keyData) {
            override fun convert(helper: BaseAdapterHelper?, item: String?) {
                helper!!.getTextView(R.id.tv_scroll).text = item
            }
        }
        scrollAdapter = InfiniteScrollAdapter.wrap(discreteAdapter)
        recyclerView.adapter = scrollAdapter
        recyclerView.setOrientation(DSVOrientation.HORIZONTAL)
        recyclerView.setSlideOnFling(true)
        recyclerView.setItemTransitionTimeMillis(100)
        recyclerView.setItemTransformer(ScaleTransformer.Builder().setMinScale(0.8f).build())
        findViewById<View>(R.id.tv_Close).setOnClickListener {
            dismiss()
        }
        recyclerView.addOnItemChangedListener { _, adapterPosition ->
            val realPosition = scrollAdapter.getRealPosition(adapterPosition)
            current = 0
            if (realPosition != currentKey) {
                mData.clear()
                map?.get(keyData[realPosition])?.let { mData.addAll(it) }
                if (mData!!.size > 0) {
                    if (findViewById<View>(R.id.linearEmpty).visibility == View.VISIBLE)
                        findViewById<View>(R.id.linearEmpty).visibility = View.GONE
                    if (recyclerView.visibility == View.GONE) {
                        recyclerView.visibility = View.VISIBLE
                    }
                }
                adapter.notifyDataSetChanged()
                listener?.changeData(keyData[realPosition])
                currentKey = realPosition
            }
        }
        discreteAdapter.setOnItemClickListener { _, position ->
            val current = recyclerView.currentItem
            if (position != current) {
                recyclerView.smoothScrollToPosition(scrollAdapter.getClosestPosition(position))
            }
        }
        adapter = object : QuickAdapter<BaseBean>(context, R.layout.item_time_machine_list, mData) {
            override fun convert(helper: BaseAdapterHelper?, item: BaseBean?) {
                helper!!.getTextView(R.id.tv_PlayStatus).text = TimeUtils.getInstance().paserTimeMachine(context, item!!.created_at)
                helper.getView(R.id.iv_isPrivacy).visibility = if (item.is_private == 1) View.VISIBLE else View.GONE
                helper.getView(R.id.iv_PlayStatus).visibility = if (helper.itemView.tag as Int == current) View.VISIBLE else View.GONE
                helper.getView(R.id.tv_PlayStatus).isSelected = helper.itemView.tag as Int == current
            }
        }
        recycler.layoutManager = LinearLayoutManager(context)
        recycler.adapter = adapter
        if (!isSelf) {
            findViewById<TextView>(R.id.tv_SomeBody_memory).text = "Ta的" + mContext.resources.getString(R.string.string_memory)
        } else {
            findViewById<TextView>(R.id.tv_SomeBody_memory).text = "我的" + mContext.resources.getString(R.string.string_memory)
        }
        if (mData?.size == 0) {
            findViewById<View>(R.id.linearEmpty).visibility = View.VISIBLE
            recyclerView.visibility = View.INVISIBLE
            if (!isSelf) {
                findViewById<View>(R.id.tv_SendVoice).visibility = View.GONE
                findViewById<ImageView>(R.id.iv_Empty_Hint).setImageResource(R.mipmap.icon_self_empty_dialog_list)
                findViewById<TextView>(R.id.tv_Empty_Hint).text = mContext.resources.getString(R.string.string_empty_other_voice)
            }
        }
        adapter.setOnItemClickListener { _, position ->
            listener?.onClickItem(position)
        }
        findViewById<View>(R.id.tv_SendVoice).setOnClickListener {
            context.startActivity(Intent(context, SendAct::class.java).putExtra("type", 1))
            (mContext as Activity).overridePendingTransition(R.anim.operate_enter, 0)
            dismiss()
        }
        linearSort.setOnClickListener {
            DialogMachineSort(context).setLocation(AppTools.dp2px(context, 350))
        }
        val p = window!!.attributes
        p.gravity = Gravity.BOTTOM
        p.width = AppTools.getWindowsWidth(context)
        window!!.attributes = p

    }

    private var listener: DialogLoadMoreListener? = null

    /**
     * 设置数据  key 发生变化时, 需要更新Adapter
     */
    fun setData(map: HashMap<String, ArrayList<BaseBean>>) {
        this.map = map
    }

    fun isSelf(isSelf: Boolean): DialogShowMachineList {
        this.isSelf = isSelf
        return this
    }

    /**
     * 提示更新有新的月份出现
     */
    fun notifyNewMonth(key: String, isAdd: Boolean) {
        try {
            if (isAdd) {
                keyData.add(key)
                discreteAdapter.notifyItemInserted(discreteAdapter.itemCount - 1)
            } else {
                if (keyData.contains(key)) {
                    val indexOf = keyData.indexOf(key)
                    keyData.remove(key)
                    discreteAdapter.notifyItemRemoved(indexOf)
                    /**
                     * 选中下一条item
                     */
                    if (indexOf < keyData.size) {
                        recyclerView.smoothScrollToPosition(scrollAdapter.getClosestPosition(currentKey))
                    } else {
                        //滚到全部
                        currentKey = 0
                        recyclerView.smoothScrollToPosition(scrollAdapter.getClosestPosition(0))
                    }
                    listener?.changeData(keyData[currentKey])
                }
            }
        } catch (e: Exception) {
        }
    }

    /**
     * 删除提示 通知删除了某一条数据
     * 1.展示的 '全部'  移除数据源
     * 2.展示其他月份   移除数据源
     *
     */
    fun deleteNotify(bean: BaseBean) {
        if (mData.contains(bean)) {
            mData.remove(bean)
            adapter.notifyDataSetChanged()
        }
    }

    /**
     * 新的数据要更新列表
     */
    fun updateData(bean: BaseBean) {
        /**
         * 1.当前展示的key
         * 2.如果当前展示的全部 或者这个key 的时间和当前展示的key 一致, 则添加到列表
         *
         */
        if (currentKey == 0 || TimeUtils.getInstance().timeMachine(bean.created_at) == keyData[currentKey]) {//全部展示
            if (!mData.contains(bean)) {
                mData.add(bean)
            }
        }
        if (mData!!.size > 0) {
            if (findViewById<View>(R.id.linearEmpty).visibility == View.VISIBLE)
                findViewById<View>(R.id.linearEmpty).visibility = View.GONE
            if (recyclerView.visibility != View.VISIBLE) {
                recyclerView.visibility = View.VISIBLE
            }
        }
        adapter.notifyDataSetChanged()
    }

    /**
     * 更新所有数据源, 清除数据
     */
    fun notifyAllResource() {
        try {
            currentKey = 0
            mData.clear()
            keyData.clear()
        } catch (e: Exception) {
        }
    }

    override fun show() {
        super.show()
        try {
            if (keyData.size > 0 && keyData.size > currentKey) {
                map?.get(keyData[currentKey])?.let {
                    if (!mData.containsAll(it)) {
                        mData.addAll(it)
                    }
                }
            } else {
                map?.get("全部")?.let {
                    if (!mData.containsAll(it)) {
                        mData.addAll(it)
                    }
                }
            }
            if (null != mData) {
                if (mData!!.size > 0) {
                    if (findViewById<View>(R.id.linearEmpty).visibility == View.VISIBLE)
                        findViewById<View>(R.id.linearEmpty).visibility = View.GONE
                    if (recyclerView.visibility != View.VISIBLE) {
                        recyclerView.visibility = View.VISIBLE
                    }
                } else {
                    if (findViewById<View>(R.id.linearEmpty).visibility == View.GONE)
                        findViewById<View>(R.id.linearEmpty).visibility = View.VISIBLE
                    if (recyclerView.visibility == View.VISIBLE) {
                        recyclerView.visibility = View.INVISIBLE
                    }
                }
            } else {
                if (findViewById<View>(R.id.linearEmpty).visibility == View.GONE)
                    findViewById<View>(R.id.linearEmpty).visibility = View.VISIBLE
            }
            adapter.notifyDataSetChanged()
            if (keyData.size == 0) {
                if (map != null) {
                    for (key in map!!.keys) {
                        keyData.add(key)
                    }
                    discreteAdapter.notifyDataSetChanged()
                }
            }
        } catch (e: Exception) {

        }
    }

    /**
     * 更新當前正在播放的item
     */
    fun updatePlayPosition(position: Int) {
        try {
            current = position
            adapter.notifyDataSetChanged()
        } catch (e: Exception) {
        }
    }

    fun setLoadmoreListener(listener: DialogLoadMoreListener): DialogShowMachineList {
        this.listener = listener
        return this
    }

    /**
     * 非好友返回此设置
     */
    fun setStrange(strangeView: Int) {
        try {
            this.strangeView = strangeView
            if (strangeView != -1) {
                tvRelation.visibility = View.VISIBLE
                tvRelation.text = String.format(context.resources.getString(R.string.string_machine_dialog_show_permission), if (strangeView == 7) "七" else "三十")
            } else {
                tvRelation.visibility = View.GONE
            }
        } catch (e: Exception) {
        }
    }
}
package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import kotlinx.android.synthetic.main.dialog_talk_album_menu.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.impl.ITalkAlbumOperatorListener
import org.xiaoxingqi.shengxi.model.BaseTalkVoiceBean
import org.xiaoxingqi.shengxi.utils.TimeUtils

class DialogTalkAlbumMenu(context: Context) : BaseDialog(context, R.style.FullDialogTheme) {
    private var data: ArrayList<BaseTalkVoiceBean>? = null
    private lateinit var adapter: QuickAdapter<BaseTalkVoiceBean>
    private var current = -1
    override fun getLayoutId(): Int {
        return R.layout.dialog_talk_album_menu
    }

    override fun initView() {
        recyclerView_menu.layoutManager = LinearLayoutManager(context)
        adapter = object : QuickAdapter<BaseTalkVoiceBean>(context, R.layout.item_dialog_album_details, data) {
            override fun convert(helper: BaseAdapterHelper?, item: BaseTalkVoiceBean?) {
                helper!!.getTextView(R.id.tv_PlayStatus).text = TimeUtils.getInstance().paserTimeMachine(context, item!!.dialog_at)
                helper.getView(R.id.iv_PlayStatus).visibility = if (helper.itemView.tag as Int == current) View.VISIBLE else View.GONE
                helper.getView(R.id.tv_PlayStatus).isSelected = helper.itemView.tag as Int == current
                helper.getTextView(R.id.tv_close).text = "删除"
                helper.getView(R.id.tv_close).setOnClickListener {
                    recyclerView_menu.smoothCloseMenu()
                    DialogDeleteTalkAlbum(context).setOnClickListener(View.OnClickListener {
                        listener?.operatedItem(item)
                    }).show()
                }
            }
        }
        recyclerView_menu.adapter = adapter
        tv_hint_move.setOnClickListener {
            DialogAlbumHowOperator(context).setTitle(context.resources.getString(R.string.string_15)).show()
        }
        tv_close.setOnClickListener { dismiss() }
        adapter.setOnItemClickListener { _, position ->
            listener?.clickItem(position)
        }
        initSystem()
    }

    fun setData(data: ArrayList<BaseTalkVoiceBean>) {
        this.data = data
    }

    fun updateCurrent(current: Int) {
        this.current = current
        try {
            adapter.notifyDataSetChanged()
        } catch (e: Exception) {
        }
    }

    override fun show() {
        super.show()
        if (data == null || data!!.size == 0) {
            frame_empty_album.visibility = View.VISIBLE
        } else {
            frame_empty_album.visibility = View.GONE
        }
        adapter.notifyDataSetChanged()
    }

    /**
     * 更新Item 移除
     */
    fun notifyDataSetChange() {
        if (data != null && data!!.size == 0) {
            frame_empty_album.visibility = View.VISIBLE
        } else {
            adapter.notifyDataSetChanged()
        }
    }

    private var listener: ITalkAlbumOperatorListener? = null

    fun setOnItemClickListener(listener: ITalkAlbumOperatorListener): DialogTalkAlbumMenu {
        this.listener = listener
        return this
    }

    override fun dismiss() {
        super.dismiss()
        recyclerView_menu.smoothCloseMenu()
    }
}
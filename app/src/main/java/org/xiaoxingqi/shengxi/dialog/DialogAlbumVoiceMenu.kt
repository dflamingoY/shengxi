package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import kotlinx.android.synthetic.main.dialog_album_voice_menu.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.impl.IAlbumMenuOperatListener
import org.xiaoxingqi.shengxi.model.BaseBean
import org.xiaoxingqi.shengxi.utils.TimeUtils
import org.xiaoxingqi.shengxi.wedgit.swiprecycler.SwipeMenuLayout

class DialogAlbumVoiceMenu(context: Context) : BaseDialog(context, R.style.FullDialogTheme) {
    private var data: ArrayList<BaseBean>? = null
    private lateinit var adapter: QuickAdapter<BaseBean>
    private var current = -1
    private var isSelf: Boolean = false
    override fun getLayoutId(): Int {
        return R.layout.dialog_album_voice_menu
    }

    override fun initView() {
        tv_hint_move.visibility = if (isSelf) View.VISIBLE else View.GONE
        recyclerView_menu.layoutManager = LinearLayoutManager(context)
        adapter = object : QuickAdapter<BaseBean>(context, R.layout.item_dialog_album_details, data) {
            override fun convert(helper: BaseAdapterHelper?, item: BaseBean?) {
                helper!!.getTextView(R.id.tv_PlayStatus).text = TimeUtils.getInstance().paserTimeMachine(context, item!!.created_at)
                helper.getView(R.id.iv_isPrivacy).visibility = if (item.is_private == 1) View.VISIBLE else View.GONE
                helper.getView(R.id.iv_PlayStatus).visibility = if (helper.itemView.tag as Int == current) View.VISIBLE else View.GONE
                helper.getView(R.id.tv_PlayStatus).isSelected = helper.itemView.tag as Int == current
                (helper.getView(R.id.swipeMenu) as SwipeMenuLayout).isSwipeEnable = isSelf
                helper.getView(R.id.tv_close).setOnClickListener {
                    recyclerView_menu.smoothCloseMenu()
                    listener?.operatedItem(item)
                }
            }
        }
        recyclerView_menu.adapter = adapter
        tv_hint_move.setOnClickListener {
            DialogAlbumHowOperator(context).show()
        }
        tv_close.setOnClickListener { dismiss() }
        adapter.setOnItemClickListener { _, position ->
            listener?.clickItem(position)
        }
        initSystem()
    }

    fun setData(data: ArrayList<BaseBean>, isSelf: Boolean) {
        this.data = data
        this.isSelf = isSelf
    }

    override fun show() {
        super.show()
        if (data == null || data!!.size == 0) {
            frame_empty_album.visibility = View.VISIBLE
        } else {
            frame_empty_album.visibility = View.GONE
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged()
        }
    }

    fun updateCurrent(current: Int) {
        this.current = current
        try {
            adapter.notifyDataSetChanged()
        } catch (e: Exception) {

        }
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

    private var listener: IAlbumMenuOperatListener? = null

    fun setOnItemClickListener(listener: IAlbumMenuOperatListener): DialogAlbumVoiceMenu {
        this.listener = listener
        return this
    }

}
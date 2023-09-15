package org.xiaoxingqi.shengxi.modules.echoes

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.text.TextUtils
import android.view.View
import kotlinx.android.synthetic.main.frag_echo.view.*
import okhttp3.FormBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.support.v4.startActivity
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseFragment
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.impl.EchoSetEvent
import org.xiaoxingqi.shengxi.impl.EchoesUpdateEvent
import org.xiaoxingqi.shengxi.impl.FragSkinUpdateTheme
import org.xiaoxingqi.shengxi.impl.ITabClickCall
import org.xiaoxingqi.shengxi.model.*
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.MainActivity
import org.xiaoxingqi.shengxi.modules.nearby.SearchNearbyActivity
import org.xiaoxingqi.shengxi.modules.user.set.UserTalkSettingActivity
import org.xiaoxingqi.shengxi.utils.*
import skin.support.SkinCompatManager

class EchoeFragment : BaseFragment(), ITabClickCall {
    private var visible = false
    private var userinfo: UserInfoData? = null
    private var echoeFrags = arrayOf(EchoMsgFragment(), RecentContactsFragment())
    override fun tabClick(isVisible: Boolean) {
        /**
         * 到当前界面
         */
        if (visible == isVisible) {
            return
        }
        visible = isVisible
        if (isVisible && isResumed) {
            getSystemInfo()
        }
    }

    override fun doubleClickRefresh() {

    }

    override fun getLayoutId(): Int {
        return R.layout.frag_echo
    }

    override fun onResume() {
        userinfo = PreferenceTools.getObj(activity, IConstant.USERCACHE, UserInfoData::class.java)
        super.onResume()
        if (visible) {
            getSystemInfo()
        }
    }

    override fun initView(view: View?) {
        val params = view!!.view_status_bar_place.layoutParams
        params.height = AppTools.getStatusBarHeight(activity)
        view.view_status_bar_place.layoutParams = params
        updateTheme()
        val params1 = view.view_statusHeight.layoutParams
        params1.height = AppTools.getStatusBarHeight(activity)
        view.view_statusHeight.layoutParams = params1
    }

    override fun initData() {
        mView!!.echo_page.adapter = EchoeAdaper(childFragmentManager)
        request(2)
    }

    override fun initEvent() {
        mView?.relative_Custom?.setOnClickListener {
//            startActivity(Intent(activity, FriendsActivity::class.java))
            startActivity(Intent(activity, ChatActivity::class.java)
                    .putExtra("uid", "1")
                    .putExtra("userName", "声昔小二")
                    .putExtra("unreadCount", 0)
                    .putExtra("chatId", "")
            )
        }
        mView!!.echo_page.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                mView!!.echo_pagerSliding.setCurrentSelect(position)
            }
        })
        mView!!.echo_pagerSliding.setOnClick {
            mView!!.echo_page.setCurrentItem(mView!!.echo_pagerSliding.indexOfChild(it), true)
        }
        mView!!.iv_Sleep.setOnClickListener {
            startActivity<UserTalkSettingActivity>()
        }
        mView!!.relativeUser.setOnClickListener {
            startActivity<SearchNearbyActivity>()
        }
    }

    /**
     * 获取系统通知消息
     */
    private fun getSystemInfo() {
        val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.get(activity, "messages/${loginBean.user_id}", SystemNoticeData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as SystemNoticeData).code == 0) {
                    if (result.data.total > 0 || result.data.chatpri.num > "0") {//显示小红点
                        (activity as MainActivity).setEchoesFlag(true)
                        if (result.data.total > 0) {
                            mView!!.echo_pagerSliding.setFlagState(0, true)
                        } else {
                            mView!!.echo_pagerSliding.setFlagState(0, false)
                        }
                        if (result.data.chatpri.num > "0") {
                            mView!!.echo_pagerSliding.setFlagState(1, true)
                        } else {
                            mView!!.echo_pagerSliding.setFlagState(1, false)

                        }
                    } else {
                        (activity as MainActivity).setEchoesFlag(false)
                        mView!!.echo_pagerSliding.setFlagState(0, false)
                        mView!!.echo_pagerSliding.setFlagState(1, false)
                    }
                }
            }

            override fun onFailure(any: Any?) {

            }
        })
    }

    override fun request(flag: Int) {
        val obj = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        if (flag == 0 || flag == 1) {
            OkClientHelper.patch(activity, "users/${obj.user_id}/setting", FormBody.Builder().add("autoReply", flag.toString()).build(), BaseRepData::class.java, object : OkResponse {
                override fun success(result: Any?) {
                    result as BaseRepData
                    if (result.code == 0) {
//                        showToast("设置成功")
                    } else {
                        showToast(result.msg)
                    }
                    mView!!.transLayout.showContent()
                }

                override fun onFailure(any: Any?) {
                    mView!!.transLayout.showContent()
                }
            })
        } else if (flag == 2) {//获取小弹窗的描述内容
            OkClientHelper.get(activity, "users/" + obj.user_id + "/setting", PatchData::class.java, object : OkResponse {
                override fun success(result: Any) {
                    result as PatchData
                    if (result.code == 0) {
                        mView!!.iv_Sleep.isSelected = result.data.auto_reply == 1
                    }
                    mView!!.transLayout.showContent()
                }

                override fun onFailure(any: Any) {
                    mView!!.transLayout.showContent()
                }
            })
        }
    }

    private inner class EchoeAdaper(fm: FragmentManager?) : FragmentPagerAdapter(fm) {
        override fun getItem(position: Int): Fragment {
            return echoeFrags[position]
        }

        override fun getCount(): Int {
            return 2
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return if (position == 0) "消息" else "私聊"
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        EventBus.getDefault().register(this)
    }

    override fun onDetach() {
        super.onDetach()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun update(event: EchoesUpdateEvent) {
        getSystemInfo()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateSkinEvent(event: FragSkinUpdateTheme) {
        updateTheme()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun echoSetEvent(event: EchoSetEvent) {
        mView!!.iv_Sleep.isSelected = event.isCheck
    }

    private fun updateTheme() {
        /**
         * 如果 <6.0   夜间模式View 的背景色为黑
         *              白天模式 View 的背景色为0xffcccccc
         *     >=6.0  夜间模式为黑色  白天模式为白色
         */
        if (mView != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) {//白天模式
                    mView!!.view_status_bar_place.setBackgroundColor(Color.WHITE)
                } else {//夜间模式
                    mView!!.view_status_bar_place.setBackgroundColor(Color.parseColor("#181828"))
                }
            } else {
                if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) {//白天模式
                    mView!!.view_status_bar_place.setBackgroundColor(Color.parseColor("#cccccc"))
                } else {//夜间模式
                    mView!!.view_status_bar_place.setBackgroundColor(Color.parseColor("#181828"))
                }
            }
        }

    }
}
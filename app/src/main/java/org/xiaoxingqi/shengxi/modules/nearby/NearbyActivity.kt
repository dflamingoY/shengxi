//package org.xiaoxingqi.shengxi.modules.nearby
//
//import android.Manifest
//import android.annotation.SuppressLint
//import android.content.Context
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.os.Build
//import android.support.v4.app.ActivityCompat
//import android.support.v4.content.ContextCompat
//import android.view.View
//import kotlinx.android.synthetic.main.activity_nearby.*
//import org.xiaoxingqi.shengxi.R
//import org.xiaoxingqi.shengxi.core.BaseAct
//import android.location.LocationManager
//import android.media.AudioManager
//import android.os.Environment
//import android.os.Message
//import android.provider.Settings
//import android.support.v7.widget.LinearLayoutManager
//import android.text.TextUtils
//import android.view.LayoutInflater
//import com.amap.api.location.AMapLocation
//import com.amap.api.location.AMapLocationClientOption
//import com.amap.api.location.AMapLocationClient
//import com.amap.api.location.AMapLocationListener
//import okhttp3.FormBody
//import org.greenrobot.eventbus.Subscribe
//import org.greenrobot.eventbus.ThreadMode
//import org.xiaoxingqi.shengxi.model.NearbyData
//import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
//import org.xiaoxingqi.shengxi.core.adapter.BaseQuickAdapter
//import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
//import org.xiaoxingqi.shengxi.core.http.OkClientHelper
//import org.xiaoxingqi.shengxi.core.http.OkResponse
//import org.xiaoxingqi.shengxi.core.http.VolleyErrorHelper
//import org.xiaoxingqi.shengxi.dialog.DialogCommitPwd
//import org.xiaoxingqi.shengxi.impl.DialogAdminPwdListener
//import org.xiaoxingqi.shengxi.impl.OnPlayListenAdapter
//import org.xiaoxingqi.shengxi.impl.ProgressHelper
//import org.xiaoxingqi.shengxi.impl.ProgressTrackListener
//import org.xiaoxingqi.shengxi.impl.StopPlayInterFace
//import org.xiaoxingqi.shengxi.model.AdminLoginData
//import org.xiaoxingqi.shengxi.model.PatchData
//import org.xiaoxingqi.shengxi.model.login.LoginData
//import org.xiaoxingqi.shengxi.modules.adminManager.ManagerAdminActivity
//import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity
//import org.xiaoxingqi.shengxi.utils.*
//import org.xiaoxingqi.shengxi.wedgit.ViewSeekProgress
//import org.xiaoxingqi.shengxi.wedgit.VoiceNearbyProgress
//import java.io.File
//import java.io.IOException
//import kotlin.collections.ArrayList
//
//const val REQUSTPERMISSIONCODE = 0x0000
//const val REQUESTLOCATION = 0x0001
//const val REQUESTGPS = 0x0002
//
//class NearbyActivity : BaseAct(), AMapLocationListener {
//    private var mlocationClient: AMapLocationClient? = null
//    private var mLocationOption: AMapLocationClientOption? = null
//    lateinit var adapter: QuickAdapter<NearbyData.NearbyBean>
//    private var playBean: NearbyData.NearbyBean? = null
//    private var current = 1
//    private val mData by lazy {
//        ArrayList<NearbyData.NearbyBean>()
//    }
//    private var latitude: String? = null
//    private var longitude: String? = null
//    private var isLocation = false
//    private lateinit var hearView: View
//    private lateinit var audioPlayer: AudioPlayer
//    override fun getLayoutId(): Int {
//        return R.layout.activity_nearby
//    }
//
//    override fun initView() {
//        swipeRefresh.isEnabled = true
//        recyclerView.layoutManager = LinearLayoutManager(this)
//        hearView = LayoutInflater.from(this).inflate(R.layout.layout_head_nearby, recyclerView, false)
//        swipeRefresh.setColorSchemeColors(resources.getColor(R.color.colorIndecators), resources.getColor(R.color.colorMovieTextColor),
//                resources.getColor(R.color.color_Text_Black))
//    }
//
//    private val progressHandler = @SuppressLint("HandlerLeak")
//    object : ProgressHelper(100) {
//
//        override fun handleMessage(msg: Message?) {
//            adapter.changeStatue(false)
//        }
//    }
//
//    override fun writeHeadSet(): Boolean {
//        val headsetOn = audioPlayer.audioManager.isWiredHeadsetOn
//        val a2dpOn = audioPlayer.audioManager.isBluetoothA2dpOn
//        val scoOn = audioPlayer.audioManager.isBluetoothScoOn
//        return headsetOn || a2dpOn || scoOn
//    }
//
//    override fun changSpeakModel(type: Int) {
//        if (type == 1) {
//            if (audioPlayer.isPlaying) {
//                val currentPosition = audioPlayer.currentPosition
//                playBean?.let { it.pasuePosition = currentPosition.toInt() }
//                audioPlayer.stop()
//                audioPlayer.start(AudioManager.STREAM_MUSIC)
//                audioPlayer.seekTo(currentPosition.toInt())
//            }
//        } else {
//            if (audioPlayer.isPlaying) {
//                val currentPosition = audioPlayer.currentPosition
//                playBean?.let { it.pasuePosition = currentPosition.toInt() }
//                audioPlayer.stop()
//                audioPlayer.start(AudioManager.STREAM_VOICE_CALL)
//                audioPlayer.seekTo(currentPosition.toInt())
//            }
//        }
//    }
//
//    override fun initData() {
//        /**
//         * 1 检查权限来判断显示的内容
//         * 2 是否开启定位功能
//         * 3 是否打开位置功能 (默认开启)
//         * 4
//         */
//        audioPlayer = AudioPlayer(this)
//        mlocationClient = AMapLocationClient(this)
//        mLocationOption = AMapLocationClientOption()
//        mlocationClient?.setLocationListener(this)
//        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
//        mLocationOption?.locationMode = AMapLocationClientOption.AMapLocationMode.Battery_Saving
//        mLocationOption?.interval = 2000
//        mlocationClient?.setLocationOption(mLocationOption)
//        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
//        try {
//            if (IConstant.userAdminArray.contains(loginBean.user_id)) {
//                tvSearch.visibility = View.VISIBLE
//            }
//        } catch (e: Exception) {
//        }
//        val character = SPUtils.getString(this, IConstant.USERCHARACTERTYPE + loginBean.user_id, "I")
//        if (IConstant.USER_EXTROVERT.equals(character, true)) {
//            tv_Nearby_hint_1.text = resources.getString(R.string.string_nearby_hint_e_1)
//        }
//        adapter = object : QuickAdapter<NearbyData.NearbyBean>(this, R.layout.item_neabry_user, mData, hearView) {
//            var cache: MutableList<BaseAdapterHelper> = ArrayList()
//            @SuppressLint("SetTextI18n")
//            override fun convert(helper: BaseAdapterHelper?, item: NearbyData.NearbyBean?) {
//                glideUtil.loadGlide(item!!.avatar_url, helper!!.getImageView(R.id.iv_img), R.mipmap.icon_user_default, glideUtil.getLastModified(item.avatar_url))
//                helper.getTextView(R.id.tv_UserName).text = item.nick_name
//                helper.getTextView(R.id.tv_Desc).text = if (TextUtils.isEmpty(item.self_intro)) {
//                    resources.getString(R.string.string_empty_desc)
//                } else {
//                    item.self_intro
//                }
//                helper.getTextView(R.id.tv_Time_).text = "${AppTools.distancePaser(item.distance)} · ${TimeUtils.getInstance().nearbyTime(this@NearbyActivity, item.login_at)}"
//                if (item.voice_total_len == 0) {
//                    helper.getTextView(R.id.tv_MemoryLength).text = ""
//                    helper.getTextView(R.id.tv_empty_length).text = resources.getString(R.string.string_empty_memory_length)
//                } else {
//                    helper.getTextView(R.id.tv_MemoryLength).text = TimeUtils.formatterS(this@NearbyActivity, item.voice_total_len)
//                    helper.getTextView(R.id.tv_empty_length).text = resources.getString(R.string.string_memory_length)
//                }
//                val animaProgress = helper.getView(R.id.voiceAnimProgress) as VoiceNearbyProgress
//                animaProgress.data = item
//                animaProgress.findViewById<ViewSeekProgress>(R.id.viewSeekProgress).setOnClickListener {
//                    sendObserver()
//                    if (audioPlayer.isPlaying) {
//                        if (item.isPlaying) {//当前正在播放
//                            item.pasuePosition = audioPlayer.currentPosition.toInt()
//                            audioPlayer.stop()
//                            progressHandler.stop()
//                            item.isPlaying = false
//                            animaProgress.finish()
//                            playBean = null
//                            return@setOnClickListener
//                        }
//                    }
//                    playBean?.let {
//                        it.isPlaying = false
//                        it.pasuePosition = 0
//                    }
//                    download(helper, item)
//                }
//                val viewSeekProgress = helper.getView(R.id.viewSeekProgress) as ViewSeekProgress
//                viewSeekProgress.setOnTrackListener(object : ProgressTrackListener {
//                    override fun startTrack() {
//                        if (!viewSeekProgress.isPressed) {
//                            if (audioPlayer.isPlaying) {
//                                item.allDuration = audioPlayer.duration
//                                progressHandler.stop()
//                                audioPlayer.stop()
//                                item.isPlaying = false
//                                animaProgress.finish()
//                            }
//                        }
//                    }
//
//                    override fun endTrack(progress: Float) {
//                        item.pasuePosition = (progress * item.allDuration).toInt()
//                        download(helper, item)
//                    }
//                })
//
//                /*animaProgress.findViewById<View>(R.id.iv_click_reStart).setOnClickListener {
//                    sendObserver()
//                    if (audioPlayer.isPlaying) {
//                        if (item!!.isPlaying) {//当前正在播放
//                            audioPlayer.stop()
//                            progressHandler.stop()
//                            item.isPlaying = false
//                            item.pasuePosition = 1
//                            animaProgress.finish()
//                            playBean = null
//                        }
//                    }
//                    playBean?.let {
//                        it.isPlaying = false
//                        it.pasuePosition = 0
//                    }
//                    download(helper, item)
//                }*/
//                if (!cache.contains(helper))
//                    cache.add(helper)
//            }
//
//            private fun download(helper: BaseAdapterHelper, item: NearbyData.NearbyBean) {
//                try {
//                    if (TextUtils.isEmpty(item.wave_url)) {
//                        showToast(resources.getString(R.string.string_empty_voice_1))
//                        return
//                    }
//                    if (item.pasuePosition > 0) {
////                        item.pasuePosition = 0
//                        val file = File(Environment.getExternalStorageDirectory(), IConstant.DOCNAME + "/" + IConstant.CACHE_NAME + "/" + IConstant.VOICENAME + "/" + AppTools.getSuffix(item.wave_url))
//                        audioPlayer.setDataSource(file.absolutePath)
//                        audioPlayer.start(if (SPUtils.getBoolean(this@NearbyActivity, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
//                    } else
//                        OkClientHelper.downWave(this@NearbyActivity, item.wave_url, { o ->
//                            try {
//                                if (null == o) {
//                                    showToast(resources.getString(R.string.string_error_file))
//                                    return@downWave
//                                }
//                                audioPlayer.setDataSource(o.toString())
//                                audioPlayer.start(if (SPUtils.getBoolean(this@NearbyActivity, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
//                            } catch (e: IOException) {
//                                e.printStackTrace()
//                            }
//                        }, { showToast(VolleyErrorHelper.getMessage(it)) })
//                    audioPlayer.onPlayListener = object : OnPlayListenAdapter() {
//
//                        override fun onCompletion() {
//                            (helper.getView(R.id.voiceAnimProgress) as VoiceNearbyProgress).finish()
//                            item.isPlaying = false
//                            progressHandler.stop()
//                        }
//
//                        override fun onInterrupt() {
//                            (helper.getView(R.id.voiceAnimProgress) as VoiceNearbyProgress).finish()
//                            item.isPlaying = false
//                            progressHandler.stop()
//                        }
//
//                        override fun onPrepared() {
//                            audioPlayer.seekTo(item.pasuePosition)
//                            item.pasuePosition = 0
//                            playBean = item
//                            item.isPlaying = true
//                            progressHandler.start()
//                        }
//                    }
//                } catch (e: IOException) {
//                    e.printStackTrace()
//                }
//            }
//
//            override fun changeStatue(isSelect: Boolean) {
//                super.changeStatue(isSelect)
//                for (helper in cache) {
//                    val currentPosition = audioPlayer.currentPosition.toInt()
//                    try {
//                        val voiceProgress = helper.getView(R.id.voiceAnimProgress) as VoiceNearbyProgress
//                        voiceProgress.changeProgress(currentPosition)
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                    }
//                }
//            }
//        }
//        recyclerView.adapter = adapter
//        adapter.setLoadMoreEnable(recyclerView, recyclerView.layoutManager, LayoutInflater.from(this).inflate(R.layout.view_loadmore, recyclerView, false))
//        queryLocationToggle()
//    }
//
//    override fun onLocationChanged(p0: AMapLocation?) {
//        if (p0?.errorCode == 0) {
//            latitude = p0.latitude.toString()//获取纬度
//            longitude = p0.longitude.toString()//获取经度
//            request(1)
//            mlocationClient?.onDestroy()
//        }
//    }
//
//    override fun initEvent() {
//        tvStartLocation.setOnClickListener {
//            if (!isLocation)
//                startActivityForResult(Intent(this, LocationServerActivity::class.java), REQUESTLOCATION)
//            else {
//                if (!isOpen()) {
//                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
//                    startActivityForResult(intent, REQUESTGPS)
//                } else {
//                    checkLocation()
//                }
//            }
//        }
//        tvSearch.setOnClickListener {
//            dialogPwd = DialogCommitPwd(this).setOnResultListener(DialogAdminPwdListener { _, _, pwd ->
//                loginAdmin(pwd)
//            })
//            dialogPwd?.show()
//        }
//        adapter.setOnItemClickListener { view, position ->
//            UserDetailsActivity.start(this, mData[position].avatar_url, mData[position].user_id, view.findViewById(R.id.iv_img))
//        }
//        btn_Back.setOnClickListener { finish() }
//        swipeRefresh.setOnRefreshListener {
//            audioPlayer.stop()
//            current = 1
//            request(current)
//        }
//        adapter.setOnLoadListener {
//            current++
//            request(current)
//        }
//    }
//
//    private fun loginAdmin(pwd: String) {
//        OkClientHelper.post(this, "admin/users/login", FormBody.Builder().add("confirmPasswd", pwd).build(), AdminLoginData::class.java, object : OkResponse {
//            override fun success(result: Any?) {
//                result as AdminLoginData
//                if (result.code == 0) {
//                    SPUtils.setString(this@NearbyActivity, IConstant.ADMINTOKEN, result.data.token)
//                    startActivity(Intent(this@NearbyActivity, ManagerAdminActivity::class.java))
//                    dialogPwd?.dismiss()
//                } else {
//                    dialogPwd?.setCallBack()
//                    showToast(result.msg)
//                }
//            }
//
//            override fun onFailure(any: Any?) {
//                showToast(any.toString())
//            }
//        })
//    }
//
//    override fun request(flag: Int) {
//        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
//        OkClientHelper.get(this, "users/${loginBean.user_id}/round?lng=$longitude&lat=$latitude&pageNo=$flag", NearbyData::class.java, object : OkResponse {
//            override fun success(result: Any?) {
//                result as NearbyData
//                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
//                if (result.data != null) {
//                    if (flag == 1) {
//                        mData.clear()
//                        mData.addAll(result.data)
//                        adapter.notifyDataSetChanged()
//                    } else {
//                        for (bean in result.data) {
//                            mData.add(bean)
//                            adapter.notifyItemInserted(adapter.itemCount - 1)
//                        }
//                    }
//                    if (result.data.size >= 10) {
//                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
//                    }
//                } else {
//                    if (flag == 1) {
//                        mData.clear()
//                        adapter.notifyDataSetChanged()
//                    }
//                }
//                if (mData.size == 0) {
//                    transLayout.showEmpty()
//                } else {
//                    transLayout.showContent()
//                }
//                swipeRefresh.isRefreshing = false
//            }
//
//            override fun onFailure(any: Any?) {
//                if (AppTools.isNetOk(this@NearbyActivity)) {
//                    showToast(any.toString())
//                } else {
//                    mData.clear()
//                    adapter.notifyDataSetChanged()
//                    transLayout.showOffline()
//                }
//                swipeRefresh.isRefreshing = false
//
//            }
//        })
//    }
//
//    /**
//     * 查询用户是否打开位置开关
//     */
//    private fun queryLocationToggle() {
//        transLayout.showProgress()
//        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
//        OkClientHelper.get(this, "users/${loginBean.user_id}/setting", PatchData::class.java, object : OkResponse {
//            override fun success(result: Any?) {
//                result as PatchData
//                if (result.code == 0) {
//                    if (result.data.gps_switch == 1) {//打开权限
//                        changStatus(true)
//                        isLocation = true
//                    } else {//关闭权限
//                        isLocation = false
//                        changStatus(false)
//                        transLayout.showContent()
//                    }
//                } else {//错误时默认为未打开权限
//                    changStatus(false)
//                    transLayout.showContent()
//                }
//            }
//
//            override fun onFailure(any: Any?) {
//                transLayout.showOffline()
//            }
//        })
//
//    }
//
//    fun changStatus(statu: Boolean) {
//        if (!statu) {
//            relativePermissionDenial.visibility = View.VISIBLE
//            swipeRefresh.visibility = View.GONE
//            transLayout.showContent()
//        } else {
//            relativePermissionDenial.visibility = View.GONE
//            swipeRefresh.visibility = View.VISIBLE
//            transLayout.showProgress()
//            checkLocation()
//        }
//    }
//
//    /**
//     * 檢測是否有權限
//     */
//    private fun checkLocation() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), REQUSTPERMISSIONCODE)
//            } else {
//                if (isOpen()) {
//                    mlocationClient?.startLocation()
//                } else {
//                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
//                    startActivityForResult(intent, REQUESTGPS)
//                }
//            }
//        } else {
//            mlocationClient?.startLocation()
//        }
//    }
//
//    /**
//     * 判断是否打开定位的开关
//     */
//    private fun isOpen(): Boolean {
//        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
//        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
//        val gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
//        // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
//        val network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
//        return gps || network
//    }
//
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        /**
//         * 检测返回的结果 是否改变了状态
//         */
//        if (requestCode == REQUESTGPS) {
//            transLayout.showContent()
//            changStatus(isOpen())
//        } else if (requestCode == REQUESTLOCATION) {//修改定位信息
//            latitude = data?.getStringExtra("lat")
//            longitude = data?.getStringExtra("lng")
//            if (latitude != null && longitude != null) {
//                changStatus(true)
//                request(current)
//            } else {
//                queryLocationToggle()
////                changStatus(false)
//            }
//        }
//    }
//
//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == REQUSTPERMISSIONCODE) {
//            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                mlocationClient?.startLocation()
//                changStatus(true)
//            } else {
//                changStatus(false)
//            }
//        }
//    }
//
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun stopEvent(event: StopPlayInterFace) {
//        /**
//         * 焦点不在当前Fragment 或者不在当前Activity时,需要clear所有状态
//         */
//        if (!isVisibleActivity) {
//            playBean?.let {
//                if (it.isPlaying) {
//                    it.isPlaying = false
//                    adapter.notifyDataSetChanged()
//                    progressHandler.stop()
//                }
//                audioPlayer.stop()
//            }
//        }
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        mlocationClient?.onDestroy()
//        audioPlayer.stop()
//        progressHandler.removeCallbacks(progressHandler)
//    }
//}
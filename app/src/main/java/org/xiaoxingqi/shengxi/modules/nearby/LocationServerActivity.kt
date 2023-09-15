//package org.xiaoxingqi.shengxi.modules.nearby
//
//import android.Manifest
//import android.app.Activity
//import android.content.Context
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.location.LocationManager
//import android.os.Build
//import android.provider.Settings
//import android.support.v4.app.ActivityCompat
//import android.support.v4.content.ContextCompat
//import android.view.KeyEvent
//import android.view.View
//import com.amap.api.location.AMapLocation
//import com.amap.api.location.AMapLocationClient
//import com.amap.api.location.AMapLocationClientOption
//import com.amap.api.location.AMapLocationListener
//import kotlinx.android.synthetic.main.activity_locationserver.*
//import okhttp3.FormBody
//import org.xiaoxingqi.shengxi.R
//import org.xiaoxingqi.shengxi.core.BaseAct
//import org.xiaoxingqi.shengxi.core.http.OkClientHelper
//import org.xiaoxingqi.shengxi.core.http.OkResponse
//import org.xiaoxingqi.shengxi.model.BaseRepData
//import org.xiaoxingqi.shengxi.model.PatchData
//import org.xiaoxingqi.shengxi.model.UserInfoData
//import org.xiaoxingqi.shengxi.model.login.LoginData
//import org.xiaoxingqi.shengxi.utils.IConstant
//import org.xiaoxingqi.shengxi.utils.PreferenceTools
//
//class LocationServerActivity : BaseAct(), AMapLocationListener {
//    private var mlocationClient: AMapLocationClient? = null
//    private var mLocationOption: AMapLocationClientOption? = null
//    private var isToggle = 0  //是否开启
//    private var latitude: String? = null
//    private var longitude: String? = null
//    override fun getLayoutId(): Int {
//        return R.layout.activity_locationserver
//    }
//
//    override fun initView() {
//
//    }
//
//    override fun initData() {
//        /**
//         * 1 检查权限来判断显示的内容
//         * 2 是否开启定位功能
//         * 3 是否打开位置功能 (默认开启)
//         * 4
//         */
//        mlocationClient = AMapLocationClient(this)
//        mLocationOption = AMapLocationClientOption()
//        mlocationClient?.setLocationListener(this)
//        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
//        mLocationOption?.locationMode = AMapLocationClientOption.AMapLocationMode.Battery_Saving
//        mLocationOption?.interval = 2000
//        mlocationClient?.setLocationOption(mLocationOption)
//        request(0)
//    }
//
//    override fun initEvent() {
//        relativeOpen.setOnClickListener {
//            if (isToggle == 1) {
//                return@setOnClickListener
//            }
//            if (!isOpen()) {
//                /**
//                 * 打开定位权限
//                 */
//                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
//                startActivityForResult(intent, REQUESTGPS)
//            } else {
//                checkLocation()
//            }
//            iv_Open.visibility = View.VISIBLE
//            iv_Close.visibility = View.GONE
//            isToggle = 1
//            request(1)
//        }
//        relativeClose.setOnClickListener {
//            if (isToggle == 0) {
//                return@setOnClickListener
//            }
//            iv_Open.visibility = View.GONE
//            iv_Close.visibility = View.VISIBLE
//            isToggle = 0
//            request(1)
//        }
//        btn_Back.setOnClickListener {
//            setResult(Activity.RESULT_OK, Intent().putExtra("lat", latitude).putExtra("lng", longitude))
//            finish()
//        }
//    }
//
//    /**
//     * 檢測是否有權限
//     */
//    fun checkLocation() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), REQUSTPERMISSIONCODE)
//            } else {
//                mlocationClient?.startLocation()
//            }
//        } else {
//            mlocationClient?.startLocation()
//        }
//    }
//
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
//    override fun request(flag: Int) {
//        transLayout.showProgress()
//        when (flag) {
//            0 -> {
//                val loginData = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
//                OkClientHelper.get(this, "users/${loginData.user_id}/setting", PatchData::class.java, object : OkResponse {
//                    override fun success(result: Any?) {
//                        result as PatchData
//                        result.data?.let {
//                            if (it.gps_switch == 1) {//打开权限
//                                iv_Open.visibility = View.VISIBLE
//                                iv_Close.visibility = View.GONE
//                                isToggle = 1
//                            } else {//关闭权限
//                                isToggle = 0
//                                iv_Open.visibility = View.GONE
//                                iv_Close.visibility = View.VISIBLE
//                            }
//                        }
//                        transLayout.showContent()
//                    }
//
//                    override fun onFailure(any: Any?) {
//                        transLayout.showContent()
//                    }
//                })
//            }
//            1 -> {
//                val infoData = PreferenceTools.getObj(this, IConstant.USERCACHE, UserInfoData::class.java)
//                val formBody = FormBody.Builder()
//                        .add("gpsSwitch", isToggle.toString())
//                        .build()
//                OkClientHelper.patch(this, "users/${infoData.data.user_id}/setting", formBody, BaseRepData::class.java, object : OkResponse {
//                    override fun success(result: Any?) {
//                        transLayout.showContent()
//                    }
//
//                    override fun onFailure(any: Any?) {
//                        transLayout.showContent()
//                    }
//                })
//            }
//            2 -> {
//                transLayout.showProgress()
//                val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
//                val formbody = FormBody.Builder()
//                        .add("lat", latitude)
//                        .add("lng", longitude)
//                        .build()
//                OkClientHelper.patch(this, "users/${loginBean.user_id}/setting", formbody, BaseRepData::class.java, object : OkResponse {
//                    override fun success(result: Any?) {
//                        transLayout.showContent()
//                    }
//
//                    override fun onFailure(any: Any?) {
//                        transLayout.showContent()
//                    }
//                })
//            }
//        }
//    }
//
//    override fun onLocationChanged(p0: AMapLocation?) {
//        if (p0?.errorCode == 0) {
//            latitude = p0.latitude.toString()//获取纬度
//            longitude = p0.longitude.toString()//获取经度
//            request(2)
//            mlocationClient?.onDestroy()
//        }
//    }
//
//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == REQUSTPERMISSIONCODE) {
//            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                mlocationClient?.startLocation()
//            }
//        }
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == REQUESTGPS) {
//            if (isOpen()) {
//                checkLocation()
//            }
//        }
//    }
//
//    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            setResult(Activity.RESULT_OK, Intent().putExtra("lat", latitude).putExtra("lng", longitude))
//            finish()
//        }
//        return super.onKeyDown(keyCode, event)
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        mlocationClient?.onDestroy()
//    }
//
//}
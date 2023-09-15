#包名:
``org.xiaoxingqi.shengxi``
#so库指引
+ libweibosdkcore.so 微博源生SDK使用
#lib指引
+ miit_mdid_1.0.9.aar  移动安全联盟获取设备唯一标识码
+ buylg 版本为最新版本 3.4
#包结构
>java
>>andnroid.support //放侧拉出菜单的库
>
>>org.xiaoxingqi
>>>org.xiaoxingqi.alarmService 闹钟服务
>
>>>org.xiaoxingqi.shengxi 项目基础服务代码
>
>>>org.xiaoxingqi.yunxin  网易云信的一些服务: 电话,画板
>
>>>org.xiaoxingqi.zxing  初代版本的二维码扫描功能
>>





#Android Q中获取不到设备的唯一标志
支持统一联盟的方案:

不支持的方案: 同一设备获取的唯一值可能重复  

google官方推荐获取广告iD:可变不推荐

首次打开app生成一个随机ID, ID保存在disk中,作为手机唯一的标识符

校验SP中获取的UUid和文件中获取UUid,如果SP中数据为空,则更改file中的数据,获取文件的last-modify 写入文件上一次修改的时间, 是否为手机修改的时间

```
if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {//android 10.0 之后采用此方法保证唯一的标识符

            val spUUid = SPUtils.getString(this, IConstant.CACHEUUID, "")
            val readLine = try {
                val reader = BufferedReader(FileReader(File(Environment.getExternalStorageDirectory(), IConstant.DOCNAME + "/" + IConstant.UUIDFILE)))
                reader.readLine()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
            if (TextUtils.isEmpty(spUUid) && TextUtils.isEmpty(readLine)) {
                //都为空,表示第一次进入app
                val uniqueID = UUID.randomUUID().toString()
                SPUtils.setString(this, IConstant.CACHEUUID, uniqueID)
                FileWriter(Environment.getExternalStorageDirectory().absolutePath + "/" + IConstant.DOCNAME + "/" + IConstant.UUIDFILE).apply {
                    write(uniqueID)
                    close()
                }
            } else if (!TextUtils.isEmpty(readLine) && TextUtils.isEmpty(spUUid)) {
                SPUtils.setString(this, IConstant.CACHEUUID, readLine)
            } else if (!TextUtils.isEmpty(spUUid) && TextUtils.isEmpty(readLine)) {
                FileWriter(Environment.getExternalStorageDirectory().absolutePath + "/" + IConstant.DOCNAME + "/" + IConstant.UUIDFILE).apply {
                    write(spUUid)
                    close()
                }
            } else {//本地文件被篡改
                if (readLine != spUUid) {
                    FileWriter(Environment.getExternalStorageDirectory().absolutePath + "/" + IConstant.DOCNAME + "/" + IConstant.UUIDFILE).apply {
                        write(spUUid)
                        close()
                    }
                }
            }
        }


```
#第三方登录

+ 微博登录国内市场开放网页授权,google play 只允许客户端登录
+ 微信只允许客户端授权
+ QQ登录只允许客户端授权(网页授权引导下载,会被谷歌 play 下架,封ID)

#闹钟功能, 基于一个开源闹钟的基础实现

[git地址](https://github.com/yuriykulikov/AlarmClock )
***
新版本迭代:

>4.1.1 新增闹钟功能
>
>4.2.0 修改语音的录制方式为MediaRecorder  socket为Server监听(android O 后台服务启动问题)  
>
>4.2.1 android o 服务异常, 恢复到Activity同步开启Socket
>
>4.2.2   1.修改socket为后台到server,兼容8.0服务的问题;2.加入日历控件; 3.修改氛围计算方式, 改用`postInvalidateOnAnimation` ;4.加入专辑时光机伪弹幕功能;5.语音录制功能恢复为云信的录制功能,;6.文件添加后缀,避免播放器识别,后缀后面+1".aac1";兼容之前的文件缓存,以后可以直接去除后缀, 一样兼容;
>
>4.3 修改录制语音为mp3 格式, 其他录制出现编解码异常,播放助手, 和cheers功能;视频播放特效功能, 引入移动联盟的设备唯一ID 的获取方式, 目前出现兼容性问题(未使用)
>4.3.1加入成就系统
>4.3.2 修改成就系统, 修改UI文案
>4.3.3修改UI文案
>4.3.4 台词UI整改 ,加入找寻, 修改UI
>
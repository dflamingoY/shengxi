package org.xiaoxingqi.shengxi.utils

class IConstant {
    companion object {
        /**
         * 本地测试环境
         */
//        const val PORT = "http://192.168.31.100:80/api/app/"
//        const val CUSTOM_YX_ID = "sx_local_0"
//        const val YUNXINPORT = "sx_local_"
        /**
         * 线上测试环境
         */
        const val PORT = "https://stagapi.byebyetext.com/api/app/"
        const val CUSTOM_YX_ID = "sx_stag_0"
        const val YUNXINPORT = "sx_stag_"
        const val SOCKETPORT = "wss://stagsocket.byebyetext.com/"

        /**
         * 正式环境
         */
//        const val PORT = "https://sx.byebyetext.com/api/app/"
//        const val CUSTOM_YX_ID = "sx_pro_0" //  线上  pro
//        const val YUNXINPORT = "sx_pro_"
//        const val SOCKETPORT = "wss:sx.byebyetext.com/websocket"

        const val DOCNAME = "shengxi"
        const val CACHE_NAME = ".cache"
        const val MSGRING = "msgRing"
        const val MSGVIBRATE = "msgVibrate"
        const val IMG_CACHE = "imgCache"
        const val QQ_ID = "101461065"
        const val ISFIRSTOPEN = "isFirstOpen"
        const val VOICENAME = "voiceCache"
        const val RESULT_CODE_SUCCESS = 200
        const val DOWNLOAD = "download"
        const val USERCACHE = "userinfo"
        const val TOPICTAG = "topicTag"
        const val TOPIC = "topic"
        const val MOVIE = "movie"
        const val BOOK = "book"
        const val MUSIC = "music"
        const val COMMENTHINT = "isCommentHint"//单页详情是否长按回声删除或者举报
        const val LANGUAGE = "language"
        const val CN = "cn"
        const val EN = "us"
        const val JP = "jp"
        const val HK = "hk"
        const val TW = "tw"
        const val DEFAULTLANGUAGE = "def_Language"
        const val REFRESHLANGUAGE = "EVENT_REFRESH_LANGUAGE"
        const val ISVITAR = "isVitar"
        const val ISSOUND = "isSound"
        const val LOCALTOKEN = "localToken"//存储在本地的token
        const val ISREFRESHTOKEN = "isRefreshToken" //當前是否需要刷新token
        const val LOCALMSGHINTCACHE = "localMsgHintCache"//本地消息状态的缓存
        const val SIGNCACHE = "signCache"
        const val ISEARPIECE = "isEarPiece"//是否是听筒模式
        const val TIMEMACHINEPLAYMODE = "timeMachineMode"//时光机播放的模式   1列表循环 2随机 3单曲循环
        const val PERSONALITYRESULT = "personalityResult"//性格测试结果
        const val THEMEKEY = "themeKey"//当前主题的类型
        const val THEME_RAIN = "themeRain"
        const val THEME_SNOW = "themeSnow"
        const val THEME_FLOWER = "themeFlower"
        const val THEME_LEAVES = "themeLeaves"
        const val WORLD_SHOW_TAB = "worldTab"
        const val LOGCACHE = "logCache"//用户操作日志系统
        const val ISSENDVOICEHINT = "sendHint"//发心情界面展示的提示文案
        const val LISTENARTICLECACHE = "listenArticleCache"//听见页缓存文件
        const val FRIENDPAGECACHE = "friendPageCache"//首頁心情页面的缓存
        const val USERCHARACTERTYPE = "userCharacterType"//用户当前性格 I E类型  用户默认是 I 型
        const val USER_INTROVERT = "I"//内向性格
        const val USER_EXTROVERT = "E"//外向性格
        const val USER_TOPIC_CACHE = "userTopic"
        const val TOTALLENGTH = "totalLength"//发布音频的总时长
        const val TABLENAME = "cacheTable"//表名
        const val STRANGEVIEW = "strangeView"//是否禁止别人浏览自己的相册,
        const val CHANNELNAME = "channelName"//通知栏的别名
        const val FINDRESULT = "findResult"
        const val FINDTIME = "findTime"//寻的时间
        const val ADMINTOKEN = "adminToken"//管理员token
        const val USERLIMITTIME = "userLimitTime"//用户被加入管制名单 关禁闭
        const val IGNORE_READ_PHONE_STATE = "readPhoneState"// 是否已经忽略 第一次请求 获取阅读手机状态的权限
        val userAdminArray = arrayOf("1", "4795", "5461", "6100", "20550", "32335", "373682")
        const val ISPAINSENDHINT = "isPaintHint"//是否关闭了提示
        const val ISHINTUPDATE = "isHintUpdate"//是否需要提示更新
        const val SEASON_PUBLIC = "seasonPublic"// 四季公所有人可见的专辑
        const val SEASON_FRIEND = "seasonFriend"// 四季公所有人可见的专辑
        const val SEASON_PRIVACY = "seasonPrivacy"// 四季公所有人可见的专辑
        const val SEASON_CACHE_PUBLIC = "seasonCachePublic"
        const val SEASON_CACHE_Friend = "seasonCacheFriend"
        const val SEASON_CACHE_PRIVACY = "seasonCachePrivacy"
        const val THEME_MAPLE = "themeMaple"
        const val PERMISSION_DENIED_PHONE = "permissionDeniedPhone"//权限被拒绝之后 阅读手机状态
        const val PERMISSION_DENIED_STORAGE = "permissionDeniedStorage"//权限被拒绝之后 访问文件夹
        const val PERMISSION_DENIED_AUDIO = "permissionDeniedAudio"//权限被拒绝之后 录音权限
        const val GRAFFITI_HINT = "graffitiHint"//涂鸦提示
        const val CACHEUUID = "cacheUid"
        const val UUIDFILE = ".uuidFile.config"
        const val DOWNAUDIO = "downAudio"//下载作为铃声的音频文件夹
        const val PUSH_WORD_HINT_BANNER = "pushWordHintBanner"//发布台词是否需要提示
        const val IS_CLOSE_INTERACT = "isCloseInteract"
        const val LOCALSENSITIVE = "localSensitive.config"
        const val LOCAL_DEFAULT_ALARM = "assets://shengxi_alarm_default.mp3"//本地默认铃声的名称
        const val IGNORE_ALARM_HINT = "alarmHint"
        const val USER_CACHE_COVER_LIST = "userCoverList"//用户的所有封面图本地缓存
        const val USER_CALENDAR_COVER = "userCalendarCover"//保存用户心情日历的月份封面图
        const val USER_THEME_LIGHT_MODEL = "userThemeLight"
        const val USER_THEME_COVER_MODEL = "userThemeCover"
        const val IS_FIRST_PLAY_WAVE = "isFirstPlayWave"//第一次播放声波在我的界面
        const val IS_FIRSE_PLAY_MINI_MACHINE = "isFirstPlayMiniMachine"//第一次播放迷你时光机, 在我的界面
        const val IS_GUIDE_USER_HOME = "isGuideUserHome"//世界是否展示动画的引导
        const val PLAY_MENU_AUTO = "isAuto"//是否自动播放
        const val PLAY_MENU_POSITION = "playMenuPosition"//存放的角标
        const val WORLD_AUTO_THUMB = "worldAutoThumb"
        const val HOME_GUIDE_VISIBLE = "homeGuideVisible"//主页引导是否可见
        const val USER_PWD = "userPwd"
        const val HAS_CHECKED_CODE = "checkedCode"//是否已经验证过二次密码  1 开启强校验,其他不作处理
        const val PWD_DEFAULT_COVER = "pwdDefaultCover"//默认封面
        const val FIRST_PUSH_ACHIEVEMENT = "isFirstAchieveTravel"
        const val FIRST_PUSH_VOICES = "isFirstPushVoices"//今天是否发布音频
        const val FIRST_ALARM_GUIDE = "firstAlarmGuide"
        const val WORD_ENABLE_VOICE = "isEnableWordVoice"//语音转文字是否展示
        const val HOME_WORLD_TITLE_RED_POINT = "homeWorldRedPoint"//世界展示小红点 注册成功之后绑定此id
        const val CHEERS_USER_COVER = "cheersUserCover"
        const val PLAY_MENU_IS_TOAST = "playMenuIsToast"//第一次点击弹提示
        const val PLAY_MENU_IS_TOAST_OPEN = "playMenuIsToastOpen"//第一次点击弹提示
        const val TAB_HOME_INDEX = "homeTabIndex"//
        const val ANIM_READ_STATUS = "cartoonReadStatus"//漫画阅读状态
        const val LIGHT_CACHE = "lightCache"

    }
}
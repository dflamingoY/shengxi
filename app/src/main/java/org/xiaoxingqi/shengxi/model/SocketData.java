package org.xiaoxingqi.shengxi.model;

public class SocketData {


    /**
     * from : sx_stag_5393
     * flag : singleChat
     * data : {"flag":"singleChat","to":"sx_stag_5447","data":{"voiceId":5789,"dialogContentType":1,"dialogContentUri":"user-chat/5393_20181229_e376cb1b8bc519b998e28c8b827d7067.aac","dialogContentLen":7}}
     */
    private int code;
    private String from;
    private String flag;//singleUser 后台推送  singleChat  私聊 回声     allUser   系统消息
    private SocketBean data;
    private String resouces;//源String
    private String msg;//
    private String action;//用户的操作

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public SocketBean getData() {
        return data;
    }

    public void setData(SocketBean data) {
        this.data = data;
    }

    public String getResouces() {
        return resouces;
    }

    public void setResouces(String resouces) {
        this.resouces = resouces;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public static class SocketBean {

        /**
         * chat_num : 0
         * confessed_num : 0
         * fpsss_num : 0
         * frequest_num : 0
         * sys_num : 2
         * user_id : 3
         * zaned_num : 0
         */

        private int chat_num;//私聊
        private int confessed_num;//表白书
        private int fpsss_num;//新好友通过的数量
        private int frequest_num;//好友请求
        private int sys_num;
        private int user_id;
        private int zaned_num;//点赞数
        /**
         * chat_id : 2047
         * created_at : 1546494362
         * dialog_content_len : 2
         * dialog_content_type : 1
         * dialog_content_url : https://sx-stag.oss-cn-shenzhen.aliyuncs.com/user-chat/7308_20190103_3fb1e241f5957ebb5b17835fabbd2e35.aac
         * dialog_id : 4678
         * from_user_id : 7308
         * user_avatar_url : https://sx-stag.oss-cn-shenzhen.aliyuncs.com/user-avatar/default_avatar.jpg?x-oss-process=image/resize,m_fixed,w_90,h_90/quality,q_90/format,webp
         */

        private int chat_type;//1 留声瓶  2 私聊  3 涂鸦对话
        private int chat_id;
        private int created_at;
        private int dialog_content_len;
        private int dialog_content_type;  //1 语音 2  图片
        private String dialog_content_url;
        private int dialog_id;
        private String from_user_id;
        private String user_avatar_url;
        private String auto_reply_msg;//用户设置是否繁忙的状态
        private String offline_prompt;//声兮小二是否处于离线状态

        /**
         * about_id : 0
         * key : newFriend
         * type : 5
         */
        private int about_id;//
        private String key;//系统通知的名称
        private int type;//系统通知类型 15打招呼匹配成功通知 16 新的配音  17 配音下载  1001 收到新的涂鸦对话
        private String voice_id;
        private String title;
        private String voice_user_id;//创建改声兮的用户id
        private String yunxin_id;//云信ID
        private int match_type;//匹配类型  1语音 2 画板
        private String to_user_id;
        private int resource_id;//资源id
        private int resource_user_id;//资源拥有者

        /**
         * action 删除的字段
         *
         * @return
         */
        private String chatId;
        private String dialogId;
        private String voiceId;
        //尬聊时,推送的话题
        private SearchTopicData.SearchTopicBean talking_topic;
        //是否被设置为仙人掌, 所有消息不展示
        private String flag;
        private String actionType;//互动类型
        //匹配互动
        private String id;
        private String nick_name;
        private String avatar_url;
        private int chatType;
        private int resourceId;

        public int getChat_num() {
            return chat_num;
        }

        public void setChat_num(int chat_num) {
            this.chat_num = chat_num;
        }

        public int getConfessed_num() {
            return confessed_num;
        }

        public void setConfessed_num(int confessed_num) {
            this.confessed_num = confessed_num;
        }

        public int getFpsss_num() {
            return fpsss_num;
        }

        public void setFpsss_num(int fpsss_num) {
            this.fpsss_num = fpsss_num;
        }

        public int getFrequest_num() {
            return frequest_num;
        }

        public void setFrequest_num(int frequest_num) {
            this.frequest_num = frequest_num;
        }

        public int getSys_num() {
            return sys_num;
        }

        public void setSys_num(int sys_num) {
            this.sys_num = sys_num;
        }

        public int getUser_id() {
            return user_id;
        }

        public void setUser_id(int user_id) {
            this.user_id = user_id;
        }

        public int getZaned_num() {
            return zaned_num;
        }

        public void setZaned_num(int zaned_num) {
            this.zaned_num = zaned_num;
        }

        public int getChat_id() {
            return chat_id;
        }

        public void setChat_id(int chat_id) {
            this.chat_id = chat_id;
        }

        public int getCreated_at() {
            return created_at;
        }

        public void setCreated_at(int created_at) {
            this.created_at = created_at;
        }

        public int getDialog_content_len() {
            return dialog_content_len;
        }

        public void setDialog_content_len(int dialog_content_len) {
            this.dialog_content_len = dialog_content_len;
        }

        public int getDialog_content_type() {
            return dialog_content_type;
        }

        public void setDialog_content_type(int dialog_content_type) {
            this.dialog_content_type = dialog_content_type;
        }

        public String getDialog_content_url() {
            return dialog_content_url;
        }

        public void setDialog_content_url(String dialog_content_url) {
            this.dialog_content_url = dialog_content_url;
        }

        public int getDialog_id() {
            return dialog_id;
        }

        public void setDialog_id(int dialog_id) {
            this.dialog_id = dialog_id;
        }

        public String getFrom_user_id() {
            return from_user_id;
        }

        public void setFrom_user_id(String from_user_id) {
            this.from_user_id = from_user_id;
        }

        public String getUser_avatar_url() {
            return user_avatar_url;
        }

        public void setUser_avatar_url(String user_avatar_url) {
            this.user_avatar_url = user_avatar_url;
        }

        public int getAbout_id() {
            return about_id;
        }

        public void setAbout_id(int about_id) {
            this.about_id = about_id;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public int getChat_type() {
            return chat_type;
        }

        public void setChat_type(int chat_type) {
            this.chat_type = chat_type;
        }

        public String getVoice_id() {
            return voice_id;
        }

        public void setVoice_id(String voice_id) {
            this.voice_id = voice_id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getVoice_user_id() {
            return voice_user_id;
        }

        public void setVoice_user_id(String voice_user_id) {
            this.voice_user_id = voice_user_id;
        }

        public int getMatch_type() {
            return match_type;
        }

        public void setMatch_type(int match_type) {
            this.match_type = match_type;
        }

        public String getYunxin_id() {
            return yunxin_id;
        }

        public void setYunxin_id(String yunxin_id) {
            this.yunxin_id = yunxin_id;
        }

        public String getAuto_reply_msg() {
            return auto_reply_msg;
        }

        public void setAuto_reply_msg(String auto_reply_msg) {
            this.auto_reply_msg = auto_reply_msg;
        }

        public String getOffline_prompt() {
            return offline_prompt;
        }

        public void setOffline_prompt(String offline_prompt) {
            this.offline_prompt = offline_prompt;
        }

        public String getChatId() {
            return chatId;
        }

        public void setChatId(String chatId) {
            this.chatId = chatId;
        }

        public String getDialogId() {
            return dialogId;
        }

        public void setDialogId(String dialogId) {
            this.dialogId = dialogId;
        }

        public String getVoiceId() {
            return voiceId;
        }

        public void setVoiceId(String voiceId) {
            this.voiceId = voiceId;
        }

        public String getTo_user_id() {
            return to_user_id;
        }

        public void setTo_user_id(String to_user_id) {
            this.to_user_id = to_user_id;
        }

        public SearchTopicData.SearchTopicBean getTalking_topic() {
            return talking_topic;
        }

        public void setTalking_topic(SearchTopicData.SearchTopicBean talking_topic) {
            this.talking_topic = talking_topic;
        }

        public String getFlag() {
            return flag;
        }

        public void setFlag(String flag) {
            this.flag = flag;
        }

        public String getActionType() {
            return actionType;
        }

        public void setActionType(String actionType) {
            this.actionType = actionType;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getNick_name() {
            return nick_name;
        }

        public void setNick_name(String nick_name) {
            this.nick_name = nick_name;
        }

        public String getAvatar_url() {
            return avatar_url;
        }

        public void setAvatar_url(String avatar_url) {
            this.avatar_url = avatar_url;
        }

        public int getResource_id() {
            return resource_id;
        }

        public void setResource_id(int resource_id) {
            this.resource_id = resource_id;
        }

        public int getResource_user_id() {
            return resource_user_id;
        }

        public void setResource_user_id(int resource_user_id) {
            this.resource_user_id = resource_user_id;
        }

        public int getChatType() {
            return chatType;
        }

        public void setChatType(int chatType) {
            this.chatType = chatType;
        }

        public int getResourceId() {
            return resourceId;
        }

        public void setResourceId(int resourceId) {
            this.resourceId = resourceId;
        }
    }


}

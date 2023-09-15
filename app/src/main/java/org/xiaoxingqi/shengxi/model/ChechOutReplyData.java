package org.xiaoxingqi.shengxi.model;

/**
 * 检测是否有权限回复, 并返回对方是否设置了繁忙状态
 */
public class ChechOutReplyData extends BaseRepData {
    private ReplyBean data;

    public ReplyBean getData() {
        return data;
    }

    public void setData(ReplyBean data) {
        this.data = data;
    }

    public static class ReplyBean {
        private int auto_reply;
        private EchoTypesData.EchoTypesBean chat_hobby;

        public int getAuto_reply() {
            return auto_reply;
        }

        public void setAuto_reply(int auto_reply) {
            this.auto_reply = auto_reply;
        }

        public EchoTypesData.EchoTypesBean getChat_hobby() {
            return chat_hobby;
        }

        public void setChat_hobby(EchoTypesData.EchoTypesBean chat_hobby) {
            this.chat_hobby = chat_hobby;
        }
    }
}

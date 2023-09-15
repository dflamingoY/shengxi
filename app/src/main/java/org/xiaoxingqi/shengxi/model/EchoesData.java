package org.xiaoxingqi.shengxi.model;

public class EchoesData extends BaseRepData {
    private EchoesCallBean data;

    public EchoesCallBean getData() {
        return data;
    }

    public void setData(EchoesCallBean data) {
        this.data = data;
    }

    public static class EchoesCallBean {
        private String chat_id;

        public String getChat_id() {
            return chat_id;
        }

        public void setChat_id(String chat_id) {
            this.chat_id = chat_id;
        }
    }
}

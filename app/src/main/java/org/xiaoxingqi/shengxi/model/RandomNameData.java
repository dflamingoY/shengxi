package org.xiaoxingqi.shengxi.model;

public class RandomNameData extends BaseRepData {

    private RandomBean data;

    public RandomBean getData() {
        return data;
    }

    public void setData(RandomBean data) {
        this.data = data;
    }

    public static class RandomBean {
        private String nick_name;

        public String getNick_name() {
            return nick_name;
        }

        public void setNick_name(String nick_name) {
            this.nick_name = nick_name;
        }
    }


}

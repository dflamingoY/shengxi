package org.xiaoxingqi.shengxi.model;


public class BindThirdData extends BaseRepData {

    private DataBean data;

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }


    public static class DataBean {
        private int bindWechat;//0 1
        private int bindWeibo;
        private int bindQQ;

        public int getBindWechat() {
            return bindWechat;
        }

        public void setBindWechat(int bindWechat) {
            this.bindWechat = bindWechat;
        }

        public int getBindWeibo() {
            return bindWeibo;
        }

        public void setBindWeibo(int bindWeibo) {
            this.bindWeibo = bindWeibo;
        }

        public int getBindQQ() {
            return bindQQ;
        }

        public void setBindQQ(int bindQQ) {
            this.bindQQ = bindQQ;
        }
    }
}

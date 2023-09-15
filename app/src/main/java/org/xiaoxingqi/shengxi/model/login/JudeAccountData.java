package org.xiaoxingqi.shengxi.model.login;

import org.xiaoxingqi.shengxi.model.BaseRepData;

public class JudeAccountData extends BaseRepData {


    /**
     * data : {"is_exist":0,"is_forbidden":0}
     */

    private DataBean data;

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * null
         * is_exist : 0
         * is_forbidden : 0
         */
        private int is_exist;//是否注册
        private int is_forbidden; //是否被拉黑
        private int exists;//是否存在，1:存在，0:不存在
        private int forbidden;//是否被禁用，1：是，0:否

        public int getIs_exist() {
            return is_exist;
        }

        public void setIs_exist(int is_exist) {
            this.is_exist = is_exist;
        }

        public int getIs_forbidden() {
            return is_forbidden;
        }

        public void setIs_forbidden(int is_forbidden) {
            this.is_forbidden = is_forbidden;
        }

        public int getExists() {
            return exists;
        }

        public void setExists(int exists) {
            this.exists = exists;
        }

        public int getForbidden() {
            return forbidden;
        }

        public void setForbidden(int forbidden) {
            this.forbidden = forbidden;
        }
    }
}

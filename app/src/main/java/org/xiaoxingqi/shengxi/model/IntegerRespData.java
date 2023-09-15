package org.xiaoxingqi.shengxi.model;

public class IntegerRespData extends BaseRepData {

    private IntegerRespBean data;

    public IntegerRespBean getData() {
        return data;
    }

    public void setData(IntegerRespBean data) {
        this.data = data;
    }

    public static class IntegerRespBean {
        private int id;
        private String sensitive_url;
        private int totalPeople;
        private int total;
        private String check_code;
        private int code_status;//校验码状态，1:正常，2:删除
        private int pick_status;//pick状态，0:没有pick过，1:正在pick中，2:以前pick过
        private double rate;
        private int created_at;
        private int top_status;//0:没有，1:正在pick,2:用户删除，3:系统删除

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getSensitive_url() {
            return sensitive_url;
        }

        public void setSensitive_url(String sensitive_url) {
            this.sensitive_url = sensitive_url;
        }

        public int getTotalPeople() {
            return totalPeople;
        }

        public void setTotalPeople(int totalPeople) {
            this.totalPeople = totalPeople;
        }

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }

        public String getCheck_code() {
            return check_code;
        }

        public void setCheck_code(String check_code) {
            this.check_code = check_code;
        }

        public int getCode_status() {
            return code_status;
        }

        public void setCode_status(int code_status) {
            this.code_status = code_status;
        }

        public int getPick_status() {
            return pick_status;
        }

        public void setPick_status(int pick_status) {
            this.pick_status = pick_status;
        }

        public double getRate() {
            return rate;
        }

        public void setRate(double rate) {
            this.rate = rate;
        }

        public int getCreated_at() {
            return created_at;
        }

        public void setCreated_at(int created_at) {
            this.created_at = created_at;
        }

        public int getTop_status() {
            return top_status;
        }

        public void setTop_status(int top_status) {
            this.top_status = top_status;
        }
    }
}

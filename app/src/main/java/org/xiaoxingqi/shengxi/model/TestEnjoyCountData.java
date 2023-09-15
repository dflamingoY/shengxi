package org.xiaoxingqi.shengxi.model;

public class TestEnjoyCountData extends BaseRepData {

    private EnjoyCountBean data;

    public EnjoyCountBean getData() {
        return data;
    }

    public void setData(EnjoyCountBean data) {
        this.data = data;
    }

    public static class EnjoyCountBean {
        private int num;
        private String pointValue;

        public int getNum() {
            return num;
        }

        public void setNum(int num) {
            this.num = num;
        }

        public String getPointValue() {
            return pointValue;
        }

        public void setPointValue(String pointValue) {
            this.pointValue = pointValue;
        }
    }
}

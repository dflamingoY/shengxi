package org.xiaoxingqi.shengxi.model;

public class AlarmUserBangCountData extends BaseRepData {
    private AlarmUserBangBean data;

    public AlarmUserBangBean getData() {
        return data;
    }

    public void setData(AlarmUserBangBean data) {
        this.data = data;
    }

    public static class AlarmUserBangBean {
        private int num1;
        private int num2;
        private int num3;
        private int dubbing_num;//配音数量

        public int getNum1() {
            return num1;
        }

        public void setNum1(int num1) {
            this.num1 = num1;
        }

        public int getNum2() {
            return num2;
        }

        public void setNum2(int num2) {
            this.num2 = num2;
        }

        public int getNum3() {
            return num3;
        }

        public void setNum3(int num3) {
            this.num3 = num3;
        }

        public int getDubbing_num() {
            return dubbing_num;
        }

        public void setDubbing_num(int dubbing_num) {
            this.dubbing_num = dubbing_num;
        }
    }
}

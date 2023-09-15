package org.xiaoxingqi.shengxi.model;

import java.util.List;

public class EchoTypesData extends BaseRepData {


    private List<EchoTypesBean> data;

    public List<EchoTypesBean> getData() {
        return data;
    }

    public void setData(List<EchoTypesBean> data) {
        this.data = data;
    }

    public static class EchoTypesBean {

        private int id;
        private String name;
        private boolean isSelected;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(boolean selected) {
            isSelected = selected;
        }
    }
}

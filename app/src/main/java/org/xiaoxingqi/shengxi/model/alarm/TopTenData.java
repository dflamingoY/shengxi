package org.xiaoxingqi.shengxi.model.alarm;

import org.xiaoxingqi.shengxi.model.BaseRepData;
import org.xiaoxingqi.shengxi.model.BaseUserBean;

import java.util.List;

public class TopTenData extends BaseRepData {

    private TopTenBean data;

    public TopTenBean getData() {
        return data;
    }

    public void setData(TopTenBean data) {
        this.data = data;
    }

    public static class TopTenBean {

        private List<BaseAlarmBean> list;

        private BaseUserBean user_info;

        public BaseUserBean getUser_info() {
            return user_info;
        }

        public void setUser_info(BaseUserBean user_info) {
            this.user_info = user_info;
        }

        public List<BaseAlarmBean> getList() {
            return list;
        }

        public void setList(List<BaseAlarmBean> list) {
            this.list = list;
        }
    }


}

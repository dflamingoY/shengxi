package org.xiaoxingqi.shengxi.model.alarm;

import com.google.gson.annotations.SerializedName;

import org.xiaoxingqi.shengxi.model.BaseRepData;
import org.xiaoxingqi.shengxi.model.BaseUserBean;
import org.xiaoxingqi.shengxi.model.DataTitleBean;

import java.util.List;

public class LoaderBoardData extends BaseRepData {
    private LoaderBoardBean data;

    public LoaderBoardBean getData() {
        return data;
    }

    public void setData(LoaderBoardBean data) {
        this.data = data;
    }

    public static class LoaderBoardBean {
        private List<LoaderTypeBean> top1;
        private List<LoaderTypeBean> top2;
        private List<LoaderTypeBean> top3;
        private List<LoaderTypeBean> top4;//台词配音状态

        public List<LoaderTypeBean> getTop1() {
            return top1;
        }

        public void setTop1(List<LoaderTypeBean> top1) {
            this.top1 = top1;
        }

        public List<LoaderTypeBean> getTop2() {
            return top2;
        }

        public void setTop2(List<LoaderTypeBean> top2) {
            this.top2 = top2;
        }

        public List<LoaderTypeBean> getTop3() {
            return top3;
        }

        public void setTop3(List<LoaderTypeBean> top3) {
            this.top3 = top3;
        }

        public List<LoaderTypeBean> getTop4() {
            return top4;
        }

        public void setTop4(List<LoaderTypeBean> top4) {
            this.top4 = top4;
        }
    }

    public static class LoaderTypeBean extends DataTitleBean {

        private BaseUserBean user_info;
        private String vote_num;
        private String dubbing_num;

        public BaseUserBean getUser_info() {
            return user_info;
        }

        public void setUser_info(BaseUserBean user_info) {
            this.user_info = user_info;
        }

        public String getVote_num() {
            return vote_num;
        }

        public void setVote_num(String vote_num) {
            this.vote_num = vote_num;
        }

        public String getDubbing_num() {
            return dubbing_num;
        }

        public void setDubbing_num(String dubbing_num) {
            this.dubbing_num = dubbing_num;
        }
    }
}

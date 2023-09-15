package org.xiaoxingqi.shengxi.model;

import java.util.List;

public class FindResultData extends BaseRepData {
    private TagBean data;

    public TagBean getData() {
        return data;
    }

    public void setData(TagBean data) {
        this.data = data;
    }

    public static class TagBean {
        private List<String> tag;

        public List<String> getTag() {
            return tag;
        }

        public void setTag(List<String> tag) {
            this.tag = tag;
        }
    }

}

package org.xiaoxingqi.shengxi.model;

import java.util.List;

public class TestBean {
    private List<QuestionBean> data;

    public List<QuestionBean> getData() {
        return data;
    }

    public void setData(List<QuestionBean> data) {
        this.data = data;
    }

    public static class QuestionBean {
        private String title;
        private List<AnswerBean> answer;
        private int id;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public List<AnswerBean> getAnswer() {
            return answer;
        }

        public void setAnswer(List<AnswerBean> answer) {
            this.answer = answer;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }

    public static class AnswerBean {
        private String desc;
        private String value;
        private boolean isSelect;

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public boolean isSelect() {
            return isSelect;
        }

        public void setSelect(boolean select) {
            isSelect = select;
        }
    }
}

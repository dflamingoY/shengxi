package org.xiaoxingqi.shengxi.model;

public class LaunguageData {

    private String title;
    private String name;
    private boolean isSelect;
    private int id;

    public LaunguageData() {
    }

    public LaunguageData(String title, String name, boolean isSelect) {
        this.title = title;
        this.name = name;
        this.isSelect = isSelect;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}

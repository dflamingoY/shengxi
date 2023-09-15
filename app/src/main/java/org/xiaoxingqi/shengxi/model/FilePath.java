package org.xiaoxingqi.shengxi.model;

public class FilePath {
    private String path;//根文件目录
    private boolean isSelected;//是否选中
    private int count;//数量
    private String firstPath;//封面图
    private String name;//当前文件夹的文件名
    private boolean isPlaying;

    public FilePath(String path, boolean isSelected) {
        this.path = path;
        this.isSelected = isSelected;
    }

    public FilePath() {

    }

    public FilePath(String parentFile, String firstPath) {
        path = parentFile;
        this.firstPath = firstPath;
    }

    public int getCount() {
        return count;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getFirstPath() {
        return firstPath;
    }

    public void setFirstPath(String firstPath) {
        this.firstPath = firstPath;
    }

    public FilePath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FilePath filePath = (FilePath) o;

        if (isSelected() != filePath.isSelected()) return false;
        return getPath().equals(filePath.getPath());
    }

    @Override
    public int hashCode() {
        int result = getPath().hashCode();
        result = 31 * result + (isSelected() ? 1 : 0);
        return result;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }
}

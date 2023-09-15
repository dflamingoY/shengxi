package org.xiaoxingqi.shengxi.model.qiniu;


public class UploadData {

    private String key;
    private String file;

    public UploadData(String key, String file) {
        this.key = key;
        this.file = file;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}

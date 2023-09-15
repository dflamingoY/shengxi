package org.xiaoxingqi.shengxi.model;

public class BaseTalkVoiceBean extends BaseAnimBean {

    /**
     * bucket_id : 0
     * created_at : 1590463220
     * dialog_id : 8568
     * id : 1
     * resource_type : 1
     * resource_uri : user-chat/20200520/704/20200520_113447_8f57d549ff177b36d03c65683ae00d42.aac
     * resource_url : https://sx-stag.oss-cn-shenzhen.aliyuncs.com/user-chat/20200520/704/20200520_113447_8f57d549ff177b36d03c65683ae00d42.aac
     */

    private int created_at;
    private int dialog_id;
    private int id;
    private int resource_type;
    private String resource_uri;
    private String resource_url;
    private int resource_len;
    private int dialog_at;

    public int getCreated_at() {
        return created_at;
    }

    public void setCreated_at(int created_at) {
        this.created_at = created_at;
    }

    public int getDialog_id() {
        return dialog_id;
    }

    public void setDialog_id(int dialog_id) {
        this.dialog_id = dialog_id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getResource_type() {
        return resource_type;
    }

    public void setResource_type(int resource_type) {
        this.resource_type = resource_type;
    }

    public String getResource_uri() {
        return resource_uri;
    }

    public void setResource_uri(String resource_uri) {
        this.resource_uri = resource_uri;
    }

    public String getResource_url() {
        return resource_url;
    }

    public void setResource_url(String resource_url) {
        this.resource_url = resource_url;
    }

    public int getResource_len() {
        return resource_len;
    }

    public void setResource_len(int resource_len) {
        this.resource_len = resource_len;
    }

    public int getDialog_at() {
        return dialog_at;
    }

    public void setDialog_at(int dialog_at) {
        this.dialog_at = dialog_at;
    }
}

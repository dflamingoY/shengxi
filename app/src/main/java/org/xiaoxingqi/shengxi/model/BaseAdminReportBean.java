package org.xiaoxingqi.shengxi.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.xiaoxingqi.shengxi.model.alarm.BaseAlarmAdminBean;
import org.xiaoxingqi.shengxi.model.alarm.BaseAlarmBean;

public class BaseAdminReportBean implements Parcelable {

    private String id;
    private int type_id;//1=人身攻击，2=色情暴力，3=垃圾广告，4=违法信息
    private int resource_type;//1=举报声兮，2=举报会话，3=举报对话，4=举报用户，5:举报艺术品
    private String resource_id;
    private String from_user_id;
    private String to_user_id;
    private String report_status;
    private int created_at;
    private int updated_at;
    private BaseUserBean from_user_info;//举报人信息
    private BaseUserBean to_user_info;//被举报人信息
    private BaseSearchBean resource_detail;
    private String user_id;
    private BaseBean voice;//仅当举报的是心情时返回
    private TalkListData.TalkListBean dialog;//仅当举报的是对话时返回
    private PaintData.PaintBean artwork;//仅当举报的是艺术品时返回
    private boolean isReadTag;
    private PaintData.PaintBean graffiti;
    private BaseAlarmAdminBean line;//台词
    private BaseAlarmAdminBean dubbing;//配音

    public BaseAdminReportBean() {

    }

    protected BaseAdminReportBean(Parcel in) {
        id = in.readString();
        type_id = in.readInt();
        resource_type = in.readInt();
        resource_id = in.readString();
        from_user_id = in.readString();
        to_user_id = in.readString();
        report_status = in.readString();
        created_at = in.readInt();
        updated_at = in.readInt();
        from_user_info = in.readParcelable(BaseUserBean.class.getClassLoader());
        to_user_info = in.readParcelable(BaseUserBean.class.getClassLoader());
        resource_detail = in.readParcelable(BaseSearchBean.class.getClassLoader());
        user_id = in.readString();
        voice = in.readParcelable(BaseBean.class.getClassLoader());
        dialog = in.readParcelable(TalkListData.TalkListBean.class.getClassLoader());
        artwork = in.readParcelable(PaintData.PaintBean.class.getClassLoader());
        graffiti = in.readParcelable(PaintData.PaintBean.class.getClassLoader());
        line = in.readParcelable(BaseAlarmBean.class.getClassLoader());
        dubbing = in.readParcelable(BaseAlarmBean.class.getClassLoader());
    }

    public static final Creator<BaseAdminReportBean> CREATOR = new Creator<BaseAdminReportBean>() {
        @Override
        public BaseAdminReportBean createFromParcel(Parcel in) {
            return new BaseAdminReportBean(in);
        }

        @Override
        public BaseAdminReportBean[] newArray(int size) {
            return new BaseAdminReportBean[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getType_id() {
        return type_id;
    }

    public void setType_id(int type_id) {
        this.type_id = type_id;
    }

    public int getResource_type() {
        return resource_type;
    }

    public void setResource_type(int resource_type) {
        this.resource_type = resource_type;
    }

    public String getResource_id() {
        return resource_id;
    }

    public void setResource_id(String resource_id) {
        this.resource_id = resource_id;
    }

    public String getFrom_user_id() {
        return from_user_id;
    }

    public void setFrom_user_id(String from_user_id) {
        this.from_user_id = from_user_id;
    }

    public String getTo_user_id() {
        return to_user_id;
    }

    public void setTo_user_id(String to_user_id) {
        this.to_user_id = to_user_id;
    }

    public String getReport_status() {
        return report_status;
    }

    public void setReport_status(String report_status) {
        this.report_status = report_status;
    }

    public int getCreated_at() {
        return created_at;
    }

    public void setCreated_at(int created_at) {
        this.created_at = created_at;
    }

    public int getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(int updated_at) {
        this.updated_at = updated_at;
    }

    public BaseUserBean getFrom_user_info() {
        return from_user_info;
    }

    public void setFrom_user_info(BaseUserBean from_user_info) {
        this.from_user_info = from_user_info;
    }

    public BaseUserBean getTo_user_info() {
        return to_user_info;
    }

    public void setTo_user_info(BaseUserBean to_user_info) {
        this.to_user_info = to_user_info;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeInt(type_id);
        dest.writeInt(resource_type);
        dest.writeString(resource_id);
        dest.writeString(from_user_id);
        dest.writeString(to_user_id);
        dest.writeString(report_status);
        dest.writeInt(created_at);
        dest.writeInt(updated_at);
        dest.writeParcelable(from_user_info, flags);
        dest.writeParcelable(to_user_info, flags);
        dest.writeParcelable(resource_detail, flags);
        dest.writeString(user_id);
        dest.writeParcelable(voice, flags);
        dest.writeParcelable(dialog, flags);
        dest.writeParcelable(artwork, flags);
        dest.writeParcelable(graffiti, flags);
        dest.writeParcelable(line, flags);
        dest.writeParcelable(dubbing, flags);
    }

    public BaseSearchBean getResource_detail() {
        return resource_detail;
    }

    public void setResource_detail(BaseSearchBean resource_detail) {
        this.resource_detail = resource_detail;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public BaseBean getVoice() {
        return voice;
    }

    public void setVoice(BaseBean voice) {
        this.voice = voice;
    }

    public TalkListData.TalkListBean getDialog() {
        return dialog;
    }

    public void setDialog(TalkListData.TalkListBean dialog) {
        this.dialog = dialog;
    }

    public PaintData.PaintBean getArtwork() {
        return artwork;
    }

    public void setArtwork(PaintData.PaintBean artwork) {
        this.artwork = artwork;
    }

    public boolean isReadTag() {
        return isReadTag;
    }

    public void setReadTag(boolean readTag) {
        isReadTag = readTag;
    }

    public PaintData.PaintBean getGraffiti() {
        return graffiti;
    }

    public void setGraffiti(PaintData.PaintBean graffiti) {
        this.graffiti = graffiti;
    }

    public BaseAlarmAdminBean getLine() {
        return line;
    }

    public void setLine(BaseAlarmAdminBean line) {
        this.line = line;
    }

    public BaseAlarmAdminBean getDubbing() {
        return dubbing;
    }

    public void setDubbing(BaseAlarmAdminBean dubbing) {
        this.dubbing = dubbing;
    }
}

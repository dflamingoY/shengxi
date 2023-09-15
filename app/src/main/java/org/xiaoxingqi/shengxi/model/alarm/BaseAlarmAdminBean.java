package org.xiaoxingqi.shengxi.model.alarm;

import android.os.Parcel;
import android.os.Parcelable;

import com.alibaba.fastjson.JSON;

import org.xiaoxingqi.shengxi.model.BaseAnimBean;
import org.xiaoxingqi.shengxi.model.BaseUserBean;

public class BaseAlarmAdminBean extends BaseAnimBean implements Parcelable {
    /**
     * created_at : 1573454852
     * dubbing_num : 1
     * id : 9
     * is_anonymous : 0
     * line_content : 您搜狗你明明磨破陪哦逆袭扔啦
     * line_status : 1
     * user : {"avatar_url":"https://sx-stag.oss-cn-shenzhen.aliyuncs.com/user-avatar/234_avatar.jpg?x-oss-process=style/thumb_90_90","id":234,"identity_type":1,"nick_name":"书童"}
     * user_id : 234
     * vote_option_one : 0
     * vote_option_three : 0
     * vote_option_two : 0
     */

    private String created_at = "0";
    private String dubbing_num;
    private String id;
    private String is_anonymous;//是否匿名发布，1:是，0:否
    private String line_content;
    private String line_status;//1：正常，2:用户删除，3:系统删除
    private BaseUserBean user;
    private String user_id;
    private int vote_option_one;
    private int vote_option_three;
    private int vote_option_two;
    private String vote_id;
    private String vote_option;
    //配音 start
    private String dubbing_len;
    private String dubbing_status;//，1:正常，2:用户删除，3:系统删除
    private String dubbing_url;
    private String from_user_id;//配音人ID
    private BaseUserBean from_user_info;
    private String to_user_id;//被配音人的id
    private String line_id;
    /*end*/
    private boolean isDownload;//文件是否下载
    private boolean isSelf;
    private int hide_at;
    private int is_dubbed;//1:是，0:否
    private boolean isDownCached = false;
    private String tag_name;//台词标签
    private String tag_id;//标签Id
    private BaseUserBean to_user_info;//台词所属用户的信息
    private int vote_option_one_d;
    private int vote_option_three_d;
    private int vote_option_two_d;
    private int vote_option_one_w;
    private int vote_option_three_w;
    private int vote_option_two_w;
    private int vote_option_one_m;
    private int vote_option_three_m;
    private int vote_option_two_m;
    private boolean isPick = false;//是否是pick 数据
    private int is_picked;//是否已经设置过pick

    public BaseAlarmAdminBean() {

    }

    protected BaseAlarmAdminBean(Parcel in) {
        super(in);
        created_at = in.readString();
        dubbing_num = in.readString();
        id = in.readString();
        is_anonymous = in.readString();
        line_content = in.readString();
        line_status = in.readString();
        user = in.readParcelable(BaseUserBean.class.getClassLoader());
        user_id = in.readString();
        vote_option_one = in.readInt();
        vote_option_three = in.readInt();
        vote_option_two = in.readInt();
        vote_id = in.readString();
        vote_option = in.readString();
        dubbing_len = in.readString();
        dubbing_status = in.readString();
        dubbing_url = in.readString();
        from_user_id = in.readString();
        from_user_info = in.readParcelable(BaseUserBean.class.getClassLoader());
        to_user_id = in.readString();
        line_id = in.readString();
        hide_at = in.readInt();
        is_dubbed = in.readInt();
        to_user_info = in.readParcelable(BaseUserBean.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(created_at);
        dest.writeString(dubbing_num);
        dest.writeString(id);
        dest.writeString(is_anonymous);
        dest.writeString(line_content);
        dest.writeString(line_status);
        dest.writeParcelable(user, flags);
        dest.writeString(user_id);
        dest.writeInt(vote_option_one);
        dest.writeInt(vote_option_three);
        dest.writeInt(vote_option_two);
        dest.writeString(vote_id);
        dest.writeString(vote_option);
        dest.writeString(dubbing_len);
        dest.writeString(dubbing_status);
        dest.writeString(dubbing_url);
        dest.writeString(from_user_id);
        dest.writeParcelable(from_user_info, flags);
        dest.writeString(to_user_id);
        dest.writeString(line_id);
        dest.writeInt(hide_at);
        dest.writeInt(is_dubbed);
        dest.writeParcelable(to_user_info, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<BaseAlarmAdminBean> CREATOR = new Creator<BaseAlarmAdminBean>() {
        @Override
        public BaseAlarmAdminBean createFromParcel(Parcel in) {
            return new BaseAlarmAdminBean(in);
        }

        @Override
        public BaseAlarmAdminBean[] newArray(int size) {
            return new BaseAlarmAdminBean[size];
        }
    };

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getDubbing_num() {
        return dubbing_num;
    }

    public void setDubbing_num(String dubbing_num) {
        this.dubbing_num = dubbing_num;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIs_anonymous() {
        return is_anonymous;
    }

    public void setIs_anonymous(String is_anonymous) {
        this.is_anonymous = is_anonymous;
    }

    public String getLine_content() {
        return line_content;
    }

    public void setLine_content(String line_content) {
        this.line_content = line_content;
    }

    public String getLine_status() {
        return line_status;
    }

    public void setLine_status(String line_status) {
        this.line_status = line_status;
    }

    public BaseUserBean getUser() {
        return user;
    }

    public void setUser(BaseUserBean user) {
        this.user = user;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public int getVote_option_one() {
        return vote_option_one;
    }

    public void setVote_option_one(int vote_option_one) {
        this.vote_option_one = vote_option_one;
    }

    public int getVote_option_three() {
        return vote_option_three;
    }

    public void setVote_option_three(int vote_option_three) {
        this.vote_option_three = vote_option_three;
    }

    public int getVote_option_two() {
        return vote_option_two;
    }

    public void setVote_option_two(int vote_option_two) {
        this.vote_option_two = vote_option_two;
    }

    public String getVote_id() {
        return vote_id;
    }

    public void setVote_id(String vote_id) {
        this.vote_id = vote_id;
    }

    public String getVote_option() {
        return vote_option;
    }

    public void setVote_option(String vote_option) {
        this.vote_option = vote_option;
    }

    public String getDubbing_len() {
        return dubbing_len;
    }

    public void setDubbing_len(String dubbing_len) {
        voice_len = dubbing_len;
        this.dubbing_len = dubbing_len;
    }

    public String getDubbing_status() {
        return dubbing_status;
    }

    public void setDubbing_status(String dubbing_status) {
        this.dubbing_status = dubbing_status;
    }

    public String getDubbing_url() {
        return dubbing_url;
    }

    public void setDubbing_url(String dubbing_url) {
        this.dubbing_url = dubbing_url;
    }

    public String getFrom_user_id() {
        return from_user_id;
    }

    public void setFrom_user_id(String from_user_id) {
        this.from_user_id = from_user_id;
    }

    public BaseUserBean getFrom_user_info() {
        return from_user_info;
    }

    public void setFrom_user_info(BaseUserBean from_user_info) {
        this.from_user_info = from_user_info;
    }

    public String getTo_user_id() {
        return to_user_id;
    }

    public void setTo_user_id(String to_user_id) {
        this.to_user_id = to_user_id;
    }

    public String getLine_id() {
        return line_id;
    }

    public void setLine_id(String line_id) {
        this.line_id = line_id;
    }

    public boolean isDownload() {
        return isDownload;
    }

    public void setDownload(boolean download) {
        isDownload = download;
    }

    public boolean isSelf() {
        return isSelf;
    }

    public void setSelf(boolean self) {
        isSelf = self;
    }

    public int getHide_at() {
        return hide_at;
    }

    public void setHide_at(int hide_at) {
        this.hide_at = hide_at;
    }

    public int getIs_dubbed() {
        return is_dubbed;
    }

    public void setIs_dubbed(int is_dubbed) {
        this.is_dubbed = is_dubbed;
    }

    public boolean isDownCached() {
        return isDownCached;
    }

    public void setDownCached(boolean downCached) {
        isDownCached = downCached;
    }

    public String getTag_name() {
        return tag_name;
    }

    public void setTag_name(String tag_name) {
        this.tag_name = tag_name;
    }

    public String getTag_id() {
        return tag_id;
    }

    public void setTag_id(String tag_id) {
        this.tag_id = tag_id;
    }

    public BaseUserBean getTo_user_info() {
        return to_user_info;
    }

    public void setTo_user_info(BaseUserBean to_user_info) {
        this.to_user_info = to_user_info;
    }

    public int getVote_option_one_d() {
        return vote_option_one_d;
    }

    public void setVote_option_one_d(int vote_option_one_d) {
        this.vote_option_one_d = vote_option_one_d;
    }

    public int getVote_option_three_d() {
        return vote_option_three_d;
    }

    public void setVote_option_three_d(int vote_option_three_d) {
        this.vote_option_three_d = vote_option_three_d;
    }

    public int getVote_option_two_d() {
        return vote_option_two_d;
    }

    public void setVote_option_two_d(int vote_option_two_d) {
        this.vote_option_two_d = vote_option_two_d;
    }

    public int getVote_option_one_w() {
        return vote_option_one_w;
    }

    public void setVote_option_one_w(int vote_option_one_w) {
        this.vote_option_one_w = vote_option_one_w;
    }

    public int getVote_option_three_w() {
        return vote_option_three_w;
    }

    public void setVote_option_three_w(int vote_option_three_w) {
        this.vote_option_three_w = vote_option_three_w;
    }

    public int getVote_option_two_w() {
        return vote_option_two_w;
    }

    public void setVote_option_two_w(int vote_option_two_w) {
        this.vote_option_two_w = vote_option_two_w;
    }

    public int getVote_option_one_m() {
        return vote_option_one_m;
    }

    public void setVote_option_one_m(int vote_option_one_m) {
        this.vote_option_one_m = vote_option_one_m;
    }

    public int getVote_option_three_m() {
        return vote_option_three_m;
    }

    public void setVote_option_three_m(int vote_option_three_m) {
        this.vote_option_three_m = vote_option_three_m;
    }

    public int getVote_option_two_m() {
        return vote_option_two_m;
    }

    public void setVote_option_two_m(int vote_option_two_m) {
        this.vote_option_two_m = vote_option_two_m;
    }

    public boolean isPick() {
        return isPick;
    }

    public void setPick(boolean pick) {
        isPick = pick;
    }

    public int getIs_picked() {
        return is_picked;
    }

    public void setIs_picked(int is_picked) {
        this.is_picked = is_picked;
    }

}

package org.xiaoxingqi.shengxi.model.login;

import android.os.Parcel;
import android.os.Parcelable;

import org.xiaoxingqi.shengxi.model.BaseRepData;

public class LoginData extends BaseRepData {


    /**
     * data : {"user_id":1,"token":"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOlwvXC8xOTIuMTY4LjMxLjIxNFwvYXBpXC9hdXRoXC9sb2dpbiIsImlhdCI6MTUzNzg0MjI4MSwiZXhwIjoxNTQwNDM0MjgxLCJuYmYiOjE1Mzc4NDIyODEsImp0aSI6IlhQMFA4M05pRmhpTzY4a28iLCJzdWIiOjEsInBydiI6IjIzYmQ1Yzg5NDlmNjAwYWRiMzllNzAxYzQwMDg3MmRiN2E1OTc2ZjcifQ.3eXJxozeQiOZIEfBpSYKWwrMM-equ4M1imcfea7TGII","access_expire":1540434281,"refresh_expire":1540434281}
     */

    private LoginBean data;

    public LoginBean getData() {
        return data;
    }

    public void setData(LoginBean data) {
        this.data = data;
    }

    public static class LoginBean implements Parcelable {
        /**
         * user_id : 1
         * token : eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOlwvXC8xOTIuMTY4LjMxLjIxNFwvYXBpXC9hdXRoXC9sb2dpbiIsImlhdCI6MTUzNzg0MjI4MSwiZXhwIjoxNTQwNDM0MjgxLCJuYmYiOjE1Mzc4NDIyODEsImp0aSI6IlhQMFA4M05pRmhpTzY4a28iLCJzdWIiOjEsInBydiI6IjIzYmQ1Yzg5NDlmNjAwYWRiMzllNzAxYzQwMDg3MmRiN2E1OTc2ZjcifQ.3eXJxozeQiOZIEfBpSYKWwrMM-equ4M1imcfea7TGII
         * access_expire : 1540434281  //先判断短的时间  24.5 H
         * refresh_expire : 1540434281 //在判断大的时间
         */

        private String user_id;
        private String token;
        private int access_expire;       //时间戳，通信token，用于数据通信的过期时间   先判断短期有效时间 ,过期则刷新token
        private int refresh_expire;      //时间戳，通信token，用于刷新通信token的过期时间
        private int need_login_check;//是否开启验证  0:没有设置，1:需要，2:关闭"

        public LoginBean() {

        }

        protected LoginBean(Parcel in) {
            user_id = in.readString();
            token = in.readString();
            access_expire = in.readInt();
            refresh_expire = in.readInt();
            need_login_check = in.readInt();
        }

        public static final Creator<LoginBean> CREATOR = new Creator<LoginBean>() {
            @Override
            public LoginBean createFromParcel(Parcel in) {
                return new LoginBean(in);
            }

            @Override
            public LoginBean[] newArray(int size) {
                return new LoginBean[size];
            }
        };

        public String getUser_id() {
            return user_id;
        }

        public void setUser_id(String user_id) {
            this.user_id = user_id;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public int getAccess_expire() {
            return access_expire;
        }

        public void setAccess_expire(int access_expire) {
            this.access_expire = access_expire;
        }

        public int getRefresh_expire() {
            return refresh_expire;
        }

        public void setRefresh_expire(int refresh_expire) {
            this.refresh_expire = refresh_expire;
        }

        public int getNeed_login_check() {
            return need_login_check;
        }

        public void setNeed_login_check(int need_login_check) {
            this.need_login_check = need_login_check;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(user_id);
            dest.writeString(token);
            dest.writeInt(access_expire);
            dest.writeInt(refresh_expire);
            dest.writeInt(need_login_check);
        }
    }
}

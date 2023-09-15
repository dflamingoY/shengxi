package org.xiaoxingqi.yunxin;

import com.netease.nimlib.sdk.uinfo.constant.GenderEnum;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import java.util.Map;

public class SxUserInfo implements NimUserInfo {

    private String name;
    private String avatar;
    private String account;

    public SxUserInfo() {
    }

    public SxUserInfo(String name, String avatar, String account) {
        this.name = name;
        this.account = account;
        this.avatar = avatar;
    }

    @Override
    public String getSignature() {
        return null;
    }

    @Override
    public GenderEnum getGenderEnum() {
        return null;
    }

    @Override
    public String getEmail() {
        return null;
    }

    @Override
    public String getBirthday() {
        return null;
    }

    @Override
    public String getMobile() {
        return null;
    }

    @Override
    public String getExtension() {
        return null;
    }

    @Override
    public Map<String, Object> getExtensionMap() {
        return null;
    }

    @Override
    public String getAccount() {
        return account;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setName(String name) {
        this.name = name;
    }
}

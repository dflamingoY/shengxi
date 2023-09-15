package org.xiaoxingqi.shengxi.modules.user.set;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationManagerCompat;
import android.view.View;

import org.xiaoxingqi.shengxi.R;
import org.xiaoxingqi.shengxi.core.BaseAct;
import org.xiaoxingqi.shengxi.core.http.OkClientHelper;
import org.xiaoxingqi.shengxi.core.http.OkResponse;
import org.xiaoxingqi.shengxi.model.BaseRepData;
import org.xiaoxingqi.shengxi.model.NewVersionSetData;
import org.xiaoxingqi.shengxi.model.PatchData;
import org.xiaoxingqi.shengxi.model.login.LoginData;
import org.xiaoxingqi.shengxi.utils.AppTools;
import org.xiaoxingqi.shengxi.utils.IConstant;
import org.xiaoxingqi.shengxi.utils.PreferenceTools;
import org.xiaoxingqi.shengxi.wedgit.ToggleLayoutView;
import org.xiaoxingqi.shengxi.wedgit.TransLayout;

import okhttp3.FormBody;

public class MsgHintActivity extends BaseAct {

    private TransLayout transLayout;
    private ToggleLayoutView toggleComment;
    private ToggleLayoutView toggleChat;
    private ToggleLayoutView toggleFriend;
    private ToggleLayoutView toggleNotice;
    private ToggleLayoutView toggleSystem;
    private LoginData.LoginBean loginData;
    private ToggleLayoutView toggleNotify;
    private ToggleLayoutView togglePainter;
    private ToggleLayoutView toggleResource;
    private ToggleLayoutView toggleAlarm;
    private View viewUserHobbit;

    @Override
    public int getLayoutId() {
        return R.layout.activity_msg_hint;
    }

    @Override
    public void initView() {
        transLayout = findViewById(R.id.transLayout);
        toggleComment = findViewById(R.id.toggleComment);
        toggleChat = findViewById(R.id.toggleChat);
        toggleFriend = findViewById(R.id.toggleFriend);
        toggleNotice = findViewById(R.id.toggleNotice);
        toggleSystem = findViewById(R.id.toggleSystem);
        toggleNotify = findViewById(R.id.toggleNotify);
        togglePainter = findViewById(R.id.togglePainter);
        toggleResource = findViewById(R.id.toggleResource);
        toggleAlarm = findViewById(R.id.toggleAlarm);
        viewUserHobbit = findViewById(R.id.viewUserHobbit);
    }

    @Override
    protected void onResume() {
        super.onResume();
        toggleNotify.setSelected(NotificationManagerCompat.from(this).areNotificationsEnabled());
    }

    @Override
    public void initData() {
        loginData = PreferenceTools.getObj(MsgHintActivity.this, IConstant.LOCALTOKEN, LoginData.LoginBean.class);
        PatchData.PatchBean patchBean = PreferenceTools.getObj(this, IConstant.LOCALMSGHINTCACHE + loginData.getUser_id(), PatchData.PatchBean.class);
        if (patchBean != null) {
            toggleComment.setSelected(patchBean.getChat_remind() == 1);
            toggleChat.setSelected(patchBean.getChat_pri_remind() == 1);
            toggleFriend.setSelected(patchBean.getNew_friend_remind() == 1);
            toggleNotice.setSelected(patchBean.getOther_remind() == 1);
            toggleSystem.setSelected(patchBean.getSys_remind() == 1);
        }
        request(0);
        request(1);
    }

    @Override
    public void initEvent() {
        findViewById(R.id.btn_Back).setOnClickListener(view -> finish());
        toggleComment.setOnClickListener(view -> request("chatRemind", toggleComment.isSelected() ? "0" : "1"));
        toggleChat.setOnClickListener(view -> request("chatPriRemind", toggleChat.isSelected() ? "0" : "1"));
        toggleFriend.setOnClickListener(view -> request("newFriendRemind", toggleFriend.isSelected() ? "0" : "1"));
        toggleNotice.setOnClickListener(view -> request("otherRemind", toggleNotice.isSelected() ? "0" : "1"));
        toggleSystem.setOnClickListener(view -> request("sysRemind", toggleSystem.isSelected() ? "0" : "1"));
        toggleNotify.setOnClickListener(view -> {
            Intent localIntent = new Intent();
            localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", this.getPackageName(), null));
            startActivity(localIntent);
        });
        togglePainter.setOnClickListener(view -> saveNewVersion("about_artwork", view.isSelected() ? "2" : "1"));
        toggleResource.setOnClickListener(view -> saveNewVersion("resource_subscription", view.isSelected() ? "2" : "1"));
        toggleAlarm.setOnClickListener(view -> saveNewVersion("about_clock_vote", view.isSelected() ? "2" : "1"));
        viewUserHobbit.setOnClickListener(view -> startActivity(new Intent(this, UserTalkSettingActivity.class)));
    }

    private void saveNewVersion(String key, String value) {
        FormBody formBody = new FormBody.Builder().add("settingTag", "notice").add("settingName", key)
                .add("settingValue", value)
                .build();
        transLayout.showProgress();
        OkClientHelper.patch(this, "users/" + loginData.getUser_id() + "/settings", formBody, BaseRepData.class, new OkResponse() {
            @Override
            public void success(Object result) {
                BaseRepData rep = (BaseRepData) result;
                if (rep.getCode() == 0) {
                    request(1);
                } else {
                    showToast(rep.getMsg());
                    transLayout.showContent();
                }
            }

            @Override
            public void onFailure(Object any) {
                transLayout.showContent();
            }
        }, "V4.2");
    }

    private void request(String key, String value) {
        transLayout.showProgress();
        FormBody formBody = new FormBody.Builder()
                .add(key, value)
                .build();
        OkClientHelper.patch(this, "users/" + loginData.getUser_id() + "/setting", formBody, BaseRepData.class, new OkResponse() {
            @Override
            public void success(Object result) {
                request(0);
            }

            @Override
            public void onFailure(Object any) {
                if (!AppTools.isNetOk(MsgHintActivity.this)) {
                    showToast("网络连接异常");
                }
                transLayout.showContent();
            }
        });
    }

    @Override
    public void request(int flag) {
        transLayout.showProgress();
        if (flag == 0) {
            OkClientHelper.get(this, "users/" + loginData.getUser_id() + "/setting", PatchData.class, new OkResponse() {
                @Override
                public void success(Object result) {
                    PatchData data = (PatchData) result;
                    if (data.getCode() == 0) {
                        PreferenceTools.saveObj(MsgHintActivity.this, IConstant.LOCALMSGHINTCACHE + loginData.getUser_id(), data.getData());
                        toggleComment.setSelected(data.getData().getChat_remind() == 1);
                        toggleChat.setSelected(data.getData().getChat_pri_remind() == 1);
                        toggleFriend.setSelected(data.getData().getNew_friend_remind() == 1);
                        toggleNotice.setSelected(data.getData().getOther_remind() == 1);
                        toggleSystem.setSelected(data.getData().getSys_remind() == 1);
                    }
                    transLayout.showContent();
                }

                @Override
                public void onFailure(Object any) {
                    transLayout.showContent();
                }
            });
        } else if (flag == 1) {
            OkClientHelper.get(this, "users/" + loginData.getUser_id() + "/settings?settingName=&settingTag=notice", NewVersionSetData.class, new OkResponse() {
                @Override
                public void success(Object result) {
                    NewVersionSetData data = (NewVersionSetData) result;
                    if (data.getCode() == 0 && data.getData() != null) {
                        for (NewVersionSetData.VersionSetBean datum : data.getData()) {
                            if ("about_artwork".equals(datum.getSetting_name())) {
                                togglePainter.setSelected(datum.getSetting_value() == 1);
                            } else if ("resource_subscription".equals(datum.getSetting_name())) {
                                toggleResource.setSelected(datum.getSetting_value() == 1);
                            } else if ("about_clock_vote".equals(datum.getSetting_name())) {
                                toggleAlarm.setSelected(datum.getSetting_value() == 1);
                            }
                        }
                    }
                    transLayout.showContent();
                }

                @Override
                public void onFailure(Object any) {
                    transLayout.showContent();
                }
            }, "V4.2");
        }
    }
}

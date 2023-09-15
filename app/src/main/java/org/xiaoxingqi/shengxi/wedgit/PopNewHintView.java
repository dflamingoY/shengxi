package org.xiaoxingqi.shengxi.wedgit;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;

import org.xiaoxingqi.shengxi.R;
import org.xiaoxingqi.shengxi.model.SocketData;
import org.xiaoxingqi.shengxi.modules.echoes.ChatActivity;
import org.xiaoxingqi.shengxi.modules.echoes.MsgNotifyActivity;
import org.xiaoxingqi.shengxi.modules.echoes.NewFriendsActivity;
import org.xiaoxingqi.shengxi.modules.echoes.SystemInfoActivity;
import org.xiaoxingqi.shengxi.modules.echoes.TalkListActivity;
import org.xiaoxingqi.shengxi.modules.listen.ActionActivity;
import org.xiaoxingqi.shengxi.modules.listen.MagicCanvasActivity;
import org.xiaoxingqi.shengxi.modules.listen.VoiceConnectActivity;
import org.xiaoxingqi.shengxi.modules.listen.soulCanvas.TalkGraffitiDetailsActivity;
import org.xiaoxingqi.shengxi.modules.publicmoudle.RecordVoiceActivity;
import org.xiaoxingqi.shengxi.utils.AppTools;

public class PopNewHintView {

    private static ViewGroup group;
    private static View view;
    private static Activity mactivity;//上一个Activity
    private static ValueAnimator valueAnimator;

    /**
     * @param activity
     * @param data     展示的数据
     */
    public static void attach(Activity activity, SocketData.SocketBean data) {
        if (mactivity != null) {//已经创建
            if (mactivity == activity) {//当前界面 重置时间
                //                valueAnimator.cancel();
                //                valueAnimator.start();
            } else {//非当前界面  置空
                group.removeView(view);
                mactivity = activity;
                group = (ViewGroup) activity.getWindow().getDecorView();
                view = View.inflate(activity, R.layout.layout_new_msg_toast, null);
                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, AppTools.dp2px(activity, 60));
                group.addView(view, params);
                ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationY", -AppTools.dp2px(activity, 60), 0f);
                animator.setDuration(500);
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        /**
                         * 开始计时  3s 倒计时
                         */
                        valueAnimator = ValueAnimator.ofFloat(0, 1).setDuration(2000);
                        valueAnimator.start();
                        valueAnimator.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                dettach();
                            }
                        });
                    }
                });
                animator.start();
            }
        } else {//未创建
            mactivity = activity;
            group = (ViewGroup) activity.getWindow().getDecorView();
            view = View.inflate(activity, R.layout.layout_new_msg_toast, null);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, AppTools.dp2px(activity, 50));
            group.addView(view, params);
            ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationY", -AppTools.dp2px(activity, 50), 0f);
            animator.setDuration(500);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    /**
                     * 开始计时  3s 倒计时
                     */
                    valueAnimator = ValueAnimator.ofFloat(0, 1).setDuration(2000);
                    valueAnimator.start();
                    valueAnimator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            dettach();
                        }
                    });
                }
            });
            animator.start();
        }
        TextView tvHint = view.findViewById(R.id.tvHint);
        tvHint.setText(data.getTitle());
        view.setOnClickListener(v -> {
            if (valueAnimator != null)
                valueAnimator.cancel();
            valueAnimator = null;
            dettach();
            Intent intent = new Intent();
            switch (data.getType()) {
                case 1://系统消息
                    intent.setClass(activity, SystemInfoActivity.class);
                    break;
                case 2://好友
                    intent.setClass(activity, NewFriendsActivity.class);
                    break;
                case 3://共鸣表白
                case 4://表白
                case 5://好友通知
                case 14://关注了心情类
                case 16://配音
                case 17://下载
                case 18://下载
                case 19://下载
                case 20://下载
                case 21://下载
                case 22://下载
                case 23://设置为pick
                case 25://会话收藏
                case 26://置顶
                case 27://送了画
                    intent.setClass(activity, MsgNotifyActivity.class);
                    break;
                case 6://新的会话
                case 7:
                    intent.setClass(activity, TalkListActivity.class)
                            .putExtra("voice_id", data.getVoice_id())
                            .putExtra("chat_id", data.getChat_id() + "")
                            .putExtra("talkId", data.getFrom_user_id())
                            .putExtra("uid", data.getVoice_user_id());
                    break;
                case 8://系统回复
                    intent = new Intent(activity, ActionActivity.class)
                            .putExtra("isHtml", true)
                            .putExtra("isScroll", true)
                            .putExtra("url", data.getAbout_id() + "");

                    break;
                case 9://新的私聊会话
                case 10:
                    intent = new Intent(activity, ChatActivity.class)
                            .putExtra("uid", data.getFrom_user_id())
                            .putExtra("chatId", data.getChat_id() + "")
                    ;
                    break;
                case 1001:
                    intent = new Intent(activity, TalkGraffitiDetailsActivity.class)
                            .putExtra("resourceId", data.getResource_id())
                            .putExtra("from", data.getFrom_user_id())
                            .putExtra("uid", data.getResource_user_id() + "")
                            .putExtra("chatId", data.getChat_id() + "");
                    break;
            }
            try {
                if (!(activity instanceof RecordVoiceActivity) && !(activity instanceof MagicCanvasActivity) && !(activity instanceof VoiceConnectActivity)) {
                    activity.startActivity(intent);
                }
            } catch (Exception e) {
                e.printStackTrace();
                dettach();
            }
        });
    }

    /**
     * 移除窗体
     */
    public synchronized static void dettach() {
        if (group != null && view != null) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationY", -AppTools.dp2px(mactivity, 60));
            animator.setDuration(320);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (group != null && view != null) {
                        group.removeView(view);
                        group = null;
                        view = null;
                    }
                    mactivity = null;
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);

                }
            });
            animator.start();
        }
    }


}

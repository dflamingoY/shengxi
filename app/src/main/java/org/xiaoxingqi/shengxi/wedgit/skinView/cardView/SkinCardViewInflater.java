package org.xiaoxingqi.shengxi.wedgit.skinView.cardView;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

import org.xiaoxingqi.shengxi.wedgit.AutoSplitTextView;
import org.xiaoxingqi.shengxi.wedgit.BorderStrokeEditText;
import org.xiaoxingqi.shengxi.wedgit.CartoonProgress;
import org.xiaoxingqi.shengxi.wedgit.CheersProgress;
import org.xiaoxingqi.shengxi.wedgit.CircleCountDown;
import org.xiaoxingqi.shengxi.wedgit.ColorArraysTextView;
import org.xiaoxingqi.shengxi.wedgit.CustomCheckImageView;
import org.xiaoxingqi.shengxi.wedgit.EchoTypeView;
import org.xiaoxingqi.shengxi.wedgit.EchoesProgress;
import org.xiaoxingqi.shengxi.wedgit.GroupToggleView;
import org.xiaoxingqi.shengxi.wedgit.HeartWaveView;
import org.xiaoxingqi.shengxi.wedgit.LayoutTopUserButton;
import org.xiaoxingqi.shengxi.wedgit.LinearStatusText;
import org.xiaoxingqi.shengxi.wedgit.NumberPickerView;
import org.xiaoxingqi.shengxi.wedgit.PagerSlidingTabStripExtends;
import org.xiaoxingqi.shengxi.wedgit.PlayWifiView;
import org.xiaoxingqi.shengxi.wedgit.ProgressSeekBar;
import org.xiaoxingqi.shengxi.wedgit.SelectRecommendView;
import org.xiaoxingqi.shengxi.wedgit.SkinChronometer;
import org.xiaoxingqi.shengxi.wedgit.SkinColorsTextView;
import org.xiaoxingqi.shengxi.wedgit.SwitchButton;
import org.xiaoxingqi.shengxi.wedgit.UserSeekBar;
import org.xiaoxingqi.shengxi.wedgit.ViewMoreGroupView;
import org.xiaoxingqi.shengxi.wedgit.ViewToggleAlarm;
import org.xiaoxingqi.shengxi.wedgit.ViewUserIdentity;
import org.xiaoxingqi.shengxi.wedgit.VoiceAnimaProgress;
import org.xiaoxingqi.shengxi.wedgit.VoiceProgress;
import org.xiaoxingqi.shengxi.wedgit.actionTabar.CustomSeletcor;
import org.xiaoxingqi.shengxi.wedgit.calendar.CalendarMonthView;
import org.xiaoxingqi.shengxi.wedgit.canvas.CircleColorSelectorView;
import org.xiaoxingqi.shengxi.wedgit.canvas.MultiColorView;
import org.xiaoxingqi.shengxi.wedgit.discretescrollview.DiscreteScrollView;
import org.xiaoxingqi.shengxi.wedgit.skinView.SkinStatueText;
import org.xiaoxingqi.zxing.view.GroupChildView;

import skin.support.app.SkinLayoutInflater;

/**
 * Created by ximsf on 2017/3/5.
 */

public class SkinCardViewInflater implements SkinLayoutInflater {
    @Override
    public View createView(@NonNull Context context, final String name, @NonNull AttributeSet attrs) {
        View view = null;
        switch (name) {
            case "android.support.v7.widget.CardView":
                view = new SkinCompatCardView(context, attrs);
                break;
            case "org.xiaoxingqi.shengxi.wedgit.actionTabar.CustomSeletcor":
                view = new CustomSeletcor(context, attrs);
                break;
            case "org.xiaoxingqi.shengxi.wedgit.skinView.SkinStatueText":
                view = new SkinStatueText(context, attrs);
                break;
            case "org.xiaoxingqi.shengxi.wedgit.PagerSlidingTabStripExtends":
                view = new PagerSlidingTabStripExtends(context, attrs);
                break;
            case "org.xiaoxingqi.shengxi.wedgit.LinearStatusText":
                view = new LinearStatusText(context, attrs);
                break;
            case "org.xiaoxingqi.shengxi.wedgit.SelectRecommendView":
                view = new SelectRecommendView(context, attrs);
                break;
            case "org.xiaoxingqi.shengxi.wedgit.ViewMoreGroupView":
                view = new ViewMoreGroupView(context, attrs);
                break;
            case "org.xiaoxingqi.shengxi.wedgit.SkinChronometer":
                view = new SkinChronometer(context, attrs);
                break;
            case "org.xiaoxingqi.shengxi.wedgit.SkinColorsTextView":
                view = new SkinColorsTextView(context, attrs);
                break;
            case "org.xiaoxingqi.shengxi.wedgit.VoiceProgress":
                view = new VoiceProgress(context, attrs);
                break;
            case "org.xiaoxingqi.shengxi.wedgit.VoiceAnimaProgress":
                view = new VoiceAnimaProgress(context, attrs);
                break;
            case "org.xiaoxingqi.shengxi.wedgit.EchoesProgress":
                view = new EchoesProgress(context, attrs);
                break;
            case "org.xiaoxingqi.shengxi.wedgit.BorderStrokeEditText":
                view = new BorderStrokeEditText(context, attrs);
                break;
            case "org.xiaoxingqi.shengxi.wedgit.SwitchButton":
                view = new SwitchButton(context, attrs);
                break;
            case "org.xiaoxingqi.shengxi.wedgit.ViewUserIdentity":
                view = new ViewUserIdentity(context, attrs);
                break;
            case "org.xiaoxingqi.shengxi.wedgit.canvas.CircleColorSelectorView":
                view = new CircleColorSelectorView(context, attrs);
                break;
            case "org.xiaoxingqi.shengxi.wedgit.canvas.MultiColorView":
                view = new MultiColorView(context, attrs);
                break;
            case "org.xiaoxingqi.shengxi.wedgit.AutoSplitTextView":
                view = new AutoSplitTextView(context, attrs);
                break;
            case "org.xiaoxingqi.shengxi.wedgit.GroupToggleView":
                view = new GroupToggleView(context, attrs);
                break;
            case "org.xiaoxingqi.shengxi.wedgit.ViewToggleAlarm":
                view = new ViewToggleAlarm(context, attrs);
                break;
            case "org.xiaoxingqi.shengxi.wedgit.CustomCheckImageView":
                view = new CustomCheckImageView(context, attrs);
                break;
            case "org.xiaoxingqi.shengxi.wedgit.NumberPickerView":
                view = new NumberPickerView(context, attrs);
                break;
            case "org.xiaoxingqi.shengxi.wedgit.calendar.CalendarMonthView":
                view = new CalendarMonthView(context, attrs);
                break;
            case "org.xiaoxingqi.shengxi.wedgit.EchoTypeView":
                view = new EchoTypeView(context, attrs);
                break;
            case "org.xiaoxingqi.shengxi.wedgit.LayoutTopUserButton":
                view = new LayoutTopUserButton(context, attrs);
                break;
            case "org.xiaoxingqi.shengxi.wedgit.UserSeekBar":
                view = new UserSeekBar(context, attrs);
                break;
            case "org.xiaoxingqi.shengxi.wedgit.CircleCountDown":
                view = new CircleCountDown(context, attrs);
                break;
            case "org.xiaoxingqi.shengxi.wedgit.discretescrollview.DiscreteScrollView":
                view = new DiscreteScrollView(context, attrs);
                break;
            case "org.xiaoxingqi.shengxi.wedgit.ColorArraysTextView":
                view = new ColorArraysTextView(context, attrs);
                break;
            case "org.xiaoxingqi.shengxi.wedgit.CheersProgress":
                view = new CheersProgress(context, attrs);
                break;
            case "org.xiaoxingqi.shengxi.wedgit.PlayWifiView":
                view = new PlayWifiView(context, attrs);
                break;
            case "org.xiaoxingqi.shengxi.wedgit.CartoonProgress":
                view = new CartoonProgress(context, attrs);
                break;
            case "org.xiaoxingqi.zxing.view.GroupChildView":
                view = new GroupChildView(context, attrs);
                break;
            case "org.xiaoxingqi.shengxi.wedgit.HeartWaveView":
                view = new HeartWaveView(context, attrs);
                break;
            default:
                break;
        }
        return view;
    }
}

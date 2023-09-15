package org.xiaoxingqi.shengxi.wedgit.canvas;

import android.animation.Animator;
import android.transition.ChangeBounds;
import android.transition.TransitionValues;
import android.view.ViewGroup;

public class CircleChangeBound  extends ChangeBounds{

    @Override
    public Animator createAnimator(ViewGroup sceneRoot, TransitionValues startValues, TransitionValues endValues) {
        Animator changeBounds = super.createAnimator(sceneRoot, startValues, endValues);

//        ViewAnimationUtils.createCircularReveal(sceneRoot,)


        return super.createAnimator(sceneRoot, startValues, endValues);
    }
}

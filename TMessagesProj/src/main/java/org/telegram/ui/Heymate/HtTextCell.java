package org.telegram.ui.Heymate;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Typeface;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Components.LayoutHelper;

public class HtTextCell extends LinearLayout {
    private TextView titleLabel;
    private boolean selected;

    public HtTextCell(Context context) {
        super(context);
        setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(8), Theme.getColor(Theme.key_actionBarDefaultSubmenuBackground)));
        titleLabel = new TextView(context);
        titleLabel.setTypeface(titleLabel.getTypeface(), Typeface.BOLD);
        addView(titleLabel, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 10,10,10,10));
    }

    public String getText(){
        return titleLabel.getText().toString();
    }

    public void setText(String title){
        titleLabel.setText(title);
    }

    public void setTextColor(int color){
        titleLabel.setTextColor(color);
    }

    public void select(){
        int colorFrom = Theme.getColor(Theme.key_actionBarDefaultSubmenuBackground);
        int colorTo = Theme.getColor(Theme.key_chat_serviceBackgroundSelected);
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(320);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(8), (int) animator.getAnimatedValue()));
            }

        });
        colorAnimation.start();
    }

    public void unSelect(){
        int colorFrom = Theme.getColor(Theme.key_chat_serviceBackgroundSelected);
        int colorTo = Theme.getColor(Theme.key_actionBarDefaultSubmenuBackground);
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(320);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(8), (int) animator.getAnimatedValue()));            }

        });
        colorAnimation.start();
    }
}

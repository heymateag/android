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
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Components.LayoutHelper;

public class HtTextCell extends LinearLayout {

    private TextView titleLabel;
    private boolean selected;

    public HtTextCell(Context context) {
        super(context);
        setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(8), Theme.getColor(Theme.key_dialogTextBlue)));
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
        int colorFrom = Theme.getColor(Theme.key_dialogTextBlue);
        int colorTo = Theme.getColor(Theme.key_dialogTextBlue3);
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(320);
        setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(8), Theme.getColor(Theme.key_dialogTextBlue)));
        colorAnimation.start();
    }

    public void unSelect(){
        int colorFrom = Theme.getColor(Theme.key_dialogTextBlue3);
        int colorTo = Theme.getColor(Theme.key_dialogTextBlue);
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(320);
        setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(8), Theme.getColor(Theme.key_dialogTextBlue3)));
        colorAnimation.start();
    }
}

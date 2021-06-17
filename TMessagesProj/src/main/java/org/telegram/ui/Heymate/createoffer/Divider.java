package org.telegram.ui.Heymate.createoffer;

import android.content.Context;
import android.widget.FrameLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;

public class Divider extends FrameLayout {

    public Divider(Context context) {
        super(context);
        setBackgroundColor(Theme.getColor(Theme.key_graySection));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(1), MeasureSpec.EXACTLY));
    }

}

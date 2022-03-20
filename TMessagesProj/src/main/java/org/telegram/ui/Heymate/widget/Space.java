package org.telegram.ui.Heymate.widget;

import android.content.Context;
import android.view.View;

import org.telegram.ui.ActionBar.Theme;

public class Space extends View {

    private final int height;

    public Space(Context context, int height, String color) {
        super(context);

        this.height = height;

        setBackgroundColor(Theme.getColor(color));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }

}

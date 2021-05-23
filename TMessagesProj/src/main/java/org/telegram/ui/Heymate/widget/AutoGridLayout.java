package org.telegram.ui.Heymate.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import org.telegram.ui.Components.Rect;

public class AutoGridLayout extends ViewGroup {

    private final Rect mRect = new Rect();

    public AutoGridLayout(Context context) {
        super(context);
        initialize(context, null, 0);
    }

    public AutoGridLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0);
    }

    public AutoGridLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(Context context, AttributeSet attrs, int defStyleAttr) {

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int width = getWidth();
        int height = getHeight();

        int childCount = getChildCount();

        layoutChildren(0, 0, width, height, 0, childCount);
    }

    private void layoutChildren(int left, int top, int right, int bottom, int start, int end) {
        if (end - start == 0) {
            return;
        }

        if (end - start == 1) {
            View child = getChildAt(start);
            child.measure(
                    MeasureSpec.makeMeasureSpec(right - left, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(bottom - top, MeasureSpec.EXACTLY)
            );
            child.layout(left, top, right, bottom);
            return;
        }

        if (right - left > bottom - top) {
            layoutChildren(left, top, (left + right) / 2, bottom, start, (start + end) / 2);
            layoutChildren((left + right) / 2, top, right, bottom,(start + end) / 2, end);
        }
        else {
            layoutChildren(left, top, right, (top + bottom) / 2, start, (start + end) / 2);
            layoutChildren(left, (top + bottom) / 2, right, bottom, (start + end) / 2, end);
        }
    }

}

package org.telegram.ui.Heymate.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import org.telegram.messenger.AndroidUtilities;

public class DraggableOverlay extends ViewGroup {

    private final Rect mBounds = new Rect();
    private final Rect mHelper = new Rect();

    private GestureDetector mGestureDetector;

    private View mSelectedChild = null;

    public DraggableOverlay(Context context) {
        super(context);
        initialize(context, null, 0);
    }

    public DraggableOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0);
    }

    public DraggableOverlay(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(Context context, AttributeSet attrs, int defStyleAttr) {
        mGestureDetector = new GestureDetector(getContext(), mOnGestureListener);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mBounds.set(0, 0, getWidth(), getHeight());

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            LayoutParams params = (LayoutParams) child.getLayoutParams();

            if (params.width < 0 || params.height < 0) {
                continue;
            }

            if (params.left == -1 && params.top == -1) {
                Gravity.apply(params.gravity, params.width, params.height, mBounds, mHelper);

                params.left = mHelper.left;
                params.top = mHelper.top;
            }

            if (params.left < params.leftMargin) {
                params.left = params.leftMargin;
            }
            else if (params.left + params.width > getWidth() - params.rightMargin) {
                params.left = getWidth() - params.rightMargin - params.width;
            }

            if (params.top < params.topMargin) {
                params.top = params.topMargin;
            }
            else if (params.top + params.height > getHeight() - params.bottomMargin) {
                params.top = getHeight() - params.bottomMargin - params.height;
            }

            if (child.getMeasuredWidth() != params.width || child.getMeasuredHeight() != params.height) {
                child.measure(
                        MeasureSpec.makeMeasureSpec(params.width, MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(params.height, MeasureSpec.EXACTLY)
                );
            }

            child.layout(params.left, params.top, params.left + params.width, params.top + params.height);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    private final GestureDetector.OnGestureListener mOnGestureListener = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onDown(MotionEvent e) {
            mSelectedChild = null;

            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);

                if (child.getLeft() < e.getX() && child.getRight() > e.getX() && child.getTop() < e.getY() && child.getBottom() > e.getY()) {
                    mSelectedChild = child;
                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            LayoutParams params = (LayoutParams) mSelectedChild.getLayoutParams();

            params.left += -distanceX;
            params.top += -distanceY;

            requestLayout();

            return true;
        }

    };

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(AndroidUtilities.dp(120), AndroidUtilities.dp(180));
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    public static class LayoutParams extends MarginLayoutParams {

        public int gravity = Gravity.RIGHT | Gravity.BOTTOM;

        private int left = -1;
        private int top = -1;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

    }

}

package org.telegram.ui.Heymate.widget;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/*
        oo * *
        * oo *
        * * oo
 */
public class PinIndexIndicator extends View {

    private static final long ANIMATION_DURATION = 200;
    private static final int COUNT = 3;

    private Paint mMainPaint;
    private Paint mOthersPaint;

    private int mIndex = 0;

    private float mRatio = 0;

    private float mRadius;

    private ObjectAnimator mAnimator = null;

    private final RectF mHelperRect = new RectF();

    public PinIndexIndicator(Context context) {
        super(context);
        initialize(context, null, 0);
    }

    public PinIndexIndicator(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0);
    }

    public PinIndexIndicator(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        mMainPaint = new Paint();
        mMainPaint.setAntiAlias(true);
        mMainPaint.setStyle(Paint.Style.FILL);

        mOthersPaint = new Paint(mMainPaint);
    }

    public void setMainColor(int color) {
        mMainPaint.setColor(color);
        invalidate();
    }

    public void setOthersColor(int color) {
        mOthersPaint.setColor(color);
        invalidate();
    }

    public void setRatio(float ratio) {
        mRatio = ratio;
        invalidate();
    }

    public void setIndex(int index, boolean animated) {
        index = Math.max(0, Math.min(index, COUNT - 1));

        stopAnimation();

        if (!animated || mIndex == index) {
            mIndex = index;
            mRatio = 0;
            invalidate();
        }
        else {
            final int targetIndex = index;

            if (index < mIndex) {
                mIndex = index;
                mRatio = 1;

                mAnimator = ObjectAnimator.ofFloat(this, "ratio", 1, 0);
            }
            else {
                mIndex = index - 1;
                mRatio = 0;

                mAnimator = ObjectAnimator.ofFloat(this, "ratio", 0, 1);
            }

            mAnimator.addListener(new Animator.AnimatorListener() {

                @Override
                public void onAnimationEnd(Animator animation) {
                    mIndex = targetIndex;
                    mRatio = 0;

                    invalidate();

                    mAnimator = null;
                }

                @Override public void onAnimationStart(Animator animation) { }
                @Override public void onAnimationCancel(Animator animation) { }
                @Override public void onAnimationRepeat(Animator animation) { }

            });

            mAnimator.setDuration(ANIMATION_DURATION);
            mAnimator.start();
            invalidate();
        }
    }

    public void stopAnimation() {
        if (mAnimator != null) {
            mAnimator.end();
            mAnimator = null;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        float verticalRadius = h / 2f;

        // (count + 1) * 2 * r + (count - 1) * r = w
        // r * (3 * count + 1) = w
        float horizontalRadius = w / (3f * COUNT + 1f);

        mRadius = Math.min(verticalRadius, horizontalRadius);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;

        float drawWidth = (3 * COUNT + 1) * mRadius;

        float currentX = centerX - drawWidth / 2f + mRadius;

        for (int i = 0; i < mIndex; i++) {
            canvas.drawCircle(currentX, centerY, mRadius, mOthersPaint);
            currentX += 3 * mRadius;
        }

        float endX = currentX + 3 * mRadius;
        float nextStartX = endX + 2 * mRadius;
        float nextEndX = currentX;

        if (mIndex < COUNT - 1) {
            float nextX = nextStartX * (1 - mRatio) + nextEndX * mRatio;
            canvas.drawCircle(nextX, centerY, mRadius, mOthersPaint);
        }

        float x = currentX * (1 - mRatio) + endX * mRatio;
        mHelperRect.left = x - mRadius;
        mHelperRect.right = mHelperRect.left + 4 * mRadius;
        mHelperRect.top = centerY - mRadius;
        mHelperRect.bottom = centerY + mRadius;
        canvas.drawRoundRect(mHelperRect, mRadius, mRadius, mMainPaint);

        currentX += 8 * mRadius;

        for (int i = mIndex + 2; i < COUNT; i++) {
            canvas.drawCircle(currentX, centerY, mRadius, mOthersPaint);
            currentX += 3 * mRadius;
        }
    }

}

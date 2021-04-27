package org.telegram.ui.Heymate.AmplifyModels;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import works.heymate.beta.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

public class ExpandableItem extends FrameLayout {

    private static final long EXPAND_DURATION = 300;

    private final ImageView mImageIcon;
    private final TextView mTextTitle;
    private final ImageView mButtonExpand;
    private final View mContent;

    private boolean mExpanded = false;
    private ValueAnimator mAnimator = null;

    public ExpandableItem(@NonNull Context context) {
        super(context);

        mImageIcon = new ImageView(context);
        mImageIcon.setColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText4), PorterDuff.Mode.SRC_IN);
        addView(mImageIcon, LayoutHelper.createFrame(24, 24, Gravity.LEFT | Gravity.TOP, 24, 16, 0, 16));

        mTextTitle = new TextView(context);
        mTextTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        mTextTitle.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        addView(mTextTitle, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 92, 16, 0, 16));

        mButtonExpand = new ImageView(context);
        mButtonExpand.setImageResource(works.heymate.beta.R.drawable.arrow_more);
        mButtonExpand.setColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText4), PorterDuff.Mode.SRC_IN);
        addView(mButtonExpand, LayoutHelper.createFrame(16, 16, Gravity.RIGHT | Gravity.TOP, 0, 16, 30, 16));

        View clickableArea = new View(context);
        addView(clickableArea, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, Gravity.TOP));

        mContent = createContent();
        addView(mContent, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP, 0, 48, 0, 0));

        View divider = new View(context);
        divider.setBackgroundColor(Theme.getColor(Theme.key_graySection));
        addView(divider, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 1, Gravity.BOTTOM, 68, 0, 0, 0));

        clickableArea.setOnClickListener(v -> {
            if (mExpanded) {
                collapse(true);
            }
            else {
                expand(true);
            }
        });

        collapse(false);
    }

    public void setIcon(Drawable drawable) {
        mImageIcon.setImageDrawable(drawable);
    }

    public void setTitle(CharSequence title) {
        mTextTitle.setText(title);
    }

    protected View createContent() {
        return new View(getContext());
    }

    public void collapse(boolean animated) {
        if (mAnimator != null) {
            mAnimator.cancel();
            mAnimator = null;
        }

        if (animated) {
            final float startAlpha = mContent.getAlpha();
            final int startHeight = mContent.getHeight();
            final float startRotation = mButtonExpand.getRotation();

            mAnimator = new ValueAnimator();
            mAnimator.setFloatValues(1, 0);
            mAnimator.setDuration(EXPAND_DURATION);
            mAnimator.addUpdateListener(animation -> {
                float ratio = (float) animation.getAnimatedValue();

                mContent.setAlpha(startAlpha * ratio);
                mContent.getLayoutParams().height = (int) (startHeight * ratio);
                mButtonExpand.setRotation(startRotation * ratio);

                requestLayout();
            });
            mAnimator.addListener(new Animator.AnimatorListener() {

                @Override
                public void onAnimationEnd(Animator animation) {
                    mAnimator = null;
                    collapse(false);
                }

                @Override public void onAnimationStart(Animator animation) { }
                @Override public void onAnimationCancel(Animator animation) { }
                @Override public void onAnimationRepeat(Animator animation) { }

            });
            mAnimator.start();
        }
        else {
            mContent.getLayoutParams().height = 0;
            mContent.setAlpha(0);
            mButtonExpand.setRotation(0);
            requestLayout();
        }

        mExpanded = false;
    }

    public void expand(boolean animated) {
        if (mAnimator != null) {
            mAnimator.cancel();
            mAnimator = null;
        }

        if (animated) {
            final float startAlpha = mContent.getAlpha();
            final int startHeight = mContent.getHeight();
            final float startRotation = mButtonExpand.getRotation();

            final float endAlpha = 1;
            final int endHeight = measureContentHeight();
            final float endRotation = 180;

            mAnimator = new ValueAnimator();
            mAnimator.setFloatValues(0, 1);
            mAnimator.setDuration(EXPAND_DURATION);
            mAnimator.addUpdateListener(animation -> {
                float ratio = (float) animation.getAnimatedValue();

                mContent.setAlpha(startAlpha * (1 - ratio) + endAlpha * ratio);
                mContent.getLayoutParams().height = (int) ((startHeight * (1 - ratio) + endHeight * ratio));
                mButtonExpand.setRotation(startRotation * (1 - ratio) + endRotation * ratio);

                requestLayout();
            });
            mAnimator.addListener(new Animator.AnimatorListener() {

                @Override
                public void onAnimationEnd(Animator animation) {
                    mAnimator = null;
                    expand(false);
                }

                @Override public void onAnimationStart(Animator animation) { }
                @Override public void onAnimationCancel(Animator animation) { }
                @Override public void onAnimationRepeat(Animator animation) { }

            });
            mAnimator.start();
        }
        else {
            mContent.getLayoutParams().height = measureContentHeight();
            mContent.setAlpha(1);
            mButtonExpand.setRotation(180);
            requestLayout();
        }

        mExpanded = true;
    }

    private int measureContentHeight() {
        int contentWidth = mContent.getWidth();

        mContent.measure(
                MeasureSpec.makeMeasureSpec(contentWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        );

        return mContent.getMeasuredHeight();
    }

    public void setError(boolean error) {
        if (error)
            mImageIcon.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_chat_inRedCall), PorterDuff.Mode.MULTIPLY));
        else
            mImageIcon.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogTextBlue2), PorterDuff.Mode.MULTIPLY));
    }

}

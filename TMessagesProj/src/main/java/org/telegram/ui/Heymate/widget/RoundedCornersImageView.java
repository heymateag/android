package org.telegram.ui.Heymate.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

public class RoundedCornersImageView extends AppCompatImageView {

    private float mCornerRadius = 0;

    private final RectF mClipRect = new RectF();
    private final Path mClipPath = new Path();

    public RoundedCornersImageView(@NonNull Context context) {
        super(context);
    }

    public RoundedCornersImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RoundedCornersImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setCornerRadius(float radius) {
        mCornerRadius = radius;

        mClipPath.reset();
        mClipPath.addRoundRect(mClipRect, mCornerRadius, mCornerRadius, Path.Direction.CW);

        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mClipRect.set(0, 0, w, h);

        mClipPath.reset();
        mClipPath.addRoundRect(mClipRect, mCornerRadius, mCornerRadius, Path.Direction.CW);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();

        if (mCornerRadius != 0) {
            canvas.clipPath(mClipPath);
        }

        super.onDraw(canvas);

        canvas.restore();
    }

}

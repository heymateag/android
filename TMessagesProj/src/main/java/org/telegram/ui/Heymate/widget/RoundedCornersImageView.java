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

    private float[] mCornerRadius = new float[8];

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

    public void setCornerRadius(float... radius) {
        if (radius.length == 1) {
            mCornerRadius = new float[] { radius[0], radius[0], radius[0], radius[0], radius[0], radius[0], radius[0], radius[0] };
        }
        else if (radius.length == 2) {
            mCornerRadius = new float[] { radius[0], radius[1], radius[0], radius[1], radius[0], radius[1], radius[0], radius[1] };
        }
        else if (radius.length == 4) {
            mCornerRadius = new float[] { radius[0], radius[0], radius[1], radius[1], radius[2], radius[2], radius[3], radius[3] };
        }
        else if (radius.length == 8) {
            mCornerRadius = radius;
        }
        else {
            throw new IllegalArgumentException("Radius array must have a length of either 1, 2, 4 or 8.");
        }

        mClipPath.reset();
        mClipPath.addRoundRect(mClipRect, mCornerRadius, Path.Direction.CW);

        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mClipRect.set(0, 0, w, h);

        mClipPath.reset();
        mClipPath.addRoundRect(mClipRect, mCornerRadius, Path.Direction.CW);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.clipPath(mClipPath);

        super.onDraw(canvas);

        canvas.restore();
    }

}

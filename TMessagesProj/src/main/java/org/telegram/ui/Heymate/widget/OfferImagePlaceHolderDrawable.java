package org.telegram.ui.Heymate.widget;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import org.telegram.messenger.ApplicationLoader;

import works.heymate.beta.R;

public class OfferImagePlaceHolderDrawable extends Drawable {

    private static final int BACKGROUND_COLOR = 0xFFECEDF1;
    private static final int GRADIENT_START_COLOR = 0x66192833;
    private static final int GRADIENT_END_COLOR = 0x00192833;

    private static final float TAG_HEIGHT_RATIO = 238f / 500f;

    private final Paint mSolidPaint;
    private final Paint mGradientPaint;
    private final Drawable mTagDrawable;

    private int mWidth = 0;
    private int mHeight = 0;

    private float mTagOffset = 0;

    public OfferImagePlaceHolderDrawable(boolean showGradient) {
        mSolidPaint = new Paint();
        mSolidPaint.setStyle(Paint.Style.FILL);
        mSolidPaint.setColor(BACKGROUND_COLOR);

        if (showGradient) {
            mGradientPaint = new Paint();
            mGradientPaint.setAntiAlias(true);
            mGradientPaint.setStyle(Paint.Style.FILL);
        }
        else {
            mGradientPaint = null;
        }

        mTagDrawable = AppCompatResources.getDrawable(ApplicationLoader.applicationContext, R.drawable.hm_ic_offer_tag);
    }

    public void setTagOffset(float offset) {
        mTagOffset = offset;

        invalidateSelf();
    }

    @Override
    public int getIntrinsicWidth() {
        return 360;
    }

    @Override
    public int getIntrinsicHeight() {
        return 180;
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);

        boolean hasChange = bounds.width() != mWidth;

        mWidth = bounds.width();

        if (bounds.height() != mHeight) {
            hasChange = true;

            mHeight = bounds.height();

            if (mGradientPaint != null) {
                LinearGradient gradient = new LinearGradient(0, 0, 0, mHeight, GRADIENT_START_COLOR, GRADIENT_END_COLOR, Shader.TileMode.CLAMP);
                mGradientPaint.setShader(gradient);
            }
        }

        if (hasChange) {
            int tagHeight = (int) (TAG_HEIGHT_RATIO * mHeight);
            int tagWidth = tagHeight * mTagDrawable.getIntrinsicWidth() / mTagDrawable.getIntrinsicHeight();

            int left = (mWidth - tagHeight * (mTagDrawable.getIntrinsicWidth() - 48) / mTagDrawable.getIntrinsicHeight()) / 2;
            int top = (mHeight - tagHeight) / 2;

            mTagDrawable.setBounds(left, top, left + tagWidth, top + tagHeight);

            invalidateSelf();
        }
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect bounds = getBounds();

        canvas.save();
        canvas.translate(0, bounds.top);

        canvas.drawRect(bounds, mSolidPaint);

        canvas.save();
        canvas.translate(0, mTagOffset);
        mTagDrawable.draw(canvas);
        canvas.restore();

        if (mGradientPaint != null) {
            canvas.drawRect(bounds, mGradientPaint);
        }

        canvas.restore();
    }

    @Override
    public void setAlpha(int alpha) { }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) { }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }

}

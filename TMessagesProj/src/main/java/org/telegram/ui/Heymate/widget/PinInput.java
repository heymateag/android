package org.telegram.ui.Heymate.widget;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.InputFilter;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import works.heymate.celo.AttestationCodeUtil;

public class PinInput extends View {

    private static final int DIGIT_COUNT = 8;
    private static final float DIGIT_GAP = 12;
    private static final float INDICATOR_HEIGHT = 4;
    private static final float MIN_TEXT_SIZE = 8;
    private static final float MAX_TEXT_SIZE = 32;
    private static final float MAX_TEXT_MARGIN = 4;

    public interface PinReceiver {

        void onPinReady(String pin);

    }

    private InputMethodManager mIMM;

    private float mDigitGap;
    private float mIndicatorHeight;
    private float mDigitWidth;
    private float mDigitHeight;
    private float mDigitY;

    private Paint mIndicatorPaint;
    private Paint mDigitPaint;

    private final Rect mHelperRect = new Rect();
    private final char[] mHelperChar = new char[1];

    private final SpannableStringBuilder mPin = new SpannableStringBuilder("");

    private PinReceiver mPinReceiver = null;
    private String mLastNotifiedPin = null;

    public PinInput(Context context) {
        super(context);
        initialize(context, null, 0);
    }

    public PinInput(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0);
    }

    public PinInput(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        mIMM = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

        Resources res = getResources();

        mDigitGap = res.getDisplayMetrics().density * DIGIT_GAP;
        mIndicatorHeight = res.getDisplayMetrics().density * INDICATOR_HEIGHT;

        mIndicatorPaint = new Paint();
        mIndicatorPaint.setStyle(Paint.Style.FILL);

        mDigitPaint = new Paint();
        mDigitPaint.setStyle(Paint.Style.FILL);
        mDigitPaint.setTextAlign(Paint.Align.CENTER);

        mPin.setFilters(new InputFilter[] { new InputFilter.LengthFilter(DIGIT_COUNT) });

        setFocusable(true);
    }

    public void setPinReceiver(PinReceiver pinReceiver) {
        mPinReceiver = pinReceiver;
    }

    public String getPin() {
        return mPin.toString();
    }

    public void setPin(String pin) {
        if (pin == null) {
            pin = "";
        }

        mPin.clear();
        mPin.append(pin);

        onPinChanged();
        invalidate();
    }

    public void setIndicatorColor(int color) {
        mIndicatorPaint.setColor(color);
        invalidate();
    }

    public void setDigitColor(int color) {
        mDigitPaint.setColor(color);
        invalidate();
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, @Nullable Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);

        if (gainFocus) {
            mIMM.showSoftInput(this, InputMethodManager.SHOW_FORCED);
        }
    }

    @Override
    public boolean onCheckIsTextEditor() {
        return true;
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        outAttrs.initialSelStart = mPin.length();
        outAttrs.initialSelEnd = mPin.length();
        outAttrs.inputType = EditorInfo.TYPE_CLASS_NUMBER | EditorInfo.TYPE_NUMBER_VARIATION_NORMAL;
        outAttrs.imeOptions = EditorInfo.IME_ACTION_DONE;
        outAttrs.packageName = getContext().getPackageName();

        return new BaseInputConnection(this, false) {

            @Override
            public boolean performPrivateCommand(String action, Bundle data) {
                return super.performPrivateCommand(action, data);
            }

            @Override
            public boolean performContextMenuAction(int id) {
                if (id == android.R.id.paste || id == android.R.id.pasteAsPlainText) {
                    ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = clipboard.getPrimaryClip();

                    if (clip != null && clip.getItemCount() > 0) {
                        String text = clip.getItemAt(0).toString();

                        String code = AttestationCodeUtil.extractURL(text);

                        if (code != null) {
                            mPin.clear();
                            onPinChanged();
                            notifyPin(code);
                        }
                        else {
                            code = AttestationCodeUtil.extractCode(text);

                            if (code != null) {
                                mPin.clear();
                                mPin.append(code);
                                onPinChanged();
                            }
                            else {
                                text = text.trim();

                                if (mPin.length() + text.length() <= DIGIT_COUNT && text.matches("[0-9]+")) {
                                    mPin.append(text);
                                    onPinChanged();
                                }
                            }
                        }
                    }

                    return true;
                }
                else if (id == android.R.id.cut) {
                    mPin.clear();
                    onPinChanged();
                }

                return false;
            }

        };
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_DEL:
                if (mPin.length() > 0) {
                    mPin.delete(mPin.length() - 1, mPin.length());
                }
                break;
            case KeyEvent.KEYCODE_0:
                mPin.append('0');
                break;
            case KeyEvent.KEYCODE_1:
                mPin.append('1');
                break;
            case KeyEvent.KEYCODE_2:
                mPin.append('2');
                break;
            case KeyEvent.KEYCODE_3:
                mPin.append('3');
                break;
            case KeyEvent.KEYCODE_4:
                mPin.append('4');
                break;
            case KeyEvent.KEYCODE_5:
                mPin.append('5');
                break;
            case KeyEvent.KEYCODE_6:
                mPin.append('6');
                break;
            case KeyEvent.KEYCODE_7:
                mPin.append('7');
                break;
            case KeyEvent.KEYCODE_8:
                mPin.append('8');
                break;
            case KeyEvent.KEYCODE_9:
                mPin.append('9');
                break;
        }

        onPinChanged();
        invalidate();
        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mDigitWidth = (w - (DIGIT_COUNT - 1) * mDigitGap) / DIGIT_COUNT;
        mDigitHeight = h - mIndicatorHeight;

        measureTextSize();

        mDigitY = (mDigitHeight - mDigitPaint.getFontMetrics().top) / 2f; // y + top/2 = height/2

        invalidate();
    }

    private void measureTextSize() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

        float minTextSize = displayMetrics.density * MIN_TEXT_SIZE;
        float maxTextSize = displayMetrics.density * MAX_TEXT_SIZE;

        mDigitPaint.setTextSize(minTextSize);
        mDigitPaint.getTextBounds("8", 0, 1, mHelperRect);
        float minWidth = mHelperRect.width();
        float minHeight = mHelperRect.height();

        mDigitPaint.setTextSize(maxTextSize);
        mDigitPaint.getTextBounds("8", 0, 1, mHelperRect);
        float maxWidth = mHelperRect.width();
        float maxHeight = mHelperRect.height();

        float maxMargin = displayMetrics.density * MAX_TEXT_MARGIN;

        measureTextSize(minTextSize, minWidth, minHeight, maxTextSize, maxWidth, maxHeight, maxMargin);
    }

    private boolean measureTextSize(float minTextSize, float minWidth, float minHeight, float maxTextSize, float maxWidth, float maxHeight, float maxMargin) {
        if (mDigitWidth - minWidth > maxMargin || mDigitHeight - minHeight > maxMargin) {
            if (maxTextSize - minTextSize < 2) {
                return false;
            }

            float textSize = (minTextSize + maxTextSize) / 2f;
            mDigitPaint.setTextSize(textSize);
            mDigitPaint.getTextBounds("8", 0, 1, mHelperRect);
            float width = mHelperRect.width();
            float height = mHelperRect.height();

            if (mDigitWidth < width || mDigitHeight < height) {
                return measureTextSize(minTextSize, minWidth, minHeight, textSize, width, height, maxMargin);
            }

            return measureTextSize(textSize, width, height, maxTextSize, maxWidth, maxHeight, maxMargin);
        }

        if (maxWidth > mDigitWidth || maxHeight > mDigitHeight) {
            if (maxTextSize - minTextSize < 2) {
                return false;
            }

            float textSize = (minTextSize + maxTextSize) / 2f;
            mDigitPaint.setTextSize(textSize);
            mDigitPaint.getTextBounds("8", 0, 1, mHelperRect);
            float width = mHelperRect.width();
            float height = mHelperRect.height();

            if (mDigitWidth - width > maxMargin || mDigitHeight - height > maxMargin) {
                return measureTextSize(textSize, width, height, maxTextSize, maxWidth, maxHeight, maxMargin);
            }
            else {
                return measureTextSize(minTextSize, minWidth, minHeight, textSize, width, height, maxMargin);
            }
        }

        mDigitPaint.setTextSize(maxTextSize);
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final float height = getHeight();
        final float indicatorTop = height - mIndicatorHeight;
        final float centerX = mDigitWidth / 2f;

        float left = 0;
        float right = mDigitWidth;

        for (int i = 0; i < DIGIT_COUNT; i++) {
            canvas.drawRect(left, indicatorTop, right, height, mIndicatorPaint);

            if (mPin.length() > i) {
                mHelperChar[0] = mPin.charAt(i);
                canvas.drawText(mHelperChar, 0, 1, left + centerX, mDigitY, mDigitPaint);
            }

            left = right + mDigitGap;
            right = left + mDigitWidth;
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        if (!enabled) {
            mIMM.hideSoftInputFromWindow(getWindowToken(), 0);
        }
    }

    private void onPinChanged() {
        if (mPin.length() == DIGIT_COUNT) {
            if (notifyPin(mPin.toString())) {
                mIMM.hideSoftInputFromWindow(getWindowToken(), 0);
            }
        }

        invalidate();
    }

    private boolean notifyPin(String pin) {
        if (TextUtils.equals(pin, mLastNotifiedPin)) {
            return false;
        }

        if (mPinReceiver != null) {
            mLastNotifiedPin = pin;

            mPinReceiver.onPinReady(mLastNotifiedPin);

            return true;
        }

        return false;
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        return new SavedState(super.onSaveInstanceState(), this);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);

        if (state instanceof SavedState) {
            SavedState myState = (SavedState) state;

            mPin.clear();
            mPin.append(myState.pin);
            mLastNotifiedPin = myState.lastNotifiedPin;
        }
    }

    private static class SavedState extends BaseSavedState {

        private String pin;
        private String lastNotifiedPin;

        public SavedState(Parcel source) {
            super(source);
            pin = source.readString();
            lastNotifiedPin = source.readString();
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        public SavedState(Parcel source, ClassLoader loader) {
            super(source, loader);
            pin = source.readString();
            lastNotifiedPin = source.readString();
        }

        public SavedState(Parcelable superState, PinInput self) {
            super(superState);

            this.pin = self.mPin.toString();
            this.lastNotifiedPin = self.mLastNotifiedPin;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeString(pin);
            out.writeString(lastNotifiedPin);
        }

        public static final ClassLoaderCreator<SavedState> CREATOR = new ClassLoaderCreator<SavedState>() {

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public SavedState createFromParcel(Parcel source, ClassLoader loader) {
                return new SavedState(source, loader);
            }

            @Override
            public SavedState createFromParcel(Parcel source) {
                return new SavedState(source);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }

        };

    }

}

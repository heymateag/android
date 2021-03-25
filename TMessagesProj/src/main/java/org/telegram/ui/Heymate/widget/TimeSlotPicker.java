package org.telegram.ui.Heymate.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.telegram.messenger.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import works.heymate.core.Texts;

public class TimeSlotPicker extends ViewGroup implements TimeSlotPickerAdapter.TimeSlotReceiver {

    public static final int SELECTION_MODE_NONE = 0;
    public static final int SELECTION_MODE_SINGLE = 1;
    // public static final int SELECTION_MODE_CONNECTED = 2; For the future
    // public static final int SELECTION_MODE_FREE = 3; For the future

    private static final long DELAY_BEFORE_REQUESTING_TIME_SLOTS = 100;

    private static final float TIME_BUTTON_WIDTH = 18;
    private static final float UNAVAILABLE_TIME_SLOT_WIDTH = 1;

    private static final long ONE_MINUTE = 60L * 1000L;
    private static final long ONE_HOUR = 60L * ONE_MINUTE;
    private static final long ONE_DAY = 24L * ONE_HOUR;
    private static final long ONE_WEEK = 7L * ONE_DAY;

    public interface OnTimeSlotSelectedListener {

        void onTimeSlotSelected(TimeSlotPickerAdapter.TimeSlot timeSlot);

    }

    private TimeSlotView mTimeSlotView;
    private HorizontalScrollView mScrollTimeSlot;
    private ImageView mButtonNextWeek;
    private ImageView mButtonPreviousWeek;
    private TextView mTextDay;
    private TextView[] mTextsHour = new TextView[24];

    private final List<PreparedTimeSlot> mTimeSlots = new ArrayList<>();

    private Calendar mCalendar = Calendar.getInstance();

    private TimeSlotPickerAdapter mAdapter = null;
    private OnTimeSlotSelectedListener mOnTimeSlotSelectedListener = null;

    private int mSelectionMode = SELECTION_MODE_NONE;
    private PreparedTimeSlot mSelectedTimeSlot = null;

    public TimeSlotPicker(Context context) {
        super(context);
        initialize(context, null, 0);
    }

    public TimeSlotPicker(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0);
    }

    public TimeSlotPicker(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        mTimeSlotView = new TimeSlotView(context);

        mScrollTimeSlot = new HorizontalScrollView(context) {

            @Override
            protected void onScrollChanged(int currentHorizontalScroll, int t, int previousHorizontalScroll, int oldt) {
                super.onScrollChanged(currentHorizontalScroll, t, previousHorizontalScroll, oldt);

                removeCallbacks(mTimeSlotRequester);
                postDelayed(mTimeSlotRequester, DELAY_BEFORE_REQUESTING_TIME_SLOTS);
            }

        };
        mScrollTimeSlot.addView(mTimeSlotView, LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        addView(mScrollTimeSlot);

        mButtonNextWeek = new ImageView(context);
        mButtonNextWeek.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        mButtonNextWeek.setImageResource(R.drawable.ic_chevron_right_white_18);
        addView(mButtonNextWeek);

        mButtonPreviousWeek = new ImageView(context);
        mButtonPreviousWeek.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        mButtonPreviousWeek.setImageResource(R.drawable.ic_chevron_left_white_18);
        addView(mButtonPreviousWeek);

        mTextDay = new TextView(context);
        mTextDay.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        mTextDay.setText(Texts.get(Texts.DAY));
        addView(mTextDay);

        for (int i = 0; i < mTextsHour.length; i++) {
            TextView textHour = new TextView(context);
            textHour.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            textHour.setText(getTextForHour(i));
            mTextsHour[i] = textHour;
            addView(mTextsHour[i]);
        }

        mTimeSlotView.setCalendar(mCalendar);
        mTimeSlotView.setTimeSlots(mTimeSlots);
        mTimeSlotView.setUnavailableDrawable(new SimpleShape(getResources().getDisplayMetrics().density * UNAVAILABLE_TIME_SLOT_WIDTH, Color.LTGRAY));
        mTimeSlotView.setAvailableDrawable(new SimpleShape(Color.GREEN));
        mTimeSlotView.setReservedDrawable(new SimpleShape(Color.DKGRAY));
        mTimeSlotView.setSelectedDrawable(new SimpleShape(Color.BLUE));
        mTimeSlotView.setOnTimeSlotClickedListener(mOnTimeSlotClickedListener);

        mButtonNextWeek.setOnClickListener(v -> mScrollTimeSlot.smoothScrollBy(mScrollTimeSlot.getWidth(), 0));
        mButtonPreviousWeek.setOnClickListener(v -> mScrollTimeSlot.smoothScrollBy(-mScrollTimeSlot.getWidth(), 0));
    }

    public void setAdapter(TimeSlotPickerAdapter adapter) {
        if (mAdapter != null) {
            mAdapter.setTimeSlotReceiver(null);
        }

        mTimeSlots.clear();
        mTimeSlotView.invalidate();

        mAdapter = adapter;

        if (mAdapter != null) {
            setTimeZone(mAdapter.getTimeZone());

            mAdapter.setTimeSlotReceiver(this);

            post(mTimeSlotRequester);
        }
    }

    public void setOnTimeSlotSelectedListener(OnTimeSlotSelectedListener listener) {
        mOnTimeSlotSelectedListener = listener;
    }

    public void setSelectionMode(int selectionMode) {
        if (mSelectionMode == selectionMode) {
            return;
        }

        mSelectionMode = selectionMode;

        if (mSelectionMode == SELECTION_MODE_NONE && mSelectedTimeSlot != null) {
            mSelectedTimeSlot.selected = false;
            mSelectedTimeSlot = null;
        }
    }

    public TimeSlotPickerAdapter.TimeSlot getSelectedTimeSlot() {
        return mSelectedTimeSlot == null ? null : mSelectedTimeSlot.slot;
    }

    public boolean setSelectedTimeSlot(TimeSlotPickerAdapter.TimeSlot timeSlot) {
        if (mSelectedTimeSlot != null) {
            mSelectedTimeSlot.selected = false;
            mSelectedTimeSlot = null;
        }

        mTimeSlotView.invalidate();

        if (timeSlot == null) {
            return true;
        }

        for (PreparedTimeSlot preparedTimeSlot: mTimeSlots) {
            if (preparedTimeSlot.slot == timeSlot || (preparedTimeSlot.slot.startTime == timeSlot.startTime && preparedTimeSlot.slot.duration == timeSlot.duration)) {
                preparedTimeSlot.slot = timeSlot;
                preparedTimeSlot.reserved = timeSlot.reserved;

                preparedTimeSlot.selected = true;

                mSelectedTimeSlot = preparedTimeSlot;

                return true;
            }
        }

        return false;
    }

    public void setTimeZone(TimeZone timeZone) {
        mCalendar.setTimeZone(timeZone);
        mTimeSlotView.setCalendar(mCalendar);
    }

    public void setDayTextSize(float textSize) {
        mTextDay.setTextSize(textSize);
    }

    public void setDayTextColor(int color) {
        mTextDay.setTextColor(color);
    }

    public void setDayTypeface(Typeface typeface) {
        mTextDay.setTypeface(typeface);
    }

    public void setHourTextSize(float textSize) {
        for (TextView textView: mTextsHour) {
            textView.setTextSize(textSize);
        }
    }

    public void setHourTextColor(int color) {
        for (TextView textView: mTextsHour) {
            textView.setTextColor(color);
        }
    }

    public void setHourTypeface(Typeface typeface) {
        for (TextView textView: mTextsHour) {
            textView.setTypeface(typeface);
        }
    }

    public void setButtonsBackgroundColor(int color) {
        mButtonPreviousWeek.setBackgroundColor(color);
        mButtonNextWeek.setBackgroundColor(color);
    }

    public void setButtonsColor(int color) {
        mButtonPreviousWeek.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        mButtonNextWeek.setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

    public void setDayOfWeekColor(int color) {
        mTimeSlotView.setDayOfWeekColor(color);
    }

    public void setDayOfWeekTypeface(Typeface typeface) {
        mTimeSlotView.setDayOfWeekTypeface(typeface);
    }

    public void setDayOfWeekTextSize(float textSize) {
        mTimeSlotView.setDayOfWeekTextSize(textSize);
    }

    public void setDateColor(int color) {
        mTimeSlotView.setDateColor(color);
    }

    public void setDateTypeface(Typeface typeface) {
        mTimeSlotView.setDateTypeface(typeface);
    }

    public void setDateTextSize(float textSize) {
        mTimeSlotView.setDateTextSize(textSize);
    }

    public void setUnavailableTimeSlotColor(int color) {
        ((SimpleShape) mTimeSlotView.mUnavailableDrawable).setColor(color);
    }

    public void setAvailableTimeSlotColor(int color) {
        ((SimpleShape) mTimeSlotView.mAvailableDrawable).setColor(color);
    }

    public void setReservedTimeSlotColor(int color) {
        ((SimpleShape) mTimeSlotView.mReservedDrawable).setColor(color);
    }

    public void setSelectedTimeSlotColor(int color) {
        ((SimpleShape) mTimeSlotView.mSelectedDrawable).setColor(color);
    }

    private final Runnable mTimeSlotRequester = () -> {
        long timeRangeStart = mTimeSlotView.getVisibleTimeStart();
        long timeRangeEnd = timeRangeStart + ONE_WEEK;

        if (mAdapter != null) {
            mAdapter.getTimeSlotsForTimeRange(timeRangeStart, timeRangeEnd);
        }
    };

    @Override
    public void onNewTimeSlots(List<TimeSlotPickerAdapter.TimeSlot> timeSlots) {
        int indexInNewTimeSlots = 0;
        int indexInExistingTimeSlots = 0;

        while (indexInExistingTimeSlots < mTimeSlots.size()) {
            PreparedTimeSlot existingTimeSlot = mTimeSlots.get(indexInExistingTimeSlots);
            TimeSlotPickerAdapter.TimeSlot newTimeSlot = timeSlots.get(indexInNewTimeSlots);

            if (existingTimeSlot.start < newTimeSlot.startTime) {
                indexInExistingTimeSlots++;
                continue;
            }

            while (newTimeSlot.startTime < existingTimeSlot.start) {
                mTimeSlots.add(indexInExistingTimeSlots, new PreparedTimeSlot(newTimeSlot));

                indexInExistingTimeSlots++;
                indexInNewTimeSlots++;

                if (indexInNewTimeSlots == timeSlots.size()) {
                    break;
                }

                newTimeSlot = timeSlots.get(indexInNewTimeSlots);
            }
        }

        for (int i = indexInNewTimeSlots; i < timeSlots.size(); i++) {
            mTimeSlots.add(new PreparedTimeSlot(timeSlots.get(i)));
        }

        mTimeSlotView.invalidate();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int buttonWidth = (int) (getResources().getDisplayMetrics().density * TIME_BUTTON_WIDTH);

        mTextsHour[0].measure(
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        );
        int timeAreaWidth = mTextsHour[0].getMeasuredWidth();

        int width = getWidth();
        int height = getHeight();

        int scrollWidth = width - 2 * buttonWidth - timeAreaWidth;
        mScrollTimeSlot.measure(
                MeasureSpec.makeMeasureSpec(scrollWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        );
        mScrollTimeSlot.layout(timeAreaWidth + buttonWidth, 0, width - buttonWidth, height);

        int timeAreaHeight = mTimeSlotView.getTimeAreaHeight();

        mButtonPreviousWeek.measure(
                MeasureSpec.makeMeasureSpec(buttonWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(timeAreaHeight, MeasureSpec.EXACTLY)
        );
        mButtonPreviousWeek.layout(timeAreaWidth, 0, timeAreaWidth + buttonWidth, timeAreaHeight);

        mButtonNextWeek.measure(
                MeasureSpec.makeMeasureSpec(buttonWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(timeAreaHeight, MeasureSpec.EXACTLY)
        );
        mButtonNextWeek.layout(width - buttonWidth, 0, width, timeAreaHeight);

        mTextDay.measure(
                MeasureSpec.makeMeasureSpec(timeAreaWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(timeAreaHeight, MeasureSpec.EXACTLY)
        );
        mTextDay.layout(0, 0, timeAreaWidth, timeAreaHeight);

        int timeSlotHeight = (height - timeAreaHeight) / 24;
        int top = timeAreaHeight;

        for (int i = 0; i < mTextsHour.length; i++) {
            mTextsHour[i].measure(
                    MeasureSpec.makeMeasureSpec(timeAreaWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(timeSlotHeight, MeasureSpec.EXACTLY)
            );
            mTextsHour[i].layout(0, top, timeAreaWidth, top + timeSlotHeight);

            top += timeSlotHeight;
        }

        removeCallbacks(mTimeSlotRequester);
        post(mTimeSlotRequester);
    }

    private final OnTimeSlotClickedListener mOnTimeSlotClickedListener = new OnTimeSlotClickedListener() {

        @Override
        public boolean onTimeSlotClicked(long start, long end) {
            // Dropped for now. It is for the future.
            return false;
        }

        @Override
        public boolean onTimeSlotClicked(int index, PreparedTimeSlot timeSlot) {
            if (mSelectionMode == SELECTION_MODE_SINGLE) {
                if (mSelectedTimeSlot != null) {
                    mSelectedTimeSlot.selected = false;
                }

                mSelectedTimeSlot = timeSlot;
                mSelectedTimeSlot.selected = true;

                mTimeSlotView.invalidate();

                if (mOnTimeSlotSelectedListener != null) {
                    mOnTimeSlotSelectedListener.onTimeSlotSelected(timeSlot.slot);
                }

                return true;
            }

            return false;
        }

    };

    private static class SimpleShape extends Drawable {

        private Paint mSolidPaint = null;
        private Paint mBorderPaint = null;

        public SimpleShape(int color) {
            mSolidPaint = new Paint();
            mSolidPaint.setStyle(Paint.Style.FILL);
            mSolidPaint.setColor(color);
        }

        public SimpleShape(float borderWidth, int borderColor) {
            mBorderPaint = new Paint();
            mBorderPaint.setStyle(Paint.Style.STROKE);
            mBorderPaint.setStrokeWidth(borderWidth);
            mBorderPaint.setColor(borderColor);
        }

        public void setColor(int color) {
            if (mSolidPaint != null) {
                mSolidPaint.setColor(color);
            }
            else if (mBorderPaint != null) {
                mBorderPaint.setColor(color);
            }

            invalidateSelf();
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            if (mSolidPaint != null) {
                canvas.drawRect(getBounds(), mSolidPaint);
            }

            if (mBorderPaint != null) {
                canvas.drawRect(getBounds(), mBorderPaint);
            }
        }

        @Override public void setAlpha(int alpha) { }
        @Override public void setColorFilter(@Nullable ColorFilter colorFilter) { }
        @Override public int getOpacity() { return PixelFormat.OPAQUE; }

    }

    private interface OnTimeSlotClickedListener {

        boolean onTimeSlotClicked(long start, long end);

        boolean onTimeSlotClicked(int index, PreparedTimeSlot timeSlot);

    }

    private static class TimeSlotView extends View {

        private static final float TIME_MIDDLE_MARGIN = 3;

        private Calendar mCalendar;
        private int mDaysInAWeek;

        private List<PreparedTimeSlot> mTimeSlots = Collections.emptyList();

        private OnTimeSlotClickedListener mOnTimeSlotClickedListener = null;

        private final long mBaseTime;

        private final GestureDetector mGestureDetector;

        private final Paint mDayOfWeekPaint;
        private final Paint mDatePaint;

        private Drawable mUnavailableDrawable = null;
        private Drawable mAvailableDrawable = null;
        private Drawable mReservedDrawable = null;
        private Drawable mSelectedDrawable = null;

        private float mDayOfWeekDrawY;
        private float mDateDrawY;

        private int mTimeAreaHeight;
        private int mSlotWidth;
        private int mSlotHeight;

        public TimeSlotView(Context context) {
            super(context);

            mBaseTime = System.currentTimeMillis();

            mGestureDetector = new GestureDetector(context, mOnGestureListener);

            mDayOfWeekPaint = new Paint();
            mDayOfWeekPaint.setAntiAlias(true);
            mDayOfWeekPaint.setStyle(Paint.Style.FILL);
            mDayOfWeekPaint.setTextAlign(Paint.Align.CENTER);

            mDatePaint = new Paint(mDayOfWeekPaint);
        }

        public void setCalendar(Calendar calendar) {
            mCalendar = calendar;
            mDaysInAWeek = mCalendar.getMaximum(Calendar.DAY_OF_WEEK);

            requestLayout();
            performMeasure();
        }

        public void setTimeSlots(List<PreparedTimeSlot> timeSlots) {
            mTimeSlots = timeSlots;
            invalidate();
        }

        public void setOnTimeSlotClickedListener(OnTimeSlotClickedListener listener) {
            mOnTimeSlotClickedListener = listener;
        }

        public int getTimeAreaHeight() {
            return mTimeAreaHeight;
        }

        public long getVisibleTimeStart() {
            ViewGroup parent = (ViewGroup) getParent();
            int scroll = parent.getScrollX();

            int passedDays = scroll / mSlotWidth;

            mCalendar.setTimeInMillis(mBaseTime);
            mCalendar.set(Calendar.HOUR_OF_DAY, 0);
            mCalendar.set(Calendar.MINUTE, 0);
            mCalendar.set(Calendar.SECOND, 0);
            mCalendar.set(Calendar.MILLISECOND, 0);

            return mCalendar.getTimeInMillis() + passedDays * ONE_DAY;
        }

        public void setDayOfWeekColor(int color) {
            mDayOfWeekPaint.setColor(color);
            invalidate();
        }

        public void setDayOfWeekTypeface(Typeface typeface) {
            mDayOfWeekPaint.setTypeface(typeface);
            performMeasure();
        }

        public void setDayOfWeekTextSize(float textSize) {
            mDayOfWeekPaint.setTextSize(textSize);
            performMeasure();
        }

        public void setDateColor(int color) {
            mDatePaint.setColor(color);
            invalidate();
        }

        public void setDateTypeface(Typeface typeface) {
            mDatePaint.setTypeface(typeface);
            performMeasure();
        }

        public void setDateTextSize(float textSize) {
            mDatePaint.setTextSize(textSize);
            performMeasure();
        }

        public void setUnavailableDrawable(Drawable drawable) {
            if (mUnavailableDrawable != null) {
                mUnavailableDrawable.setCallback(null);
            }

            mUnavailableDrawable = drawable;

            if (mUnavailableDrawable != null) {
                mUnavailableDrawable.setCallback(this);
            }

            invalidate();
        }

        public void setAvailableDrawable(Drawable drawable) {
            if (mAvailableDrawable != null) {
                mAvailableDrawable.setCallback(null);
            }

            mAvailableDrawable = drawable;

            if (mAvailableDrawable != null) {
                mAvailableDrawable.setCallback(this);
            }

            invalidate();
        }

        public void setReservedDrawable(Drawable drawable) {
            if (mReservedDrawable != null) {
                mReservedDrawable.setCallback(null);
            }

            mReservedDrawable = drawable;

            if (mReservedDrawable != null) {
                mReservedDrawable.setCallback(this);
            }

            invalidate();
        }

        public void setSelectedDrawable(Drawable drawable) {
            if (mSelectedDrawable != null) {
                mSelectedDrawable.setCallback(null);
            }

            mSelectedDrawable = drawable;

            if (mSelectedDrawable != null) {
                mSelectedDrawable.setCallback(this);
            }

            invalidate();
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int width = ((ViewGroup) getParent()).getMeasuredWidth();
            mCalendar.setTimeInMillis(System.currentTimeMillis());
            int yearWidth = width * mCalendar.getActualMaximum(Calendar.DAY_OF_YEAR) / mCalendar.getMaximum(Calendar.DAY_OF_WEEK);
            super.onMeasure(MeasureSpec.makeMeasureSpec(yearWidth, MeasureSpec.EXACTLY), heightMeasureSpec);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            performMeasure();
        }

        @Override
        protected boolean verifyDrawable(@NonNull Drawable who) {
            return super.verifyDrawable(who) || who == mUnavailableDrawable || who == mAvailableDrawable || who == mReservedDrawable || who == mSelectedDrawable;
        }

        private void performMeasure() {
            float timeMiddleMargin = getResources().getDisplayMetrics().density * TIME_MIDDLE_MARGIN;

            Paint.FontMetrics dayOfWeekMetrics = mDayOfWeekPaint.getFontMetrics();
            Paint.FontMetrics dateMetrics = mDatePaint.getFontMetrics();

            float dayOfWeekHeight = dayOfWeekMetrics.bottom - dayOfWeekMetrics.top;
            float dateHeight = dateMetrics.bottom - dateMetrics.top;

            float verticalMargin = Math.max(dayOfWeekHeight, dateHeight);

            mTimeAreaHeight = (int) (verticalMargin + dayOfWeekHeight + timeMiddleMargin + dateHeight + verticalMargin);

            mDayOfWeekDrawY = verticalMargin + dayOfWeekHeight / 2f + (dayOfWeekMetrics.ascent + dayOfWeekMetrics.descent) / 2f;
            mDateDrawY = verticalMargin + dayOfWeekHeight + timeMiddleMargin + dateHeight / 2f + (dateMetrics.ascent + dateMetrics.descent) / 2f;

            ViewGroup parent = (ViewGroup) getParent();
            int width = parent.getWidth();

            mSlotWidth = width / mDaysInAWeek;
            mSlotHeight = (int) ((getHeight() - mTimeAreaHeight) / 24);

            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            ViewGroup parent = (ViewGroup) getParent();
            int scroll = parent.getScrollX();

            if (mUnavailableDrawable != null) {
                mUnavailableDrawable.setBounds(0, 0, mSlotWidth, mSlotHeight);
            }

            int passedDays = scroll / mSlotWidth;

            mCalendar.setTimeInMillis(mBaseTime);
            mCalendar.set(Calendar.HOUR_OF_DAY, 0);
            mCalendar.set(Calendar.MINUTE, 0);
            mCalendar.set(Calendar.SECOND, 0);
            mCalendar.set(Calendar.MILLISECOND, 0);

            mCalendar.roll(Calendar.DAY_OF_YEAR, passedDays);

            canvas.save();

            canvas.translate(mSlotWidth * passedDays, 0);

            int slotIndex = 0;

            for (int i = 0; i <= mDaysInAWeek; i++) {
                slotIndex = drawDay(mSlotWidth, mSlotHeight, slotIndex, canvas);

                mCalendar.roll(Calendar.DAY_OF_YEAR, 1);

                canvas.translate(mSlotWidth, 0);
            }

            canvas.restore();
        }

        private int drawDay(int slotWidth, int slotHeight, int slotIndex, Canvas canvas) {
            slotIndex = Math.max(0, slotIndex - 1);

            long endOfDay = mCalendar.getTimeInMillis() + ONE_DAY;

            int dayOfWeek = mCalendar.get(Calendar.DAY_OF_WEEK);
            int month = mCalendar.get(Calendar.MONTH) + 1;
            int dayOfMonth = mCalendar.get(Calendar.DAY_OF_MONTH);

            canvas.drawText(getNameOfDay(dayOfWeek), slotWidth / 2f, mDayOfWeekDrawY, mDayOfWeekPaint);
            canvas.drawText(month + "/" + dayOfMonth, slotWidth / 2f, mDateDrawY, mDatePaint);

            canvas.save();
            canvas.translate(0, mTimeAreaHeight);

            if (mUnavailableDrawable != null) {
                canvas.save();

                for (int i = 0; i < 24; i++) {
                    mUnavailableDrawable.draw(canvas);
                    canvas.translate(0, slotHeight);
                }

                canvas.restore();
            }

            while (slotIndex < mTimeSlots.size()) {
                PreparedTimeSlot timeSlot = mTimeSlots.get(slotIndex);

                if (timeSlot.end < mCalendar.getTimeInMillis()) {
                    slotIndex++;
                    continue;
                }

                if (timeSlot.start > endOfDay) {
                    break;
                }

                int start = (int) (Math.max(timeSlot.start - mCalendar.getTimeInMillis(), 0) / ONE_HOUR * slotHeight);
                int end = (int) (Math.min(timeSlot.end - mCalendar.getTimeInMillis(), ONE_DAY) / ONE_HOUR * slotHeight);

                if (timeSlot.reserved) {
                    if (mReservedDrawable != null) {
                        mReservedDrawable.setBounds(0, start, slotWidth, end);
                        mReservedDrawable.draw(canvas);
                    }
                }
                else if (mAvailableDrawable != null) {
                    mAvailableDrawable.setBounds(0, start, slotWidth, end);
                    mAvailableDrawable.draw(canvas);
                }

                if (timeSlot.selected && mSelectedDrawable != null) {
                    mSelectedDrawable.setBounds(0, start, slotWidth, end);
                    mSelectedDrawable.draw(canvas);
                }

                slotIndex++;
            }

            canvas.restore();

            return slotIndex;
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            return mGestureDetector.onTouchEvent(event);
        }

        private final GestureDetector.OnGestureListener mOnGestureListener = new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onDown(MotionEvent e) {
                return e.getY() > mTimeAreaHeight;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                ViewGroup parent = (ViewGroup) getParent();
                int scroll = parent.getScrollX();

                int passedDays = (int) ((scroll + e.getX()) / mSlotWidth);

                mCalendar.setTimeInMillis(mBaseTime);
                mCalendar.set(Calendar.HOUR_OF_DAY, 0);
                mCalendar.set(Calendar.MINUTE, 0);
                mCalendar.set(Calendar.SECOND, 0);
                mCalendar.set(Calendar.MILLISECOND, 0);

                mCalendar.roll(Calendar.DAY_OF_YEAR, passedDays);

                float exactTime = (e.getY() - mTimeAreaHeight) / mSlotHeight;

                long time = mCalendar.getTimeInMillis() + (long) (exactTime * ONE_HOUR);

                if (mTimeSlots != null) {
                    for (int i = 0; i < mTimeSlots.size(); i++) {
                        PreparedTimeSlot timeSlot = mTimeSlots.get(i);

                        if (timeSlot.start < time && timeSlot.end > time) {
                            if (mOnTimeSlotClickedListener != null) {
                                if (mOnTimeSlotClickedListener.onTimeSlotClicked(i, timeSlot)) {
                                    performClick();
                                }
                            }
                            return true;
                        }
                    }
                }

                int hourIndex = (int) (exactTime);

                long start = mCalendar.getTimeInMillis() + hourIndex * ONE_HOUR;
                long end = start + ONE_HOUR;

                if (mOnTimeSlotClickedListener != null) {
                    if (mOnTimeSlotClickedListener.onTimeSlotClicked(start, end)) {
                        performClick();
                    }
                }

                return true;
            }

        };

    }

    private static class PreparedTimeSlot {

        public TimeSlotPickerAdapter.TimeSlot slot;

        public final long start;
        public final long end;
        public boolean reserved;
        public boolean selected;

        public PreparedTimeSlot(TimeSlotPickerAdapter.TimeSlot timeSlot) {
            this.slot = timeSlot;

            start = timeSlot.startTime;
            end = start + timeSlot.duration * ONE_MINUTE;
            reserved = timeSlot.reserved;
            selected = false;
        }

    }

    private static final String getNameOfDay(int day) {
        switch (day) {
            case Calendar.SUNDAY:
                return Texts.get(Texts.SUNDAY_SHORT).toString();
            case Calendar.MONDAY:
                return Texts.get(Texts.MONDAY_SHORT).toString();
            case Calendar.TUESDAY:
                return Texts.get(Texts.TUESDAY_SHORT).toString();
            case Calendar.WEDNESDAY:
                return Texts.get(Texts.WEDNESDAY_SHORT).toString();
            case Calendar.THURSDAY:
                return Texts.get(Texts.THURSDAY_SHORT).toString();
            case Calendar.FRIDAY:
                return Texts.get(Texts.FRIDAY_SHORT).toString();
            case Calendar.SATURDAY:
                return Texts.get(Texts.SATURDAY_SHORT).toString();
        }

        return "";
    }

    private static String getTextForHour(int hour) {
        String hourStr = String.valueOf(hour);

        if (hourStr.length() == 1) {
            hourStr = "0" + hourStr;
        }

        return hourStr + ":00";
    }

}

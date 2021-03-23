package org.telegram.ui.Heymate.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import works.heymate.core.Texts;

public class TimeSlotPicker extends ViewGroup {

    private static final long ONE_HOUR = 60L * 60L * 1000L;
    private static final long ONE_DAY = 24L * ONE_HOUR;

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

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }

    private static class TimeSlotView extends View {

        private static final float TIME_MIDDLE_MARGIN = 3;

        private Calendar mCalendar;

        private List<TimeSlot> mTimeSlots = Collections.emptyList();

        private final Paint mDayOfWeekPaint;
        private final Paint mDatePaint;
        private Drawable mUnavailableDrawable = null;
        private Drawable mAvailableDrawable = null;
        private Drawable mReservedDrawable = null;
        private Drawable mSelectedDrawable = null;

        private float mDayOfWeekDrawY;
        private float mDateDrawY;

        private int mTimeAreaHeight;

        public TimeSlotView(Context context) {
            super(context);

            mDayOfWeekPaint = new Paint();
            mDayOfWeekPaint.setAntiAlias(true);
            mDayOfWeekPaint.setStyle(Paint.Style.FILL);
            mDayOfWeekPaint.setTextAlign(Paint.Align.CENTER);

            mDatePaint = new Paint(mDayOfWeekPaint);
        }

        public void setCalendar(Calendar calendar) {
            mCalendar = calendar;

            requestLayout();
            invalidate();
        }

        public void setTimeSlots(List<TimeSlot> timeSlots) {
            mTimeSlots = timeSlots;
            invalidate();
        }

        public int getTimeAreaHeight() {
            return mTimeAreaHeight;
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

            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            ViewGroup parent = (ViewGroup) getParent();
            int width = parent.getWidth();
            int scroll = parent.getScrollX();

            int daysInAWeek = mCalendar.getMaximum(Calendar.DAY_OF_WEEK);
            int slotWidth = width / daysInAWeek;
            int slotHeight = (int) ((getHeight() - mTimeAreaHeight) / 24);

            if (mUnavailableDrawable != null) {
                mUnavailableDrawable.setBounds(0, 0, slotWidth, slotHeight);
            }

            int passedDays = scroll / slotWidth;

            mCalendar.setTimeInMillis(System.currentTimeMillis());
            mCalendar.set(Calendar.HOUR_OF_DAY, 0);
            mCalendar.set(Calendar.MINUTE, 0);
            mCalendar.set(Calendar.SECOND, 0);
            mCalendar.set(Calendar.MILLISECOND, 0);

            mCalendar.roll(Calendar.DAY_OF_YEAR, passedDays);

            canvas.save();

            canvas.translate(slotWidth * passedDays, 0);

            int slotIndex = 0;

            for (int i = 0; i <= daysInAWeek; i++) {
                slotIndex = drawDay(slotWidth, slotHeight, slotIndex, canvas);

                mCalendar.roll(Calendar.DAY_OF_YEAR, 1);

                canvas.translate(slotWidth, 0);
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
                TimeSlot timeSlot = mTimeSlots.get(slotIndex);

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

    private static class TimeSlot {

        public long start;
        public long end;
        public boolean reserved;
        public boolean selected;

    }

}

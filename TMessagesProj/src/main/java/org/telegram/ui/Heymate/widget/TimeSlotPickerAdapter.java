package org.telegram.ui.Heymate.widget;

import java.util.List;
import java.util.TimeZone;

public interface TimeSlotPickerAdapter {

    interface TimeSlotReceiver {

        /**
         * Only the new ones please. It helps improve the performance. I am not checking doubles.
         * Also let them be sorted.
         * @param timeSlots
         */
        void onNewTimeSlots(List<TimeSlot> timeSlots);

        // This is for the future
        // void onTimeSlotsChanged();

    }

    class TimeSlot implements Comparable<TimeSlot> {

        public final long startTime;
        public final int duration; // minutes
        public final boolean reserved;

        public TimeSlot(long startTime, int duration, boolean reserved) {
            this.startTime = startTime;
            this.duration = duration;
            this.reserved = reserved;
        }

        @Override
        public int compareTo(TimeSlot o) {
            return (int) (startTime - o.startTime);
        }

    }

    TimeZone getTimeZone();

    void setTimeSlotReceiver(TimeSlotReceiver receiver);

    void getTimeSlotsForTimeRange(long from, long to);

}

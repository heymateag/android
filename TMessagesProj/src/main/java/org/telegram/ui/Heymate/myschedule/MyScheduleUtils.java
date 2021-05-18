package org.telegram.ui.Heymate.myschedule;

import works.heymate.core.Texts;

public class MyScheduleUtils {

    public static final String PLACEHOLDER_SUB_CATEGORY = "{sub_category}";
    public static final String PLACEHOLDER_TIME_DIFF = "{time_diff}";
    public static final String PLACEHOLDER_TIME_DIFF_DAY = "{time_diff_d}";
    public static final String PLACEHOLDER_TIME_DIFF_HOUR = "{time_diff_h}";
    public static final String PLACEHOLDER_TIME_DIFF_MINUTE = "{time_diff_m}";

    public static final long ONE_MINUTE = 60L * 1000L;
    public static final long ONE_HOUR = 60L * ONE_MINUTE;
    public static final long ONE_DAY = 24L * ONE_HOUR;

    public static String getTimeDiff(long time) {
        long now = System.currentTimeMillis();

        long diff = now - time;

        if (diff < 0) {
            diff = 0;
        }

        int days = (int) (diff / ONE_DAY);
        int hours = (int) ((diff % ONE_DAY) / ONE_HOUR);
        int minutes = (int) ((diff % ONE_HOUR) / ONE_MINUTE);

        if (days > 0) {
            return Texts.get(Texts.TIME_DIFF_DAY).toString().replace(PLACEHOLDER_TIME_DIFF_DAY, String.valueOf(hours));
        }

        return hours > 0 ?
                Texts.get(Texts.TIME_DIFF_HOUR).toString().replace(PLACEHOLDER_TIME_DIFF_HOUR, String.valueOf(hours)) :
                Texts.get(Texts.TIME_DIFF_MINUTE).toString().replace(PLACEHOLDER_TIME_DIFF_MINUTE, String.valueOf(minutes));
    }

}

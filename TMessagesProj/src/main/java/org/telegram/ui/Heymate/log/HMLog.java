package org.telegram.ui.Heymate.log;

import android.os.Environment;
import android.util.Log;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Heymate.HeymateConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

public class HMLog {

    private static Calendar mCalendar = Calendar.getInstance();

    private static List<Log> mLog = new LinkedList<>();

    private static class Log {

        String tag;
        String message;
        Throwable t;

        String thread;
        String trace;
        String time;

        public Log(String tag, String message, Throwable t) {
            this.tag = tag;
            this.message = message;
            this.t = t;

            thread = Thread.currentThread().getName();

            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            trace = stackTrace[1].getClassName() + "." + stackTrace[1].getMethodName() + ":" + stackTrace[1].getLineNumber();

            mCalendar.setTimeInMillis(System.currentTimeMillis());
            time = mCalendar.get(Calendar.MINUTE) + ":" + mCalendar.get(Calendar.SECOND) + "." + mCalendar.get(Calendar.MILLISECOND);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

//            sb.append(thread).append(':').append(' ').append("Called from:").append(' ').append(trace).append('\n');
//            sb.append(tag).append(' ').append(time).append(" - ").append(message);

            sb.append(tag).append(' ').append(time).append(" - ").append(message).append(' ').append('(').append(thread).append(')');

            if (t != null) {
                sb.append('\n').append("______________________________").append('\n');
                sb.append(t.getMessage()).append('\n');

                StringWriter writer = new StringWriter();
                t.printStackTrace(new PrintWriter(writer));
                sb.append(writer.toString()).append('\n').append("------------------------------");
            }

            return sb.toString();
        }
    }

    public static void d(String tag, String message) {
        d(tag, message, null);
    }

    public static void d(String tag, String message, Throwable t) {
        if (HeymateConfig.DEBUG) {
            mLog.add(new Log(tag, message, t));
        }

        android.util.Log.d(tag, message, t);
    }

    public static void report(BaseFragment fragment) {
        if (mLog.isEmpty()) {
            return;
        }

        String message = digest();

        mLog.clear();

        try {
            LogToGroup.log(message, fragment);
        } catch (Throwable t) { }

        try {
            OutputStream stream = new FileOutputStream(new File(ApplicationLoader.applicationContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "special_log.txt"));
            stream.write(message.getBytes());
            stream.close();
        } catch (IOException e) { }
    }

    private static String digest() {
        StringBuilder sb = new StringBuilder();

        for (Log log: mLog) {
            sb.append(log.toString()).append('\n').append('\n');
        }

        return sb.toString();
    }

}

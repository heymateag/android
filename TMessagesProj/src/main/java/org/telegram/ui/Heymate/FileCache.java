package org.telegram.ui.Heymate;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.S3Object;
import com.google.android.exoplayer2.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.ApplicationLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import works.heymate.core.Utils;

public class FileCache {

    private static final String TAG = "FileCache";

    private static final long MAXIMUM_IMAGE_SIZE = 1024 * 1024;
    private static final long MAXIMUM_MEMORY = 4 * 1024 * 1024;

    public static final int ORIGINAL_SIZE = 0;

    private static final String FILES_DIR = "files";

    public interface Callback {

        void onResult(boolean success, Exception exception);

    }

    public interface BitmapCallback {

        void onResult(boolean success, BitmapWrapperDrawable drawable, Exception exception);

    }

    private static FileCache mInstance;

    public static FileCache get() {
        if (mInstance == null) {
            mInstance = new FileCache();
        }

        return mInstance;
    }

    private final Context mContext;
    private final SharedPreferences mPreferences;
    private final Handler mHandler;

    private final Map<String, Bitmap> mBitmaps = new HashMap<>();
    private final Map<String, List<WeakReference<BitmapWrapperDrawable>>> mReferrers = new HashMap<>();

    private FileCache() {
        mContext = ApplicationLoader.applicationContext;
        mPreferences = mContext.getSharedPreferences(TAG, Context.MODE_PRIVATE);

        HandlerThread thread = new HandlerThread(TAG);
        thread.start();

        mHandler = new Handler(thread.getLooper());
    }

    public void captureImage(String id, Uri uri, Callback callback) {
        mHandler.post(() -> {
            File file = getFileForId(id, ORIGINAL_SIZE);

            try {
                InputStream inputStream = mContext.getContentResolver().openInputStream(uri);
                OutputStream outputStream = new FileOutputStream(file);

                byte[] buffer = new byte[1024];
                int size = 0;

                while (size != -1) {
                    size = inputStream.read(buffer);

                    if (size > 0) {
                        outputStream.write(buffer, 0, size);
                    }
                }

                inputStream.close();
                outputStream.close();

                CacheInfo cacheInfo = new CacheInfo();
                setCacheInfo(id, cacheInfo);

                if (callback != null) {
                    Utils.runOnUIThread(() -> callback.onResult(true, null));
                }
            } catch (IOException e) {
                reportError("Failed to capture image", e, callback);
            }
        });
    }

    public void uploadImage(String id, Callback callback) {
        mHandler.post(() -> {
            CacheInfo cacheInfo = getCacheInfo(id);

            if (cacheInfo == null) {
                reportError("File not captured", null, callback);
                return;
            }

            File file = getFileForId(id, ORIGINAL_SIZE);

            if (!file.exists()) {
                reportError("File not found", null, callback);
                return;
            }

            if (file.length() > MAXIMUM_IMAGE_SIZE) {
                Bitmap bitmap = getBitmapFromFileConsideringMemory(file);

                if (bitmap == null) {
                    reportError("Failed to read bitmap from captured file", null, callback);
                    return;
                }

                File tempFile = new File(file.getPath() + "_temp");

                try {
                    FileOutputStream fos = new FileOutputStream(tempFile);

                    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, fos);

                    fos.close();

                    bitmap.recycle();

                    if (!file.delete() || !tempFile.renameTo(file)) {
                        tempFile.delete();
                        reportError("Failed to replace resized file with the original", null, callback);
                        return;
                    }
                } catch (IOException e) {
                    reportError("Failed to save resized bitmap file", e, callback);
                    return;
                }
            }

            // TODO fix file upload
            Utils.runOnUIThread(() -> callback.onResult(true, null));
//            try {
//                HtAmplify.getInstance(mContext).uploadFile(id, file);
//
//                if (callback != null) {
//                    Utils.runOnUIThread(() -> callback.onResult(true, null));
//                }
//            } catch (AmazonClientException e) {
//                reportError("Failed to update image file", e, callback);
//            }
        });
    }

    public void getImage(String id, int size, BitmapCallback callback) {
        mHandler.post(() -> {
            BitmapWrapperDrawable handle = new BitmapWrapperDrawable();

            String key = getKey(id, size);

            Bitmap bitmap = mBitmaps.get(key);

            if (bitmap != null) {
                if (handle != null) {
                    List<WeakReference<BitmapWrapperDrawable>> referrers = getReferrers(key, true);
                    referrers.add(new WeakReference<>(handle));
                }

                onFileUsed(id);

                handle.setBitmap(bitmap);

                notifyGetImageResult(true, handle, null, callback);
                return;
            }

            File file = getFileForId(id, size);

            if (file.exists()) {
                getImageFromFile(id, size, file, handle, callback);
                return;
            }

            if (size != ORIGINAL_SIZE) {
                File originalFile = getFileForId(id, ORIGINAL_SIZE);

                if (originalFile.exists()) {
                    getImageFromOriginalFile(id, size, originalFile, handle, callback);
                    return;
                }
            }

            File originalFile = getFileForId(id, ORIGINAL_SIZE);

            InputStream inputStream = null;
            OutputStream outputStream = null;

            // TODO actually download file
            notifyGetImageResult(false, null, null, callback);
//            try {
//                S3Object imageObject = HtAmplify.getInstance(mContext).downloadFile(id);
//
//                inputStream = imageObject.getObjectContent();
//                outputStream = new FileOutputStream(originalFile);
//
//                byte[] buffer = new byte[1024];
//                int readSize = 0;
//
//                while (readSize != -1) {
//                    readSize = inputStream.read(buffer);
//
//                    if (readSize > 0) {
//                        outputStream.write(buffer, 0, readSize);
//                    }
//                }
//
//                outputStream.close();
//
//                getImageFromOriginalFile(id, size, originalFile, handle, callback);
//            } catch (Exception e) {
//                if (e instanceof AmazonS3Exception && ((AmazonS3Exception) e).getStatusCode() == 404) {
//                    notifyGetImageResult(true, null, null, callback);
//                    return;
//                }
//
//                notifyGetImageResult(false, null, e, callback);
//            } finally {
//                try { inputStream.close(); } catch (Throwable t) { }
//                try { outputStream.close(); } catch (Throwable t) { }
//            }
        });
    }

    private void getImageFromOriginalFile(String id, int size, File originalFile, BitmapWrapperDrawable handle, BitmapCallback callback) {
        if (size == ORIGINAL_SIZE) {
            getImageFromFile(id, size, originalFile, handle, callback);
            return;
        }

        trimMemory();

        Bitmap bitmap = getBitmapFromFileConsideringMemory(originalFile);

        if (bitmap == null) {
            notifyGetImageResult(false, null, new Exception("File is not an image."), callback);
            return;
        }

        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();
        int bitmapSize = Math.min(bitmapWidth, bitmapHeight);

        if (bitmapSize < size) {
            File file = getFileForId(id, size);

            OutputStream outputStream = null;

            try {
                outputStream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
                outputStream.close();

                getImageFromFile(id, size, file, handle, callback);
            } catch (IOException e) {
                notifyGetImageResult(false, null, new Exception("Failed to write resized image.", e), callback);
            }

            try { outputStream.close(); } catch (Throwable t) { }

            return;
        }

        int targetWidth;
        int targetHeight;

        if (bitmapWidth > bitmapHeight) {
            targetHeight = size;
            targetWidth = bitmapWidth * targetHeight / bitmapHeight;
        }
        else {
            targetWidth = size;
            targetHeight = bitmapHeight * targetWidth / bitmapWidth;
        }

        Bitmap targetBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true);

        bitmap.recycle();

        File file = getFileForId(id, size);

        OutputStream outputStream = null;

        try {
            outputStream = new FileOutputStream(file);
            targetBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
            outputStream.close();

            onBitmapReadyForSize(id, size, targetBitmap, handle, callback);
        } catch (IOException e) {
            notifyGetImageResult(false, null, new Exception("Failed to write resized image.", e), callback);
        }

        try { outputStream.close(); } catch (Throwable t) { }
    }

    private void getImageFromFile(String id, int size, File file, BitmapWrapperDrawable handle, BitmapCallback callback) {
        Bitmap bitmap = getBitmapFromFileConsideringMemory(file);

        if (bitmap == null) {
            notifyGetImageResult(false, null, new Exception("File is not an image."), callback);
            return;
        }

        onBitmapReadyForSize(id, size, bitmap, handle, callback);
    }

    private void onBitmapReadyForSize(String id, int size, Bitmap bitmap, BitmapWrapperDrawable handle, BitmapCallback callback) {
        String key = getKey(id, size);

        List<WeakReference<BitmapWrapperDrawable>> referrers = getReferrers(key, true);
        referrers.add(new WeakReference<>(handle));

        mBitmaps.put(key, bitmap);

        onFileUsed(id);

        handle.setBitmap(bitmap);

        notifyGetImageResult(true, handle, null, callback);
    }

    private void notifyGetImageResult(boolean success, BitmapWrapperDrawable drawable, Exception exception, BitmapCallback callback) {
        if (!success) {
            Log.e(TAG, exception.getMessage(), exception);
        }

        if (callback != null) {
            Utils.runOnUIThread(() -> callback.onResult(success, drawable, exception));
        }
    }

    private void trimMemory() {
        ArrayList<String> keys = new ArrayList<>(mBitmaps.keySet());

        for (String key: keys) {
            List<WeakReference<BitmapWrapperDrawable>> referrers = getReferrers(key, false);

            if (referrers == null || referrers.isEmpty()) {
                Bitmap bitmap = mBitmaps.get(key);

                if (bitmap != null) {
                    bitmap.recycle();
                }

                mBitmaps.remove(key);
                mReferrers.remove(key);
            }
        }
    }

    private List<WeakReference<BitmapWrapperDrawable>> getReferrers(String key, boolean create) {
        List<WeakReference<BitmapWrapperDrawable>> referrers = mReferrers.get(key);

        if (referrers == null) {
            if (create) {
                referrers = new ArrayList<>();
                mReferrers.put(key, referrers);
            }

            return referrers;
        }

        ListIterator<WeakReference<BitmapWrapperDrawable>> iterator = referrers.listIterator();

        while (iterator.hasNext()) {
            WeakReference<BitmapWrapperDrawable> handleReference = iterator.next();

            if (handleReference.get() == null) {
                iterator.remove();
            }
        }

        return referrers;
    }

    private void reportError(String text, Exception exception, Callback callback) {
        Log.e(TAG, text, exception);

        if (callback != null) {
            Utils.runOnUIThread(() -> callback.onResult(false, exception != null ? exception : new Exception(text)));
        }
    }

    private File getFileForId(String id, int size) {
        File parent = new File(mContext.getExternalCacheDir(), FILES_DIR);

        if (!parent.exists()) {
            parent.mkdirs();
        }

        return new File(parent, getKey(id, size));
    }

    private String getKey(String id, int size) {
        return id + '-' + size;
    }

    private Bitmap getBitmapFromFileConsideringMemory(File file) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(file.getPath(), options);

        if (options.outWidth == -1 || options.outHeight == -1) {
            file.delete();
            return null;
        }

        int width = options.outWidth;
        int height = options.outHeight;

        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        ((ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryInfo(memoryInfo);
        long memoryToUse = Math.min(MAXIMUM_MEMORY, memoryInfo.availMem / 8);

        long originalMemory = width * height * 4;

        while (originalMemory / options.inSampleSize / options.inSampleSize > memoryToUse) {
            options.inSampleSize *= 2;
        }

        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(file.getPath(), options);
    }

    private void onFileUsed(String id) {
        CacheInfo cacheInfo = getCacheInfo(id);

        if (cacheInfo == null) {
            cacheInfo = new CacheInfo();
        }
        else {
            cacheInfo.lastAccessTime = System.currentTimeMillis();
        }

        setCacheInfo(id, cacheInfo);
    }

    private CacheInfo getCacheInfo(String id) {
        String sCacheInfo = mPreferences.getString(id, null);

        return sCacheInfo == null ? null : new CacheInfo(sCacheInfo);
    }

    private void setCacheInfo(String id, CacheInfo cacheInfo) {
        if (cacheInfo == null) {
            mPreferences.edit().remove(id).apply();
        }
        else {
            mPreferences.edit().putString(id, cacheInfo.toString()).apply();
        }
    }

    private static class CacheInfo {

        long lastAccessTime;

        public CacheInfo() {
            lastAccessTime = System.currentTimeMillis();
        }

        public CacheInfo(String value) {
            try {
                JSONObject jCacheInfo = new JSONObject(value);
                lastAccessTime = jCacheInfo.getLong("lat");
            } catch (JSONException e) {
                lastAccessTime = 0;
            }
        }

        @Override
        public String toString() {
            JSONObject jCacheInfo = new JSONObject();

            try {
                jCacheInfo.put("lat", lastAccessTime);
            } catch (JSONException e) { }

            return jCacheInfo.toString();
        }

    }

    public static class BitmapWrapperDrawable extends Drawable {

        private final Rect mSize = new Rect();

        private Bitmap mBitmap = null;

        private void setBitmap(Bitmap bitmap) {
            mBitmap = bitmap;

            mSize.set(0, 0, bitmap.getWidth(), bitmap.getHeight());

            invalidateSelf();
        }

        public Bitmap getBitmap() {
            return mBitmap;
        }

        @Override
        public int getIntrinsicWidth() {
            return mSize.width();
        }

        @Override
        public int getIntrinsicHeight() {
            return mSize.height();
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            if (mBitmap != null) {
                canvas.drawBitmap(mBitmap, mSize, getBounds(), null);
            }
        }

        @Override
        public void setAlpha(int alpha) {

        }

        @Override
        public void setColorFilter(@Nullable ColorFilter colorFilter) {

        }

        @Override
        public int getOpacity() {
            return PixelFormat.OPAQUE;
        }

    }

}

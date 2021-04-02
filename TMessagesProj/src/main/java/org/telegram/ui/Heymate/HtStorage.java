package org.telegram.ui.Heymate;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import com.amplifyframework.core.Amplify;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class HtStorage {

    private static HtStorage instance;
    public static final String OFFER_IMAGES_DIR = "offerImages";
    public static final String OFFER_IMAGES_NAME = "offerImage";
    public static final String OFFER_IMAGES_EXTENSION = ".jpg";

    public static HtStorage getInstance(){
        if (instance == null)
            instance = new HtStorage();
        return instance;
    }

    private HtStorage(){

    }

    public boolean imageExists(Context context, String offerId){
        ContextWrapper cw = new ContextWrapper(context);
        File directory = cw.getDir(OFFER_IMAGES_DIR, Context.MODE_PRIVATE);
        File file = new File(directory, OFFER_IMAGES_NAME + offerId + OFFER_IMAGES_EXTENSION);
        if (file.exists())
            return true;
        else
            return false;
    }

    public Bitmap getOfferImage(Context context, String offerId){
        Bitmap b;
        ContextWrapper cw = new ContextWrapper(context);
        File directory = cw.getDir(OFFER_IMAGES_DIR, Context.MODE_PRIVATE);
        File file = new File(directory, OFFER_IMAGES_NAME + offerId + OFFER_IMAGES_EXTENSION);
        if (file.exists()) {
            try {
                b = BitmapFactory.decodeStream(new FileInputStream(file));
                return b;
            } catch (FileNotFoundException e) {
                return null;
            }
        }
        return null;
    }

    public void setOfferImage(Context context, String offerId, Uri uri){
        String[] filePath = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, filePath, null, null, null);
        cursor.moveToFirst();
        String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
        cursor.close();
        ContextWrapper cw2 = new ContextWrapper(context);
        File directory2 = cw2.getDir(OFFER_IMAGES_DIR, Context.MODE_PRIVATE);
        File myPath = new File(directory2, OFFER_IMAGES_NAME + offerId + OFFER_IMAGES_EXTENSION);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(myPath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        HtAmplify.getInstance(context).saveOfferImage(offerId, myPath);
    }

    public void updateOfferImages(Context context, HashMap<String, S3Object> images){
        for(String uuid : images.keySet()){
            try {
                ContextWrapper cw2 = new ContextWrapper(context);
                File directory2 = cw2.getDir(OFFER_IMAGES_DIR, Context.MODE_PRIVATE);
                File newFile = new File(directory2, OFFER_IMAGES_NAME + uuid + OFFER_IMAGES_EXTENSION);
                FileOutputStream fos = new FileOutputStream(newFile);
                IOUtils.copy(images.get(uuid).getObjectContent(), fos);
                fos.flush();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

}

package org.telegram.ui.Heymate;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.ui.Heymate.AmplifyModels.Offer;

import java.io.File;
import java.util.ArrayList;

public class HtSQLite extends SQLiteOpenHelper {

    private static HtSQLite instance;

    public static void setInstance(Context context){
        if(instance == null){
            File dbFile = new File(ApplicationLoader.getFilesDirFixed().getPath(), "cache4.db");
            instance = new HtSQLite(context, dbFile.getPath() , null, 74);
        }
    }

    public static HtSQLite getInstance(){
        return instance;
    }

    public HtSQLite(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
//        db.execSQL("DROP TABLE IF EXISTS offer;");
        db.execSQL("CREATE TABLE IF NOT EXISTS offer(uid INTEGER PRIMARY KEY, title TEXT, rate TEXT, rateType TEXT, currency TEXT, location TEXT, time TEXT, category TEXT, subCategory TEXT, configText TEXT, terms TEXT, description TEXT, status INTEGER,  serverUUID TEXT, userId TEXT, longitude TEXT, latitude TEXT);");
        db.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public int addOffer(Offer offer) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("title", offer.getTitle());
        contentValues.put("rate", offer.getRate());
        contentValues.put("rateType", offer.getRateType());
        contentValues.put("currency", offer.getCurrency());
        contentValues.put("location", offer.getLocationData());
        contentValues.put("time", offer.getExpiry().toDate().toLocaleString());
        contentValues.put("category", offer.getCategory());
        contentValues.put("subCategory", offer.getSubCategory());
        contentValues.put("configText", offer.getTermsConfig());
        contentValues.put("terms", offer.getTerms());
        contentValues.put("description", offer.getDescription());
        contentValues.put("status", OfferStatus.ACTIVE.ordinal());
        contentValues.put("serverUUID", offer.getId());
        contentValues.put("userId", offer.getUserId());
        contentValues.put("longitude", offer.getLongitude());
        contentValues.put("latitude", offer.getLatitude());
        int createdId =  (int) database.insert("offer", null, contentValues);
        database.close();
        return createdId;
    }

    public void addOffer(int offerId, OfferDto offer) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("title", offer.getTitle());
        contentValues.put("rate", offer.getRate());
        contentValues.put("rateType", offer.getRateType());
        contentValues.put("currency", offer.getCurrency());
        contentValues.put("location", offer.getLocation());
        contentValues.put("time", offer.getExpire().toLocaleString());
        contentValues.put("category", offer.getCategory());
        contentValues.put("subCategory", offer.getSubCategory());
        contentValues.put("configText", offer.getConfigText());
        contentValues.put("terms", offer.getTerms());
        contentValues.put("description", offer.getDescription());
        contentValues.put("status", OfferStatus.ACTIVE.ordinal());
        contentValues.put("longitude", "" + offer.getLongitude());
        contentValues.put("latitude", "" + offer.getLatitude());
        contentValues.put("uid", offerId);
        database.update("offer", contentValues, "uid = ?", new String[]{"" + offerId});
        database.close();
    }

    public OfferDto getOffer(int id) {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM offer WHERE uid = ? LIMIT 1;", new String[]{"" + id});
        if (cursor.moveToFirst()) {
            OfferDto offerDto = new OfferDto();
            offerDto.setId(cursor.getInt(0));
            offerDto.setTitle(cursor.getString(1));
            offerDto.setRate(cursor.getString(2));
            offerDto.setRateType(cursor.getString(3));
            offerDto.setCurrency(cursor.getString(4));
            offerDto.setLocation(cursor.getString(5));
            offerDto.setTime(cursor.getString(6));
            offerDto.setCategory(cursor.getString(7));
            offerDto.setSubCategory(cursor.getString(8));
            offerDto.setConfigText(cursor.getString(9));
            offerDto.setTerms(cursor.getString(10));
            offerDto.setDescription(cursor.getString(11));
            offerDto.setStatus(getOfferStatus(cursor.getInt(12)));
            database.close();
            return offerDto;
        }
        database.close();
        return null;
    }

    private OfferStatus getOfferStatus(int offerStatus) {
        switch (offerStatus) {
            case 1: {
                return OfferStatus.ACTIVE;
            }
            case 2: {
                return OfferStatus.DRAFTED;
            }
            case 3: {
                return OfferStatus.ARCHIVED;
            }
            default: {
                return OfferStatus.EXPIRED;
            }
        }
    }

    public void archiveOffer(int id) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("status", 3);
        database.update("offer", contentValues, "uid = ?", new String[]{"" + id});
        database.close();
    }

    private ArrayList<OfferDto> extract(Cursor cursor) {
        ArrayList<OfferDto> offers = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                OfferDto offerDto = new OfferDto();
                offerDto.setId(cursor.getInt(0));
                offerDto.setTitle(cursor.getString(1));
                offerDto.setRate(cursor.getString(2));
                offerDto.setRateType(cursor.getString(3));
                offerDto.setCurrency(cursor.getString(4));
                offerDto.setLocation(cursor.getString(5));
                offerDto.setTime(cursor.getString(6));
                offerDto.setCategory(cursor.getString(7));
                offerDto.setSubCategory(cursor.getString(8));
                offerDto.setConfigText(cursor.getString(9));
                offerDto.setTerms(cursor.getString(10));
                offerDto.setDescription(cursor.getString(11));
                offerDto.setStatus(getOfferStatus(cursor.getInt(12)));
                offers.add(offerDto);
            } while (cursor.moveToNext());
        }
        return offers;
    }

    public ArrayList<OfferDto> getOffers(String category, String subCategory, int status, int userId) {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor;
        cursor = database.rawQuery("SELECT * FROM offer WHERE category = ? AND status = ? AND subCategory = ? AND userId = ? ORDER BY uid DESC;", new String[]{category, "" + status, subCategory, "" + userId});
        ArrayList<OfferDto> result =  extract(cursor);
        database.close();
        return result;
    }

    public ArrayList<OfferDto> getOffers(String category, String subCategory, int userId) {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor;
        cursor = database.rawQuery("SELECT * FROM offer WHERE category = ? AND subCategory = ? AND userId = ? ORDER BY uid DESC;", new String[]{category, subCategory, "" + userId});
        ArrayList<OfferDto> result =  extract(cursor);
        database.close();
        return result;
    }

    public ArrayList<OfferDto> getOffers(String category, int status, int userId) {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor;
        cursor = database.rawQuery("SELECT * FROM offer WHERE category = ? AND status = ? AND userId = ? ORDER BY uid DESC;", new String[]{category, "" + status, "" + userId});
        ArrayList<OfferDto> result =  extract(cursor);
        database.close();
        return result;
    }

    public ArrayList<OfferDto> getOffers(int status, int userId) {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor;
        cursor = database.rawQuery("SELECT * FROM offer WHERE status = ? AND userId = ? ORDER BY uid DESC;", new String[]{"" + status, "" + userId});
        ArrayList<OfferDto> result =  extract(cursor);
        database.close();
        return result;
    }

    public ArrayList<OfferDto> getOffers(String category, int userId) {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor;
        cursor = database.rawQuery("SELECT * FROM offer WHERE category = ? AND userId = ? ORDER BY uid DESC;", new String[]{category, "" + userId});
        ArrayList<OfferDto> result =  extract(cursor);
        database.close();
        return result;
    }

    public ArrayList<OfferDto> getAllOffers(int userId) {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor;
        cursor = database.rawQuery("SELECT * FROM offer WHERE userId = ? ORDER BY uid DESC;", new String[]{"" + userId});
        ArrayList<OfferDto> result = extract(cursor);
        database.close();
        return result;
    }

    public void updateOffers(ArrayList<Offer> offers, int userId) {
        SQLiteDatabase database = this.getWritableDatabase();
        database.beginTransaction();
        for (Offer offer : offers) {
            database.execSQL("INSERT INTO offer(title, rate, rateType, currency, location, time, category, subCategory, configText, terms, description, status, userId, serverUUID, userId, longitude, latitude) SELECT ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,? WHERE not exists(SELECT * FROM offer WHERE serverUUID = ?);", new String[]{offer.getTitle(), offer.getRate(), offer.getRateType(), offer.getCurrency(), offer.getLocationData(), offer.getExpiry().toDate().toLocaleString(), offer.getCategory(), offer.getSubCategory(), offer.getTermsConfig(), offer.getTerms(), offer.getDescription(), "" + OfferStatus.ACTIVE.ordinal(), "" + userId, offer.getId(), offer.getUserId(), offer.getLongitude(), offer.getLatitude(), offer.getId()});
        }
        database.endTransaction();
        database.close();
    }

}

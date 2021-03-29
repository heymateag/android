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
            instance = new HtSQLite(context, dbFile.getPath() , null, 76);
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
        db.execSQL("CREATE TABLE IF NOT EXISTS offer(uuid TEXT PRIMARY KEY, title TEXT, rate TEXT, rateType TEXT, currency TEXT, location TEXT, time TEXT, category TEXT, subCategory TEXT, configText TEXT, terms TEXT, description TEXT, status INTEGER, userId TEXT, longitude TEXT, latitude TEXT, createdAt INTEGER, editedAt INTEGER);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS offer;");
        db.execSQL("CREATE TABLE IF NOT EXISTS offer(uuid TEXT PRIMARY KEY, title TEXT, rate TEXT, rateType TEXT, currency TEXT, location TEXT, time TEXT, category TEXT, subCategory TEXT, configText TEXT, terms TEXT, description TEXT, status INTEGER, userId TEXT, longitude TEXT, latitude TEXT, createdAt INTEGER, editedAt INTEGER);");
    }

    public String addOffer(Offer offer) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("uuid", offer.getId());
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
        contentValues.put("status", offer.getStatus());
        contentValues.put("userId", offer.getUserId());
        contentValues.put("longitude", offer.getLongitude());
        contentValues.put("latitude", offer.getLatitude());
        contentValues.put("createdAt", offer.getCreatedAt());
        contentValues.put("editedAt", offer.getEditedAt());
        database.insertWithOnConflict("offer", null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);
        return offer.getId();
    }

    public void addOffer(String uuid, OfferDto offer) {
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
        contentValues.put("status", offer.getStatus().ordinal());
        contentValues.put("longitude", "" + offer.getLongitude());
        contentValues.put("latitude", "" + offer.getLatitude());
        contentValues.put("uuid", uuid);
        contentValues.put("createdAt", offer.getCreatedAt());
        contentValues.put("editedAt", offer.getEditedAt());
        database.update("offer", contentValues, "uuid = ?", new String[]{uuid});
    }

    public OfferDto getOffer(String uuid) {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM offer WHERE uuid = ? LIMIT 1;", new String[]{uuid});
        if (cursor.moveToFirst()) {
            OfferDto offerDto = new OfferDto();
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
            offerDto.setServerUUID(cursor.getString(0));
            offerDto.setCreatedAt(cursor.getInt(13));
            offerDto.setEditedAt(cursor.getInt(14));
            return offerDto;
        }
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

    public void archiveOffer(String uuid) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("status", 3);
        database.update("offer", contentValues, "uuid = ?", new String[]{uuid});
    }

    private ArrayList<OfferDto> extract(Cursor cursor) {
        ArrayList<OfferDto> offers = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                OfferDto offerDto = new OfferDto();
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
                offerDto.setServerUUID(cursor.getString(0));
                offerDto.setCreatedAt(cursor.getInt(13));
                offerDto.setEditedAt(cursor.getInt(14));
                offers.add(offerDto);
            } while (cursor.moveToNext());
        }
        return offers;
    }

    public ArrayList<OfferDto> getOffers(String category, String subCategory, int status, int userId) {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor;
        cursor = database.rawQuery("SELECT * FROM offer WHERE category = ? AND status = ? AND subCategory = ? AND userId = ? ORDER BY editedAt DESC;", new String[]{category, "" + status, subCategory, "" + userId});
        return extract(cursor);
    }

    public ArrayList<OfferDto> getOffers(String category, String subCategory, int userId) {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor;
        cursor = database.rawQuery("SELECT * FROM offer WHERE category = ? AND subCategory = ? AND userId = ? ORDER BY editedAt DESC;", new String[]{category, subCategory, "" + userId});
        return extract(cursor);
    }

    public ArrayList<OfferDto> getOffers(String category, int status, int userId) {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor;
        cursor = database.rawQuery("SELECT * FROM offer WHERE category = ? AND status = ? AND userId = ? ORDER BY editedAt DESC;", new String[]{category, "" + status, "" + userId});
        return extract(cursor);
    }

    public ArrayList<OfferDto> getOffers(int status, int userId) {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor;
        cursor = database.rawQuery("SELECT * FROM offer WHERE status = ? AND userId = ? ORDER BY editedAt DESC;", new String[]{"" + status, "" + userId});
        return extract(cursor);
    }

    public ArrayList<OfferDto> getOffers(String category, int userId) {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor;
        cursor = database.rawQuery("SELECT * FROM offer WHERE category = ? AND userId = ? ORDER BY editedAt DESC;", new String[]{category, "" + userId});
        return extract(cursor);
    }

    public ArrayList<OfferDto> getAllOffers(int userId) {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor;
        cursor = database.rawQuery("SELECT * FROM offer WHERE userId = ? ORDER BY editedAt DESC;", new String[]{"" + userId});
        return extract(cursor);
    }

    public void updateOffers(ArrayList<Offer> offers, int userId) {
        SQLiteDatabase database = this.getWritableDatabase();
        for (Offer offer : offers) {
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
            contentValues.put("status", offer.getStatus());
            contentValues.put("uuid", offer.getId());
            contentValues.put("userId", offer.getUserId());
            contentValues.put("longitude", offer.getLongitude());
            contentValues.put("latitude", offer.getLatitude());
            contentValues.put("createdAt", offer.getCreatedAt());
            contentValues.put("editedAt", offer.getEditedAt());
            database.insertWithOnConflict("offer", null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);
        }
    }

}

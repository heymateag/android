package org.telegram.ui.Heymate;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

import com.amplifyframework.datastore.generated.model.Offer;

import org.json.JSONObject;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.ui.Heymate.createoffer.PriceInputItem;

import java.io.File;
import java.util.ArrayList;

public class HtSQLite extends SQLiteOpenHelper {

    private static HtSQLite instance;

    public static void setInstance(Context context){
        if(instance == null){
            File dbFile = new File(ApplicationLoader.getFilesDirFixed().getPath(), "cache4.db");
            instance = new HtSQLite(context, dbFile.getPath() , null, 83);
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
        db.execSQL("CREATE TABLE IF NOT EXISTS offer(uuid TEXT PRIMARY KEY, title TEXT, pricingInfo TEXT, location TEXT, time TEXT, category TEXT, subCategory TEXT, configText TEXT, terms TEXT, description TEXT, status INTEGER, userId TEXT, longitude TEXT, latitude TEXT, createdAt INTEGER, editedAt INTEGER, maximumReservations INTEGER, meetingType TEXT, hasImage INTEGER);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS offer;");
        db.execSQL("CREATE TABLE IF NOT EXISTS offer(uuid TEXT PRIMARY KEY, title TEXT, pricingInfo TEXT, location TEXT, time TEXT, category TEXT, subCategory TEXT, configText TEXT, terms TEXT, description TEXT, status INTEGER, userId TEXT, longitude TEXT, latitude TEXT, createdAt INTEGER, editedAt INTEGER, maximumReservations INTEGER, meetingType TEXT, hasImage INTEGER);");
    }

    public String addOffer(Offer offer) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("uuid", offer.getId());
        contentValues.put("title", offer.getTitle());
        contentValues.put("pricingInfo", offer.getPricingInfo());
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
        contentValues.put("maximumReservations", offer.getMaximumReservations());
        contentValues.put("meetingType", offer.getMeetingType());
        contentValues.put("hasImage", offer.getHasImage() == null ? 0 : (offer.getHasImage() ? 1 : 0));
        database.insertWithOnConflict("offer", null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);
        return offer.getId();
    }

    public void addOffer(String uuid, OfferDto offer) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("title", offer.getTitle());
        contentValues.put("pricingInfo", offer.getPricingInfo() == null ? null : offer.getPricingInfo().asJSON().toString());
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
        contentValues.put("maximumReservations", offer.getMaximumReservations());
        contentValues.put("meetingType", offer.getMeetingType());
        contentValues.put("hasImage", offer.hasImage() ? 1 : 0);
        database.update("offer", contentValues, "uuid = ?", new String[]{uuid});
    }

    public OfferDto getOffer(String uuid) {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM offer WHERE uuid = ? LIMIT 1;", new String[]{uuid});
        if (cursor.moveToFirst()) {
            OfferDto offerDto = new OfferDto();
            offerDto.setTitle(cursor.getString(1));
            try {
                String sPricingInfo = cursor.getString(2);
                JSONObject jPricingInfo = new JSONObject(sPricingInfo);
                offerDto.setPricingInfo(new PriceInputItem.PricingInfo(jPricingInfo));
            } catch (Throwable t) { }
            offerDto.setLocation(cursor.getString(3));
            offerDto.setTime(cursor.getString(4));
            offerDto.setCategory(cursor.getString(5));
            offerDto.setSubCategory(cursor.getString(6));
            offerDto.setConfigText(cursor.getString(7));
            offerDto.setTerms(cursor.getString(8));
            offerDto.setDescription(cursor.getString(9));
            offerDto.setStatus(getOfferStatus(cursor.getInt(10)));
            offerDto.setServerUUID(cursor.getString(0));
            offerDto.setCreatedAt(cursor.getInt(11));
            offerDto.setEditedAt(cursor.getInt(12));
            offerDto.setMaximumReservations(cursor.getInt(13));
            offerDto.setMeetingType(cursor.getString(14));
            offerDto.setHasImage(cursor.getInt(15) == 1);
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
                try {
                    String sPricingInfo = cursor.getString(2);
                    JSONObject jPricingInfo = new JSONObject(sPricingInfo);
                    offerDto.setPricingInfo(new PriceInputItem.PricingInfo(jPricingInfo));
                } catch (Throwable t) { }
                offerDto.setLocation(cursor.getString(3));
                offerDto.setTime(cursor.getString(4));
                offerDto.setCategory(cursor.getString(5));
                offerDto.setSubCategory(cursor.getString(6));
                offerDto.setConfigText(cursor.getString(7));
                offerDto.setTerms(cursor.getString(8));
                offerDto.setDescription(cursor.getString(9));
                offerDto.setStatus(getOfferStatus(cursor.getInt(10)));
                offerDto.setServerUUID(cursor.getString(0));
                try {
                    offerDto.setUserId(cursor.getInt(11));
                } catch (Throwable t) {}
                offerDto.setLatitude(cursor.getDouble(12));
                offerDto.setLongitude(cursor.getDouble(13));
                offerDto.setCreatedAt(cursor.getInt(14));
                offerDto.setEditedAt(cursor.getInt(15));
                offerDto.setMaximumReservations(cursor.getInt(16));
                offerDto.setMeetingType(cursor.getString(17));
                offerDto.setHasImage(cursor.getInt(18) == 1);
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
            contentValues.put("pricingInfo", offer.getPricingInfo());
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
            contentValues.put("maximumReservations", offer.getMaximumReservations());
            contentValues.put("meetingType", offer.getMeetingType());
            contentValues.put("hasImage", offer.getHasImage() == null ? 0 : (offer.getHasImage() ? 1 : 0));
            database.insertWithOnConflict("offer", null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
        }
    }

}

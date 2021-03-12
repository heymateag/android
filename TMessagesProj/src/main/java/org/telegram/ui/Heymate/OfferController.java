package org.telegram.ui.Heymate;

import org.telegram.SQLite.SQLiteCursor;
import org.telegram.SQLite.SQLiteDatabase;
import org.telegram.SQLite.SQLiteException;
import org.telegram.SQLite.SQLitePreparedStatement;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BaseController;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.FileLog;

import java.io.File;
import java.util.ArrayList;

public class OfferController {

    private static OfferController instance = new OfferController(0);
    private SQLiteDatabase database;

    private OfferController(int currentAccount) {
        try {
            File filesDir = ApplicationLoader.getFilesDirFixed();
            cacheFile = new File(filesDir, "cache4.db");
            walCacheFile = new File(filesDir, "cache4.db-wal");
            shmCacheFile = new File(filesDir, "cache4.db-shm");
            database = new SQLiteDatabase(cacheFile.getPath());
            openDatabase(currentAccount);
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
    }

    private File cacheFile;
    private File walCacheFile;
    private File shmCacheFile;

    public void openDatabase(int currentAccount) {
        File filesDir = ApplicationLoader.getFilesDirFixed();
        if (currentAccount != 0) {
            filesDir = new File(filesDir, "account" + currentAccount + "/");
            filesDir.mkdirs();
        }

        boolean createTable = false;
        //cacheFile.delete();
        if (!cacheFile.exists()) {
            createTable = true;
        }
        try {
            database = new SQLiteDatabase(cacheFile.getPath());
            database.executeFast("PRAGMA secure_delete = ON").stepThis().dispose();
            database.executeFast("PRAGMA temp_store = MEMORY").stepThis().dispose();
            database.executeFast("PRAGMA journal_mode = WAL").stepThis().dispose();
            database.executeFast("PRAGMA journal_size_limit = 10485760").stepThis().dispose();
//            database.executeFast("DROP TABLE IF EXISTS offer;").stepThis().dispose();;
            database.executeFast("CREATE TABLE IF NOT EXISTS offer(uid INTEGER PRIMARY KEY, title TEXT, rate INTEGER, rateType INTEGER, currency TEXT, location TEXT, time TEXT, category TEXT, subCategory TEXT, configText TEXT, terms TEXT, description TEXT, status INTEGER, accountNumber INTEGER);").stepThis().dispose();

            if (createTable) {
                if (BuildVars.LOGS_ENABLED) {
                    FileLog.d("create new database");
                }

                database.executeFast("PRAGMA user_version = 74").stepThis().dispose();
            } else {
                int version = database.executeInt("PRAGMA user_version");
                if (BuildVars.LOGS_ENABLED) {
                    FileLog.d("current db version = " + version);
                }
                if (version == 0) {
                    throw new Exception("malformed");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static OfferController getInstance() {
        return instance;
    }

    public int addOffer(String title, int rate, String rateType, String currency, String location, String time, String category, String subCategory, String configText, String terms, String description, int status, int accountNumber) {
        try {
            database.beginTransaction();
            SQLitePreparedStatement statement = database.executeFast("INSERT INTO offer(title, rate, rateType, currency, location, time, category, subCategory, configText, terms, description, status, accountNumber) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?);");
            statement.bindString(1, title);
            statement.bindInteger(2, rate);
            statement.bindString(3, rateType);
            statement.bindString(4, currency);
            statement.bindString(5, location);
            statement.bindString(6, time);
            statement.bindString(7, category);
            statement.bindString(8, subCategory);
            statement.bindString(9, configText);
            statement.bindString(10, terms);
            statement.bindString(11, description);
            statement.bindInteger(12, status);
            statement.bindInteger(13, accountNumber);
            statement.step();
            database.commitTransaction();
            SQLiteCursor cursor = database.queryFinalized("SELECT MAX(uid) FROM offer;");
            cursor.next();
            return cursor.intValue(0);
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void addOffer(int offerId, String title, int rate, String rateType, String currency, String location, String time, String category, String subCategory, String configText, String terms, String description, int status) {
        try {
            database.beginTransaction();
            SQLitePreparedStatement statement2 = database.executeFast("UPDATE offer SET title = ?, rate = ?, rateType = ?, currency = ?, location = ?, time = ?, category = ?, subCategory = ?, configText = ?, terms = ?, description = ?, status = ? WHERE uid = ?;");
            statement2.bindString(1, title);
            statement2.bindInteger(2, rate);
            statement2.bindString(3, rateType);
            statement2.bindString(4, currency);
            statement2.bindString(5, location);
            statement2.bindString(6, time);
            statement2.bindString(7, category);
            statement2.bindString(8, subCategory);
            statement2.bindString(9, configText);
            statement2.bindString(10, terms);
            statement2.bindString(11, description);
            statement2.bindInteger(12, status);
            statement2.bindInteger(13, offerId);
            statement2.step();
            database.commitTransaction();
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
    }

    public OfferDto getOffer(int id) {
        try {
            SQLiteCursor cursor = database.queryFinalized("SELECT * FROM offer WHERE uid = ? LIMIT 1;", id);
            OfferDto offerDto = new OfferDto();
            cursor.next();
            offerDto.setId(cursor.intValue(0));
            offerDto.setTitle(cursor.stringValue(1));
            offerDto.setRate(cursor.intValue(2));
            offerDto.setRateType(cursor.stringValue(3));
            offerDto.setCurrency(cursor.stringValue(4));
            offerDto.setLocation(cursor.stringValue(5));
            offerDto.setTime(cursor.stringValue(6));
            offerDto.setCategory(cursor.stringValue(7));
            offerDto.setSubCategory(cursor.stringValue(8));
            offerDto.setConfigText(cursor.stringValue(9));
            offerDto.setTerms(cursor.stringValue(10));
            offerDto.setDescription(cursor.stringValue(11));
            offerDto.setStatus(getOfferStatus(cursor.intValue(12)));
            return offerDto;
        } catch (SQLiteException e) {
            e.printStackTrace();
            return null;
        }
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
        try {
            database.beginTransaction();
            SQLitePreparedStatement statement = database.executeFast("UPDATE offer SET status = 3 WHERE uid = ?;");
            statement.bindInteger(1, id);
            statement.step();
            database.commitTransaction();
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<OfferDto> extract(SQLiteCursor cursor) throws SQLiteException {
        ArrayList<OfferDto> offers = new ArrayList<>();
        while (cursor.next()) {
            OfferDto offerDto = new OfferDto();
            offerDto.setId(cursor.intValue(0));
            offerDto.setTitle(cursor.stringValue(1));
            offerDto.setRate(cursor.intValue(2));
            offerDto.setRateType(cursor.stringValue(3));
            offerDto.setCurrency(cursor.stringValue(4));
            offerDto.setLocation(cursor.stringValue(5));
            offerDto.setTime(cursor.stringValue(6));
            offerDto.setCategory(cursor.stringValue(7));
            offerDto.setSubCategory(cursor.stringValue(8));
            offerDto.setConfigText(cursor.stringValue(9));
            offerDto.setTerms(cursor.stringValue(10));
            offerDto.setDescription(cursor.stringValue(11));
            offerDto.setStatus(getOfferStatus(cursor.intValue(12)));
            offers.add(offerDto);
        }
        return offers;
    }

    public ArrayList<OfferDto> getOffers(String category, String subCategory, int status, int accountNumber) {
        try {
            SQLiteCursor cursor;
            cursor = database.queryFinalized("SELECT * FROM offer WHERE category = ? AND status = ? AND subCategory = ? AND accountNumber = ? ORDER BY uid DESC;", category, status, subCategory, accountNumber);
            return extract(cursor);
        } catch (SQLiteException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<OfferDto> getOffers(String category, String subCategory, int accountNumber) {
        try {
            SQLiteCursor cursor;
            cursor = database.queryFinalized("SELECT * FROM offer WHERE category = ? AND subCategory = ? AND accountNumber = ? ORDER BY uid DESC;", category, subCategory, accountNumber);
            return extract(cursor);
        } catch (SQLiteException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<OfferDto> getOffers(String category, int status, int accountNumber) {
        try {
            SQLiteCursor cursor;
            cursor = database.queryFinalized("SELECT * FROM offer WHERE category = ? AND status = ? AND accountNumber = ? ORDER BY uid DESC;", category, status, accountNumber);
            return extract(cursor);
        } catch (SQLiteException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<OfferDto> getOffers(int status, int accountNumber) {
        try {
            SQLiteCursor cursor;
            cursor = database.queryFinalized("SELECT * FROM offer WHERE status = ? AND accountNumber = ? ORDER BY uid DESC;", status, accountNumber);
            return extract(cursor);
        } catch (SQLiteException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<OfferDto> getOffers(String category, int accountNumber) {
        try {
            SQLiteCursor cursor;
            cursor = database.queryFinalized("SELECT * FROM offer WHERE category = ? AND accountNumber = ? ORDER BY uid DESC;", category, accountNumber);
            return extract(cursor);
        } catch (SQLiteException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<OfferDto> getAllOffers(int accountNumber) {
        try {
            SQLiteCursor cursor = database.queryFinalized("SELECT * FROM offer WHERE accountNumber = ? ORDER BY uid DESC;", accountNumber);
            ArrayList<OfferDto> offers = new ArrayList<>();
            int i = 0;
            while (cursor.next()) {
                OfferDto offerDto = new OfferDto();
                offerDto.setId(cursor.intValue(0));
                offerDto.setTitle(cursor.stringValue(1));
                offerDto.setRate(cursor.intValue(2));
                offerDto.setRateType(cursor.stringValue(3));
                offerDto.setCurrency(cursor.stringValue(4));
                offerDto.setLocation(cursor.stringValue(5));
                offerDto.setTime(cursor.stringValue(6));
                offerDto.setCategory(cursor.stringValue(7));
                offerDto.setSubCategory(cursor.stringValue(8));
                offerDto.setConfigText(cursor.stringValue(9));
                offerDto.setTerms(cursor.stringValue(10));
                offerDto.setDescription(cursor.stringValue(11));
                offerDto.setStatus(getOfferStatus(cursor.intValue(12)));
                offers.add(offerDto);
            }
            return offers;
        } catch (SQLiteException e) {
            e.printStackTrace();
            return null;
        }
    }

}

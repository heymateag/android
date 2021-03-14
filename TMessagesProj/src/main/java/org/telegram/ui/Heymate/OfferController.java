package org.telegram.ui.Heymate;

import org.telegram.SQLite.SQLiteCursor;
import org.telegram.SQLite.SQLiteDatabase;
import org.telegram.SQLite.SQLiteException;
import org.telegram.SQLite.SQLitePreparedStatement;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BaseController;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Heymate.AmplifyModels.Offer;

import java.io.File;
import java.util.ArrayList;

public class OfferController {

    private static OfferController instance = new OfferController();
    private SQLiteDatabase database;
    private BaseFragment parent;

    private OfferController() {
        try {
            File filesDir = ApplicationLoader.getFilesDirFixed();
            cacheFile = new File(filesDir, "cache4.db");
            walCacheFile = new File(filesDir, "cache4.db-wal");
            shmCacheFile = new File(filesDir, "cache4.db-shm");
            database = new SQLiteDatabase(cacheFile.getPath());
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
//            database.executeFast("DROP TABLE IF EXISTS offer;").stepThis().dispose();
            database.executeFast("CREATE TABLE IF NOT EXISTS offer(uid INTEGER PRIMARY KEY, title TEXT, rate TEXT, rateType TEXT, currency TEXT, location TEXT, time TEXT, category TEXT, subCategory TEXT, configText TEXT, terms TEXT, description TEXT, status INTEGER, accountNumber INTEGER, serverUUID TEXT, userId STRING, longitude TEXT, latitude TEXT);").stepThis().dispose();

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

    public int addOffer(Offer offer, int accountNumber) {
        try {
            database.beginTransaction();
            SQLitePreparedStatement statement = database.executeFast("INSERT INTO offer(title, rate, rateType, currency, location, time, category, subCategory, configText, terms, description, status, accountNumber, serverUUID, userId, longitude, latitude) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);");
            statement.bindString(1, offer.getTitle());
            statement.bindString(2, offer.getRate());
            statement.bindString(3, offer.getRateType());
            statement.bindString(4, offer.getCurrency());
            statement.bindString(5, offer.getLocationData());
            statement.bindString(6, offer.getExpiry().toDate().toLocaleString());
            statement.bindString(7, offer.getCategory());
            statement.bindString(8, offer.getSubCategory());
            statement.bindString(9, offer.getTermsConfig());
            statement.bindString(10, offer.getTerms());
            statement.bindString(11, offer.getDescription());
            statement.bindInteger(12, OfferStatus.ACTIVE.ordinal());
            statement.bindInteger(13, accountNumber);
            statement.bindString(14, offer.getId());
            statement.bindString(15, offer.getUserId());
            statement.bindString(16, offer.getLongitude());
            statement.bindString(17, offer.getLatitude());
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

    public void addOffer(int offerId, OfferDto offer) {
        try {
            database.beginTransaction();
            SQLitePreparedStatement statement = database.executeFast("UPDATE offer SET title = ?, rate = ?, rateType = ?, currency = ?, location = ?, time = ?, category = ?, subCategory = ?, configText = ?, terms = ?, description = ?, status = ?, longitude = ?, latitude = ? WHERE uid = ?;");
            statement.bindString(1, offer.getTitle());
            statement.bindString(2, offer.getRate());
            statement.bindString(3, offer.getRateType());
            statement.bindString(4, offer.getCurrency());
            statement.bindString(5, offer.getLocation());
            statement.bindString(6, offer.getExpire().toLocaleString());
            statement.bindString(7, offer.getCategory());
            statement.bindString(8, offer.getSubCategory());
            statement.bindString(9, offer.getConfigText());
            statement.bindString(10, offer.getTerms());
            statement.bindString(11, offer.getDescription());
            statement.bindInteger(12, OfferStatus.ACTIVE.ordinal());
            statement.bindString(13, "" + offer.getLongitude());
            statement.bindString(14, "" + offer.getLatitude());
            statement.bindInteger(15, offerId);
            statement.step();
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
            offerDto.setRate(cursor.stringValue(2));
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
            offerDto.setRate(cursor.stringValue(2));
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
                offerDto.setRate(cursor.stringValue(2));
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

    public void updateOffers(ArrayList<Offer> offers, int accountNumber) {
        try {
            database.beginTransaction();
            for (Offer offer : offers) {
                SQLitePreparedStatement statement = database.executeFast("INSERT INTO offer(title, rate, rateType, currency, location, time, category, subCategory, configText, terms, description, status, accountNumber, serverUUID, userId, longitude, latitude) SELECT ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,? WHERE not exists(SELECT * FROM offer WHERE serverUUID = ?);");
                statement.bindString(1, offer.getTitle());
                statement.bindString(2, offer.getRate());
                statement.bindString(3, offer.getRateType());
                statement.bindString(4, offer.getCurrency());
                statement.bindString(5, offer.getLocationData());
                statement.bindString(6, offer.getExpiry().toDate().toLocaleString());
                statement.bindString(7, offer.getCategory());
                statement.bindString(8, offer.getSubCategory());
                statement.bindString(9, offer.getTermsConfig());
                statement.bindString(10, offer.getTerms());
                statement.bindString(11, offer.getDescription());
                statement.bindInteger(12, OfferStatus.ACTIVE.ordinal());
                statement.bindInteger(13, accountNumber);
                statement.bindString(14, offer.getId());
                statement.bindString(15, offer.getUserId());
                statement.bindString(16, offer.getLongitude());
                statement.bindString(17, offer.getLatitude());
                statement.bindString(18, offer.getId());
                statement.step();
            }
            database.commitTransaction();
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
    }

    public void setParent(BaseFragment parent) {
        this.parent = parent;
    }
}

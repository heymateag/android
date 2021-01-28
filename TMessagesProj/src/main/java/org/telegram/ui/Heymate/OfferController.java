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

public class OfferController extends BaseController {
    private static OfferController instance = new OfferController(0);
    private SQLiteDatabase database;

    private OfferController(int num) {
        super(num);
        try {
            File filesDir = ApplicationLoader.getFilesDirFixed();
            cacheFile = new File(filesDir, "cache4.db");
            walCacheFile = new File(filesDir, "cache4.db-wal");
            shmCacheFile = new File(filesDir, "cache4.db-shm");
            database = new SQLiteDatabase(cacheFile.getPath());
            openDatabase(1);
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
    }

    private File cacheFile;
    private File walCacheFile;
    private File shmCacheFile;

    public void openDatabase(int openTries) {
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
            database.executeFast("DROP TABLE offer;").stepThis().dispose();;
            database.executeFast("CREATE TABLE IF NOT EXISTS offer(uid INTEGER PRIMARY KEY, title TEXT, rate INTEGER, rateType INTEGER, currency TEXT, location TEXT, time TEXT);").stepThis().dispose();;

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

    public void addOffer(String title, int rate, String rateType, String currency, String location, String time){
        try {
            database.beginTransaction();
            SQLitePreparedStatement statement = database.executeFast("INSERT INTO offer(title, rate, rateType, currency, location, time) VALUES(?,?,?,?,?,?);");
            statement.bindString(1,title);
            statement.bindInteger(2,rate);
            statement.bindString(3,rateType);
            statement.bindString(4,currency);
            statement.bindString(5,location);
            statement.bindString(6,time);
            statement.step();
            database.commitTransaction();
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
    }

    public OfferDto getOffer(int id){
        try {
            openDatabase(1);
            SQLiteCursor cursor = database.queryFinalized("SELECT * FROM offer LIMIT 1;");
            OfferDto offerDto = new OfferDto();
            cursor.next();
            offerDto.setTitle(cursor.stringValue(1));
            offerDto.setRate(cursor.intValue(2));
            offerDto.setRateType(cursor.stringValue(3));
            offerDto.setCurrency(cursor.stringValue(4));
            offerDto.setLocation(cursor.stringValue(5));
            offerDto.setTime(cursor.stringValue(6));
            return offerDto;
        } catch (SQLiteException e) {
            e.printStackTrace();
            return  null;
        }
    }

    public ArrayList<OfferDto> getAllOffers(){
        try {
            SQLiteCursor cursor = database.queryFinalized("SELECT * FROM offer ORDER BY uid DESC LIMIT 20;");
            ArrayList<OfferDto> offers = new ArrayList<>();
            int i = 0;
            while (cursor.next()){
                OfferDto offerDto = new OfferDto();
                offerDto.setTitle(cursor.stringValue(1));
                offerDto.setRate(cursor.intValue(2));
                offerDto.setRateType(cursor.stringValue(3));
                offerDto.setCurrency(cursor.stringValue(4));
                offerDto.setLocation(cursor.stringValue(5));
                offerDto.setTime(cursor.stringValue(6));
                offers.add(offerDto);
            }
            return offers;
        } catch (SQLiteException e) {
            e.printStackTrace();
            return  null;
        }
    }

}

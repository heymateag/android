package org.telegram.ui.Heymate;

public class DatabaseWatchDog {

    public static DatabaseWatchDog instance = new DatabaseWatchDog();
    public void config(){
        OfferController offerController = OfferController.getInstance();
        offerController.openDatabase(1);
    }

    private DatabaseWatchDog(){
    }

    public static DatabaseWatchDog getInstance(){
        return instance;
    }
}

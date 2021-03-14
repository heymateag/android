package org.telegram.ui.Heymate;

public class DatabaseWatchDog {

    public static DatabaseWatchDog instance = new DatabaseWatchDog();
    public void config(int currentAccount){
        OfferController offerController = OfferController.getInstance();
        offerController.openDatabase(currentAccount);
    }

    private DatabaseWatchDog(){
    }

    public static DatabaseWatchDog getInstance(){
        return instance;
    }
}

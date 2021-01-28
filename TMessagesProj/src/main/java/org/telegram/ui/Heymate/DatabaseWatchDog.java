package org.telegram.ui.Heymate;

public class DatabaseWatchDog {

    public static DatabaseWatchDog instance = new DatabaseWatchDog();
    public void config(){
        OfferController offerController = OfferController.getInstance();
        offerController.openDatabase(1);
        offerController.addOffer("Nail Job", 40,"Per Item","US$","Some Address", "22:00 01-02-2020");
        offerController.addOffer("Babysitter", 35,"Per Hour","US$","Some Address", "18:00 17-02-2021");
        offerController.addOffer("Car Repair", 350,"Per Item","CA$","Some Address", "07:30 11-05-2021");
        offerController.addOffer("Web Design", 15,"Per Hour","US$","South Eskandari, Tehran", "11:15 28-01-2021");
    }

    private DatabaseWatchDog(){
    }

    public static DatabaseWatchDog getInstance(){
        return instance;
    }
}

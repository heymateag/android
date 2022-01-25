package works.heymate.model;

import works.heymate.api.APIArray;
import works.heymate.api.APIObject;

public class Offers {

    public static String getImageFileName(APIObject offer) {
        if (offer == null) {
            return null;
        }

        APIArray media = offer.getArray(Offer.MEDIA);

        if (media == null || media.size() < 1) {
            return null;
        }

        return media.getObject(0).getString(Offer.Media.KEY);
    }

}

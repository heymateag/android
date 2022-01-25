package works.heymate.api;

import java.util.HashMap;
import java.util.Map;

public class APIs {

    private static IHeymateAPI sAPI = null;
    private static final Map<String, APIObject> sUsers = new HashMap<>();

    public static IHeymateAPI get() {
        if (sAPI == null) {
            sAPI = new HeymateAPIImpl();
        }

        return sAPI;
    }

}

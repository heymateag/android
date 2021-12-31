package works.heymate.api;

public class APIs {

    private static IHeymateAPI sAPI = null;

    public static IHeymateAPI get() {
        if (sAPI == null) {
            sAPI = new HeymateAPIImpl();
        }

        return sAPI;
    }

}

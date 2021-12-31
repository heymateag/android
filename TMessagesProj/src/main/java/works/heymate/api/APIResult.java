package works.heymate.api;

public class APIResult {

    public final boolean success;
    public final APIObject response;
    public final Exception error;

    APIResult(Exception error) {
        success = false;
        response = null;
        this.error = error;
    }

    APIResult(boolean success, Exception error) {
        this.success = success;
        response = null;
        this.error = success ? null : error;
    }

    APIResult(APIObject response) {
        success = true;
        this.response = response;
        error = null;
    }

}

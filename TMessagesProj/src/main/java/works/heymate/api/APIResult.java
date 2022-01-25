package works.heymate.api;

public class APIResult {

    public final boolean success;
    public final APIObject response;
    public final Exception error;

    public APIResult(boolean success) {
        this.success = success;
        this.response = null;
        this.error = null;
    }

    public APIResult(APIObject response) {
        success = true;
        this.response = response;
        error = null;
    }

    public APIResult(Exception error) {
        success = false;
        response = null;
        this.error = error;
    }

    APIResult(boolean success, Exception error) {
        this.success = success;
        response = null;
        this.error = success ? null : error;
    }

}

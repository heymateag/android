package works.heymate.core;

public interface APICallback<T> {

    void onAPICallResult(boolean success, T result, Exception exception);

}

package works.heymate.core;

import android.os.Handler;
import android.os.Looper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class HeymateEvents {

    public static final int WALLET_CREATED = 0; // Wallet
    public static final int PHONE_NUMBER_VERIFIED_STATUS_UPDATED = 1; // Wallet, VerifiedStatus, CeloError
    public static final int RESERVATION_STATUS_UPDATED = 2; // reservationId
    public static final int SHOPS_UPDATED = 3; // shops
    public static final int JOINING_MEETING = 4;
    public static final int FAILED_TO_JOIN_MEETING = 5; // exception
    public static final int JOINED_MEETING = 6;
    public static final int LEFT_MEETING = 7;
    public static final int USER_JOINED_MEETING = 8; // userId, meetingMember
    public static final int USER_LEFT_MEETING = 9; // userId, meetingMember
    public static final int MEETING_USER_STATUS_CHANGED = 10; // userId

    public interface HeymateEventObserver {

        void onHeymateEvent(int event, Object... args);

    }

    private static Map<Integer, List<WeakReference<HeymateEventObserver>>> mObservers = new Hashtable<>();

    public static void register(int event, HeymateEventObserver observer) {
        Utils.runOnUIThread(() -> {
            List<WeakReference<HeymateEventObserver>> observerReferences = mObservers.get(event);

            if (observerReferences == null) {
                observerReferences = new LinkedList<>();

                mObservers.put(event, observerReferences);
            }

            ListIterator<WeakReference<HeymateEventObserver>> observerIterator = observerReferences.listIterator();

            while (observerIterator.hasNext()) {
                WeakReference<HeymateEventObserver> observerReference = observerIterator.next();

                if (observerReference.get() == null) {
                    observerIterator.remove();
                }
                else if (observerReference.get().equals(observer)) {
                    return;
                }
            }

            observerReferences.add(new WeakReference<>(observer));
        });
    }

    public static void unregister(int event, HeymateEventObserver observer) {
        Utils.runOnUIThread(() -> {
            List<WeakReference<HeymateEventObserver>> observerReferences = mObservers.get(event);

            if (observerReferences == null) {
                return;
            }

            ListIterator<WeakReference<HeymateEventObserver>> observerIterator = observerReferences.listIterator();

            while (observerIterator.hasNext()) {
                WeakReference<HeymateEventObserver> observerReference = observerIterator.next();

                if (observerReference.get() == null) {
                    observerIterator.remove();
                }
                else if (observerReference.get().equals(observer)) {
                    observerIterator.remove();
                    return;
                }
            }
        });
    }

    public static void notify(int event, Object... args) {
        Utils.postOnUIThread(() -> {
            List<WeakReference<HeymateEventObserver>> observerReferences = mObservers.get(event);

            if (observerReferences == null) {
                return;
            }

            ArrayList<WeakReference<HeymateEventObserver>> notifyObservers = new ArrayList<>(observerReferences);

            for (WeakReference<HeymateEventObserver> observerReference: notifyObservers) {
                HeymateEventObserver observer = observerReference.get();

                if (observer != null) {
                    observer.onHeymateEvent(event, args);
                }
            }
        });
    }

}

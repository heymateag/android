package org.telegram.ui.Heymate;

import android.content.Context;

import com.amplifyframework.datastore.generated.model.Offer;
import com.amplifyframework.datastore.generated.model.Reservation;
import com.amplifyframework.datastore.generated.model.TimeSlot;
import com.google.android.exoplayer2.util.Log;

import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.UserConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import works.heymate.core.reservation.ReservationUtils;
import works.heymate.core.wallet.Wallet;

public class OnlineReservation {

    private static final String TAG = "OnlineReservation";

    public static void stabilizeOnlineMeetingStatuses(Context context) {
        stabilizeMyOffersStatuses(context);
        stabilizeMyOrdersStatuses(context);
    }

    private static void stabilizeMyOffersStatuses(Context context) {
        HtAmplify.getInstance(context).getMyReservedTimeSlots((success, result, exception) -> {
            if (success) {
                ListIterator<TimeSlot> iterator = result.listIterator();

                int now = (int) (System.currentTimeMillis() / 1000);

                Map<String, List<TimeSlot>> offerToTimeSlots = new HashMap<>();

                while (iterator.hasNext()) {
                    TimeSlot timeSlot = iterator.next();

                    if (timeSlot.getEndTime() == null || now > timeSlot.getEndTime() || !MeetingType.ONLINE_MEETING.equals(timeSlot.getMeetingType())) {
                        iterator.remove();
                    }
                    else {
                        List<TimeSlot> timeSlots = offerToTimeSlots.get(timeSlot.getId());

                        if (timeSlots == null) {
                            timeSlots = new ArrayList<>(1);
                            offerToTimeSlots.put(timeSlot.getId(), timeSlots);
                        }

                        timeSlots.add(timeSlot);
                    }
                }

                if (offerToTimeSlots.isEmpty()) {
                    return;
                }

                HtAmplify.getInstance(context).getOffers(offerToTimeSlots.keySet(), (success1, result1, exception1) -> {
                    if (success1) {
                        for (Offer offer: result1) {
                            List<TimeSlot> timeSlots = offerToTimeSlots.get(offer.getId());

                            stabilizeOfferStatuses(context, offer, timeSlots);
                        }
                    }
                    else {
                        Log.e(TAG, "Failed to get offers of reservations");
                    }
                });
            }
            else {
                Log.e(TAG, "Failed to get my reserved timeslots", exception);
            }
        });
    }

    private static void stabilizeOfferStatuses(Context context, Offer offer, List<TimeSlot> timeSlots) {
        for (TimeSlot timeSlot: timeSlots) {
            HtAmplify.getInstance(context).getTimeSlotReservations(timeSlot.getId(), (success, result, exception) -> {
                if (success) {
                    stabilizeTimeSlotStatuses(context, offer, timeSlot, result, false);
                }
                else {
                    Log.e(TAG, "Failed to get time slot reservations.", exception);
                }
            });
        }
    }

    public static HtTimeSlotStatus stabilizeTimeSlotStatuses(Context context, Offer offer, TimeSlot timeSlot, List<Reservation> reservations, boolean withInterface) {
        HtTimeSlotStatus status = concludeTimeSlotStatus(reservations);

        if (reservations == null || offer == null || timeSlot == null || status == null) {
            return status;
        }

        ensureUniformStatus(context, offer, timeSlot, reservations, status, withInterface);

        return status;
    }

    // TODO Error handling is missing.
    public static String ensureUniformStatus(Context context, Offer offer, TimeSlot timeSlot, List<Reservation> reservations, HtTimeSlotStatus status, boolean withInterface) {
        String meetingId = getMeetingId(reservations);

        ArrayList<Reservation> reservationsCopy = new ArrayList<>(reservations);

        if (withInterface) {
            LoadingUtil.onLoadingStarted();
        }

        ensureNextStatus(context, reservationsCopy, offer, status, meetingId, withInterface);

        return meetingId;
    }

    private static void ensureNextStatus(Context context, ArrayList<Reservation> reservations, Offer offer, HtTimeSlotStatus status, String meetingId, boolean hideLoading) {
        if (reservations.isEmpty()) {
            if (hideLoading) {
                LoadingUtil.onLoadingFinished();
            }
            return;
        }

        Reservation reservation = reservations.remove(0);

        HtTimeSlotStatus reservationStatus = HtTimeSlotStatus.valueOf(reservation.getStatus());

        HtTimeSlotStatus statusToApply = status;

        boolean onlineMeeting = MeetingType.ONLINE_MEETING.equals(reservation.getMeetingType());

        switch (reservationStatus) {
            case CANCELLED_BY_CONSUMER:
                statusToApply = reservationStatus;
                break;
            case BOOKED:
            case MARKED_AS_STARTED:
                statusToApply = (status == HtTimeSlotStatus.BOOKED || status == HtTimeSlotStatus.CANCELLED_BY_SERVICE_PROVIDER) ? status : HtTimeSlotStatus.MARKED_AS_STARTED;
                break;
            case STARTED:
                switch (status) {
                    case MARKED_AS_FINISHED:
                    case FINISHED:
                        statusToApply = status;
                        break;
                    case CANCELLED_BY_SERVICE_PROVIDER:
                    default:
                        statusToApply = reservationStatus;
                        break;
                }
                break;
            case MARKED_AS_FINISHED:
            case FINISHED:
            case CANCELLED_BY_SERVICE_PROVIDER:
                statusToApply = reservationStatus;
                break;
        }

        if (statusToApply == reservationStatus) {
            ensureNextStatus(context, reservations, offer, status, meetingId, hideLoading);
            return;
        }

        if (onlineMeeting && reservationStatus == HtTimeSlotStatus.BOOKED && statusToApply == HtTimeSlotStatus.MARKED_AS_STARTED) {
            String sUserId = reservation.getConsumerId();

            try {
                int userId = Integer.parseInt(sUserId);

                String message = ReservationUtils.serializeBeautiful(reservation, offer, ReservationUtils.OFFER_ID, ReservationUtils.MEETING_ID, ReservationUtils.MEETING_TYPE, ReservationUtils.START_TIME, ReservationUtils.SERVICE_PROVIDER_ID);
                SendMessagesHelper.getInstance(UserConfig.selectedAccount).sendMessage(message, userId, null, null, null, false, null, null, null, true, 0);
            } catch (NumberFormatException e) { }
        }

        if (statusToApply == HtTimeSlotStatus.CANCELLED_BY_SERVICE_PROVIDER) {
            Wallet wallet = Wallet.get(context, TG2HM.getCurrentPhoneNumber());

            wallet.cancelOffer(offer, reservation, false, (success, errorCause) -> {
                HtAmplify.getInstance(context).updateReservation(reservation, HtTimeSlotStatus.CANCELLED_BY_SERVICE_PROVIDER, meetingId);
                ensureNextStatus(context, reservations, offer, status, meetingId, hideLoading);
            });
            return;
        }

        HtAmplify.getInstance(context).updateReservation(reservation, statusToApply, meetingId);
        ensureNextStatus(context, reservations, offer, status, meetingId, hideLoading);
    }

    private static HtTimeSlotStatus concludeTimeSlotStatus(List<Reservation> reservations) {
        if (reservations == null || reservations.isEmpty()) {
            return null;
        }

        if (reservations.size() == 1) {
            return HtTimeSlotStatus.valueOf(reservations.get(0).getStatus());
        }

        HtTimeSlotStatus status = HtTimeSlotStatus.BOOKED;

        for (Reservation reservation: reservations) {
            HtTimeSlotStatus reservationStatus = HtTimeSlotStatus.valueOf(reservation.getStatus());

            if (reservationStatus.happensAfter(status)) {
                status = reservationStatus;
            }
        }

        return status;
    }

    public static String getMeetingId(List<Reservation> reservations) {
        if (reservations == null) {
            return null;
        }

        for (Reservation reservation: reservations) {
            if (reservation.getMeetingId() != null) {
                return reservation.getMeetingId();
            }
        }

        return UUID.randomUUID().toString();
    }

    public static void stabilizeMyOrdersStatuses(Context context) {
        HtAmplify.getInstance(context).getMyPendingOnlineReservations((success, result, exception) -> {
            if (success) {
                for (Reservation reservation: result) {
                    stabilizeReservationStatus(context, reservation, null);
                }
            }
            else {
                Log.e(TAG, "Failed to get my pending online reservations.", exception);
            }
        });
    }

    public static void stabilizeReservationStatus(Context context, String reservationId, Offer offer) {
        HtAmplify.getInstance(context).getReservation(reservationId, (success, result, exception) -> {
            if (success) {
                if (MeetingType.ONLINE_MEETING.equals(result.getMeetingType())) {
                    stabilizeReservationStatus(context, result, offer);
                }
            }
            else {
                Log.e(TAG, "Failed to get reservation.", exception);
            }
        });
    }

    private static final Set<String> sOnGoingReservationStabilizations = new HashSet<>();

    public static void stabilizeReservationStatus(Context context, Reservation reservation, Offer offer) {
        if (sOnGoingReservationStabilizations.contains(reservation.getId())) {
            return;
        }

        sOnGoingReservationStabilizations.add(reservation.getId());

        boolean isOnlineMeeting = MeetingType.ONLINE_MEETING.equals(reservation.getMeetingType());

        if (isOnlineMeeting && HtTimeSlotStatus.MARKED_AS_STARTED.name().equals(reservation.getStatus())) {
            if (offer != null) {
                confirmStarted(context, reservation, offer);
            }
            else {
                HtAmplify.getInstance(context).getOffer(reservation.getOfferId(), (success, data, exception) -> {
                    if (success) {
                        confirmStarted(context, reservation, data);
                    }
                    else {
                        sOnGoingReservationStabilizations.remove(reservation.getId());

                        Log.e(TAG, "Failed to get offer to stabilize reservation.", exception);
                    }
                });
            }
        }
        else if (isOnlineMeeting && HtTimeSlotStatus.MARKED_AS_FINISHED.name().equals(reservation.getStatus())) {
            if (offer != null) {
                confirmFinished(context, reservation, offer);
            }
            else {
                HtAmplify.getInstance(context).getOffer(reservation.getOfferId(), (success, data, exception) -> {
                    if (success) {
                        confirmFinished(context, reservation, data);
                    }
                    else {
                        sOnGoingReservationStabilizations.remove(reservation.getId());

                        Log.e(TAG, "Failed to get offer to stabilize reservation.", exception);
                    }
                });
            }
        }
        else {
            sOnGoingReservationStabilizations.remove(reservation.getId());
        }
    }

    private static void confirmStarted(Context context, Reservation reservation, Offer offer) {
        Wallet wallet = Wallet.get(context, TG2HM.getCurrentPhoneNumber());

        wallet.startOffer(offer, reservation, (success, errorCause) -> {
            if (success) {
                HtAmplify.getInstance(context).updateReservation(reservation, HtTimeSlotStatus.STARTED, (success1, result, exception) -> {
                    sOnGoingReservationStabilizations.remove(reservation.getId());
                });
            }
            else {
                sOnGoingReservationStabilizations.remove(reservation.getId());

                Log.e(TAG, "Failed to confirm started offer", errorCause);
            }
        });
    }

    private static void confirmFinished(Context context, Reservation reservation, Offer offer) {
        Wallet wallet = Wallet.get(context, TG2HM.getCurrentPhoneNumber());

        wallet.finishOffer(offer, reservation, (success, errorCause) -> {
            if (success) {
                HtAmplify.getInstance(context).updateReservation(reservation, HtTimeSlotStatus.FINISHED, (success1, result, exception) -> {
                    sOnGoingReservationStabilizations.remove(reservation.getId());
                });
            }
            else {
                sOnGoingReservationStabilizations.remove(reservation.getId());

                Log.e(TAG, "Failed to confirm finished offer", errorCause);
            }
        });
    }

    public static void onlineMeetingClosed(Context context, String timeSlotId, String reservationId, HtAmplify.APICallback<Void> callback) {
        if (timeSlotId != null) {
            HtAmplify.getInstance(context).getTimeSlotReservations(timeSlotId, (success, result, exception) -> {
                if (success) {
                    if (result.isEmpty()) {
                        callback.onCallResult(true, null, null);
                        return;
                    }

                    String userId = String.valueOf(UserConfig.getInstance(UserConfig.selectedAccount).clientUserId);

                    class PendingCallbacks {

                        int count = 0;

                    }

                    PendingCallbacks pendingCallbacks = new PendingCallbacks();

                    for (Reservation reservation: result) {
                        if (!userId.equals(reservation.getServiceProviderId())) {
                            return;
                        }

                        pendingCallbacks.count++;

                        HtAmplify.getInstance(context).updateReservation(reservation, HtTimeSlotStatus.MARKED_AS_FINISHED, (success1, result1, exception1) -> {
                            pendingCallbacks.count--;

                            if (pendingCallbacks.count == 0) {
                                callback.onCallResult(true, null, null);
                            }
                        });
                    }
                }
                else {
                    Log.e(TAG, "Failed to get time slot reservations.", exception);

                    callback.onCallResult(false, null, exception);
                }
            });
        }
    }

}

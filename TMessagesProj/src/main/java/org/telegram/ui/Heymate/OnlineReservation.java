package org.telegram.ui.Heymate;

import android.content.Context;

import com.google.android.exoplayer2.util.Log;

import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.UserConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import works.heymate.api.APICallback;
import works.heymate.api.APIObject;
import works.heymate.api.APIResult;
import works.heymate.api.APIs;
import works.heymate.core.HeymateEvents;
import works.heymate.core.reservation.ReservationUtils;
import works.heymate.core.wallet.Wallet;
import works.heymate.model.Reservation;
import works.heymate.model.TimeSlot;

public class OnlineReservation {

    private static final String TAG = "OnlineReservation";

    public static void stabilizeOnlineMeetingStatuses(Context context) {
        stabilizeMyOffersStatuses(context);
        stabilizeMyOrdersStatuses(context);
    }

    private static void stabilizeMyOffersStatuses(Context context) {
//        HtAmplify.getInstance(context).getMyReservedTimeSlots((success, result, exception) -> {
//            if (success) {
//                ListIterator<TimeSlot> iterator = result.listIterator();
//
//                int now = (int) (System.currentTimeMillis() / 1000);
//
//                Map<String, List<TimeSlot>> offerToTimeSlots = new HashMap<>();
//
//                while (iterator.hasNext()) {
//                    TimeSlot timeSlot = iterator.next();
//
//                    if (timeSlot.getEndTime() == null || now > timeSlot.getEndTime() || !MeetingType.ONLINE_MEETING.equals(timeSlot.getMeetingType())) {
//                        iterator.remove();
//                    }
//                    else {
//                        List<TimeSlot> timeSlots = offerToTimeSlots.get(timeSlot.getId());
//
//                        if (timeSlots == null) {
//                            timeSlots = new ArrayList<>(1);
//                            offerToTimeSlots.put(timeSlot.getId(), timeSlots);
//                        }
//
//                        timeSlots.add(timeSlot);
//                    }
//                }
//
//                if (offerToTimeSlots.isEmpty()) {
//                    return;
//                }
//
//                HtAmplify.getInstance(context).getOffers(offerToTimeSlots.keySet(), (success1, result1, exception1) -> {
//                    if (success1) {
//                        for (Offer offer: result1) {
//                            List<TimeSlot> timeSlots = offerToTimeSlots.get(offer.getId());
//
//                            stabilizeOfferStatuses(context, offer, timeSlots);
//                        }
//                    }
//                    else {
//                        Log.e(TAG, "Failed to get offers of reservations");
//                    }
//                });
//            }
//            else {
//                Log.e(TAG, "Failed to get my reserved timeslots", exception);
//            }
//        });
    }

//    private static void stabilizeOfferStatuses(Context context, Offer offer, List<TimeSlot> timeSlots) {
//        for (TimeSlot timeSlot: timeSlots) {
//            HtAmplify.getInstance(context).getTimeSlotReservations(timeSlot.getId(), (success, result, exception) -> {
//                if (success) {
//                    stabilizeTimeSlotStatuses(context, offer, timeSlot, result, false);
//                }
//                else {
//                    Log.e(TAG, "Failed to get time slot reservations.", exception);
//                }
//            });
//        }
//    }

    // TODO Error handling is missing.
//    public static String ensureUniformStatus(Context context, Offer offer, TimeSlot timeSlot, List<Reservation> reservations, HtTimeSlotStatus status, boolean withInterface) {
//        String meetingId = getMeetingId(reservations);
//
//        ArrayList<Reservation> reservationsCopy = new ArrayList<>(reservations);
//
//        if (withInterface) {
//            LoadingUtil.onLoadingStarted();
//        }
//
//        ensureNextStatus(context, reservationsCopy, offer, status, meetingId, withInterface);
//
//        return meetingId;
//    }

//    private static void ensureNextStatus(Context context, ArrayList<Reservation> reservations, Offer offer, HtTimeSlotStatus status, String meetingId, boolean hideLoading) {
//        if (reservations.isEmpty()) {
//            if (hideLoading) {
//                LoadingUtil.onLoadingFinished();
//            }
//            return;
//        }
//
//        Reservation reservation = reservations.remove(0);
//
//        HtTimeSlotStatus reservationStatus = HtTimeSlotStatus.valueOf(reservation.getStatus());
//
//        HtTimeSlotStatus statusToApply = status;
//
//        boolean onlineMeeting = MeetingType.ONLINE_MEETING.equals(reservation.getMeetingType());
//
//        switch (reservationStatus) {
//            case CANCELLED_BY_CONSUMER:
//                statusToApply = reservationStatus;
//                break;
//            case BOOKED:
//            case MARKED_AS_STARTED:
//                statusToApply = (status == HtTimeSlotStatus.BOOKED || status == HtTimeSlotStatus.CANCELLED_BY_SERVICE_PROVIDER) ? status : HtTimeSlotStatus.MARKED_AS_STARTED;
//                break;
//            case STARTED:
//                switch (status) {
//                    case MARKED_AS_FINISHED:
//                    case FINISHED:
//                        statusToApply = status;
//                        break;
//                    case CANCELLED_BY_SERVICE_PROVIDER:
//                    default:
//                        statusToApply = reservationStatus;
//                        break;
//                }
//                break;
//            case MARKED_AS_FINISHED:
//            case FINISHED:
//            case CANCELLED_BY_SERVICE_PROVIDER:
//                statusToApply = reservationStatus;
//                break;
//        }
//
//        if (statusToApply == reservationStatus) {
//            ensureNextStatus(context, reservations, offer, status, meetingId, hideLoading);
//            return;
//        }
//
//        if (onlineMeeting && reservationStatus == HtTimeSlotStatus.BOOKED && statusToApply == HtTimeSlotStatus.MARKED_AS_STARTED) {
//            String sUserId = reservation.getConsumerId();
//
//            try {
//                int userId = Integer.parseInt(sUserId);
//
//                String message = ReservationUtils.serializeBeautiful(reservation, offer, ReservationUtils.OFFER_ID, ReservationUtils.MEETING_ID, ReservationUtils.MEETING_TYPE, ReservationUtils.START_TIME, ReservationUtils.SERVICE_PROVIDER_ID);
//                SendMessagesHelper.getInstance(UserConfig.selectedAccount).sendMessage(message, userId, null, null, null, false, null, null, null, true, 0, null);
//            } catch (NumberFormatException e) { }
//        }
//
//        if (statusToApply == HtTimeSlotStatus.CANCELLED_BY_SERVICE_PROVIDER) {
//            Wallet wallet = Wallet.get(context, TG2HM.getCurrentPhoneNumber());
//
//            wallet.cancelOffer(offer, reservation, false, (success, errorCause) -> {
//                HtAmplify.getInstance(context).updateReservation(reservation, HtTimeSlotStatus.CANCELLED_BY_SERVICE_PROVIDER, meetingId);
//                ensureNextStatus(context, reservations, offer, status, meetingId, hideLoading);
//            });
//            return;
//        }
//
//        HtAmplify.getInstance(context).updateReservation(reservation, statusToApply, meetingId);
//        ensureNextStatus(context, reservations, offer, status, meetingId, hideLoading);
//    }

//    private static HtTimeSlotStatus concludeTimeSlotStatus(List<Reservation> reservations) {
//        if (reservations == null || reservations.isEmpty()) {
//            return null;
//        }
//
//        if (reservations.size() == 1) {
//            return HtTimeSlotStatus.valueOf(reservations.get(0).getStatus());
//        }
//
//        HtTimeSlotStatus status = HtTimeSlotStatus.BOOKED;
//
//        for (Reservation reservation: reservations) {
//            HtTimeSlotStatus reservationStatus = HtTimeSlotStatus.valueOf(reservation.getStatus());
//
//            if (reservationStatus.happensAfter(status)) {
//                status = reservationStatus;
//            }
//        }
//
//        return status;
//    }

//    public static String getMeetingId(List<Reservation> reservations) {
//        if (reservations == null) {
//            return null;
//        }
//
//        for (Reservation reservation: reservations) {
//            if (reservation.getMeetingId() != null) {
//                return reservation.getMeetingId();
//            }
//        }
//
//        return UUID.randomUUID().toString();
//    }

    public static void stabilizeMyOrdersStatuses(Context context) {
        APIs.get().getMyOrders(result -> {
            if (result.response != null) {
                List<APIObject> reservations = result.response.getArray("data").asObjectList();

                for (APIObject reservation: reservations) {
                    stabilizeReservationStatus(context, reservation, null);
                }
            }
            else {
                Log.e(TAG, "Failed to get my pending online reservations.", result.error);
            }
        });
    }

    public static void stabilizeReservationStatus(Context context, String reservationId, APIObject offer) {
        APIs.get().getReservation(reservationId, result -> {
            if (result.response != null) {
                stabilizeReservationStatus(context, result.response, offer);
            }
            else {
                Log.e(TAG, "Failed to get reservation.", result.error);
            }
        });
    }

    private static final Set<String> sOnGoingReservationStabilizations = new HashSet<>();

    public static void stabilizeReservationStatus(Context context, APIObject reservation, APIObject offer) {
        String reservationId = reservation.getString(Reservation.ID);

        if (sOnGoingReservationStabilizations.contains(reservationId)) {
            return;
        }

        sOnGoingReservationStabilizations.add(reservationId);

        if (reservation.getObject(Reservation.TIMESLOT) != null) {
            stabilizeReservationStatus(context, reservation, reservation.getObject(Reservation.TIMESLOT), offer);
            return;
        }

        APIs.get().getTimeSlot(reservation.getString(Reservation.TIMESLOT_ID), result -> {
            if (result.response != null) {
                stabilizeReservationStatus(context, reservation, result.response, offer);
            }
            else {
                sOnGoingReservationStabilizations.remove(reservationId);

                Log.e(TAG, "Failed to get timeslot", result.error);
            }
        });
    }

    private static void stabilizeReservationStatus(Context context, APIObject reservation, APIObject timeSlot, APIObject offer) {
        String reservationId = reservation.getString(Reservation.ID);
        String meetingType = timeSlot.getString(TimeSlot.OFFER_TYPE);

        boolean isOnlineMeeting = MeetingType.ONLINE_MEETING.equals(meetingType);

        if (isOnlineMeeting && HtTimeSlotStatus.MARKED_AS_STARTED.name().equals(reservation.getString(Reservation.STATUS))) {
            if (offer != null) {
                confirmStarted(context, reservation, null, offer);
            }
            else {
                APIs.get().getOffer(reservation.getString(Reservation.OFFER_ID), offerResult -> {
                    if (offerResult.response != null) {
                        confirmStarted(context, reservation, null, offerResult.response);
                    }
                    else {
                        sOnGoingReservationStabilizations.remove(reservationId);

                        Log.e(TAG, "Failed to get offer to stabilize reservation.", offerResult.error);
                    }
                });
            }
        }
        else if (isOnlineMeeting && HtTimeSlotStatus.MARKED_AS_FINISHED.name().equals(reservation.getString(Reservation.STATUS))) {
            if (offer != null) {
                confirmFinished(context, reservation, null, offer);
            }
            else {
                APIs.get().getOffer(reservation.getString(Reservation.OFFER_ID), offerResult -> {
                    if (offerResult.response != null) {
                        confirmFinished(context, reservation, null, offerResult.response);
                    }
                    else {
                        sOnGoingReservationStabilizations.remove(reservationId);

                        Log.e(TAG, "Failed to get offer to stabilize reservation.", offerResult.error);
                    }
                });
            }
        }
        else {
            sOnGoingReservationStabilizations.remove(reservationId);
        }
    }

    private static void confirmStarted(Context context, APIObject reservation, APIObject purchasedPlan, APIObject offer) {
        Wallet wallet = Wallet.get(context, TG2HM.getCurrentPhoneNumber());

        wallet.startOffer(offer, purchasedPlan, reservation, TG2HM.getDefaultCurrency(), (success, errorCause) -> {
            if (success) {
                APIs.get().updateReservation(reservation.getString(Reservation.ID), HtTimeSlotStatus.STARTED.name(), result -> {
                    sOnGoingReservationStabilizations.remove(reservation.getString(Reservation.ID));

                    HeymateEvents.notify(HeymateEvents.RESERVATION_STATUS_UPDATED, reservation.getString(Reservation.ID));
                });
            }
            else {
                sOnGoingReservationStabilizations.remove(reservation.getString(Reservation.ID));

                Log.e(TAG, "Failed to confirm started offer", errorCause);
            }
        });
    }

    private static void confirmFinished(Context context, APIObject reservation, APIObject purchasedPlan, APIObject offer) {
        Wallet wallet = Wallet.get(context, TG2HM.getCurrentPhoneNumber());

        wallet.finishOffer(offer, purchasedPlan, reservation, TG2HM.getDefaultCurrency(), (success, errorCause) -> {
            if (success) {
                APIs.get().updateReservation(reservation.getString(Reservation.ID), HtTimeSlotStatus.FINISHED.name(), result -> {
                    sOnGoingReservationStabilizations.remove(reservation.getString(Reservation.ID));

                    HeymateEvents.notify(HeymateEvents.RESERVATION_STATUS_UPDATED, reservation.getString(Reservation.ID));
                });
            }
            else {
                sOnGoingReservationStabilizations.remove(reservation.getString(Reservation.ID));

                Log.e(TAG, "Failed to confirm finished offer", errorCause);
            }
        });
    }

    public static void onlineMeetingClosed(Context context, String timeSlotId, String reservationId, APICallback callback) {
        if (timeSlotId != null) {
            APIs.get().updateTimeSlot(timeSlotId, HtTimeSlotStatus.MARKED_AS_FINISHED.name(), null, null, callback);
        }
        else {
            callback.onAPIResult(new APIResult(false));
        }
    }

}

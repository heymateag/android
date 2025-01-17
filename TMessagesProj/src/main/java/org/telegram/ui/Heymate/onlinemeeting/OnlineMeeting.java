package org.telegram.ui.Heymate.onlinemeeting;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;

import com.google.android.exoplayer2.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Heymate.log.HMLog;
import org.telegram.ui.Heymate.log.LogToGroup;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import us.zoom.sdk.ZoomInstantSDK;
import us.zoom.sdk.ZoomInstantSDKAudioHelper;
import us.zoom.sdk.ZoomInstantSDKAudioOption;
import us.zoom.sdk.ZoomInstantSDKAudioRawData;
import us.zoom.sdk.ZoomInstantSDKChatHelper;
import us.zoom.sdk.ZoomInstantSDKChatMessage;
import us.zoom.sdk.ZoomInstantSDKDelegate;
import us.zoom.sdk.ZoomInstantSDKErrors;
import us.zoom.sdk.ZoomInstantSDKInitParams;
import us.zoom.sdk.ZoomInstantSDKLiveStreamHelper;
import us.zoom.sdk.ZoomInstantSDKLiveStreamStatus;
import us.zoom.sdk.ZoomInstantSDKPasswordHandler;
import us.zoom.sdk.ZoomInstantSDKSession;
import us.zoom.sdk.ZoomInstantSDKSessionContext;
import us.zoom.sdk.ZoomInstantSDKShareHelper;
import us.zoom.sdk.ZoomInstantSDKShareStatus;
import us.zoom.sdk.ZoomInstantSDKUser;
import us.zoom.sdk.ZoomInstantSDKUserHelper;
import us.zoom.sdk.ZoomInstantSDKVideoHelper;
import us.zoom.sdk.ZoomInstantSDKVideoOption;
import works.heymate.api.APIs;
import works.heymate.core.HeymateEvents;
import works.heymate.core.Utils;
import works.heymate.model.Reservation;
import works.heymate.model.TimeSlot;

public class OnlineMeeting {

    private static final String TAG = "OnlineMeeting";

    private static OnlineMeeting mInstance = null;

    private interface DoWithUserCall {

        void doWithUser(ZoomInstantSDKSession session, ZoomInstantSDKUser user);

    }

    public static OnlineMeeting get() {
        if (mInstance == null) {
            mInstance = new OnlineMeeting(ApplicationLoader.applicationContext);
        }

        return mInstance;
    }

    private Context mContext;
    private ZoomInstantSDK mSDK;

    private String mSessionId = null;
    private String mTimeSlotId = null;
    private String mReservationId = null;

    private String mHostId = null;

    private MeetingMember mSelf;
    private Map<String, MeetingMember> mMembers = new HashMap<>();
    private Map<String, MeetingMember> mMembersByZoomIds = new HashMap<>();

    private OnlineMeeting(Context context) {
        LogToGroup.logIfCrashed(() -> {
            mContext = context.getApplicationContext();

            ZoomInstantSDKInitParams params = new ZoomInstantSDKInitParams();
            params.domain = "https://zoom.us"; // Required
            params.enableLog = true; // Optional for debugging

            mSDK = ZoomInstantSDK.getInstance();

            int initResult = mSDK.initialize(context, params);

            if (initResult == ZoomInstantSDKErrors.Errors_Success) {
                // You have successfully initialized the SDK
                mSDK.addListener(mZoomDelegate);
            } else {
                // Something went wrong, see error code documentation
                Log.e(TAG, "Failed to initialize Zoom SDK with error code: " + initResult);
            }
        });
    }

    /**
     *
     * @param sessionId
     * @return true if already is in the session.
     */
    public boolean ensureSession(String sessionId, String sessionPassword, String timeSlotId, String reservationId) {
        HMLog.d(TAG, "ensure session="+sessionId+" timeSlot="+timeSlotId+" reservation="+reservationId);

        if (sessionId.equals(mSessionId)) {
            return true;
        }

        if (mSessionId != null) {
            mSDK.leaveSession(true); // shouldEndSession = true
        }

        mSessionId = sessionId;
        mTimeSlotId = timeSlotId;
        mReservationId = reservationId;

        if (timeSlotId != null) {
            APIs.get().getTimeSlot(timeSlotId, result -> {
                if (!timeSlotId.equals(mTimeSlotId)) {
                    return;
                }

                if (result.success) {
//                    mHostId = result.response.getString(TimeSlot.) TODO Host id from time slot
//                    ensureHost();
                }
            });
        }
        else if (reservationId != null) {
            APIs.get().getReservation(reservationId, result -> {
                if (!reservationId.equals(mReservationId)) {
                    return;
                }

                if (result.success) {
                    mHostId = result.response.getString(Reservation.SERVICE_PROVIDER_ID);
                    ensureHost();
                }
            });
        }

        HMLog.d(TAG, "Notified joining meeting");
        HeymateEvents.notify(HeymateEvents.JOINING_MEETING, sessionId);

        UserInfo userInfo = new UserInfo();

        APIs.get().getZoomToken(sessionId, sessionPassword, result -> {
            LogToGroup.logIfCrashed(() -> {
                if (!sessionId.equals(mSessionId)) {
                    return;
                }

                HMLog.d(TAG, "Received Zoom token: " + result);

                if (result.success) {
                    // Setup audio options
                    ZoomInstantSDKAudioOption audioOptions = new ZoomInstantSDKAudioOption();
                    audioOptions.connect = true; // Auto connect to audio upon joining
                    audioOptions.mute = true; // Auto mute audio upon joining
                    // Setup video options
                    ZoomInstantSDKVideoOption videoOptions = new ZoomInstantSDKVideoOption();
                    videoOptions.localVideoOn = true; // Turn on local/self video upon joining
                    // Pass options into session
                    ZoomInstantSDKSessionContext params = new ZoomInstantSDKSessionContext();
                    params.audioOption = audioOptions;
                    params.videoOption = videoOptions;
                    params.sessionName = sessionId;
                    params.sessionPassword = sessionPassword;
                    params.userName = userInfo.toString();
                    params.token = result.response.getString("data");

                    HMLog.d(TAG, "Called join session with user name: " + params.userName);
                    ZoomInstantSDKSession session = mSDK.joinSession(params);

                    if (session == null) {
                        HMLog.d(TAG, "Notified failed to join meeting");
                        HeymateEvents.notify(HeymateEvents.FAILED_TO_JOIN_MEETING, sessionId);
                    }
                }
                else {
                    Log.e(TAG, "Failed to get token for video session.");

                    HMLog.d(TAG, "Notified failed to join meeting");
                    HeymateEvents.notify(HeymateEvents.FAILED_TO_JOIN_MEETING, sessionId);
                }
            });
        });

        return false;
    }

    public void leaveMeeting() {
        HMLog.d(TAG, "Leave meeting just been called");
        Utils.postOnUIThread(() -> {
            HMLog.d(TAG, "Actually calling leave meeting. Has session: " + (mSDK.getSession() != null));
            mSDK.leaveSession(true); // shouldEndSession = true
        });
    }

    public MeetingMember getSelf() {
        return mSelf;
    }

    public MeetingMember getMember(String userId) {
        return mMembers.get(userId);
    }

    public Collection<MeetingMember> getMembers() {
        return mMembers.values();
    }

    public void switchCamera() {
        try {
            mSDK.getVideoHelper().switchCamera();
        } catch (Throwable t) { }
    }

    public void mute(String userId) {
        doWithUser(userId, (session, user) -> {
            HMLog.d(TAG, "About to call mute on " + userId + " " + user);
            ZoomInstantSDKAudioHelper audioHelper = mSDK.getAudioHelper();
            audioHelper.muteAudio(user);
        });
    }

    public void unMute(String userId) {
        doWithUser(userId, (session, user) -> {
            HMLog.d(TAG, "About to call unmute on " + userId + " " + user);
            ZoomInstantSDKAudioHelper audioHelper = mSDK.getAudioHelper();
            audioHelper.unMuteAudio(user);
        });
    }

    public boolean muteAll() {
        HMLog.d(TAG, "muteAll called");

        ZoomInstantSDKSession session = mSDK.getSession();

        if (session == null) {
            HMLog.d(TAG, "muteAll session is null");
            return false;
        }

        if (mSelf == null || !session.getUser(mSelf.getZoomUser().getUserId()).isHost()) {
            HMLog.d(TAG, "muteAll user is not host. user=" + mSelf);
            return false;
        }

        for (ZoomInstantSDKUser user: session.getAllUsers()) {
            if (!user.isHost()) {
                mSDK.getAudioHelper().muteAudio(user);
            }
        }
        HMLog.d(TAG, "muteAll success for " + session.getAllUsers().size() + " members in the meeting.");

        return true;
    }

    public boolean unMuteAll() {
        ZoomInstantSDKSession session = mSDK.getSession();

        if (session == null) {
            return false;
        }

        if (mSelf == null || !session.getUser(mSelf.getUserId()).isHost()) {
            return false;
        }

        for (ZoomInstantSDKUser user: session.getAllUsers()) {
            if (!user.isHost()) {
                mSDK.getAudioHelper().unMuteAudio(user);
            }
        }

        return true;
    }

    public void kick(String userId) {
        if (mSelf != null && mSelf.getUserId().equals(userId)) {
            return;
        }

        doWithUser(userId, (session, user) -> mSDK.getUserHelper().removeUser(user));
    }

    public void startVideo() {
        LogToGroup.logIfCrashed(() -> {
            if (mSDK.isInSession()) {
                mSDK.getVideoHelper().startVideo();
            }
        });
    }

    public void stopVideo() {
        LogToGroup.logIfCrashed(() -> {
            if (mSDK.isInSession()) {
                mSDK.getVideoHelper().stopVideo();
            }
        });
    }

    private void doWithUser(String userId, DoWithUserCall call) {
        ZoomInstantSDKSession session = mSDK.getSession();

        if (session == null) {
            return;
        }

        MeetingMember meetingMember = mMembers.get(userId);

        if (meetingMember == null) {
            return;
        }

        LogToGroup.logIfCrashed(() -> {
            call.doWithUser(session, meetingMember.getZoomUser());
        });
    }

    private void ensureHost() {
        if (mHostId == null) {
            HMLog.d(TAG, "Dropping ensure host. No host id.");
            return;
        }

        LogToGroup.logIfCrashed(() -> {
            HMLog.d(TAG, "About to ensure host");
            MeetingMember member = mMembers.get(mHostId);

            if (member != null && mSelf != null && mSelf != member && mSelf.getZoomUser().isHost()) {
                HMLog.d(TAG, "Changing the host");
                mSDK.getUserHelper().makeHost(member.getZoomUser());
            }
        });
    }

    private void leftSession() {
        for (MeetingMember meetingMember: mMembers.values()) {
            meetingMember.release();
        }
        mMembers.clear();
        mMembersByZoomIds.clear();
        mSelf = null;

        mHostId = null;

        mReservationId = null;
        mTimeSlotId = null;
        mSessionId = null;
    }

    private final ZoomInstantSDKDelegate mZoomDelegate = new ZoomInstantSDKDelegate() {

        @Override
        public void onSessionJoin() {
            LogToGroup.logIfCrashed(() -> {
                mSelf = new MeetingMember(mSDK.getSession().getMySelf(), mHostId);
                HMLog.d(TAG, "onSessionJoin: " + mSelf.getUserId());
                mMembers.put(mSelf.getUserId(), mSelf);
                mMembersByZoomIds.put(mSelf.getZoomUser().getUserId(), mSelf);

                HMLog.d(TAG, "Notified joined meeting");
                HeymateEvents.notify(HeymateEvents.JOINED_MEETING, mSelf.getUserId(), mSelf);
            });
        }

        @Override
        public void onSessionLeave() {
            LogToGroup.logIfCrashed(() -> {
                if (mSelf == null) { // TODO Loose handling. Need to figure out how mSelf can be null here.
                    HMLog.d(TAG, "onSessionLeave. SELF IS SOMEHOW NULL!");
                    return;
                }

                HMLog.d(TAG, "onSessionLeave: " + mSelf.getUserId());

                HeymateEvents.notify(HeymateEvents.LEFT_MEETING);

                Utils.postOnUIThread(() -> {
                    HMLog.d(TAG, "Clearing self");
                    leftSession();
                });
            });
        }

        @Override
        public void onSessionNeedPassword(ZoomInstantSDKPasswordHandler handler) {
            Log.e(TAG, "Session requires password!");
            HeymateEvents.notify(HeymateEvents.FAILED_TO_JOIN_MEETING, new Exception("Session requires passwod."));
        }

        @Override
        public void onError(int errorCode) {
            // See error code documentation https://marketplace.zoom.us/docs/sdk/video/android/errors
            Log.e(TAG, "onError: " + errorCode);
            HMLog.d(TAG, "onError: " + errorCode);

            LogToGroup.logIfCrashed(() -> {
                switch (errorCode) {
                    case ZoomInstantSDKErrors.Errors_Auth_Disable_SDK:
                    case ZoomInstantSDKErrors.Errors_Auth_DoesNot_Support_SDK:
                    case ZoomInstantSDKErrors.Errors_Auth_Empty_Key_or_Secret:
                    case ZoomInstantSDKErrors.Errors_Auth_Error:
                    case ZoomInstantSDKErrors.Errors_Auth_Wrong_Key_or_Secret:
                    case ZoomInstantSDKErrors.Errors_Session_Join_Failed:
                        HMLog.d(TAG, "Notified failed to join meeting");
                        HeymateEvents.notify(HeymateEvents.FAILED_TO_JOIN_MEETING, new Exception("Failed to join meeting with error code " + errorCode));
                        leftSession();
                        return;
                    case ZoomInstantSDKErrors.Errors_Session_Disconnect:
                        HMLog.d(TAG, "Notified left meeting");
                        HeymateEvents.notify(HeymateEvents.LEFT_MEETING);

                        Utils.postOnUIThread(() -> {
                            HMLog.d(TAG, "Releasing members and clearing self");
                            leftSession();
                        });
                        return;
                }
            });
        }

        @Override
        public void onUserJoin(ZoomInstantSDKUserHelper userHelper, List<ZoomInstantSDKUser> userList) {
            LogToGroup.logIfCrashed(() -> {
                for (ZoomInstantSDKUser user: userList) {
                    MeetingMember meetingMember = new MeetingMember(user, mHostId);

                    HMLog.d(TAG, "onUserJoin: " + meetingMember.getUserId());

                    mMembers.put(meetingMember.getUserId(), meetingMember);
                    mMembersByZoomIds.put(user.getUserId(), meetingMember);

                    ensureHost();

                    HMLog.d(TAG, "Notified user joined meeting. user id: " + meetingMember.getUserId());
                    HeymateEvents.notify(HeymateEvents.USER_JOINED_MEETING, meetingMember.getUserId(), meetingMember);
                }
            });
        }

        @Override
        public void onUserLeave(ZoomInstantSDKUserHelper userHelper, List<ZoomInstantSDKUser> userList) {
            LogToGroup.logIfCrashed(() -> {
                for (ZoomInstantSDKUser user: userList) {
                    MeetingMember meetingMember = mMembersByZoomIds.get(user.getUserId());

                    if (meetingMember == null || mSelf == null || TextUtils.equals(mSelf.getUserId(), meetingMember.getUserId())) {
                        continue;
                    }

                    HMLog.d(TAG, "onUserLeave: " + meetingMember.getUserId());

                    HeymateEvents.notify(HeymateEvents.USER_LEFT_MEETING, meetingMember.getUserId(), meetingMember);

                    Utils.postOnUIThread(() -> {
                        HMLog.d(TAG, "Clearing user id: " + user.getUserId());

                        mMembersByZoomIds.remove(user.getUserId());

                        mMembers.remove(meetingMember.getUserId());

                        meetingMember.release();
                    });
                }
            });
        }

        @Override
        public void onUserVideoStatusChanged(ZoomInstantSDKVideoHelper videoHelper, List<ZoomInstantSDKUser> userList) {
            LogToGroup.logIfCrashed(() -> {
                for (ZoomInstantSDKUser user: userList) {
                    MeetingMember meetingMember = mMembersByZoomIds.get(user.getUserId());

                    if (meetingMember != null) {
                        HMLog.d(TAG, "User(" + meetingMember.getUserId() + ") video status changed. Is ON: " + user.getVideoStatus().isOn());

                        HeymateEvents.notify(HeymateEvents.MEETING_USER_STATUS_CHANGED, meetingMember.getUserId(), meetingMember);
                    }
                }
            });
        }

        @Override
        public void onUserAudioStatusChanged(ZoomInstantSDKAudioHelper audioHelper, List<ZoomInstantSDKUser> userList) {
            LogToGroup.logIfCrashed(() -> {
                for (ZoomInstantSDKUser user: userList) {
                    MeetingMember meetingMember = mMembersByZoomIds.get(user.getUserId());

                    if (meetingMember != null) {
                        HMLog.d(TAG, "User(" + meetingMember.getUserId() + ") audio status changed. Muted: " + user.getAudioStatus().isMuted());

                        HeymateEvents.notify(HeymateEvents.MEETING_USER_STATUS_CHANGED, meetingMember.getUserId(), meetingMember);
                    }
                }
            });
        }

        @Override
        public void onUserHostChanged(ZoomInstantSDKUserHelper userHelper, ZoomInstantSDKUser userInfo) {
            HMLog.d(TAG, "onUserHostChanged");
        }

        @Override
        public void onUserActiveAudioChanged(ZoomInstantSDKAudioHelper audioHelper, List<ZoomInstantSDKUser> list) {

        }

        @Override public void onChatNewMessageNotify(ZoomInstantSDKChatHelper chatHelper, ZoomInstantSDKChatMessage messageItem) { }

        @Override public void onUserShareStatusChanged(ZoomInstantSDKShareHelper shareHelper, ZoomInstantSDKUser userInfo, ZoomInstantSDKShareStatus status) { }
        @Override public void onLiveStreamStatusChanged(ZoomInstantSDKLiveStreamHelper liveStreamHelper, ZoomInstantSDKLiveStreamStatus status) { }
        @Override public void onUserManagerChanged(ZoomInstantSDKUser zoomInstantSDKUser) { }
        @Override public void onUserNameChanged(ZoomInstantSDKUser zoomInstantSDKUser) { }
        @Override public void onMixedAudioRawDataReceived(ZoomInstantSDKAudioRawData zoomInstantSDKAudioRawData) { }
        @Override public void onOneWayAudioRawDataReceived(ZoomInstantSDKAudioRawData zoomInstantSDKAudioRawData, ZoomInstantSDKUser zoomInstantSDKUser) { }
        @Override public void onSessionPasswordWrong(ZoomInstantSDKPasswordHandler handler) { }

    };

    static class UserInfo {

        public final String name;
        public final String id;

        public UserInfo() {
            TLRPC.User user = UserConfig.getInstance(UserConfig.selectedAccount).getCurrentUser();

            name = UserObject.getUserName(user);
            id = String.valueOf(user.id);
        }

        public UserInfo(String userInfo) {
            String tName;
            String tId;
            try {
                try {
                    userInfo = new String(Base64.decode(userInfo, Base64.URL_SAFE));
                } catch (Throwable t) { }

                JSONObject jUserInfo = new JSONObject(userInfo);

                tName = jUserInfo.getString("f");
                tId = jUserInfo.getString("i");
            } catch (JSONException e) {
                tName = "[ERROR]";
                tId = "0";

                LogToGroup.log("Failed to read user info", e);
            }

            name = tName;
            id = tId;
        }

        @Override
        public String toString() {
            JSONObject jUserInfo = new JSONObject();
            try {
                jUserInfo.put("f", name);
                jUserInfo.put("i", id);
            } catch (JSONException e) { }
            return Base64.encodeToString(jUserInfo.toString().getBytes(), Base64.NO_WRAP | Base64.URL_SAFE);
        }

    }

}

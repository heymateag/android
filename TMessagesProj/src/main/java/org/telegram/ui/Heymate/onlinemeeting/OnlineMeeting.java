package org.telegram.ui.Heymate.onlinemeeting;

import android.content.Context;

import com.google.android.exoplayer2.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Heymate.HtAmplify;

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
import works.heymate.core.HeymateEvents;
import works.heymate.core.Utils;

public class OnlineMeeting {

    private static final String TAG = "OnlineMeeting";

    private static OnlineMeeting mInstance = null;

    public static OnlineMeeting get() {
        if (mInstance == null) {
            mInstance = new OnlineMeeting(ApplicationLoader.applicationContext);
        }

        return mInstance;
    }

    private Context mContext;
    private ZoomInstantSDK mSDK;

    private MeetingMember mSelf;
    private Map<String, MeetingMember> mMeetingMembers = new HashMap<>();

    private OnlineMeeting(Context context) {
        mContext = context;

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
    }

    /**
     *
     * @param sessionId
     * @return true if already is in the session.
     */
    public boolean ensureSession(String sessionId) {
        if (mSDK.getSession() != null) {
            if (mSDK.getSession().getSessionName().equals(sessionId)) {
                return true;
            }
            else {
                mSDK.leaveSession(true); // shouldEndSession = true
            }
        }

        HeymateEvents.notify(HeymateEvents.JOINING_MEETING, sessionId);

        HtAmplify.getInstance(mContext).getZoomToken(System.currentTimeMillis() / 1000, (success, result, exception) -> {
            if (success) {
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
                params.userName = new UserInfo().toString();
                params.token = result;

                ZoomInstantSDKSession session = mSDK.joinSession(params);

//                if (session == null) {
//                    HeymateEvents.notify(HeymateEvents.FAILED_TO_JOIN_MEETING, sessionId);
//                }
            }
        });

        return false;
    }

    public void leaveMeeting() {
        Utils.postOnUIThread(() -> {
            if (mSDK.getSession() != null) {
                mSDK.leaveSession(true); // shouldEndSession = true
            }
        });
    }

    public MeetingMember getSelf() {
        return mSelf;
    }

    public MeetingMember getMeetingMember(String userId) {
        return mMeetingMembers.get(userId);
    }

    private final ZoomInstantSDKDelegate mZoomDelegate = new ZoomInstantSDKDelegate() {

        @Override
        public void onSessionJoin() {
            mSelf = new MeetingMember(mSDK.getSession().getMySelf());
            HeymateEvents.notify(HeymateEvents.JOINED_MEETING);
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

            switch (errorCode) {
                case ZoomInstantSDKErrors.Errors_Auth_Disable_SDK:
                case ZoomInstantSDKErrors.Errors_Auth_DoesNot_Support_SDK:
                case ZoomInstantSDKErrors.Errors_Auth_Empty_Key_or_Secret:
                case ZoomInstantSDKErrors.Errors_Auth_Error:
                case ZoomInstantSDKErrors.Errors_Auth_Wrong_Key_or_Secret:
                case ZoomInstantSDKErrors.Errors_Session_Join_Failed:
                    HeymateEvents.notify(HeymateEvents.FAILED_TO_JOIN_MEETING, new Exception("Failed to join meeting with error code " + errorCode));
                    return;
                case ZoomInstantSDKErrors.Errors_Session_Disconnect:
                    HeymateEvents.notify(HeymateEvents.LEFT_MEETING);

                    Utils.postOnUIThread(() -> {
                        for (MeetingMember meetingMember: mMeetingMembers.values()) {
                            meetingMember.release();
                        }
                        mMeetingMembers.clear();
                    });
                    return;
            }
        }

        @Override
        public void onUserJoin(ZoomInstantSDKUserHelper userHelper, List<ZoomInstantSDKUser> userList) {
            for (ZoomInstantSDKUser user: userList) {
                MeetingMember meetingMember = new MeetingMember(user);

                mMeetingMembers.put(user.getUserId(), meetingMember);

                HeymateEvents.notify(HeymateEvents.USER_JOINED_MEETING, user.getUserId());
            }
        }

        @Override
        public void onUserLeave(ZoomInstantSDKUserHelper userHelper, List<ZoomInstantSDKUser> userList) {
            for (ZoomInstantSDKUser user: userList) {
                HeymateEvents.notify(HeymateEvents.USER_LEFT_MEETING, user.getUserId());

                Utils.postOnUIThread(() -> {
                    MeetingMember meetingMember = mMeetingMembers.get(user.getUserId());

                    if (meetingMember != null) {
                        meetingMember.release();
                    }
                });
            }
        }

        @Override
        public void onUserVideoStatusChanged(ZoomInstantSDKVideoHelper videoHelper, List<ZoomInstantSDKUser> userList) {

        }

        @Override
        public void onUserAudioStatusChanged(ZoomInstantSDKAudioHelper audioHelper, List<ZoomInstantSDKUser> userHelper) {

        }

        @Override
        public void onUserHostChanged(ZoomInstantSDKUserHelper userHelper, ZoomInstantSDKUser userInfo) {

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
        @Override public void onSessionLeave() { }

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
                JSONObject jUserInfo = new JSONObject(userInfo);

                tName = jUserInfo.getString("n");
                tId = jUserInfo.getString("i");
            } catch (JSONException e) {
                tName = "[ERROR]";
                tId = "0";
            }

            name = tName;
            id = tId;
        }

        @Override
        public String toString() {
            JSONObject jUserInfo = new JSONObject();
            try {
                jUserInfo.put("n", name);
                jUserInfo.put("i", id);
            } catch (JSONException e) { }
            return jUserInfo.toString();
        }

    }

}

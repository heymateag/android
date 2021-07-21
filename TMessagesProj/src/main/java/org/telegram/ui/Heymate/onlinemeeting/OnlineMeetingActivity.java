package org.telegram.ui.Heymate.onlinemeeting;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.yashoid.sequencelayout.SequenceLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.ui.ActionBar.ActionBarLayout;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Heymate.HtAmplify;
import org.telegram.ui.Heymate.LoadingUtil;
import org.telegram.ui.Heymate.log.HMLog;
import org.telegram.ui.Heymate.log.LogToGroup;
import org.telegram.ui.Heymate.OnlineReservation;
import org.telegram.ui.Heymate.widget.AutoGridLayout;
import org.telegram.ui.Heymate.widget.DraggableOverlay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import works.heymate.beta.R;
import works.heymate.core.HeymateEvents;

public class OnlineMeetingActivity extends BaseFragment implements HeymateEvents.HeymateEventObserver {

    private static final String TAG = "OnlineMeetingActivity";

    private static final String KEY_MEETING_ID = "meetingId";
    private static final String KEY_TIME_SLOT_ID = "timeSlotId";
    private static final String KEY_RESERVATION_ID = "reservationId";

    private static Bundle createArgs(String meetingId, String timeSlotId, String reservationId) {
        Bundle args = new Bundle();
        args.putString(KEY_MEETING_ID, meetingId);
        args.putString(KEY_TIME_SLOT_ID, timeSlotId);
        args.putString(KEY_RESERVATION_ID, reservationId);
        return args;
    }

    private String mMeetingId;
    private String mTimeSlotId;
    private String mReservationId;

    private AutoGridLayout mGrid;
    private DraggableOverlay mOverlay;
    private TextView mLeave;
    private ImageView mMute;
    private ImageView mImageMic;
    private TextView mTextMic;
    private View mMic;
    private ImageView mImageVideo;
    private TextView mTextVideo;
    private View mVideo;
    private ImageView mImageMembers;
    private TextView mTextMembers;
    private View mMembers;

    private final Map<String, View> mMemberViews = new HashMap<>();

    private boolean mStarted = false;

    public OnlineMeetingActivity(String meetingId, String timeSlotId, String reservationId) {
        super(createArgs(meetingId, timeSlotId, reservationId));
    }

    @Override
    public boolean onFragmentCreate() {
        mMeetingId = getArguments().getString(KEY_MEETING_ID);
        mTimeSlotId = getArguments().getString(KEY_TIME_SLOT_ID);
        mReservationId = getArguments().getString(KEY_RESERVATION_ID);

        return super.onFragmentCreate();
    }

    @Override
    public View createView(Context context) {
        HMLog.d(TAG, "createView");
        HeymateEvents.register(HeymateEvents.JOINING_MEETING, this);
        HeymateEvents.register(HeymateEvents.JOINED_MEETING, this);
        HeymateEvents.register(HeymateEvents.FAILED_TO_JOIN_MEETING, this);
        HeymateEvents.register(HeymateEvents.USER_JOINED_MEETING, this);
        HeymateEvents.register(HeymateEvents.USER_LEFT_MEETING, this);
        HeymateEvents.register(HeymateEvents.MEETING_USER_STATUS_CHANGED, this);

        SequenceLayout content = (SequenceLayout) LayoutInflater.from(context).inflate(R.layout.activity_onlinemeeting, null, false);

        content.findSequenceById("grid").getSpans().get(0).size = Build.VERSION.SDK_INT >= 21 ? AndroidUtilities.statusBarHeight : 0;
        content.setBackgroundColor(0xff292929);

        mGrid = content.findViewById(R.id.grid);
        mOverlay = content.findViewById(R.id.overlay);

        mLeave = content.findViewById(R.id.leave);
        mLeave.setText("Leave");  // TODO Texts
        mLeave.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(8), 0xffFE0000));
        mLeave.setTextColor(0xffffffff);
        mLeave.setOnClickListener(v -> confirmCloseMeeting());

        mMute = content.findViewById(R.id.mute);
        mMute.setImageResource(R.drawable.ic_not_muted);

        mImageMic = content.findViewById(R.id.image_mic);
        mTextMic = content.findViewById(R.id.text_mic);
        mTextMic.setTextColor(0xFFFFFFFF);
        mMic = content.findViewById(R.id.mic);

        mImageVideo = content.findViewById(R.id.image_video);
        mTextVideo = content.findViewById(R.id.text_video);
        mTextVideo.setTextColor(0xFFFFFFFF);
        mVideo = content.findViewById(R.id.video);

        mImageMembers = content.findViewById(R.id.image_members);
        mImageMembers.setImageResource(R.drawable.ic_zoom_participants);
        mTextMembers = content.findViewById(R.id.text_members);
        mTextMembers.setTextColor(0xFFFFFFFF);
        mTextMembers.setText("Members");
        mMembers = content.findViewById(R.id.members);

        mMic.setOnClickListener(v -> {
            MeetingMember self = OnlineMeeting.get().getSelf();

            if (self == null) {
                return;
            }

            if (self.isMuted()) {
                OnlineMeeting.get().unMute(self.getUserId());
            }
            else {
                OnlineMeeting.get().mute(self.getUserId());
            }
        });

        mVideo.setOnClickListener(v -> {
            MeetingMember self = OnlineMeeting.get().getSelf();

            if (self == null) {
                return;
            }

            if (self.isVideoOn()) {
                OnlineMeeting.get().stopVideo();
            }
            else {
                OnlineMeeting.get().startVideo();
            }
        });

        mMembers.setOnClickListener(v -> presentFragment(new MeetingMembersActivity()));

        updateState();

        fragmentView = content;

        return content;
    }

    private void updateState() {
        MeetingMember self = OnlineMeeting.get().getSelf();

        if (self == null || !self.isMuted()) {
            mImageMic.setImageResource(R.drawable.ic_mic_on);
            mTextMic.setText("Mute");
        }
        else {
            mImageMic.setImageResource(R.drawable.ic_mic_off);
            mTextMic.setText("Unmute");
        }

        if (self == null || self.isVideoOn()) {
            mImageVideo.setImageResource(R.drawable.ic_video_on);
            mTextVideo.setText("Disable video");
        }
        else {
            mImageVideo.setImageResource(R.drawable.ic_video_off);
            mTextVideo.setText("Enable video");
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        LogToGroup.logIfCrashed(() -> {
            checkPermissionsAndStart();
        });
    }

    private void checkPermissionsAndStart() {
        String[] missingPermissions = getMissingPermissions();

        if (missingPermissions.length > 0) {
            ActivityCompat.requestPermissions(getParentActivity(), missingPermissions, 780);
        }
        else {
            checkSessionAndStart();
        }
    }

    @Override
    public void onRequestPermissionsResultFragment(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResultFragment(requestCode, permissions, grantResults);

        if (getMissingPermissions().length > 0) {
            finishFragment();
        }
        else {
            checkSessionAndStart();
        }
    }

    private void checkSessionAndStart() {
        HMLog.d(TAG, "checkSessionAndStart. Started=" + mStarted);
        if (mStarted && OnlineMeeting.get().getSelf() != null) { // TODO Weak handling. Session is ending somewhere we're not aware of! (probably in meeting members screen)
            HMLog.d(TAG, "Self is not null. Ensuring member views.");
            ensureMemberViews();
            return;
        }

        mStarted = true;

        HMLog.d(TAG, "About to ensure session");
        if (OnlineMeeting.get().ensureSession(mMeetingId, mTimeSlotId, mReservationId)) {
            MeetingMember self = OnlineMeeting.get().getSelf();

            HMLog.d(TAG, "Already in session. Self exists: " + (self != null));
            if (self != null) {
                onHeymateEvent(HeymateEvents.JOINED_MEETING, self.getUserId(), self);
            }
        }
    }

    private void ensureMemberViews() {
        HMLog.d(TAG, "ensureMemberViews");
        List<MeetingMember> members = new ArrayList<>(OnlineMeeting.get().getMembers());

        for (int i = 0; i < mOverlay.getChildCount(); i++) {
            if (mOverlay.getChildAt(i).getTag() instanceof MeetingMember) {
                members.remove((MeetingMember) mOverlay.getChildAt(i).getTag());
            }
        }

        for (int i = 0; i < mGrid.getChildCount(); i++) {
            if (mGrid.getChildAt(i).getTag() instanceof MeetingMember) {
                members.remove((MeetingMember) mGrid.getChildAt(i).getTag());
            }
        }

        String userId = String.valueOf(getUserConfig().clientUserId);

        for (MeetingMember member: members) {
            if (userId.equals(member.getUserId())) {
                HMLog.d(TAG, "ensureMemberViews: joined meeting");
                onHeymateEvent(HeymateEvents.JOINED_MEETING, member.getUserId(), member, true);
            }
            else {
                HMLog.d(TAG, "ensureMemberViews: user joined meeting");
                onHeymateEvent(HeymateEvents.USER_JOINED_MEETING, member.getUserId(), member);
            }
        }

        ensureSelfOnTop();
    }

    private void ensureSelfOnTop() {
        MeetingMember myself = OnlineMeeting.get().getSelf();

        if (myself == null) {
            return;
        }

        if (mOverlay.getChildCount() > 0) {
            myself.releaseView(mOverlay.getChildAt(0));
            mOverlay.removeAllViews();
        }

        View myselfView = myself.createView(getParentActivity());

        if (myselfView != null) {
            myselfView.setTag(OnlineMeeting.get().getSelf());
            mMemberViews.put(myself.getUserId(), myselfView);

            if (myself.isHost()) {
                mLeave.setText("End");
            }

            DraggableOverlay.LayoutParams params = new DraggableOverlay.LayoutParams(
                    AndroidUtilities.dp(120),
                    AndroidUtilities.dp(180)
            );
            params.gravity = Gravity.RIGHT | Gravity.BOTTOM;
            params.bottomMargin = AndroidUtilities.dp(80);
            params.rightMargin = AndroidUtilities.dp(12);
            params.leftMargin = params.rightMargin;
            mOverlay.addView(myselfView, params);
        }
    }

    @Override
    public void onHeymateEvent(int event, Object... args) {
        LogToGroup.logIfCrashed(() -> {
            if (getParentActivity() == null) {
                HMLog.d(TAG, "RECEIVED EVENT WHILE THERE IS NO PARENT");
                return; // TODO Why?!
            }

            switch (event) {
                case HeymateEvents.JOINING_MEETING:
                    HMLog.d(TAG, "Event: JOINING_MEETING");
                    // Nothing to do.
                    break;
                case HeymateEvents.JOINED_MEETING:
                    HMLog.d(TAG, "Event: JOINED_MEETING");
                    MeetingMember myself = (MeetingMember) args[1];
                    View myselfView = myself.createView(getParentActivity());

                    if (myselfView != null) {
                        myselfView.setTag(OnlineMeeting.get().getSelf());
                        mMemberViews.put(myself.getUserId(), myselfView);

                        if (myself.isHost()) {
                            mLeave.setText("End");
                        }

                        DraggableOverlay.LayoutParams params = new DraggableOverlay.LayoutParams(
                                AndroidUtilities.dp(120),
                                AndroidUtilities.dp(180)
                        );
                        params.gravity = Gravity.RIGHT | Gravity.BOTTOM;
                        params.bottomMargin = AndroidUtilities.dp(80);
                        params.rightMargin = AndroidUtilities.dp(12);
                        params.leftMargin = params.rightMargin;
                        mOverlay.addView(myselfView, params);
                    }

                    if (args.length == 2) {
                        HMLog.d(TAG, "Event: JOINED_MEETING - Calling user joined for " + OnlineMeeting.get().getMembers().size() + " members.");
                        for (MeetingMember existingMember: OnlineMeeting.get().getMembers()) {
                            if (existingMember != myself) {
                                onHeymateEvent(HeymateEvents.USER_JOINED_MEETING, existingMember.getUserId(), existingMember);
                            }
                        }
                    }
                    break;
                case HeymateEvents.FAILED_TO_JOIN_MEETING:
                    HMLog.d(TAG, "Event: FAILED_TO_JOIN_MEETING");
                    mStarted = false;
                    Toast.makeText(getParentActivity(), "Failed to join to the meeting!", Toast.LENGTH_LONG).show(); // TODO Texts
                    finishFragment();
                    break;
                case HeymateEvents.LEFT_MEETING:
                    HMLog.d(TAG, "Event: LEFT_MEETING");
                    OnlineReservation.stabilizeMyOrdersStatuses(getParentActivity());
                    break;
                case HeymateEvents.USER_JOINED_MEETING:
                    HMLog.d(TAG, "Event: USER_JOINED_MEETING");
                    MeetingMember joinedMember = (MeetingMember) args[1];

                    if (mMemberViews.containsKey(joinedMember.getUserId())) {
                        break;
                    }

                    View joinedMemberView = joinedMember.createView(getParentActivity());

                    if (joinedMemberView != null) {
                        joinedMemberView.setTag(joinedMember);
                        mMemberViews.put(joinedMember.getUserId(), joinedMemberView);
                        mGrid.addView(joinedMemberView);

                        ensureSelfOnTop();
                    }
                    break;
                case HeymateEvents.USER_LEFT_MEETING:
                    MeetingMember leftMember = (MeetingMember) args[1];
                    View leftMemberView = mMemberViews.get(leftMember.getUserId());
                    HMLog.d(TAG, "Event: USER_LEFT_MEETING - leftMemberView exists: " + (leftMemberView != null));

                    if (leftMemberView != null) {
                        leftMember.releaseView(leftMemberView);
                        mMemberViews.remove(leftMember.getUserId());
                        mGrid.removeView(leftMemberView);
                    }
                    break;
                case HeymateEvents.MEETING_USER_STATUS_CHANGED:
                    HMLog.d(TAG, "Event: MEETING_USER_STATUS_CHANGED");
                    updateState();
                    ensureMemberViews();
                    break;
            }
        });
    }

    @Override
    protected void clearViews() {
        HMLog.d(TAG, "clearViews");
        HeymateEvents.unregister(HeymateEvents.JOINING_MEETING, this);
        HeymateEvents.unregister(HeymateEvents.JOINED_MEETING, this);
        HeymateEvents.unregister(HeymateEvents.FAILED_TO_JOIN_MEETING, this);
        HeymateEvents.unregister(HeymateEvents.USER_JOINED_MEETING, this);
        HeymateEvents.unregister(HeymateEvents.USER_LEFT_MEETING, this);
        HeymateEvents.unregister(HeymateEvents.MEETING_USER_STATUS_CHANGED, this);

        for (int i = 0; i < mGrid.getChildCount(); i++) {
            View child = mGrid.getChildAt(i);

            if (child.getTag() instanceof MeetingMember) {
                MeetingMember meetingMember = (MeetingMember) child.getTag();
                meetingMember.releaseView(child);
            }
        }

        if (mOverlay.getChildCount() > 0) {
            MeetingMember myself = (MeetingMember) mOverlay.getChildAt(0).getTag();

            myself.releaseView(mOverlay.getChildAt(0));
        }

        ((ViewGroup) fragmentView).removeAllViews();

        mMemberViews.clear();

        super.clearViews();
    }

    @Override
    public boolean onBackPressed() {
        HMLog.d(TAG, "onBackPressed");
        confirmCloseMeeting();
        return false;
    }

    private void confirmCloseMeeting() {
        HMLog.d(TAG, "confirmCloseMeeting");
        LoadingUtil.onLoadingStarted(getParentActivity());

        if (mTimeSlotId != null) {
            HtAmplify.getInstance(getParentActivity()).getTimeSlot(mTimeSlotId, (success, result, exception) -> {
                LoadingUtil.onLoadingFinished();

                if (success) {
                    confirmCloseMeeting(String.valueOf(getUserConfig().clientUserId).equals(result.getUserId()));
                }
            });
        }
        else if (mReservationId != null) {
            HtAmplify.getInstance(getParentActivity()).getReservation(mReservationId, (success, result, exception) -> {
                LoadingUtil.onLoadingFinished();

                if (success) {
                    confirmCloseMeeting(String.valueOf(getUserConfig().clientUserId).equals(result.getServiceProviderId()));
                }
            });
        }
    }

    private void confirmCloseMeeting(boolean isServiceProvider) {
        HMLog.d(TAG, "confirmCloseMeeting. isServiceProvider=" + isServiceProvider);
        new AlertDialog.Builder(getParentActivity())
                .setTitle(isServiceProvider ? "End meeting" : "Leave meeting")
                .setMessage(isServiceProvider ? "Do you want to end the meeting and finish the offer?" : "Do you want to leave the meeting? You can join again as long as the offer has not finished.")
                .setPositiveButton(isServiceProvider ? "Yes, end" : "Yes, leave", (dialog, which) -> {
                    closeMeeting();
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void closeMeeting() {
        HMLog.d(TAG, "closeMeeting. started=" + mStarted);
        if (mStarted) {
            OnlineMeeting.get().leaveMeeting();
            OnlineReservation.onlineMeetingClosed(getParentActivity(), mTimeSlotId, mReservationId);
        }

        finishFragment();
    }

    private String[] getMissingPermissions() {
        ArrayList<String> permissions = new ArrayList<>(2);

        if (ActivityCompat.checkSelfPermission(getParentActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CAMERA);
        }

        if (ActivityCompat.checkSelfPermission(getParentActivity(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.RECORD_AUDIO);
        }

        return permissions.toArray(new String[0]);
    }

    @Override
    protected void setParentLayout(ActionBarLayout layout) {
        if (parentLayout != layout) {
            parentLayout = layout;
            inBubbleMode = parentLayout != null && parentLayout.isInBubbleMode();
            if (fragmentView != null) {
                ViewGroup parent = (ViewGroup) fragmentView.getParent();
                if (parent != null) {
                    try {
                        onRemoveFromParent();
                        parent.removeViewInLayout(fragmentView);
                    } catch (Exception e) {
                        FileLog.e(e);
                    }
                }
                if (parentLayout != null && parentLayout.getContext() != fragmentView.getContext()) {
                    fragmentView = null;
                }
            }
            if (actionBar != null) {
                boolean differentParent = parentLayout != null && parentLayout.getContext() != actionBar.getContext();
                if (actionBar.shouldAddToContainer() || differentParent) {
                    ViewGroup parent = (ViewGroup) actionBar.getParent();
                    if (parent != null) {
                        try {
                            parent.removeViewInLayout(actionBar);
                        } catch (Exception e) {
                            FileLog.e(e);
                        }
                    }
                }
                if (differentParent) {
                    actionBar = null;
                }
            }
        }
    }

    @Override
    public void finishFragment() {
        HMLog.report(this);
        super.finishFragment();
    }

}

package org.telegram.ui.Heymate.onlinemeeting;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
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
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Heymate.widget.AutoGridLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import works.heymate.beta.R;
import works.heymate.core.HeymateEvents;

public class OnlineMeetingActivity extends BaseFragment implements HeymateEvents.HeymateEventObserver {

    private static final String KEY_MEETING_ID = "meetingId";

    private static Bundle createArgs(String meetingId) {
        Bundle args = new Bundle();
        args.putString(KEY_MEETING_ID, meetingId);
        return args;
    }

    private String mMeetingId;

    private AutoGridLayout mGrid;
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

    public OnlineMeetingActivity(String meetingId) {
        super(createArgs(meetingId));
    }

    @Override
    public boolean onFragmentCreate() {
        mMeetingId = getArguments().getString(KEY_MEETING_ID);

        return super.onFragmentCreate();
    }

    @Override
    public View createView(Context context) {
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

        mLeave = content.findViewById(R.id.leave);
        mLeave.setText("Leave");  // TODO Texts
        mLeave.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(8), 0xffFE0000));
        mLeave.setTextColor(0xffffffff);
        mLeave.setOnClickListener(v -> closeMeeting());

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

        checkPermissionsAndStart();
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
        if (mStarted) {
            return;
        }

        mStarted = true;

        if (OnlineMeeting.get().ensureSession(mMeetingId)) {
            onHeymateEvent(HeymateEvents.USER_JOINED_MEETING);
        }
    }

    @Override
    public void onHeymateEvent(int event, Object... args) {
        switch (event) {
            case HeymateEvents.JOINING_MEETING:
                // Nothing to do.
                break;
            case HeymateEvents.JOINED_MEETING:
                MeetingMember myself = OnlineMeeting.get().getSelf();
                View myselfView = myself.createView(getParentActivity());
                myselfView.setTag(OnlineMeeting.get().getSelf());
                mMemberViews.put(myself.getUserId(), myselfView);

                if (myself.isHost()) {
                    mLeave.setText("End");
                }

                mGrid.addView(myselfView);
                break;
            case HeymateEvents.FAILED_TO_JOIN_MEETING:
                mStarted = false;
                Toast.makeText(getParentActivity(), "Failed to join to the meeting!", Toast.LENGTH_LONG).show(); // TODO Texts
                finishFragment();
                break;
            case HeymateEvents.LEFT_MEETING:
                // Nothing to do.
                break;
            case HeymateEvents.USER_JOINED_MEETING:
                MeetingMember joinedMember = OnlineMeeting.get().getMember((String) args[0]);
                View joinedMemberView = joinedMember.createView(getParentActivity());
                joinedMemberView.setTag(joinedMember);
                mMemberViews.put(joinedMember.getUserId(), joinedMemberView);
                mGrid.addView(joinedMemberView);
                break;
            case HeymateEvents.USER_LEFT_MEETING:
                MeetingMember leftMember = OnlineMeeting.get().getMember((String) args[0]);
                View leftMemberView = mMemberViews.get(leftMember.getUserId());

                if (leftMemberView != null) {
                    leftMember.releaseView(leftMemberView);
                    mMemberViews.remove(leftMember.getUserId());
                    mGrid.removeView(leftMemberView);
                }
                break;
            case HeymateEvents.MEETING_USER_STATUS_CHANGED:
                updateState();
                break;
        }
    }

    @Override
    protected void clearViews() {
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

        ((ViewGroup) fragmentView).removeAllViews();

        mMemberViews.clear();

        super.clearViews();
    }

    @Override
    public boolean onBackPressed() {
        closeMeeting();
        return true;
    }

    private void closeMeeting() {
        if (mStarted) {
            OnlineMeeting.get().leaveMeeting();
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


}

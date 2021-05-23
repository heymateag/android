package org.telegram.ui.Heymate.onlinemeeting;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Heymate.widget.AutoGridLayout;

import java.util.HashMap;
import java.util.Map;

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

    private final Map<String, View> mMemberViews = new HashMap<>();

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
        FrameLayout content = new FrameLayout(context);

        mGrid = new AutoGridLayout(context);
        content.addView(mGrid, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        HeymateEvents.register(HeymateEvents.JOINED_MEETING, this);
        HeymateEvents.register(HeymateEvents.USER_JOINED_MEETING, this);
        HeymateEvents.register(HeymateEvents.USER_LEFT_MEETING, this);

        OnlineMeeting.get().ensureSession(mMeetingId);

        TextView endButton = new TextView(context);
        endButton.setText("End");
        endButton.setGravity(Gravity.CENTER);
        endButton.setPadding(AndroidUtilities.dp(12), AndroidUtilities.dp(8), AndroidUtilities.dp(12), AndroidUtilities.dp(8));
        endButton.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(8), 0xffdd0000));
        endButton.setTextColor(Theme.getColor(Theme.key_dialogFloatingIcon));
        content.addView(endButton, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.RIGHT, 0, 24, 16, 0));

        endButton.setOnClickListener(v -> closeMeeting());

        return content;
    }

    @Override
    protected void clearViews() {
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
    public void onHeymateEvent(int event, Object... args) {
        switch (event) {
            case HeymateEvents.JOINED_MEETING:
                MeetingMember myself = OnlineMeeting.get().getSelf();
                View myselfView = myself.createView(getParentActivity());
                myselfView.setTag(OnlineMeeting.get().getSelf());
                mMemberViews.put(myself.getUserId(), myselfView);
                mGrid.addView(myselfView);
                break;
            case HeymateEvents.USER_JOINED_MEETING:
                MeetingMember joinedMember = OnlineMeeting.get().getMeetingMember((String) args[0]);
                View joinedMemberView = joinedMember.createView(getParentActivity());
                joinedMemberView.setTag(joinedMember);
                mMemberViews.put(joinedMember.getUserId(), joinedMemberView);
                mGrid.addView(joinedMemberView);
                break;
            case HeymateEvents.USER_LEFT_MEETING:
                MeetingMember leftMember = OnlineMeeting.get().getMeetingMember((String) args[0]);
                View leftMemberView = mMemberViews.get(leftMember.getUserId());

                if (leftMemberView != null) {
                    leftMember.releaseView(leftMemberView);
                    mMemberViews.remove(leftMember.getUserId());
                    mGrid.removeView(leftMemberView);
                }
                break;
        }
    }

    @Override
    public boolean onBackPressed() {
        closeMeeting();
        return true;
    }

    private void closeMeeting() {
        OnlineMeeting.get().leaveMeeting();
        finishFragment();
    }

}

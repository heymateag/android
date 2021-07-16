package org.telegram.ui.Heymate.onlinemeeting;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;

import java.util.ArrayList;
import java.util.List;

import works.heymate.beta.R;
import works.heymate.core.HeymateEvents;
import works.heymate.core.Texts;

public class MeetingMembersActivity extends BaseFragment implements HeymateEvents.HeymateEventObserver {

    private List<MeetingMember> mMembers;

    @Override
    public View createView(Context context) {
        mMembers = new ArrayList<>(OnlineMeeting.get().getMembers());

        ActionBar actionBar = getActionBar();
        actionBar.setBackButtonDrawable(new BackDrawable(false));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (getParentActivity() == null) {
                    return;
                }

                if (id == -1) {
                    finishFragment();
                }
            }
        });
        updateTitle();

        View content = LayoutInflater.from(context).inflate(R.layout.activity_meetingmembers, null, false);
        content.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        RecyclerView listMember = content.findViewById(R.id.list_member);
        listMember.setLayoutManager(new LinearLayoutManager(context));
        listMember.setAdapter(mMemberListAdapter);
        listMember.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

        TextView muteAll = content.findViewById(R.id.mute_all);
        muteAll.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(4), Theme.getColor(Theme.key_windowBackgroundWhite)));
        muteAll.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        muteAll.setText("Mute All"); // TODO Texts

        Drawable mute = AppCompatResources.getDrawable(context, R.drawable.ic_mic_off).mutate();
        mute.setColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText), PorterDuff.Mode.SRC_IN);
        mute.setBounds(0, 0, AndroidUtilities.dp(16), AndroidUtilities.dp(16));
        muteAll.setCompoundDrawables(mute, null, null, null);

        muteAll.setOnClickListener(v -> OnlineMeeting.get().muteAll());

        HeymateEvents.register(HeymateEvents.FAILED_TO_JOIN_MEETING, this);
        HeymateEvents.register(HeymateEvents.LEFT_MEETING, this);
        HeymateEvents.register(HeymateEvents.USER_JOINED_MEETING, this);
        HeymateEvents.register(HeymateEvents.USER_LEFT_MEETING, this);

        fragmentView = content;

        return content;
    }

    @Override
    protected void clearViews() {
        ((ViewGroup) fragmentView).removeAllViews();

        HeymateEvents.unregister(HeymateEvents.FAILED_TO_JOIN_MEETING, this);
        HeymateEvents.unregister(HeymateEvents.LEFT_MEETING, this);
        HeymateEvents.unregister(HeymateEvents.USER_JOINED_MEETING, this);
        HeymateEvents.unregister(HeymateEvents.USER_LEFT_MEETING, this);

        super.clearViews();
    }

    @Override
    public void onHeymateEvent(int event, Object... args) {
        switch (event) {
            case HeymateEvents.FAILED_TO_JOIN_MEETING:
            case HeymateEvents.LEFT_MEETING:
                finishFragment(true);
                return;
            case HeymateEvents.USER_JOINED_MEETING:
                mMembers.add((MeetingMember) args[1]);
                mMemberListAdapter.notifyItemInserted(mMembers.size() - 1);
                updateTitle();
                break;
            case HeymateEvents.USER_LEFT_MEETING:
                MeetingMember leftMember = (MeetingMember) args[1];
                int index = mMembers.indexOf(leftMember);

                if (index >= 0) {
                    mMembers.remove(index);
                    mMemberListAdapter.notifyItemRemoved(index);
                }

                updateTitle();
                break;
        }
    }

    private void updateTitle() {
        getActionBar().setTitle("Members (" + mMembers.size() + ")");
    }

    private final RecyclerView.Adapter<RecyclerView.ViewHolder> mMemberListAdapter = new RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            MeetingMemberItem item = new MeetingMemberItem(parent.getContext());
            return new RecyclerView.ViewHolder(item) { };
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            MeetingMemberItem item = (MeetingMemberItem) holder.itemView;
            item.setMeetingMember(mMembers.get(position));
        }

        @Override
        public int getItemCount() {
            return mMembers.size();
        }

    };

}

package org.telegram.ui.Heymate.onlinemeeting;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;

import com.yashoid.sequencelayout.SequenceLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Heymate.log.HMLog;

import works.heymate.beta.R;
import works.heymate.core.HeymateEvents;

public class MeetingMemberItem extends SequenceLayout implements HeymateEvents.HeymateEventObserver {

    private static final String TAG = "MeetingMemberItem";

    private ImageView mImage;
    private TextView mName;
    private ImageView mVideo;
    private ImageView mAudio;

    private MeetingMember mMeetingMember = null;

    private ImageReceiver avatarImage = new ImageReceiver(this);
    private AvatarDrawable avatarDrawable = new AvatarDrawable();

    public MeetingMemberItem(Context context) {
        super(context);
        initialize(context, null, 0);
    }

    public MeetingMemberItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0);
    }

    public MeetingMemberItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(Context context, AttributeSet attrs, int defStyleAttr) {
        setWillNotDraw(false);

        LayoutInflater.from(context).inflate(R.layout.item_meetingmember, this, true);
        addSequences(R.xml.sequences_item_meetingmember);

        mImage = findViewById(R.id.image);
        mName = findViewById(R.id.name);
        mVideo = findViewById(R.id.video);
        mAudio = findViewById(R.id.audio);
        View divider = findViewById(R.id.divider);

        mName.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        divider.setBackgroundColor(Theme.getColor(Theme.key_divider));

        mVideo.setOnClickListener(v -> {
            if (mMeetingMember == null) {
                return;
            }

            HMLog.d(TAG, "videoToggle clicked");

            if (mMeetingMember == OnlineMeeting.get().getSelf()) {
                HMLog.d(TAG, "videoToggle item user is current user");

                if (mMeetingMember.isVideoOn()) {
                    OnlineMeeting.get().stopVideo();
                }
                else {
                    OnlineMeeting.get().startVideo();
                }
            }
            else {
                HMLog.d(TAG, "videoToggle users differ: " + mMeetingMember + " != " + OnlineMeeting.get().getSelf());
            }
        });

        mAudio.setOnClickListener(v -> toggleMute());

        setOnClickListener(v -> {
            if (OnlineMeeting.get().getSelf() != null && OnlineMeeting.get().getSelf().isServiceProvider()) {
                showOptionsPopup();
            }
        });
    }

    private void toggleMute() {
        if (mMeetingMember == null) {
            return;
        }

        HMLog.d(TAG, "muteToggle user id is " + mMeetingMember.getUserId());

        if (mMeetingMember.isMuted()) {
            OnlineMeeting.get().unMute(mMeetingMember.getUserId());
        }
        else {
            OnlineMeeting.get().mute(mMeetingMember.getUserId());
        }
    }

    public void setMeetingMember(MeetingMember meetingMember) {
        mMeetingMember = meetingMember;

        try {
            int userId = Integer.parseInt(meetingMember.getUserId());
            TLRPC.User user = MessagesController.getInstance(UserConfig.selectedAccount).getUser(userId);

            mName.setText(user == null ? "" : UserObject.getUserName(user));
            avatarDrawable.setInfo(user);
            avatarImage.setImage(ImageLocation.getForUser(user, ImageLocation.TYPE_SMALL), "50_50", avatarDrawable, null, user, 0);
        } catch (Throwable t) { }

        updateState();
    }

    private void updateState() {
        if (mMeetingMember == null) {
            return;
        }

        if (mMeetingMember.isVideoOn()) {
            Drawable viewDrawable = AppCompatResources.getDrawable(getContext(), R.drawable.ic_video_on).mutate();
            viewDrawable.setColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteBlueText), PorterDuff.Mode.SRC_IN);
            mVideo.setImageDrawable(viewDrawable);
        }
        else {
            Drawable viewDrawable = AppCompatResources.getDrawable(getContext(), R.drawable.ic_video_off).mutate();
            viewDrawable.setColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText), PorterDuff.Mode.SRC_IN);
            mVideo.setImageDrawable(viewDrawable);
        }

        if (mMeetingMember.isMuted()) {
            Drawable audioDrawable = AppCompatResources.getDrawable(getContext(), R.drawable.ic_mic_off).mutate();
            audioDrawable.setColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText), PorterDuff.Mode.SRC_IN);
            mAudio.setImageDrawable(audioDrawable);
        }
        else {
            Drawable audioDrawable = AppCompatResources.getDrawable(getContext(), R.drawable.ic_mic_on).mutate();
            audioDrawable.setColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteBlueText), PorterDuff.Mode.SRC_IN);
            mAudio.setImageDrawable(audioDrawable);
        }
    }

    @Override
    public void onHeymateEvent(int event, Object... args) {
        MeetingMember meetingMember = (MeetingMember) args[1];

        if (meetingMember == mMeetingMember) {
            updateState();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        avatarImage.setImageCoords(mImage.getLeft(), mImage.getTop(), mImage.getWidth(), mImage.getHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        avatarImage.draw(canvas);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        avatarImage.onAttachedToWindow();

        HeymateEvents.register(HeymateEvents.MEETING_USER_STATUS_CHANGED, this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        avatarImage.onDetachedFromWindow();

        HeymateEvents.unregister(HeymateEvents.MEETING_USER_STATUS_CHANGED, this);
    }

    private void showOptionsPopup() {
        if (mMeetingMember == null || OnlineMeeting.get().getSelf() == null || !OnlineMeeting.get().getSelf().isServiceProvider()) {
            return;
        }

        LinearLayout container = new LinearLayout(getContext());
        container.setOrientation(LinearLayout.VERTICAL);

        PopupWindow popup = new PopupWindow(getContext());
        popup.setContentView(container);
        popup.setBackgroundDrawable(Theme.createRoundRectDrawable(AndroidUtilities.dp(8), Theme.getColor(Theme.key_dialogBackground)));
        popup.setAnimationStyle(R.style.PopupAnimation);
        popup.setOutsideTouchable(false);
        popup.setFocusable(true);
        popup.setTouchable(true);
        popup.setWidth(AndroidUtilities.dp(312));
        popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.setContentView(container);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popup.setElevation(AndroidUtilities.dp(4));
        }

        TextView name = new TextView(getContext());
        name.setTextSize(20);
        name.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        name.setPadding(AndroidUtilities.dp(24), AndroidUtilities.dp(24), AndroidUtilities.dp(24), AndroidUtilities.dp(24));
        name.setText(mName.getText());
        container.addView(name);

        boolean muted = mMeetingMember.isMuted();

        TextView muteButton = createButton(
                muted ? "Unmute" : "Mute",
                muted ? R.drawable.ic_mic_on : R.drawable.ic_mic_off,
                Theme.getColor(Theme.key_dialogTextBlack),
                Theme.getColor(Theme.key_dialogTextGray2));
        muteButton.setOnClickListener(v -> {
            toggleMute();
            popup.dismiss();
        });
        container.addView(muteButton);

        TextView removeButton = createButton(
                "Remove",
                R.drawable.msg_delete,
                Theme.getColor(Theme.key_dialogTextRed),
                Theme.getColor(Theme.key_dialogTextRed)
        );
        removeButton.setOnClickListener(v -> {
            OnlineMeeting.get().kick(mMeetingMember.getUserId());
            popup.dismiss();
        });
        container.addView(removeButton);

        popup.showAtLocation(this, Gravity.CENTER, 0, 0);
    }

    private TextView createButton(CharSequence text, int iconResId, int textColor, int iconColor) {
        TextView button = new TextView(getContext());
        button.setTextSize(14);
        button.setTextColor(textColor);
        button.setPadding(AndroidUtilities.dp(24), 0, AndroidUtilities.dp(24), 0);
        button.setCompoundDrawablePadding(AndroidUtilities.dp(24));

        button.setText(text);

        Drawable icon = AppCompatResources.getDrawable(getContext(), iconResId);

        if (icon != null) {
            icon = icon.mutate();
            icon.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
            icon.setBounds(0, 0, AndroidUtilities.dp(24), AndroidUtilities.dp(24));

            button.setCompoundDrawables(icon, null, null, null);
        }

        button.setGravity(Gravity.CENTER_VERTICAL);

        button.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, AndroidUtilities.dp(72)));

        return button;
    }

}

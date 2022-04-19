package org.telegram.ui.Heymate.user;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.yashoid.sequencelayout.SequenceLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.ProfileActivity;

import works.heymate.api.APIObject;
import works.heymate.beta.R;
import works.heymate.model.User;
import works.heymate.model.Users;

public class SentMoneyItem extends SequenceLayout {

    private ImageView mImageFrom;
    private TextView mTextFrom;
    private ImageView mImageTo;
    private TextView mTextTo;
    private TextView mMoney;
    private TextView mDescription;
    private TextView mReceipt;

    private ImageReceiver senderImage = new ImageReceiver(this);
    private AvatarDrawable senderDrawable = new AvatarDrawable();

    private ImageReceiver receiverImage = new ImageReceiver(this);
    private AvatarDrawable receiverDrawable = new AvatarDrawable();

    private BaseFragment mParent;

    private APIObject mContent;

    private long senderTelegramId;
    private long receiverTelegramId;

    public SentMoneyItem(Context context) {
        super(context);
        setWillNotDraw(false);

        LayoutInflater.from(context).inflate(R.layout.hm_item_sendmoney, this, true);
        addSequences(R.xml.sequences_item_sendmoney);

        mImageFrom = findViewById(R.id.image_from);
        mTextFrom = findViewById(R.id.text_from);
        mImageTo = findViewById(R.id.image_to);
        mTextTo = findViewById(R.id.text_to);
        mMoney = findViewById(R.id.money);
        mDescription = findViewById(R.id.description);
        mReceipt = findViewById(R.id.receipt);

        TextView titleFrom = findViewById(R.id.title_from);
        TextView titleTo = findViewById(R.id.title_to);

        View background = findViewById(R.id.background);
        int cornerRadius = AndroidUtilities.dp(8);
        background.setBackground(Theme.createRoundRectDrawable(cornerRadius, Theme.getColor(Theme.key_windowBackgroundWhite)));

        titleFrom.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        mTextFrom.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        titleTo.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        mTextTo.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        mMoney.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        mDescription.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));

        titleFrom.setText("Sender:");
        titleTo.setText("Receiver:");

        mReceipt.setBackground(Theme.createRoundRectDrawable(cornerRadius, getResources().getColor(R.color.ht_theme)));
        mReceipt.setTextColor(Theme.getColor(Theme.key_chats_actionIcon));
        mReceipt.setText("See Receipt");

        OnClickListener senderProfileOpener = v -> {
            if (mParent != null && senderTelegramId != 0) {
                Bundle args = new Bundle();
                args.putLong("user_id", senderTelegramId);
                mParent.presentFragment(new ProfileActivity(args));
            }
        };

        mImageFrom.setOnClickListener(senderProfileOpener);
        mTextFrom.setOnClickListener(senderProfileOpener);

        OnClickListener receiverProfileOpener = v -> {
            if (mParent != null && receiverTelegramId != 0) {
                Bundle args = new Bundle();
                args.putLong("user_id", receiverTelegramId);
                mParent.presentFragment(new ProfileActivity(args));
            }
        };

        mImageTo.setOnClickListener(receiverProfileOpener);
        mTextTo.setOnClickListener(receiverProfileOpener);

        mReceipt.setOnClickListener(v -> {
            if (mContent == null) {
                return;
            }

            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(mContent.getString(SendMoneyUtils.URL)));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(intent);
            } catch (Throwable t) { }
        });

        senderImage.setRoundRadius(AndroidUtilities.dp(18));
        receiverImage.setRoundRadius(AndroidUtilities.dp(18));
    }

    public void setParent(BaseFragment parent) {
        mParent = parent;
    }

    public void setContent(String text) {
        mContent = SendMoneyUtils.parseMessage(text);

        if (mContent == null) {
            return;
        }

        Users.getUser(mContent.getString(SendMoneyUtils.SENDER_ID), result -> {
            if (result.response != null) {
                mTextFrom.setText(result.response.getString(User.FULL_NAME));

                senderTelegramId = result.response.getLong(User.TELEGRAM_ID);

                if (senderTelegramId != 0) {
                    TLRPC.User user = MessagesController.getInstance(UserConfig.selectedAccount).getUser(senderTelegramId);
                    senderDrawable.setInfo(user);
                    senderImage.setImage(ImageLocation.getForUser(user, ImageLocation.TYPE_SMALL), "50_50", senderDrawable, null, user, 0);
                }
            }
        });

        Users.getUser(mContent.getString(SendMoneyUtils.RECEIVER_ID), result -> {
            if (result.response != null) {
                mTextTo.setText(result.response.getString(User.FULL_NAME));

                receiverTelegramId = result.response.getLong(User.TELEGRAM_ID);

                if (receiverTelegramId != 0) {
                    TLRPC.User user = MessagesController.getInstance(UserConfig.selectedAccount).getUser(receiverTelegramId);
                    receiverDrawable.setInfo(user);
                    receiverImage.setImage(ImageLocation.getForUser(user, ImageLocation.TYPE_SMALL), "50_50", receiverDrawable, null, user, 0);
                }
            }
        });

        mMoney.setText(mContent.getString(SendMoneyUtils.MONEY));
        mDescription.setText(mContent.getString(SendMoneyUtils.MESSAGE));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        senderImage.setImageCoords(mImageFrom.getLeft(), mImageFrom.getTop(), mImageFrom.getWidth(), mImageFrom.getHeight());
        receiverImage.setImageCoords(mImageTo.getLeft(), mImageTo.getTop(), mImageTo.getWidth(), mImageTo.getHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        senderImage.draw(canvas);
        receiverImage.draw(canvas);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        senderImage.onAttachedToWindow();
        receiverImage.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        senderImage.onDetachedFromWindow();
        receiverImage.onDetachedFromWindow();
    }

}

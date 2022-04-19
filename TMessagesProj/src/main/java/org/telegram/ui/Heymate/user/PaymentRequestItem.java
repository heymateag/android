package org.telegram.ui.Heymate.user;

import android.content.Context;
import android.graphics.Canvas;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

import works.heymate.api.APIArray;
import works.heymate.api.APIObject;
import works.heymate.beta.R;
import works.heymate.core.Money;
import works.heymate.model.User;
import works.heymate.model.Users;

public class PaymentRequestItem extends SequenceLayout {

    private ImageView mImage;
    private TextView mName;
    private TextView mMoney;
    private TextView mDescription;
    private TextView mSend;

    private ImageReceiver avatarImage = new ImageReceiver(this);
    private AvatarDrawable avatarDrawable = new AvatarDrawable();

    private BaseFragment mParent;

    private APIObject mContent;
    private APIObject mUser;

    public PaymentRequestItem(Context context) {
        super(context);
        setWillNotDraw(false);

        LayoutInflater.from(context).inflate(R.layout.hm_item_paymentrequest, this, true);
        addSequences(R.xml.sequences_item_paymentrequest);

        mImage = findViewById(R.id.image);
        mName = findViewById(R.id.name);
        mMoney = findViewById(R.id.money);
        mDescription = findViewById(R.id.description);
        mSend = findViewById(R.id.send);

        TextView title = findViewById(R.id.title);

        View background = findViewById(R.id.background);
        int cornerRadius = AndroidUtilities.dp(8);
        background.setBackground(Theme.createRoundRectDrawable(cornerRadius, Theme.getColor(Theme.key_windowBackgroundWhite)));

        title.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        mName.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        mMoney.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        mDescription.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));

        title.setText("Money request from:");

        mSend.setBackground(Theme.createRoundRectDrawable(cornerRadius, getResources().getColor(R.color.ht_theme)));
        mSend.setTextColor(Theme.getColor(Theme.key_chats_actionIcon));
        mSend.setText("Send money");

        mSend.setOnClickListener(v -> {
            if (mContent == null || mUser == null) {
                return;
            }

            Money money = Money.create(mContent.getString(PaymentRequestUtils.MONEY));

            if (money == null) {
                return;
            }
            
            String walletAddress = mContent.getString(PaymentRequestUtils.WALLET);
            
            APIArray devices = mUser.getArray(User.DEVICES);
            
            boolean walletFound = false;
            
            for (int i = 0; i < devices.size(); i++) {
                APIObject device = devices.getObject(i);
                
                if (walletAddress.equals(device.getString(User.Device.WALLET_ADDRESS))) {
                    walletFound = true;
                    break;
                }
            }

            if (!walletFound) {
                Toast.makeText(getContext(), "This wallet address is not registered for this user.", Toast.LENGTH_SHORT).show();
                return;
            }

            SendMoneySheet sendMoneySheet = new SendMoneySheet(getContext(), walletAddress);
            sendMoneySheet.setReceiver(mUser);
            sendMoneySheet.setReceiveCurrency(money.getCurrency()); // TODO receive amount
            sendMoneySheet.setReceiveCents(money.getCents());
            sendMoneySheet.show();
        });

        avatarImage.setRoundRadius(AndroidUtilities.dp(18));
    }

    public void setParent(BaseFragment parent) {
        mParent = parent;
    }

    public void setContent(String text) {
        mContent = PaymentRequestUtils.parseMessage(text);

        if (mContent == null) {
            return;
        }

        Users.getUser(mContent.getString(PaymentRequestUtils.USER_ID), result -> {
            if (result.response != null) {
                mUser = result.response;

                mName.setText(mUser.getString(User.FULL_NAME));

                long telegramId = mUser.getLong(User.TELEGRAM_ID);

                if (telegramId != 0) {
                    TLRPC.User user = MessagesController.getInstance(UserConfig.selectedAccount).getUser(telegramId);
                    avatarDrawable.setInfo(user);
                    avatarImage.setImage(ImageLocation.getForUser(user, ImageLocation.TYPE_SMALL), "50_50", avatarDrawable, null, user, 0);
                }
            }
        });

        mMoney.setText(mContent.getString(SendMoneyUtils.MONEY));
        mDescription.setText(mContent.getString(SendMoneyUtils.MESSAGE));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        avatarImage.setImageCoords(mImage.getLeft(), mImage.getTop(), mImage.getWidth(), mImage.getHeight());
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        avatarImage.draw(canvas);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        avatarImage.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        avatarImage.onDetachedFromWindow();
    }

}

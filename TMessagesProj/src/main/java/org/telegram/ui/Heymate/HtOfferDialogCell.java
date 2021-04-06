package org.telegram.ui.Heymate;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.DividerCell;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;

public class HtOfferDialogCell extends FrameLayout {

    private Context context;
    private TLRPC.Dialog dialog;
    private BackupImageView avatar;
    private TLRPC.User user;
    private TLRPC.Chat chat;
    private TLRPC.EncryptedChat encryptedChat;
    private TextView titleLabel;
    private TextView timeLabel;
    private TextView unreadLabel;
    private int unreadCount = -1;

    public HtOfferDialogCell(@NonNull Context context) {
        super(context);
        this.context = context;

        LinearLayout holderLayout = new LinearLayout(context);
        holderLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout mainLayout = new LinearLayout(context);
        avatar = new BackupImageView(context);
        avatar.setRoundRadius(AndroidUtilities.dp(4));
        mainLayout.addView(avatar, LayoutHelper.createFrame(40, 40, Gravity.LEFT, 10, 10, 17, 10));

        LinearLayout statusBar = new LinearLayout(context);
        statusBar.setBackgroundColor(context.getResources().getColor(R.color.ht_green));
        mainLayout.addView(statusBar, LayoutHelper.createFrame(2, LayoutHelper.MATCH_PARENT, Gravity.LEFT, 0,8,0,12));

        LinearLayout titleLayout = new LinearLayout(context);
        titleLayout.setOrientation(LinearLayout.VERTICAL);
        titleLabel = new TextView(context);
        titleLayout.addView(titleLabel, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 10,10,10,0));

        TextView categoryLabel = new TextView(context);
        categoryLabel.setText("");
        categoryLabel.setTextColor(Theme.getColor(Theme.key_dialogTextGray));
        categoryLabel.setTextSize(13);
        titleLayout.addView(categoryLabel, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 10,0,10,10));
        mainLayout.addView(titleLayout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,0.99f));

        LinearLayout rightLayout = new LinearLayout(context);
        rightLayout.setGravity(Gravity.RIGHT);
        rightLayout.setOrientation(LinearLayout.VERTICAL);

        timeLabel = new TextView(context);
        timeLabel.setTextSize(11);
        timeLabel.setTextColor(Theme.getColor(Theme.key_dialogTextGray2));
        rightLayout.addView(timeLabel, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 10,10,10,10));

        LinearLayout unreadLayout = new LinearLayout(context);
        unreadLayout.setGravity(Gravity.CENTER);
        unreadLayout.setBackgroundColor(Theme.getColor(Theme.key_voipgroup_overlayGreen1));
        unreadLayout.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(14), Theme.getColor(Theme.key_voipgroup_overlayGreen1)));

        unreadLabel = new TextView(context);
        unreadLabel.setTextColor(Theme.getColor(Theme.key_wallet_whiteText));
        unreadLabel.setTextSize(12);
        unreadLayout.addView(unreadLabel, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 7,3,7,3));
        rightLayout.addView(unreadLayout, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 10,0,10,0));
        mainLayout.addView(rightLayout, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, Gravity.RIGHT));
        holderLayout.addView(mainLayout);
        holderLayout.addView(new DividerCell(context), LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 25,0,25 ,0));
        addView(holderLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 72));
    }

    public void setDialog(TLRPC.Dialog dialog) {
        if(unreadCount != -1)
            return;
        unreadCount = dialog.unread_count;
        this.dialog = dialog;
        long dialogId = dialog.id;
        if (dialogId != 0) {
            int lower_id = (int) dialogId;
            int high_id = (int) (dialogId >> 32);
            if (lower_id != 0) {
                if (lower_id < 0) {
                    chat = MessagesController.getInstance(0).getChat(-lower_id);
                    if (chat != null && chat.migrated_to != null) {
                        TLRPC.Chat chat2 = MessagesController.getInstance(0).getChat(chat.migrated_to.channel_id);
                        if (chat2 != null) {
                            chat = chat2;
                        }
                    }
                } else {
                    user = MessagesController.getInstance(0).getUser(lower_id);
                }
            } else {
                encryptedChat = MessagesController.getInstance(0).getEncryptedChat(high_id);
                if (encryptedChat != null) {
                    user = MessagesController.getInstance(0).getUser(encryptedChat.user_id);
                }
            }
        }
        if(user != null){
            Drawable drawable = context.getResources().getDrawable(R.drawable.offer);
            drawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogTextBlack), PorterDuff.Mode.MULTIPLY));
            avatar.setImage(ImageLocation.getForUser(user, false), "50_50", drawable, null);
            titleLabel.setText(user.first_name);
        } else if(chat != null) {
            Drawable drawable = context.getResources().getDrawable(R.drawable.offer);
            drawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogTextBlack), PorterDuff.Mode.MULTIPLY));
            avatar.setImage(ImageLocation.getForChat(chat, false), "50_50", drawable, null);
            titleLabel.setText(chat.title);
        }
        titleLabel.setTextSize(15);
        titleLabel.setTypeface(titleLabel.getTypeface(), Typeface.BOLD);
        titleLabel.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        Drawable titleDrawable = context.getResources().getDrawable(R.drawable.offer);
        titleDrawable.setAlpha(40);
        titleDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogTextBlack), PorterDuff.Mode.MULTIPLY));
        titleLabel.setCompoundDrawablesWithIntrinsicBounds(null, null, titleDrawable, null);
        titleLabel.setCompoundDrawablePadding(AndroidUtilities.dp(4));
        timeLabel.setText(LocaleController.stringForMessageListDate(dialog.last_message_date));
        unreadLabel.setText("" + dialog.unread_count);
    }
}

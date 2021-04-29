package org.telegram.ui.Heymate;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Build;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.yashoid.sequencelayout.Sequence;
import com.yashoid.sequencelayout.SequenceLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.DialogCell;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.ForegroundColorSpanThemable;
import org.telegram.ui.Components.StatusDrawable;

import java.util.List;

public class ShopDialogCell extends SequenceLayout {

    private ImageView mImageDialog;
    private TextView mTextName;
    private TextView mTextMessage;
    private ImageView mImageTag;
    private TextView mTextTime;
    private TextView mTextUnreadCount;

    private Paint mDividerPaint;

    private boolean mHasImage;

    private ImageReceiver avatarImage = new ImageReceiver(this);
    private AvatarDrawable avatarDrawable = new AvatarDrawable();

    public ShopDialogCell(Context context) {
        super(context);
        initialize(context, null, 0);
    }

    public ShopDialogCell(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0);
    }

    public ShopDialogCell(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(Context context, AttributeSet attrs, int defStyleAttr) {
        setPageWidth(140);
        LayoutInflater.from(context).inflate(works.heymate.beta.R.layout.cell_shopdialog, this, true);
        List<Sequence> sequences = addSequences(works.heymate.beta.R.xml.sequences_cell_shopdialog);
        sequences.get(0).getSpans().get(0).size = SharedConfig.useThreeLinesLayout ? 78 : 72;

        setWillNotDraw(false);

        mImageDialog = findViewById(works.heymate.beta.R.id.image_dialog);
        View dividerImage = findViewById(works.heymate.beta.R.id.divider_image);
        mTextName = findViewById(works.heymate.beta.R.id.text_name);
        mTextMessage = findViewById(works.heymate.beta.R.id.text_message);
        mImageTag = findViewById(works.heymate.beta.R.id.image_tag);
        mTextTime = findViewById(works.heymate.beta.R.id.text_time);
        mTextUnreadCount = findViewById(works.heymate.beta.R.id.text_unreadcount);

        mImageDialog.setImageResource(works.heymate.beta.R.drawable.offer);
        mImageDialog.setColorFilter(Theme.getColor(Theme.key_chats_actionIcon), PorterDuff.Mode.SRC_IN);
        mImageDialog.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(4), ContextCompat.getColor(context, works.heymate.beta.R.color.ht_theme)));
//        mImageDialog.setVisibility(INVISIBLE);

        dividerImage.setBackgroundColor(ContextCompat.getColor(context, works.heymate.beta.R.color.ht_theme));

        mTextName.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        mTextMessage.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));

        mImageTag.setImageResource(works.heymate.beta.R.drawable.offer);
        mImageTag.setColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteHintText), PorterDuff.Mode.SRC_IN);

        mTextTime.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));

        mTextUnreadCount.setBackground(Theme.createCircleDrawable(AndroidUtilities.dp(24), Theme.getColor(Theme.key_actionBarDefault)));
        mTextUnreadCount.setTextColor(Theme.getColor(Theme.key_chats_actionIcon));

        mDividerPaint = new Paint();
        mDividerPaint.setColor(Theme.getColor(Theme.key_divider));
        mDividerPaint.setStyle(Paint.Style.STROKE);
        mDividerPaint.setStrokeWidth(AndroidUtilities.dp(1));

        avatarImage.setRoundRadius(AndroidUtilities.dp(4));
    }

    public void setDialog(TLRPC.Dialog dialog) {
        TLRPC.Chat chat = null;
        TLRPC.EncryptedChat encryptedChat = null;
        TLRPC.User user = null;

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

        if (user != null) {
            mTextName.setText(UserObject.getUserName(user));

            avatarDrawable.setInfo(user);
            if (UserObject.isReplyUser(user)) {
                mHasImage = false;
                avatarDrawable.setAvatarType(AvatarDrawable.AVATAR_TYPE_REPLIES);
                avatarImage.setImage(null, null, avatarDrawable, null, user, 0);
            } else if (UserObject.isUserSelf(user)) {
                mHasImage = false;
                avatarDrawable.setAvatarType(AvatarDrawable.AVATAR_TYPE_SAVED);
                avatarImage.setImage(null, null, avatarDrawable, null, user, 0);
            } else {
                ImageLocation imageLocation = ImageLocation.getForUser(user, ImageLocation.TYPE_SMALL);
                mHasImage = imageLocation != null;
                avatarImage.setImage(imageLocation, "50_50", avatarDrawable, null, user, 0);
            }
        } else if (chat != null) {
            mTextName.setText(chat.title);

            avatarDrawable.setInfo(chat);
            ImageLocation imageLocation = ImageLocation.getForChat(chat, ImageLocation.TYPE_SMALL);
            mHasImage = imageLocation != null;
            avatarImage.setImage(imageLocation, "50_50", avatarDrawable, null, chat, 0);
        }

        mTextMessage.setText(getMessageText(dialog, chat, encryptedChat, user));
        mTextTime.setText(LocaleController.stringForMessageListDate(dialog.last_message_date));
        if (dialog.unread_count > 0) {
            mTextUnreadCount.setVisibility(VISIBLE);
            mTextUnreadCount.setText(String.valueOf(dialog.unread_count));
        }
        else {
            mTextUnreadCount.setVisibility(INVISIBLE);
        }
    }

    private CharSequence getMessageText(TLRPC.Dialog dialog, TLRPC.Chat chat, TLRPC.EncryptedChat encryptedChat, TLRPC.User user) {
        int currentAccount = UserConfig.selectedAccount;
        long currentDialogId = dialog.id;

        String nameString = "";
        String timeString = "";
        String countString = null;
        String mentionString = null;
        CharSequence messageString = "";
        CharSequence messageNameString = null;
        CharSequence printingString = MessagesController.getInstance(currentAccount).getPrintingString(currentDialogId, 0, true);
        boolean checkMessage = true;

        boolean drawTime = true;
        int printingStringType;

        String messageFormat;
        boolean hasNameInMessage;
        if (Build.VERSION.SDK_INT >= 18) {
            if (!SharedConfig.useThreeLinesLayout) {
                messageFormat = "%2$s: \u2068%1$s\u2069";
                hasNameInMessage = true;
            } else {
                messageFormat = "\u2068%s\u2069";
                hasNameInMessage = false;
            }
        } else {
            if (!SharedConfig.useThreeLinesLayout) {
                messageFormat = "%2$s: %1$s";
                hasNameInMessage = true;
            } else {
                messageFormat = "%1$s";
                hasNameInMessage = false;
            }
        }

        MessageObject message = MessagesController.getInstance(currentAccount).dialogMessage.get(dialog.id);

        int lastDate = 0;
        if (message != null) {
            lastDate = message.messageOwner.date;
        }

        if (printingString != null) {
            printingStringType = MessagesController.getInstance(currentAccount).getPrintingStringType(currentDialogId, 0);
            StatusDrawable statusDrawable = Theme.getChatStatusDrawable(printingStringType);
            int startPadding = 0;
            if (statusDrawable != null) {
                startPadding = statusDrawable.getIntrinsicWidth() + AndroidUtilities.dp(3);
            }
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
            spannableStringBuilder.append(" ").append(TextUtils.replace(printingString, new String[]{"..."}, new String[]{""})).setSpan(new DialogCell.FixedWidthSpan(startPadding), 0, 1, 0);

            messageString = spannableStringBuilder;
            checkMessage = false;
        } else {
            if (message == null) {
                if (encryptedChat != null) {
                    if (encryptedChat instanceof TLRPC.TL_encryptedChatRequested) {
                        messageString = LocaleController.getString("EncryptionProcessing", works.heymate.beta.R.string.EncryptionProcessing);
                    } else if (encryptedChat instanceof TLRPC.TL_encryptedChatWaiting) {
                        messageString = LocaleController.formatString("AwaitingEncryption", works.heymate.beta.R.string.AwaitingEncryption, UserObject.getFirstName(user));
                    } else if (encryptedChat instanceof TLRPC.TL_encryptedChatDiscarded) {
                        messageString = LocaleController.getString("EncryptionRejected", works.heymate.beta.R.string.EncryptionRejected);
                    } else if (encryptedChat instanceof TLRPC.TL_encryptedChat) {
                        if (encryptedChat.admin_id == UserConfig.getInstance(currentAccount).getClientUserId()) {
                            messageString = LocaleController.formatString("EncryptedChatStartedOutgoing", works.heymate.beta.R.string.EncryptedChatStartedOutgoing, UserObject.getFirstName(user));
                        } else {
                            messageString = LocaleController.getString("EncryptedChatStartedIncoming", works.heymate.beta.R.string.EncryptedChatStartedIncoming);
                        }
                    }
                } else {
                    messageString = "";
                }
            } else {
                TLRPC.User fromUser = null;
                TLRPC.Chat fromChat = null;
                int fromId = message.getFromChatId();
                if (fromId > 0) {
                    fromUser = MessagesController.getInstance(currentAccount).getUser(fromId);
                } else {
                    fromChat = MessagesController.getInstance(currentAccount).getChat(-fromId);
                }
                if (message.messageOwner instanceof TLRPC.TL_messageService) {
                    if (ChatObject.isChannel(chat) && (message.messageOwner.action instanceof TLRPC.TL_messageActionHistoryClear ||
                            message.messageOwner.action instanceof TLRPC.TL_messageActionChannelMigrateFrom)) {
                        messageString = "";
                    } else {
                        messageString = message.messageText;
                    }
                } else {
                    boolean needEmoji = true;
                    if (chat != null && chat.id > 0 && fromChat == null && (!ChatObject.isChannel(chat) || ChatObject.isMegagroup(chat))) {
                        if (message.isOutOwner()) {
                            messageNameString = LocaleController.getString("FromYou", works.heymate.beta.R.string.FromYou);
                        } else if (fromUser != null) {
                            if (SharedConfig.useThreeLinesLayout) {
                                if (UserObject.isDeleted(fromUser)) {
                                    messageNameString = LocaleController.getString("HiddenName", works.heymate.beta.R.string.HiddenName);
                                } else {
                                    messageNameString = ContactsController.formatName(fromUser.first_name, fromUser.last_name).replace("\n", "");
                                }
                            } else {
                                messageNameString = UserObject.getFirstName(fromUser).replace("\n", "");
                            }
                        } else if (fromChat != null) {
                            messageNameString = fromChat.title.replace("\n", "");
                        } else {
                            messageNameString = "DELETED";
                        }
                        checkMessage = false;
                        SpannableStringBuilder stringBuilder;
                        if (message.caption != null) {
                            String mess = message.caption.toString();
                            if (mess.length() > 150) {
                                mess = mess.substring(0, 150);
                            }
                            String emoji;
                            if (!needEmoji) {
                                emoji = "";
                            } else if (message.isVideo()) {
                                emoji = "\uD83D\uDCF9 ";
                            } else if (message.isVoice()) {
                                emoji = "\uD83C\uDFA4 ";
                            } else if (message.isMusic()) {
                                emoji = "\uD83C\uDFA7 ";
                            } else if (message.isPhoto()) {
                                emoji = "\uD83D\uDDBC ";
                            } else {
                                emoji = "\uD83D\uDCCE ";
                            }
                            stringBuilder = SpannableStringBuilder.valueOf(String.format(messageFormat, emoji + mess.replace('\n', ' '), messageNameString));
                        } else if (message.messageOwner.media != null && !message.isMediaEmpty()) {
                            String innerMessage;
                            if (message.messageOwner.media instanceof TLRPC.TL_messageMediaPoll) {
                                TLRPC.TL_messageMediaPoll mediaPoll = (TLRPC.TL_messageMediaPoll) message.messageOwner.media;
                                if (Build.VERSION.SDK_INT >= 18) {
                                    innerMessage = String.format("\uD83D\uDCCA \u2068%s\u2069", mediaPoll.poll.question);
                                } else {
                                    innerMessage = String.format("\uD83D\uDCCA %s", mediaPoll.poll.question);
                                }
                            } else if (message.messageOwner.media instanceof TLRPC.TL_messageMediaGame) {
                                if (Build.VERSION.SDK_INT >= 18) {
                                    innerMessage = String.format("\uD83C\uDFAE \u2068%s\u2069", message.messageOwner.media.game.title);
                                } else {
                                    innerMessage = String.format("\uD83C\uDFAE %s", message.messageOwner.media.game.title);
                                }
                            } else if (message.type == 14) {
                                if (Build.VERSION.SDK_INT >= 18) {
                                    innerMessage = String.format("\uD83C\uDFA7 \u2068%s - %s\u2069", message.getMusicAuthor(), message.getMusicTitle());
                                } else {
                                    innerMessage = String.format("\uD83C\uDFA7 %s - %s", message.getMusicAuthor(), message.getMusicTitle());
                                }
                            } else {
                                innerMessage = message.messageText.toString();
                            }
                            innerMessage = innerMessage.replace('\n', ' ');
                            stringBuilder = SpannableStringBuilder.valueOf(String.format(messageFormat, innerMessage, messageNameString));
                            try {
                                stringBuilder.setSpan(new ForegroundColorSpanThemable(Theme.key_chats_attachMessage), hasNameInMessage ? messageNameString.length() + 2 : 0, stringBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            } catch (Exception e) {
                                FileLog.e(e);
                            }
                        } else if (message.messageOwner.message != null) {
                            String mess = message.messageOwner.message;
                            if (message.hasHighlightedWords()) {
                                if (message.messageTrimmedToHighlight != null) {
                                    mess = message.messageTrimmedToHighlight;
                                }
                            } else {
                                if (mess.length() > 150) {
                                    mess = mess.substring(0, 150);
                                }
                                mess = mess.replace('\n', ' ').trim();
                            }
                            stringBuilder = SpannableStringBuilder.valueOf(String.format(messageFormat, mess, messageNameString));
                        } else {
                            stringBuilder = SpannableStringBuilder.valueOf("");
                        }
                        int thumbInsertIndex = 0;
                        if (!SharedConfig.useThreeLinesLayout && stringBuilder.length() > 0) {
                            try {
                                stringBuilder.setSpan(new ForegroundColorSpanThemable(Theme.key_chats_nameMessage), 0, thumbInsertIndex = messageNameString.length() + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            } catch (Exception e) {
                                FileLog.e(e);
                            }
                        }
                        messageString = Emoji.replaceEmoji(stringBuilder, mTextMessage.getPaint().getFontMetricsInt(), AndroidUtilities.dp(20), false);
                        if (message.hasHighlightedWords()) {
                            CharSequence messageH = AndroidUtilities.highlightText(messageString, message.highlightedWords);
                            if (messageH != null) {
                                messageString = messageH;
                            }
                        }
                    } else {
                        if (message.messageOwner.media instanceof TLRPC.TL_messageMediaPhoto && message.messageOwner.media.photo instanceof TLRPC.TL_photoEmpty && message.messageOwner.media.ttl_seconds != 0) {
                            messageString = LocaleController.getString("AttachPhotoExpired", works.heymate.beta.R.string.AttachPhotoExpired);
                        } else if (message.messageOwner.media instanceof TLRPC.TL_messageMediaDocument && message.messageOwner.media.document instanceof TLRPC.TL_documentEmpty && message.messageOwner.media.ttl_seconds != 0) {
                            messageString = LocaleController.getString("AttachVideoExpired", works.heymate.beta.R.string.AttachVideoExpired);
                        } else if (message.caption != null) {
                            String emoji;
                            if (!needEmoji) {
                                emoji = "";
                            } else if (message.isVideo()) {
                                emoji = "\uD83D\uDCF9 ";
                            } else if (message.isVoice()) {
                                emoji = "\uD83C\uDFA4 ";
                            } else if (message.isMusic()) {
                                emoji = "\uD83C\uDFA7 ";
                            } else if (message.isPhoto()) {
                                emoji = "\uD83D\uDDBC ";
                            } else {
                                emoji = "\uD83D\uDCCE ";
                            }
                            if (message.hasHighlightedWords() && !TextUtils.isEmpty(message.messageOwner.message)) {
                                String str = message.messageTrimmedToHighlight;
                                if (message.messageTrimmedToHighlight != null) {
                                    str = message.messageTrimmedToHighlight;
                                }
                                messageString = emoji + str;
                            } else {
                                messageString = emoji + message.caption;
                            }
                        } else {
                            if (message.messageOwner.media instanceof TLRPC.TL_messageMediaPoll) {
                                TLRPC.TL_messageMediaPoll mediaPoll = (TLRPC.TL_messageMediaPoll) message.messageOwner.media;
                                messageString = "\uD83D\uDCCA " + mediaPoll.poll.question;
                            } else if (message.messageOwner.media instanceof TLRPC.TL_messageMediaGame) {
                                messageString = "\uD83C\uDFAE " + message.messageOwner.media.game.title;
                            } else if (message.type == 14) {
                                messageString = String.format("\uD83C\uDFA7 %s - %s", message.getMusicAuthor(), message.getMusicTitle());
                            } else {
                                if (message.hasHighlightedWords() && !TextUtils.isEmpty(message.messageOwner.message)){
                                    messageString = message.messageTrimmedToHighlight;
                                    if (message.messageTrimmedToHighlight != null) {
                                        messageString = message.messageTrimmedToHighlight;
                                    }
                                    int w = getMeasuredWidth() - AndroidUtilities.dp(72 + 23 );
                                    messageString = AndroidUtilities.ellipsizeCenterEnd(messageString, message.highlightedWords.get(0), w, mTextMessage.getPaint(), 130).toString();
                                } else {
                                    messageString = message.messageText;
                                }
                                AndroidUtilities.highlightText(messageString, message.highlightedWords);
                            }
                        }
                    }
                }
            }
        }

        if (message != null) {
            timeString = LocaleController.stringForMessageListDate(message.messageOwner.date);
        }

        int unreadCount = dialog.unread_count;
        int mentionCount = dialog.unread_mentions_count;

        if (message != null) {
            countString = String.format("%d", unreadCount);
            mentionString = "@";
        }

        boolean promoDialog = false;
        MessagesController messagesController = MessagesController.getInstance(currentAccount);
        if (messagesController.isPromoDialog(currentDialogId, true)) {
            promoDialog = true;
            if (messagesController.promoDialogType == MessagesController.PROMO_TYPE_PROXY) {
                timeString = LocaleController.getString("UseProxySponsor", works.heymate.beta.R.string.UseProxySponsor);
            } else if (messagesController.promoDialogType == MessagesController.PROMO_TYPE_PSA) {
                timeString = LocaleController.getString("PsaType_" + messagesController.promoPsaType);
                if (TextUtils.isEmpty(timeString)) {
                    timeString = LocaleController.getString("PsaTypeDefault", works.heymate.beta.R.string.PsaTypeDefault);
                }
                if (!TextUtils.isEmpty(messagesController.promoPsaMessage)) {
                    messageString = messagesController.promoPsaMessage;
                }
            }
        }

        if (chat != null) {
            nameString = chat.title;
        } else if (user != null) {
            if (UserObject.isReplyUser(user)) {
                nameString = LocaleController.getString("RepliesTitle", works.heymate.beta.R.string.RepliesTitle);
            } else if (UserObject.isUserSelf(user)) {
                nameString = LocaleController.getString("FromYou", works.heymate.beta.R.string.FromYou);
            } else {
                nameString = UserObject.getUserName(user);
            }
        }
        if (nameString.length() == 0) {
            nameString = LocaleController.getString("HiddenName", works.heymate.beta.R.string.HiddenName);
        }

        if (checkMessage) {
            if (messageString == null) {
                messageString = "";
            }
            String mess = messageString.toString();
            if (mess.length() > 150) {
                mess = mess.substring(0, 150);
            }
            if (!SharedConfig.useThreeLinesLayout || messageNameString != null) {
                mess = mess.replace('\n', ' ');
            } else {
                mess = mess.replace("\n\n", "\n");
            }
            messageString = Emoji.replaceEmoji(mess, mTextMessage.getPaint().getFontMetricsInt(), AndroidUtilities.dp(17), false);
            if (message != null) {
                CharSequence s = AndroidUtilities.highlightText(messageString, message.highlightedWords);
                if (s != null) {
                    messageString = s;
                }
            }
        }
        if ((SharedConfig.useThreeLinesLayout) && messageNameString != null) {
            try {
                if (message != null && message.hasHighlightedWords()) {
                    CharSequence s = AndroidUtilities.highlightText(messageNameString, message.highlightedWords);
                    if (s != null) {
                        messageNameString = s;
                    }
                }
            } catch (Exception e) {
                FileLog.e(e);
            }
        }

        return messageString;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        avatarImage.setImageCoords(mImageDialog.getLeft(), mImageDialog.getTop(), mImageDialog.getWidth(), mImageDialog.getHeight());
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        if (mHasImage) {
            avatarImage.draw(canvas);
        }

        canvas.drawLine(mTextName.getLeft(), getHeight(), getWidth(), getHeight(), mDividerPaint);
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

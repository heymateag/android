package org.telegram.messenger;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.LongSparseArray;
import android.util.SparseArray;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.ForegroundColorSpanThemable;
import org.telegram.ui.EditWidgetActivity;

import java.io.File;
import java.util.ArrayList;

public class ChatsWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ChatsRemoteViewsFactory(getApplicationContext(), intent);
    }
}

class ChatsRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private ArrayList<Long> dids = new ArrayList<>();
    private Context mContext;
    private int appWidgetId;
    private AccountInstance accountInstance;
    private Paint roundPaint;
    private RectF bitmapRect;
    private LongSparseArray<TLRPC.Dialog> dialogs = new LongSparseArray<>();
    private LongSparseArray<MessageObject> messageObjects = new LongSparseArray<>();
    private boolean deleted;

    public ChatsRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        Theme.createDialogsResources(context);
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        SharedPreferences preferences = context.getSharedPreferences("shortcut_widget", Activity.MODE_PRIVATE);
        int accountId = preferences.getInt("account" + appWidgetId, -1);
        if (accountId >= 0) {
            accountInstance = AccountInstance.getInstance(accountId);
        }
        deleted = preferences.getBoolean("deleted" + appWidgetId, false) || accountInstance == null;
    }

    public void onCreate() {
        ApplicationLoader.postInitApplication();
    }

    public void onDestroy() {

    }

    public int getCount() {
        if (deleted) {
            return 1;
        }
        return dids.size() + 1;
    }

    public RemoteViews getViewAt(int position) {
        if (deleted) {
            RemoteViews rv = new RemoteViews(mContext.getPackageName(), works.heymate.beta.R.layout.widget_deleted);
            rv.setTextViewText(works.heymate.beta.R.id.widget_deleted_text, LocaleController.getString("WidgetLoggedOff", works.heymate.beta.R.string.WidgetLoggedOff));
            return rv;
        } else if (position >= dids.size()) {
            RemoteViews rv = new RemoteViews(mContext.getPackageName(), works.heymate.beta.R.layout.widget_edititem);
            rv.setTextViewText(works.heymate.beta.R.id.widget_edititem_text, LocaleController.getString("TapToEditWidget", works.heymate.beta.R.string.TapToEditWidget));
            Bundle extras = new Bundle();
            extras.putInt("appWidgetId", appWidgetId);
            extras.putInt("appWidgetType", EditWidgetActivity.TYPE_CHATS);
            extras.putInt("currentAccount", accountInstance.getCurrentAccount());
            Intent fillInIntent = new Intent();
            fillInIntent.putExtras(extras);
            rv.setOnClickFillInIntent(works.heymate.beta.R.id.widget_edititem, fillInIntent);
            return rv;
        }
        Long id = dids.get(position);
        String name = "";

        TLRPC.FileLocation photoPath = null;
        TLRPC.User user = null;
        TLRPC.Chat chat = null;
        if (id > 0) {
            user = accountInstance.getMessagesController().getUser((int) (long) id);
            if (user != null) {
                if (UserObject.isUserSelf(user)) {
                    name = LocaleController.getString("SavedMessages", works.heymate.beta.R.string.SavedMessages);
                } else if (UserObject.isReplyUser(user)) {
                    name = LocaleController.getString("RepliesTitle", works.heymate.beta.R.string.RepliesTitle);
                } else if (UserObject.isDeleted(user)) {
                    name = LocaleController.getString("HiddenName", works.heymate.beta.R.string.HiddenName);
                } else {
                    name = ContactsController.formatName(user.first_name, user.last_name);
                }
                if (!UserObject.isReplyUser(user) && !UserObject.isUserSelf(user) && user != null && user.photo != null && user.photo.photo_small != null && user.photo.photo_small.volume_id != 0 && user.photo.photo_small.local_id != 0) {
                    photoPath = user.photo.photo_small;
                }
            }
        } else {
            chat = accountInstance.getMessagesController().getChat(-(int) (long) id);
            if (chat != null) {
                name = chat.title;
                if (chat.photo != null && chat.photo.photo_small != null && chat.photo.photo_small.volume_id != 0 && chat.photo.photo_small.local_id != 0) {
                    photoPath = chat.photo.photo_small;
                }
            }
        }
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), works.heymate.beta.R.layout.shortcut_widget_item);
        rv.setTextViewText(works.heymate.beta.R.id.shortcut_widget_item_text, name);

        try {
            Bitmap bitmap = null;
            if (photoPath != null) {
                File path = FileLoader.getPathToAttach(photoPath, true);
                bitmap = BitmapFactory.decodeFile(path.toString());
            }

            int size = AndroidUtilities.dp(48);
            Bitmap result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            result.eraseColor(Color.TRANSPARENT);
            Canvas canvas = new Canvas(result);
            if (bitmap == null) {
                AvatarDrawable avatarDrawable;
                if (user != null) {
                    avatarDrawable = new AvatarDrawable(user);
                    if (UserObject.isReplyUser(user)) {
                        avatarDrawable.setAvatarType(AvatarDrawable.AVATAR_TYPE_REPLIES);
                    } else if (UserObject.isUserSelf(user)) {
                        avatarDrawable.setAvatarType(AvatarDrawable.AVATAR_TYPE_SAVED);
                    }
                } else {
                    avatarDrawable = new AvatarDrawable(chat);
                }
                avatarDrawable.setBounds(0, 0, size, size);
                avatarDrawable.draw(canvas);
            } else {
                BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                if (roundPaint == null) {
                    roundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                    bitmapRect = new RectF();
                }
                float scale = size / (float) bitmap.getWidth();
                canvas.save();
                canvas.scale(scale, scale);
                roundPaint.setShader(shader);
                bitmapRect.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
                canvas.drawRoundRect(bitmapRect, bitmap.getWidth(), bitmap.getHeight(), roundPaint);
                canvas.restore();
            }
            canvas.setBitmap(null);
            rv.setImageViewBitmap(works.heymate.beta.R.id.shortcut_widget_item_avatar, result);
        } catch (Throwable e) {
            FileLog.e(e);
        }

        MessageObject message = messageObjects.get(id);
        TLRPC.Dialog dialog = dialogs.get(id);
        if (message != null) {
            TLRPC.User fromUser = null;
            TLRPC.Chat fromChat = null;
            int fromId = message.getFromChatId();
            if (fromId > 0) {
                fromUser = accountInstance.getMessagesController().getUser(fromId);
            } else {
                fromChat = accountInstance.getMessagesController().getChat(-fromId);
            }
            CharSequence messageString;
            CharSequence messageNameString;
            int textColor = mContext.getResources().getColor(works.heymate.beta.R.color.widget_text);
            if (message.messageOwner instanceof TLRPC.TL_messageService) {
                if (ChatObject.isChannel(chat) && (message.messageOwner.action instanceof TLRPC.TL_messageActionHistoryClear ||
                        message.messageOwner.action instanceof TLRPC.TL_messageActionChannelMigrateFrom)) {
                    messageString = "";
                } else {
                    messageString = message.messageText;
                }
                textColor = mContext.getResources().getColor(works.heymate.beta.R.color.widget_action_text);
            } else {
                boolean needEmoji = true;
                if (chat != null && chat.id > 0 && fromChat == null && (!ChatObject.isChannel(chat) || ChatObject.isMegagroup(chat))) {
                    if (message.isOutOwner()) {
                        messageNameString = LocaleController.getString("FromYou", works.heymate.beta.R.string.FromYou);
                    } else if (fromUser != null) {
                        messageNameString = UserObject.getFirstName(fromUser).replace("\n", "");
                    } else {
                        messageNameString = "DELETED";
                    }
                    SpannableStringBuilder stringBuilder;
                    String messageFormat = "%2$s: \u2068%1$s\u2069";
                    if (message.caption != null) {
                        String mess = message.caption.toString();
                        if (mess.length() > 150) {
                            mess = mess.substring(0, 150);
                        }
                        String emoji;
                        if (message.isVideo()) {
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
                        textColor = mContext.getResources().getColor(works.heymate.beta.R.color.widget_action_text);
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
                            stringBuilder.setSpan(new ForegroundColorSpanThemable(Theme.key_chats_attachMessage), messageNameString.length() + 2, stringBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        } catch (Exception e) {
                            FileLog.e(e);
                        }
                    } else if (message.messageOwner.message != null) {
                        String mess = message.messageOwner.message;
                        if (mess.length() > 150) {
                            mess = mess.substring(0, 150);
                        }
                        mess = mess.replace('\n', ' ').trim();
                        stringBuilder = SpannableStringBuilder.valueOf(String.format(messageFormat, mess, messageNameString));
                    } else {
                        stringBuilder = SpannableStringBuilder.valueOf("");
                    }
                    try {
                        stringBuilder.setSpan(new ForegroundColorSpanThemable(Theme.key_chats_nameMessage), 0, messageNameString.length() + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    } catch (Exception e) {
                        FileLog.e(e);
                    }
                    messageString = stringBuilder;
                } else {
                    if (message.messageOwner.media instanceof TLRPC.TL_messageMediaPhoto && message.messageOwner.media.photo instanceof TLRPC.TL_photoEmpty && message.messageOwner.media.ttl_seconds != 0) {
                        messageString = LocaleController.getString("AttachPhotoExpired", works.heymate.beta.R.string.AttachPhotoExpired);
                    } else if (message.messageOwner.media instanceof TLRPC.TL_messageMediaDocument && message.messageOwner.media.document instanceof TLRPC.TL_documentEmpty && message.messageOwner.media.ttl_seconds != 0) {
                        messageString = LocaleController.getString("AttachVideoExpired", works.heymate.beta.R.string.AttachVideoExpired);
                    } else if (message.caption != null) {
                        String emoji;
                        if (message.isVideo()) {
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
                        messageString = emoji + message.caption;
                    } else {
                        if (message.messageOwner.media instanceof TLRPC.TL_messageMediaPoll) {
                            TLRPC.TL_messageMediaPoll mediaPoll = (TLRPC.TL_messageMediaPoll) message.messageOwner.media;
                            messageString = "\uD83D\uDCCA " + mediaPoll.poll.question;
                        } else if (message.messageOwner.media instanceof TLRPC.TL_messageMediaGame) {
                            messageString = "\uD83C\uDFAE " + message.messageOwner.media.game.title;
                        } else if (message.type == 14) {
                            messageString = String.format("\uD83C\uDFA7 %s - %s", message.getMusicAuthor(), message.getMusicTitle());
                        } else {
                            messageString = message.messageText;
                            AndroidUtilities.highlightText(messageString, message.highlightedWords);
                        }
                        if (message.messageOwner.media != null && !message.isMediaEmpty()) {
                            textColor = mContext.getResources().getColor(works.heymate.beta.R.color.widget_action_text);
                        }
                    }
                }
            }

            rv.setTextViewText(works.heymate.beta.R.id.shortcut_widget_item_time, LocaleController.stringForMessageListDate(message.messageOwner.date));
            rv.setTextViewText(works.heymate.beta.R.id.shortcut_widget_item_message, messageString.toString());
            rv.setTextColor(works.heymate.beta.R.id.shortcut_widget_item_message, textColor);
        } else {
            if (dialog != null && dialog.last_message_date != 0) {
                rv.setTextViewText(works.heymate.beta.R.id.shortcut_widget_item_time, LocaleController.stringForMessageListDate(dialog.last_message_date));
            } else {
                rv.setTextViewText(works.heymate.beta.R.id.shortcut_widget_item_time, "");
            }
            rv.setTextViewText(works.heymate.beta.R.id.shortcut_widget_item_message, "");
        }
        if (dialog != null && dialog.unread_count > 0) {
            rv.setTextViewText(works.heymate.beta.R.id.shortcut_widget_item_badge, String.format("%d", dialog.unread_count));
            rv.setViewVisibility(works.heymate.beta.R.id.shortcut_widget_item_badge, View.VISIBLE);
            if (accountInstance.getMessagesController().isDialogMuted(dialog.id)) {
                rv.setInt(works.heymate.beta.R.id.shortcut_widget_item_badge, "setBackgroundResource", works.heymate.beta.R.drawable.widget_counter_muted);
            } else {
                rv.setInt(works.heymate.beta.R.id.shortcut_widget_item_badge, "setBackgroundResource", works.heymate.beta.R.drawable.widget_counter);
            }
        } else {
            rv.setViewVisibility(works.heymate.beta.R.id.shortcut_widget_item_badge, View.GONE);
        }

        Bundle extras = new Bundle();

        if (id > 0) {
            extras.putInt("userId", (int) (long) id);
        } else {
            extras.putInt("chatId", -(int) (long) id);
        }
        extras.putInt("currentAccount", accountInstance.getCurrentAccount());

        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);
        rv.setOnClickFillInIntent(works.heymate.beta.R.id.shortcut_widget_item, fillInIntent);

        rv.setViewVisibility(works.heymate.beta.R.id.shortcut_widget_item_divider, position == getCount() ? View.GONE : View.VISIBLE);

        return rv;
    }

    public RemoteViews getLoadingView() {
        return null;
    }

    public int getViewTypeCount() {
        return 2;
    }

    public long getItemId(int position) {
        return position;
    }

    public boolean hasStableIds() {
        return true;
    }

    public void onDataSetChanged() {
        dids.clear();
        messageObjects.clear();
        if (accountInstance == null || !accountInstance.getUserConfig().isClientActivated()) {
            return;
        }
        ArrayList<TLRPC.User> users = new ArrayList<>();
        ArrayList<TLRPC.Chat> chats = new ArrayList<>();
        LongSparseArray<TLRPC.Message> messages = new LongSparseArray<>();
        accountInstance.getMessagesStorage().getWidgetDialogs(appWidgetId, 0, dids, dialogs, messages, users, chats);
        accountInstance.getMessagesController().putUsers(users, true);
        accountInstance.getMessagesController().putChats(chats, true);
        messageObjects.clear();
        for (int a = 0, N = messages.size(); a < N; a++) {
            MessageObject messageObject = new MessageObject(accountInstance.getCurrentAccount(), messages.valueAt(a), (SparseArray<TLRPC.User>) null, null, false, true);
            messageObjects.put(messages.keyAt(a), messageObject);
        }
    }
}
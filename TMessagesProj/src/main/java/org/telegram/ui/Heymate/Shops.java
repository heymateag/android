package org.telegram.ui.Heymate;

import android.content.Context;

import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Heymate.AmplifyModels.Shop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import works.heymate.core.HeymateEvents;
import works.heymate.core.Utils;

public class Shops {

    private static final List<Long> OLD_SHOP_IDS = Arrays.asList(348289536L, 541980570L, 596896146L);;

    public static ArrayList<TLRPC.Dialog> dialogs = new ArrayList<>();

    private static boolean lastTimeFailed = false;

    public static void reloadShops(Context context, int currentAccount) {
        HtAmplify.getInstance(context).getShops((success, shops, exception) -> {
            if (success) {
                MessagesController messagesController = MessagesController.getInstance(currentAccount);

                ArrayList<TLRPC.Dialog> dialogList = new ArrayList<>((shops == null ? 0 : shops.size()) + OLD_SHOP_IDS.size());

                List<Integer> dialogsToLoad = new ArrayList<>();

                for (long id: OLD_SHOP_IDS) {
                    TLRPC.Dialog dialog = messagesController.dialogs_dict.get(-id);

                    if (dialog != null) {
                        dialogList.add(dialog);
                    }
                }

                if (shops != null) {
                    for (Shop shop: shops) {
                        int id = shop.getTgId();

                        TLRPC.Dialog dialog = messagesController.dialogs_dict.get(-id);

                        if (dialog != null) {
                            dialogList.add(dialog);
                        }
                        else {
                            dialogsToLoad.add(-id);
                        }
                    }
                }

                if (dialogsToLoad.isEmpty()) {
                    updateDialogs(dialogList);
                    return;
                }

                TLRPC.TL_messages_getPeerDialogs req = new TLRPC.TL_messages_getPeerDialogs();

                for (int id: dialogsToLoad) {
                    TLRPC.TL_inputDialogPeer peer = new TLRPC.TL_inputDialogPeer();
                    peer.peer = messagesController.getInputPeer(id);
                    req.peers.add(peer);
                }

                ConnectionsManager.getInstance(currentAccount).sendRequest(req, (response, error) -> Utils.runOnUIThread(() -> {
                    boolean failed = true;

                    if (response instanceof TLRPC.TL_messages_peerDialogs) {
                        failed = false;

                        TLRPC.TL_messages_peerDialogs peerDialogs = (TLRPC.TL_messages_peerDialogs) response;

                        TLRPC.messages_Dialogs dialogsRes = new TLRPC.TL_messages_dialogs();
                        dialogsRes.chats = peerDialogs.chats;
                        dialogsRes.count = peerDialogs.dialogs.size();
                        dialogsRes.dialogs = peerDialogs.dialogs;
                        dialogsRes.messages = peerDialogs.messages;
                        dialogsRes.users = peerDialogs.users;

                        messagesController.processLoadedDialogs(dialogsRes, null, 0, -1, 0, 0, false, true, false);

                        MessagesStorage messagesStorage = MessagesStorage.getInstance(currentAccount);
                        messagesStorage.putDialogs(dialogsRes, 1);

                        dialogList.addAll(peerDialogs.dialogs);
                    }

                    if (!failed || lastTimeFailed) {
                        updateDialogs(dialogList);
                    }

                    lastTimeFailed = failed;
                }));
            }
        });
    }

    private static void updateDialogs(ArrayList<TLRPC.Dialog> dialogList) {
        Collections.sort(dialogList, (o1, o2) -> o2.last_message_date - o1.last_message_date);

        if (dialogList.size() == dialogs.size()) {
            boolean identical = true;

            for (int i = 0; i < dialogList.size(); i++) {
                if (dialogList.get(i).id != dialogs.get(i).id) {
                    identical = false;
                    break;
                }
            }

            if (identical) {
                return;
            }
        }

        dialogs = dialogList;
        HeymateEvents.notify(HeymateEvents.SHOPS_UPDATED, dialogs);
    }

}

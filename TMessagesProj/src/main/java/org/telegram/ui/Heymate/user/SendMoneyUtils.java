package org.telegram.ui.Heymate.user;

import org.web3j.utils.Numeric;

import works.heymate.api.APIObject;
import works.heymate.core.Money;
import works.heymate.core.Texts;
import works.heymate.model.User;
import works.heymate.model.Users;
import works.heymate.util.DefaultObjectBuilder;
import works.heymate.util.DefaultObjectProvider;
import works.heymate.util.Template;

public class SendMoneyUtils {

    public static final String SENDER_ID = "sender_id";
    public static final String RECEIVER_ID = "receiverId";
    public static final String MONEY = "money";
    public static final String RECEIVER_WALLET = "address";
    public static final String MESSAGE = "message";
    public static final String URL = "url";

    public static String serialize(APIObject receiver, Money money, String walletAddress, String message, String transactionHash) {
        APIObject sender = Users.currentUser;

        return Template.parse(Texts.get("sentmoney_phrase").toString()).apply(new DefaultObjectProvider() {
            @Override
            protected Object getRootObject(String name) {
                switch (name) {
                    case "user":
                        return receiver;
                    case MONEY:
                        return money;
                    case RECEIVER_WALLET:
                        return walletAddress;
                    case MESSAGE:
                        return message;
                    case URL:
                        return "https://explorer.celo.org/tx/" + transactionHash + "/token-transfers?_s=" + sender.getString(User.ID) + "&_r=" + receiver.getString(User.ID);
                }

                return null;
            }
        });
    }

    public static APIObject parseMessage(String message) {
        DefaultObjectBuilder objectBuilder = new DefaultObjectBuilder();

        Template.parse(Texts.get("sentmoney_phrase").toString()).build(message, objectBuilder);

        APIObject parsedMessage = new APIObject(objectBuilder.getJSON());

        if (Money.create(parsedMessage.getString(MONEY)) == null) {
            return null;
        }

        String walletAddress = parsedMessage.getString(RECEIVER_WALLET);

        if (walletAddress == null || !walletAddress.startsWith("0x")) {
            return null;
        }

        try {
            Numeric.toBigInt(walletAddress);
        } catch (Throwable t) {
            return null;
        }

        String rawURL = parsedMessage.getString(URL);

        if (rawURL == null) {
            return null;
        }

        String senderId = getParameter(rawURL, "_s=");
        String receiverId = getParameter(rawURL, "_r=");

        if (senderId == null || receiverId == null) {
            return null;
        }

        int urlEnd = rawURL.indexOf("?");

        parsedMessage.set(SENDER_ID, senderId);
        parsedMessage.set(RECEIVER_ID, receiverId);
        parsedMessage.set(URL, rawURL.substring(0, urlEnd));

        return parsedMessage;
    }

    private static String getParameter(String url, String parameterName) {
        int start = url.lastIndexOf(parameterName);

        if (start == -1) {
            return null;
        }

        start += parameterName.length();

        int end = url.indexOf("&", start);

        if (end == -1) {
            end = url.length();
        }

        return url.substring(start, end);
    }

}

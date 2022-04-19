package org.telegram.ui.Heymate.user;

import org.web3j.utils.Numeric;

import java.util.UUID;

import works.heymate.api.APIObject;
import works.heymate.core.Money;
import works.heymate.core.Texts;
import works.heymate.model.User;
import works.heymate.model.Users;
import works.heymate.util.DefaultObjectBuilder;
import works.heymate.util.DefaultObjectProvider;
import works.heymate.util.Template;

public class PaymentRequestUtils {

    public static final String USER_ID = "user_id";
    public static final String MONEY = "money";
    public static final String WALLET = "wallet";
    public static final String MESSAGE = "message";
    public static final String URL = "url";

    public static String serialize(APIObject user, Money money, String walletAddress, String message) {
        return Template.parse(Texts.get("invoice_phrase").toString()).apply(new DefaultObjectProvider() {
            @Override
            protected Object getRootObject(String name) {
                switch (name) {
                    case "user":
                        return user;
                    case MONEY:
                        return money;
                    case WALLET:
                        return walletAddress;
                    case MESSAGE:
                        return message;
                    case URL:
                        return "https://heymate.works/invoice/" + user.getString(User.ID);
                }

                return null;
            }
        });
    }

    public static APIObject parseMessage(String message) {
        DefaultObjectBuilder objectBuilder = new DefaultObjectBuilder();

        Template.parse(Texts.get("invoice_phrase").toString()).build(message, objectBuilder);

        APIObject parsedMessage = new APIObject(objectBuilder.getJSON());

        if (Money.create(parsedMessage.getString(MONEY)) == null) {
            return null;
        }

        String walletAddress = parsedMessage.getString(WALLET);

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

        int userIdIndex = rawURL.lastIndexOf("/");

        if (userIdIndex == -1) {
            return null;
        }

        String userId = rawURL.substring(userIdIndex + 1).trim();

        if (userId.isEmpty()) {
            return null;
        }

        parsedMessage.set(USER_ID, userId);
        parsedMessage.set(URL, rawURL);

        return parsedMessage;
    }

}

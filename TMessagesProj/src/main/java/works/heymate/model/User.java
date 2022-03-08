package works.heymate.model;

public interface User {

    interface Device {

        String ID = "deviceUUID";
        String TYPE = "deviceType";
        String NAME = "deviceName";
        String WALLET_ADDRESS = "walletAddress";
        String CURRENCY = "currency";

    }

    String DEVICE_ID = "deviceId";
    String ID = "userId";
    String USERNAME = "userName";
    String TELEGRAM_ID = "telegramId";
    String FULL_NAME = "fullName";
    String AVATAR_HASH = "avatarHash";
    String DEVICES = "devices";

}

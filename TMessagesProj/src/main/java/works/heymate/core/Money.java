package works.heymate.core;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

public class Money implements Parcelable, Comparable<Money> {

    private static final String AMOUNT = "amount";
    private static final String CURRENCY = "currency";

    public static Money create(long cents, Currency currency) {
        Money money = new Money();
        money.mAmount = cents;
        money.mCurrency = currency;
        return money;
    }

    public static Money create(JSONObject jMoney) throws JSONException {
        Money money = new Money();
        money.mAmount = jMoney.getInt(AMOUNT);
        money.mCurrency = Currency.forName(jMoney.getString(CURRENCY));
        return money;
    }

    private Currency mCurrency;
    private long mAmount;

    private Money() {

    }

    protected Money(Parcel in) {
        mCurrency = Currency.forName(in.readString());
        mAmount = in.readLong();
    }

    public Currency getCurrency() {
        return mCurrency;
    }

    public long getCents() {
        return mAmount;
    }

    public void setCents(long cents) {
        mAmount = cents;
    }

    public Money plus(Money money) {
        return Money.create(mAmount + money.mAmount, mCurrency);
    }

    public Money plus(long cents) {
        return Money.create(mAmount + cents, mCurrency);
    }

    public Money minus(Money money) {
        return Money.create(mAmount - money.mAmount, mCurrency);
    }

    public Money multiplyBy(float multiplicand) {
        return Money.create((long) (mAmount * multiplicand), mCurrency);
    }

    public JSONObject asJSON() {
        JSONObject jMoney = new JSONObject();

        try {
            jMoney.put(AMOUNT, mAmount);
            jMoney.put(CURRENCY, mCurrency.name());
        } catch (JSONException e) { }

        return jMoney;
    }

    @Override
    public String toString() {
        if (mAmount % 100 != 0) {
            String cents = String.valueOf(mAmount % 100);

            return mCurrency.format(mAmount / 100 + "." + (cents.length() == 1 ? "0" + cents : cents));
        }
        else {
            return mCurrency.format(String.valueOf(mAmount / 100));
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mCurrency.name());
        dest.writeLong(mAmount);
    }

    public static final Creator<Money> CREATOR = new Creator<Money>() {
        @Override
        public Money createFromParcel(Parcel in) {
            return new Money(in);
        }

        @Override
        public Money[] newArray(int size) {
            return new Money[size];
        }
    };

    @Override
    public int compareTo(Money o) {
        if (!mCurrency.equals(o.mCurrency)) {
            throw new IllegalArgumentException("Moneys should be of the same currency.");
        }

        return (int) (mAmount - o.mAmount);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Money)) {
            return false;
        }

        Money oMoney = (Money) o;

        return mAmount == oMoney.mAmount && mCurrency.equals(oMoney.mCurrency);
    }

    @Override
    public int hashCode() {
        int result = mCurrency.hashCode();
        result = 31 * result + (int) (mAmount ^ (mAmount >>> 32));
        return result;
    }

}

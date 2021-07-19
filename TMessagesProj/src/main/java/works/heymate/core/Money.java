package works.heymate.core;

import android.os.Parcel;
import android.os.Parcelable;

public class Money implements Parcelable {

    public static Money create(int cents, Currency currency) {
        Money money = new Money();
        money.mCents = cents;
        money.mCurrency = currency;
        return money;
    }

    private Currency mCurrency;
    private int mCents;

    public Money() {

    }

    protected Money(Parcel in) {
        mCurrency = Currency.valueOf(in.readString());
        mCents = in.readInt();
    }

    public Currency getCurrency() {
        return mCurrency;
    }

    public int getCents() {
        return mCents;
    }

    public Money plus(Money money) {
        return Money.create(mCents + money.mCents, mCurrency);
    }

    public Money minus(Money money) {
        return Money.create(mCents - money.mCents, mCurrency);
    }

    public Money multiplyBy(float multiplicand) {
        return Money.create((int) (mCents * multiplicand), mCurrency);
    }

    @Override
    public String toString() {
        return mCents / 100 + "." + mCents % 100 + " " + mCurrency;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mCurrency.name());
        dest.writeInt(mCents);
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

}

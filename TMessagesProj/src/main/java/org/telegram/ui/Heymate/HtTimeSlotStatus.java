package org.telegram.ui.Heymate;

public enum HtTimeSlotStatus {
    AVAILABLE,
    BOOKED,
    MARKED_AS_STARTED,
    STARTED,
    MARKED_AS_FINISHED,
    FINISHED,
    CANCELLED_BY_SERVICE_PROVIDER,
    CANCELLED_BY_CONSUMER;

    public int getStateIndex() {
        switch (this) {
            case AVAILABLE: case CANCELLED_BY_CONSUMER: return 0;
            case BOOKED: return 1;
            case MARKED_AS_STARTED: return 2;
            case STARTED: return 3;
            case MARKED_AS_FINISHED: return 4;
            case FINISHED: return 5;
            case CANCELLED_BY_SERVICE_PROVIDER: return 6;
        }

        return -1;
    }

    public boolean happensAfter(HtTimeSlotStatus that) {
        return this.getStateIndex() > that.getStateIndex();
    }

}

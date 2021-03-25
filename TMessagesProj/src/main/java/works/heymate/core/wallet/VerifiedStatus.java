package works.heymate.core.wallet;

public class VerifiedStatus {

    public final boolean verified;
    public final int completedAttestations;
    public final int totalAttestations;
    public final int remainingAttestations;

    public VerifiedStatus(boolean verified, int completed, int total, int remaining) {
        this.verified = verified;
        this.completedAttestations = completed;
        this.totalAttestations = total;
        this.remainingAttestations = remaining;
    }

}

package in.siteurl.www.vendorloyalty.Model;

/**
 * Created by siteurl on 5/1/18.
 */

public class ApprovedClaim {

    private String mName;
    private String mPurchasedAmount;
    private String mApprovedOn;
    private String mPoints;

    public ApprovedClaim(String mName, String mPurchasedAmount, String mApprovedOn, String mPoints) {
        this.mName = mName;
        this.mPurchasedAmount = mPurchasedAmount;
        this.mApprovedOn = mApprovedOn;
        this.mPoints = mPoints;
    }

    public String getmName() {
        return mName;
    }

    public String getmPurchasedAmount() {
        return mPurchasedAmount;
    }

    public String getmApprovedOn() {
        return mApprovedOn;
    }

    public String getmPoints() {
        return mPoints;
    }
}

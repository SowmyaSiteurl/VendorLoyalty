package in.siteurl.www.vendorloyalty.Model;

/**
 * Created by siteurl on 6/1/18.
 */

public class AcceptDecline {

    private String mName;
    private String mPointsEarnedId;
    private String mDescription;
    private String mAmount;
    private String mProductImage;

    public AcceptDecline(String mName, String mPointsEarnedId, String mDescription, String mAmount, String mProductImage) {
        this.mName = mName;
        this.mPointsEarnedId = mPointsEarnedId;
        this.mDescription = mDescription;
        this.mAmount = mAmount;
        this.mProductImage = mProductImage;

    }

    public String getmName() {
        return mName;
    }

    public String getmPointsEarnedId() {
        return mPointsEarnedId;
    }

    public String getmDescription() {
        return mDescription;
    }

    public String getmAmount() {
        return mAmount;
    }

    public String getmProductImage() {
        return mProductImage;
    }



}

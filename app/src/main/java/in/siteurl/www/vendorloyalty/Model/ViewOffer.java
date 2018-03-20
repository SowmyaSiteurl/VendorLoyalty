package in.siteurl.www.vendorloyalty.Model;

import java.io.Serializable;

/**
 * Created by siteurl on 18/12/17.
 */

public class ViewOffer implements Serializable {

    private String mOfferid , mVendorid, mOffername,mOfferdescription,
            mOfferimage,mUpdatedate,mExpirydate,mOfferprice,mTermsAndCondtion,mStatus;

    public ViewOffer(String mOfferid, String mVendorid, String mOffername, String mOfferdescription, String mOfferimage, String mUpdatedate, String mExpirydate, String mOfferprice, String mTermsAndCondtion, String mStatus) {
        this.mOfferid = mOfferid;
        this.mVendorid = mVendorid;
        this.mOffername = mOffername;
        this.mOfferdescription = mOfferdescription;
        this.mOfferimage = mOfferimage;
        this.mUpdatedate = mUpdatedate;
        this.mExpirydate = mExpirydate;
        this.mOfferprice = mOfferprice;
        this.mTermsAndCondtion = mTermsAndCondtion;
        this.mStatus = mStatus;
    }

    public String getmOfferid() {
        return mOfferid;
    }

    public void setmOfferid(String mOfferid) {
        this.mOfferid = mOfferid;
    }

    public String getmVendorid() {
        return mVendorid;
    }

    public void setmVendorid(String mVendorid) {
        this.mVendorid = mVendorid;
    }

    public String getmOffername() {
        return mOffername;
    }

    public void setmOffername(String mOffername) {
        this.mOffername = mOffername;
    }

    public String getmOfferdescription() {
        return mOfferdescription;
    }

    public void setmOfferdescription(String mOfferdescription) {
        this.mOfferdescription = mOfferdescription;
    }

    public String getmOfferimage() {
        return mOfferimage;
    }

    public void setmOfferimage(String mOfferimage) {
        this.mOfferimage = mOfferimage;
    }

    public String getmUpdatedate() {
        return mUpdatedate;
    }

    public void setmUpdatedate(String mUpdatedate) {
        this.mUpdatedate = mUpdatedate;
    }

    public String getmExpirydate() {
        return mExpirydate;
    }

    public void setmExpirydate(String mExpirydate) {
        this.mExpirydate = mExpirydate;
    }

    public String getmOfferprice() {
        return mOfferprice;
    }

    public void setmOfferprice(String mOfferprice) {
        this.mOfferprice = mOfferprice;
    }

    public String getmTermsAndCondtion() {
        return mTermsAndCondtion;
    }

    public void setmTermsAndCondtion(String mTermsAndCondtion) {
        this.mTermsAndCondtion = mTermsAndCondtion;
    }

    public String getmStatus() {
        return mStatus;
    }

    public void setmStatus(String mStatus) {
        this.mStatus = mStatus;
    }

    @Override
    public String toString() {
        return "ViewOffer{" +
                "mOfferid='" + mOfferid + '\'' +
                ", mVendorid='" + mVendorid + '\'' +
                ", mOffername='" + mOffername + '\'' +
                ", mOfferdescription='" + mOfferdescription + '\'' +
                ", mOfferimage='" + mOfferimage + '\'' +
                ", mUpdatedate='" + mUpdatedate + '\'' +
                ", mExpirydate='" + mExpirydate + '\'' +
                ", mOfferprice='" + mOfferprice + '\'' +
                ", mTermsAndCondtion='" + mTermsAndCondtion + '\'' +
                ", mStatus='" + mStatus + '\'' +
                '}';
    }
}

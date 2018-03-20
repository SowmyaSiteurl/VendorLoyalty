package in.siteurl.www.vendorloyalty.Model;

import java.io.Serializable;

/**
 * Created by siteurl on 2/1/18.
 */

public class VendorProfile implements Serializable {

    private String mName;
    private String mPhone;
    private String mEmail;
    private String mLatLong;
    private String mVendorShopImage;
    private String mAddress;
    private String mCategoryId;

    public VendorProfile(String mName, String mPhone, String mEmail, String mLatLong, String mVendorShopImage, String mAddress,String mCategoryId) {
        this.mName = mName;
        this.mPhone = mPhone;
        this.mEmail = mEmail;
        this.mLatLong = mLatLong;
        this.mVendorShopImage = mVendorShopImage;
        this.mAddress = mAddress;
        this.mCategoryId = mCategoryId;
    }

    public String getmName() {
        return mName;
    }

    public String getmPhone() {
        return mPhone;
    }

    public String getmEmail() {
        return mEmail;
    }

    public String getmLatLong() {
        return mLatLong;
    }

    public String getmVendorShopImage() {
        return mVendorShopImage;
    }

    public String getmAddress() {
        return mAddress;
    }

    public String getmCategoryId() {
        return mCategoryId;
    }
}

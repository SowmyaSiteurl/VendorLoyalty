package in.siteurl.www.vendorloyalty.Model;

import java.io.Serializable;

/**
 * Created by siteurl on 30/12/17.
 */

public class Transactions implements Serializable{

    private String mOpeningBalance;
    private String mClosingBalance;
    private String mStatus;
    private String mName;
    private String mDate;
    private String mApprovedAmount;
    private String mPurchaseAmount;
    private String prodImage;
    private String prodDesc;
    private  String purchaseAmount;

    public Transactions(String mClosingBalance, String mName, String mDate, String mApprovedAmount, String mPurchaseAmount) {
        this.mClosingBalance = mClosingBalance;
        this.mName = mName;
        this.mDate = mDate;
        this.mApprovedAmount = mApprovedAmount;
        this.mPurchaseAmount = mPurchaseAmount;
    }

    public Transactions(String mClosingBalance, String mName, String mDate, String mApprovedAmount, String mPurchaseAmount,String prodImage, String prodDesc ,String purchaseAmount) {
        this.mClosingBalance = mClosingBalance;
        this.mName = mName;
        this.mDate = mDate;
        this.mApprovedAmount = mApprovedAmount;
        this.mPurchaseAmount = mPurchaseAmount;
        this.prodDesc = prodDesc;
        this.prodImage = prodImage;
        this.purchaseAmount = purchaseAmount;
    }


    public String getmOpeningBalance() {
        return mOpeningBalance;
    }

    public void setmOpeningBalance(String mOpeningBalance) {
        this.mOpeningBalance = mOpeningBalance;
    }

    public String getmClosingBalance() {
        return mClosingBalance;
    }

    public void setmClosingBalance(String mClosingBalance) {
        this.mClosingBalance = mClosingBalance;
    }

    public String getmStatus() {
        return mStatus;
    }

    public void setmStatus(String mStatus) {
        this.mStatus = mStatus;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public String getmDate() {
        return mDate;
    }

    public void setmDate(String mDate) {
        this.mDate = mDate;
    }

    public String getmApprovedAmount() {
        return mApprovedAmount;
    }

    public void setmApprovedAmount(String mApprovedAmount) {
        this.mApprovedAmount = mApprovedAmount;
    }

    public String getmPurchaseAmount() {
        return mPurchaseAmount;
    }

    public void setmPurchaseAmount(String mPurchaseAmount) {
        this.mPurchaseAmount = mPurchaseAmount;
    }

    public String getProdImage() {
        return prodImage;
    }

    public void setProdImage(String prodImage) {
        this.prodImage = prodImage;
    }

    public String getProdDesc() {
        return prodDesc;
    }

    public void setProdDesc(String prodDesc) {
        this.prodDesc = prodDesc;
    }

    public String getPurchaseAmount() {
        return purchaseAmount;
    }

    public void setPurchaseAmount(String purchaseAmount) {
        this.purchaseAmount = purchaseAmount;
    }

    @Override
    public String toString() {
        return "Transactions{" +
                "mOpeningBalance='" + mOpeningBalance + '\'' +
                ", mClosingBalance='" + mClosingBalance + '\'' +
                ", mStatus='" + mStatus + '\'' +
                ", mName='" + mName + '\'' +
                ", mDate='" + mDate + '\'' +
                ", mApprovedAmount='" + mApprovedAmount + '\'' +
                ", mPurchaseAmount='" + mPurchaseAmount + '\'' +
                ", prodImage='" + prodImage + '\'' +
                ", prodDesc='" + prodDesc + '\'' +
                ", purchaseAmount='" + purchaseAmount + '\'' +
                '}';
    }
}

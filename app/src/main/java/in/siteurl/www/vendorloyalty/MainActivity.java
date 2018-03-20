package in.siteurl.www.vendorloyalty;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import in.siteurl.www.vendorloyalty.Helper.UpdateHelper;
import in.siteurl.www.vendorloyalty.Model.ViewOffer;
import in.siteurl.www.vendorloyalty.Services.VendorJobCreator;
import in.siteurl.www.vendorloyalty.Services.VendorNotificationServices;
import in.siteurl.www.vendorloyalty.Services.VendorServices;
import me.tatarka.support.job.JobInfo;
import me.tatarka.support.job.JobScheduler;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, UpdateHelper.onUpdateCheckListener {


    private DrawerLayout mMainDrawer;
    private ActionBarDrawerToggle mToogle;
    private NavigationView mNavigationView;
    private Toolbar mToolbar;
    SharedPreferences loginpref;
    SharedPreferences.Editor editor;
    JobScheduler jobScheduler;
    private ImageView mLogout;
    String sessionid, uid;
    private TextView mSalesAmount, mAwardedPoints, mBalancePoints;
    private TextView mProductName, mProductAmount, mProductTerms, mProductDesc, mProductStatus, mProductExpiry;
    ImageView mProductImage;
    ImageView mEndingSoon;
    private ViewOffer mExpiryOffer;
    private LinearLayout mExpiryOfferLayout;
    TextView noOffers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        //Intialize
        mMainDrawer = findViewById(R.id.maindrawer);
        mNavigationView = findViewById(R.id.nv_main);
        mToogle = new ActionBarDrawerToggle(this, mMainDrawer, R.string.open, R.string.close);
        mMainDrawer.addDrawerListener(mToogle);
        mToogle.syncState();
        mNavigationView.setNavigationItemSelectedListener(this);
        loginpref = getApplicationContext().getSharedPreferences("LoginPref", MODE_PRIVATE);
        editor = loginpref.edit();
        mSalesAmount = findViewById(R.id.tv_salesamount);
        mAwardedPoints = findViewById(R.id.tv_awardedpoints);
        mBalancePoints = findViewById(R.id.tv_balancepoints);

        mProductName = findViewById(R.id.tv_productname);
        mProductDesc = findViewById(R.id.tv_description);
        mProductAmount = findViewById(R.id.tv_productprice);
        mProductExpiry = findViewById(R.id.tv_expiry);
        mProductStatus = findViewById(R.id.tv_status);
        mProductTerms = findViewById(R.id.tv_termsandcondition);
        mProductImage = findViewById(R.id.iv_productimage);
        mEndingSoon = findViewById(R.id.iv_endingsoon);
        mExpiryOfferLayout = findViewById(R.id.expiryofferlayout);
        noOffers = findViewById(R.id.vendorNoOffers);


        mToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mMainDrawer.addDrawerListener(mToogle);
        mToogle.syncState();

        mLogout = mToolbar.findViewById(R.id.iv_logout);
        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logoutvendor();
            }
        });


        UpdateHelper.with(this)
                .onUpdateCheck(this)
                .check();

        jobScheduler = JobScheduler.getInstance(this);
        VendorNotificationServices.schedulePeriodic();
        //constructjob();
        // mToogle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.colorAccent));

        loginpref = getApplicationContext().getSharedPreferences("LoginPref", MODE_PRIVATE);
        sessionid = loginpref.getString("sessionid", null);
        uid = loginpref.getString("User-id", null);
        getVendorDashboardDetails();

    }

    private void getVendorDashboardDetails() {

        final AlertDialog loadingDialog = new SpotsDialog(MainActivity.this, R.style.Loading);
        loadingDialog.show();

        StringRequest dashboardrequest = new StringRequest(Request.Method.POST, API.vendordashboard,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loadingDialog.dismiss();

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String responseError = jsonObject.getString("Error");
                            String responsemessage = jsonObject.getString("Message");

                            if (responseError.equals("false")) {
                                JSONObject data = jsonObject.getJSONObject("data");

                                String salesamount = data.getString("sales_amount");
                                String awardedpoints = data.getString("awarded_points");
                                String balancepoints = data.getString("balance_points");
                                String expiryOffers = data.getString("expirying_offer");

                                mSalesAmount.setText(salesamount + " Rs");
                                mAwardedPoints.setText(awardedpoints + " Pts");
                                mBalancePoints.setText(balancepoints + " Pts");

                                if (!expiryOffers.equals("No offers found")) {

                                    JSONObject expiryobject = data.getJSONObject("expirying_offer");
                                    String offername = expiryobject.getString("offer_name");
                                    String offerdesc = expiryobject.getString("offer_description");
                                    String offerimage = expiryobject.getString("offer_image");
                                    String offerprice = expiryobject.getString("offer_price");
                                    String offerexpiry = expiryobject.getString("expiry_date");
                                    String offerterms = expiryobject.getString("terms_and_condtion");
                                    String status = expiryobject.getString("status");

                                    String offer_id = expiryobject.getString("offer_id");
                                    String vendor_id = expiryobject.getString("vendor_id");
                                    String offer_name = expiryobject.getString("offer_name");
                                    String offer_description = expiryobject.getString("offer_description");
                                    String offer_image = expiryobject.getString("offer_image");
                                    String offer_price = expiryobject.getString("offer_price");
                                    String updated_at = expiryobject.getString("updated_at");
                                    String expiry_date = expiryobject.getString("expiry_date");
                                    String terms_and_condtion = expiryobject.getString("terms_and_condtion");

                                    mExpiryOffer = new ViewOffer(offer_id, vendor_id, offer_name,
                                            offer_description, offer_image, updated_at, expiry_date, offer_price,
                                            terms_and_condtion, status);

                                    mProductName.setText(offername);
                                    mProductDesc.setText(offerdesc);
                                    mProductAmount.setText("₹ " + offerprice);
                                    mProductExpiry.setText(offerexpiry);
                                    mProductTerms.setText(offerterms);
                                    mProductStatus.setText(status);

                                    Glide.with(MainActivity.this).load(offerimage).into(mProductImage);
                                    Glide.with(MainActivity.this).load(R.drawable.ending).into(mEndingSoon);

                                } else {

                                    mExpiryOfferLayout.setVisibility(View.GONE);
                                    noOffers.setVisibility(View.VISIBLE);
                                }

                            } else {
                                Snackbar.make(mMainDrawer, responsemessage, Snackbar.LENGTH_SHORT)
                                        .show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loadingDialog.dismiss();
                if (error.networkResponse != null) {
                    parseVolleyError(error);
                }
                mSalesAmount.setText("0 Rs");
                mAwardedPoints.setText("0 Pts");
                mBalancePoints.setText("0 Pts");
                mExpiryOfferLayout.setVisibility(View.GONE);
                if (error instanceof ServerError) {
                    //Toast.makeText(MainActivity.this, "Server is under maintenance.Please try later.", Toast.LENGTH_LONG).show();
                    Log.d("Error", String.valueOf(error instanceof ServerError));
                    error.printStackTrace();
                } else if (error instanceof AuthFailureError) {
                    Toast.makeText(MainActivity.this, "Authentication Error", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Authentication Error");
                    error.printStackTrace();
                } else if (error instanceof ParseError) {
                    Toast.makeText(MainActivity.this, "Parse Error", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Parse Error");
                    error.printStackTrace();
                } else if (error instanceof NoConnectionError) {
                    Toast.makeText(MainActivity.this, "Server is under maintenance.Please try later.", Toast.LENGTH_LONG).show();
                    Log.d("Error", "No Connection Error");
                    error.printStackTrace();
                } else if (error instanceof NetworkError) {
                    Toast.makeText(MainActivity.this, "Please check your connection.", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Network Error");
                    error.printStackTrace();
                } else if (error instanceof TimeoutError) {
                    Toast.makeText(MainActivity.this, "Timeout Error", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Timeout Error");
                    error.printStackTrace();
                } else {
                    Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
                    error.printStackTrace();
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new Hashtable<String, String>();
                params.put("user_id", uid);
                params.put("sid", sessionid);
                params.put("api_key", API.APIKEY);
                return params;
            }
        };
        dashboardrequest.setRetryPolicy(new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        LoyaltySingleton.getInstance(getApplicationContext()).addtorequestqueue(dashboardrequest);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();

        if (mToogle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int itemid = item.getItemId();

        if (itemid == R.id.offers_menu) {
            startActivity(new Intent(MainActivity.this, OffersActivity.class));
        }
        if (itemid == R.id.qrcode_menu) {
            startActivity(new Intent(MainActivity.this, QRCodeActivity.class));
        }

        if (itemid == R.id.transactions_menu) {
            startActivity(new Intent(MainActivity.this, TransactionHistoryActivity.class));

        }
        if (itemid == R.id.changepassword_menu) {
            startActivity(new Intent(MainActivity.this, ChangePasswordActivity.class));
        }
        if (itemid == R.id.profile_menu) {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        }
        if (itemid == R.id.logout_menu) {
            logoutvendor();
        }

        if (itemid == R.id.claimrequest_menu) {
            startActivity(new Intent(MainActivity.this, ClaimRequestActivity.class).
                    setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        }
        mMainDrawer.closeDrawers();
        return false;
    }


    public void gotoeditoffer(View view) {
        Intent editIntent = new Intent(MainActivity.this, AddorEditOfferActivity.class);
        editIntent.putExtra("editoffer", mExpiryOffer);
        startActivity(editIntent);
    }

    private void logoutvendor() {
        editor.putString("loginname", "");
        editor.putString("loginemail", "");
        editor.putString("role", "");
        editor.putString("sessionid", "");
        editor.putString("User-id", "");
        editor.putString("user_group_id", "");
        editor.putString("hash", "");
        editor.commit();
        startActivity(new Intent(MainActivity.this, LoginActivity.class).
                setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }


    //Handling Volley Error
    public void parseVolleyError(VolleyError error) {
        try {
            String responseBody = new String(error.networkResponse.data, "utf-8");
            JSONObject data = new JSONObject(responseBody);
            String message = data.getString("Message");
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
            android.app.AlertDialog.Builder loginErrorBuilder = new android.app.AlertDialog.Builder(MainActivity.this);
            loginErrorBuilder.setTitle("Error");
            loginErrorBuilder.setMessage(message);
            loginErrorBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            loginErrorBuilder.show();
        } catch (JSONException e) {
        } catch (UnsupportedEncodingException errorr) {
        }
    }


    @Override
    public void onUpdateCheckListener(final String urlApp) {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("New Version Available")
                .setMessage("Please Update to New Version to Continue Use")
                .setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();

                        Toast.makeText(MainActivity.this, "Update", Toast.LENGTH_SHORT).show();

                        String appPackageName = getPackageName();

                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                        }

                    }
                }).setNegativeButton("Remind Me Later!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        Toast.makeText(MainActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
                    }
                }).create();

        alertDialog.show();
    }
}

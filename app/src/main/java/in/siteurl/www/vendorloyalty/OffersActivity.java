package in.siteurl.www.vendorloyalty;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import in.siteurl.www.vendorloyalty.Adapters.ClaimRequestPagerAdapter;
import in.siteurl.www.vendorloyalty.Adapters.ViewOfferAdapter;
import in.siteurl.www.vendorloyalty.App.VendorApplication;
import in.siteurl.www.vendorloyalty.Fragments.ApprovedClaimsFragment;
import in.siteurl.www.vendorloyalty.Fragments.ClaimRequestFragment;
import in.siteurl.www.vendorloyalty.Model.ViewOffer;
import in.siteurl.www.vendorloyalty.Services.ConnectivityReceiver;

public class OffersActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener {

    private Toolbar mOfferToolbar;
    private ArrayList<ViewOffer> mViewOfferList;
    private ViewOfferAdapter mViewOfferAdapter;
    private ListView mOffersListView;
    SharedPreferences loginpref;
    String sessionid, uid;
    private android.support.v7.widget.SearchView mSearchOffer;
    Dialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offers);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        //To get login details from preferences
        loginpref = getApplicationContext().getSharedPreferences("LoginPref", MODE_PRIVATE);
        sessionid = loginpref.getString("sessionid", null);
        uid = loginpref.getString("User-id", null);

        //Toolbar
        mOfferToolbar = findViewById(R.id.offerstoolbar);
        setSupportActionBar(mOfferToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mViewOfferList = new ArrayList<>();

        mOffersListView = findViewById(R.id.lv_offers);


        alertDialog = new Dialog(this);

        checkConnection();

    }


    private void checkConnection() {
        boolean isConnected = ConnectivityReceiver.isConnected();
        showSnack(isConnected);
    }

    // Showing the status in Snackbar
    private void showSnack(boolean isConnected) {
        if (isConnected) {
            //Method To get all products
            getallofferedproducts();
            if (alertDialog.isShowing()) {
                alertDialog.dismiss();
            }
        } else {
            shownointernetdialog();
        }
    }

    //To show no internet dialog
    private void shownointernetdialog() {
        //alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.setContentView(R.layout.nointernet);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.setCancelable(false);
        Button retry = alertDialog.findViewById(R.id.exit_btn);
        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                //checkConnection();
                System.exit(0);
            }
        });
        alertDialog.show();
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        showSnack(isConnected);
    }

    @Override
    protected void onResume() {
        super.onResume();
        VendorApplication.getInstance().setConnectivityListener(this);
    }

    //Get all products
    private void getallofferedproducts() {
        final AlertDialog loadingDialog = new SpotsDialog(OffersActivity.this, R.style.Loading);
        loadingDialog.show();
        StringRequest viewoffersrequest = new StringRequest(Request.Method.POST, API.vendoroffers,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            loadingDialog.dismiss();
                            JSONObject responsefromserver = new JSONObject(response);
                            String response_error = responsefromserver.getString("Error");
                            if (response_error.equals("false")) {
                                JSONObject data = responsefromserver.getJSONObject("data");
                                JSONArray offers = data.getJSONArray("offers");
                                if (offers.length() > 0) {
                                    //rawvendorlist.clear();
                                    mViewOfferList.clear();
                                    for (int i = 0; i < offers.length(); i++) {
                                        JSONObject data_object = offers.getJSONObject(i);
                                        String offer_id = data_object.getString("offer_id");
                                        String vendor_id = data_object.getString("vendor_id");
                                        String offer_name = data_object.getString("offer_name");
                                        String offer_description = data_object.getString("offer_description");
                                        String offer_image = data_object.getString("offer_image");
                                        String offer_price = data_object.getString("offer_price");
                                        String updated_at = data_object.getString("updated_at");
                                        String expiry_date = data_object.getString("expiry_date");
                                        String terms_and_condtion = data_object.getString("terms_and_condtion");
                                        String status = data_object.getString("status");

                                        //rawvendorlist.add(data_object);
                                        mViewOfferList.add(new ViewOffer(offer_id, vendor_id, offer_name,
                                                offer_description, offer_image, updated_at, expiry_date, offer_price,
                                                terms_and_condtion, status));
                                    }
                                    mViewOfferAdapter = new ViewOfferAdapter(getApplicationContext(), R.layout.vendor_offers, mViewOfferList);
                                    mOffersListView.setAdapter(mViewOfferAdapter);

                                    //enables filtering for the contents of the given ListView
                                    mOffersListView.setTextFilterEnabled(true);

                                    mOffersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                        }
                                    });

                                    mSearchOffer = findViewById(R.id.sv_offers);
                                    mSearchOffer.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
                                        @Override
                                        public boolean onQueryTextSubmit(String query) {
                                            return false;
                                        }

                                        @Override
                                        public boolean onQueryTextChange(String newText) {
                                            mViewOfferAdapter.getFilter().filter(newText.toString());
                                            return true;
                                        }
                                    });

                                } else {

                                    Toast.makeText(OffersActivity.this, ":( Looks Like No Offers To Show", Toast.LENGTH_LONG).show();
                                }
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
                if (error instanceof ServerError) {
                    Toast.makeText(OffersActivity.this, "Server is under maintenance.Please try later.", Toast.LENGTH_LONG).show();
                    Log.d("Error", String.valueOf(error instanceof ServerError));
                    error.printStackTrace();
                } else if (error instanceof AuthFailureError) {
                    Toast.makeText(OffersActivity.this, "Authentication Error", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Authentication Error");
                    error.printStackTrace();
                } else if (error instanceof ParseError) {
                    Toast.makeText(OffersActivity.this, "Parse Error", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Parse Error");
                    error.printStackTrace();
                } else if (error instanceof NoConnectionError) {
                    Toast.makeText(OffersActivity.this, "Server is under maintenance.Please try later.", Toast.LENGTH_LONG).show();
                    Log.d("Error", "No Connection Error");
                    error.printStackTrace();
                } else if (error instanceof NetworkError) {
                    Toast.makeText(OffersActivity.this, "Please check your connection.", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Network Error");
                    error.printStackTrace();
                } else if (error instanceof TimeoutError) {
                    Toast.makeText(OffersActivity.this, "Timeout Error", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Timeout Error");
                    error.printStackTrace();
                } else {
                    Toast.makeText(OffersActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
                    error.printStackTrace();
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new Hashtable<String, String>();
                params.put("user_id", uid);
                params.put("vendor_id", uid);
                params.put("sid", sessionid);
                params.put("api_key", API.APIKEY);
                return params;
            }
        };
        viewoffersrequest.setRetryPolicy(new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        LoyaltySingleton.getInstance(getApplicationContext()).addtorequestqueue(viewoffersrequest);
    }

    //Handling Volley Error
    public void parseVolleyError(VolleyError error) {
        try {
            String responseBody = new String(error.networkResponse.data, "utf-8");
            JSONObject data = new JSONObject(responseBody);
            String message = data.getString("Message");
            Toast.makeText(OffersActivity.this, message, Toast.LENGTH_LONG).show();
            android.app.AlertDialog.Builder loginErrorBuilder = new android.app.AlertDialog.Builder(OffersActivity.this);
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

    public void gotoaddoffer(View view) {
        startActivity(new Intent(OffersActivity.this, AddorEditOfferActivity.class));
    }

}

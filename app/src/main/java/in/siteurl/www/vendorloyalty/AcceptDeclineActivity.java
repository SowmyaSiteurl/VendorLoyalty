package in.siteurl.www.vendorloyalty;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.WindowManager;
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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import in.siteurl.www.vendorloyalty.Adapters.AcceptDeclineAdapter;
import in.siteurl.www.vendorloyalty.Model.AcceptDecline;
import in.siteurl.www.vendorloyalty.Services.VendorServices;

public class AcceptDeclineActivity extends AppCompatActivity {


    SharedPreferences loginpref;
    String uid,sessionid;
    ArrayList<AcceptDecline> mAcceptDeclineArrayList;
    ListView mAcceptDeclineList;
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accept_decline);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        loginpref = getApplicationContext().getSharedPreferences("LoginPref", MODE_PRIVATE);
        uid = loginpref.getString("User-id", null);
        sessionid = loginpref.getString("sessionid", null);
        mAcceptDeclineArrayList = new ArrayList<>();
        mAcceptDeclineList = findViewById(R.id.lv_acceptdecline);
        mToolbar = findViewById(R.id.accepedeclinetoolbar);
        mToolbar.setTitle("Accept Claims");
        setSupportActionBar(mToolbar);
        getnewclaimsfromserver();
    }

    private void getnewclaimsfromserver() {
        StringRequest newclaimsrequest = new StringRequest(Request.Method.POST, API.notifyvendorCliam, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject responsefromserver = new JSONObject(response);
                    String Error = responsefromserver.getString("Error");
                    String Message = responsefromserver.getString("Message");
                    if (Error.equals("false")) {
                        String data = responsefromserver.getString("data");
                        JSONArray checkdataarray = new JSONArray(data);

                        if (checkdataarray.length() > 0) {
                            mAcceptDeclineArrayList.clear();
                            for (int i = 0; i < checkdataarray.length(); i++) {
                                JSONObject dataobject = checkdataarray.getJSONObject(i);
                                String status = dataobject.getString("status");
                                if (status.equals("New") || status.equals("new")) {

                                    String description = dataobject.getString("description");
                                    String purchaseamount = dataobject.getString("purchase_amount");
                                    String pointsearnedid = dataobject.getString("points_earned_id");
                                    JSONObject userdetails = dataobject.getJSONObject("user_details");
                                    String name = userdetails.getString("name");
                                    String productImage = dataobject.getString("prod_img");

                                    AcceptDecline acceptDecline = new AcceptDecline(name, pointsearnedid, description, purchaseamount,productImage);
                                    mAcceptDeclineArrayList.add(acceptDecline);
                                    sendstatusasseentoserver(pointsearnedid);
                                }
                            }
                            AcceptDeclineAdapter acceptDeclineAdapter = new AcceptDeclineAdapter(AcceptDeclineActivity.this,R.layout.acceptdecline_item,mAcceptDeclineArrayList);
                            mAcceptDeclineList.setAdapter(acceptDeclineAdapter);
                        }
                    } else {
                        Toast.makeText(AcceptDeclineActivity.this, Message, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (error.networkResponse != null) {
                    //parseVolleyError(error);
                }
                if (error instanceof ServerError) {
                    Toast.makeText(AcceptDeclineActivity.this, "Server is under maintenance.Please try later.", Toast.LENGTH_LONG).show();
                    Log.d("Error", String.valueOf(error instanceof ServerError));
                    error.printStackTrace();
                } else if (error instanceof AuthFailureError) {
                    Toast.makeText(AcceptDeclineActivity.this, "Authentication Error", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Authentication Error");
                    error.printStackTrace();
                } else if (error instanceof ParseError) {
                    Toast.makeText(AcceptDeclineActivity.this, "Parse Error", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Parse Error");
                    error.printStackTrace();
                } else if (error instanceof NoConnectionError) {
                    Toast.makeText(AcceptDeclineActivity.this, "Server is under maintenance.Please try later.", Toast.LENGTH_LONG).show();
                    Log.d("Error", "No Connection Error");
                    error.printStackTrace();
                } else if (error instanceof NetworkError) {
                    Toast.makeText(AcceptDeclineActivity.this, "Please check your connection.", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Network Error");
                    error.printStackTrace();
                } else if (error instanceof TimeoutError) {
                    Toast.makeText(AcceptDeclineActivity.this, "Timeout Error", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Timeout Error");
                    error.printStackTrace();
                } else {
                    Toast.makeText(AcceptDeclineActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
                    error.printStackTrace();
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new Hashtable<String, String>();
                params.put("vendor_id", uid);
                params.put("api_key", API.APIKEY);
                return params;
            }
        };
        newclaimsrequest.setRetryPolicy(new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        LoyaltySingleton.getInstance(getApplicationContext()).addtorequestqueue(newclaimsrequest);

    }

    private void sendstatusasseentoserver(final String pointsearnedid) {

        StringRequest statusupdate_request = new StringRequest(Request.Method.POST,
                API.updatestatus,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String responseerror = jsonObject.getString("Error");
                            String message  = jsonObject.getString("Message");
                            if(responseerror.equals("false"))
                            {

                            }
                            else
                            {
                                Toast.makeText(AcceptDeclineActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (error.networkResponse != null) {
                    //parseVolleyError(error);
                }
                if (error instanceof ServerError) {
                    Toast.makeText(AcceptDeclineActivity.this, "Server is under maintenance.Please try later.", Toast.LENGTH_LONG).show();
                    Log.d("Error", String.valueOf(error instanceof ServerError));
                    error.printStackTrace();
                } else if (error instanceof AuthFailureError) {
                    Toast.makeText(AcceptDeclineActivity.this, "Authentication Error", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Authentication Error");
                    error.printStackTrace();
                } else if (error instanceof ParseError) {
                    Toast.makeText(AcceptDeclineActivity.this, "Parse Error", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Parse Error");
                    error.printStackTrace();
                } else if (error instanceof NoConnectionError) {
                    Toast.makeText(AcceptDeclineActivity.this, "Server is under maintenance.Please try later.", Toast.LENGTH_LONG).show();
                    Log.d("Error", "No Connection Error");
                    error.printStackTrace();
                } else if (error instanceof NetworkError) {
                    Toast.makeText(AcceptDeclineActivity.this, "Please check your connection.", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Network Error");
                    error.printStackTrace();
                } else if (error instanceof TimeoutError) {
                    Toast.makeText(AcceptDeclineActivity.this, "Timeout Error", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Timeout Error");
                    error.printStackTrace();
                } else {
                    Toast.makeText(AcceptDeclineActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
                    error.printStackTrace();
                }
            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new Hashtable<String, String>();
                params.put("user_id", uid);
                params.put("sid", sessionid);
                params.put("points_earned_id", pointsearnedid);
                params.put("api_key",API.APIKEY);
                params.put("status","Seen");

                return params;
            }
        };
        statusupdate_request.setRetryPolicy(new DefaultRetryPolicy(30000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        LoyaltySingleton.getInstance(getApplicationContext()).addtorequestqueue(statusupdate_request);

    }
}

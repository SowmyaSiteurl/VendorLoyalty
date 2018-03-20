package in.siteurl.www.vendorloyalty;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import in.siteurl.www.vendorloyalty.Adapters.TransactionsAdapter;
import in.siteurl.www.vendorloyalty.App.VendorApplication;
import in.siteurl.www.vendorloyalty.Model.Transactions;
import in.siteurl.www.vendorloyalty.Services.ConnectivityReceiver;

public class TransactionHistoryActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener {

    private ArrayList<String> stringArrayList;
    private RecyclerView mTransactions;
    private TransactionsAdapter adapter;
    private CollapsingToolbarLayout mCollapseTBLayout;
    private ImageView mPointsImage;
    private AppBarLayout mAppBar;
    private TextView mBalance, mBalanceTitle;
    SharedPreferences loginpref;
    String sessionid, uid;
    private ArrayList<Transactions> mTransactionList;
    Dialog alertDialog;
    String name, message;
    Transactions transactions;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        //Intialzing views
        mAppBar = findViewById(R.id.transactionappbarlayout);
        mCollapseTBLayout = findViewById(R.id.collapse_toolbar);
        mBalance = findViewById(R.id.tv_balance);
        mPointsImage = findViewById(R.id.iv_pointsimage);
        mBalanceTitle = findViewById(R.id.tv_balancetitle);
        loginpref = getApplicationContext().getSharedPreferences("LoginPref", MODE_PRIVATE);
        sessionid = loginpref.getString("sessionid", null);
        uid = loginpref.getString("User-id", null);

        //adding off set change listener to hide and show the balance
        mAppBar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                    mBalance.setVisibility(View.VISIBLE);
                    mBalanceTitle.setVisibility(View.VISIBLE);
                    mPointsImage.setVisibility(View.VISIBLE);
                }
                if (scrollRange + verticalOffset == 0) {
                    mCollapseTBLayout.setTitle("History");
                    isShow = true;
                    mBalance.setVisibility(View.GONE);
                    mBalanceTitle.setVisibility(View.GONE);
                    mPointsImage.setVisibility(View.GONE);
                } else if (isShow) {
                    mCollapseTBLayout.setTitle(" ");//carefull there should a space between double quote otherwise it wont work
                    isShow = false;
                    mBalance.setVisibility(View.VISIBLE);
                    mBalanceTitle.setVisibility(View.VISIBLE);
                    mPointsImage.setVisibility(View.VISIBLE);
                }
            }
        });

        mTransactions = findViewById(R.id.rv_transactions);
        mTransactions.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mTransactions.setLayoutManager(layoutManager);

        setData(); //adding data to array list
        adapter = new TransactionsAdapter(this, stringArrayList);
        mTransactions.setAdapter(adapter);

        mTransactionList = new ArrayList<>();

        alertDialog = new Dialog(this);
        checkConnection();
        //getallvendortransactions();
    }

    //To check connection
    private void checkConnection() {
        boolean isConnected = ConnectivityReceiver.isConnected();
        showSnack(isConnected);
    }

    // Showing the status in Snackbar
    private void showSnack(boolean isConnected) {
        if (isConnected) {
            //Method to get vendor transation details
            getallvendortransactions();
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


    //To get all vendor transactions
    private void getallvendortransactions() {

        final AlertDialog loadingDialog = new SpotsDialog(TransactionHistoryActivity.this, R.style.Loading);
        loadingDialog.show();
        StringRequest vendortransactionrequest = new StringRequest(Request.Method.POST, API.allvendortransaction, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                loadingDialog.dismiss();

                try {
                    JSONObject responsefromserver = new JSONObject(response);
                    String error = responsefromserver.getString("Error");
                    String message = responsefromserver.getString("Message");
                    if (error.equals("false")) {
                        JSONArray data = responsefromserver.getJSONArray("data");
                        if (data.length() > 0) {
                            //getallvendortransactiondetails(data);
                            getalltransactiondetails(data);

                        } else {
                            Toast.makeText(TransactionHistoryActivity.this, "No Transactions Found", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(TransactionHistoryActivity.this, message, Toast.LENGTH_LONG).show();
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
                    Toast.makeText(TransactionHistoryActivity.this, "Server is under maintenance.Please try later.", Toast.LENGTH_LONG).show();
                    Log.d("Error", String.valueOf(error instanceof ServerError));
                    error.printStackTrace();
                } else if (error instanceof AuthFailureError) {
                    Toast.makeText(TransactionHistoryActivity.this, "Authentication Error", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Authentication Error");
                    error.printStackTrace();
                } else if (error instanceof ParseError) {
                    Toast.makeText(TransactionHistoryActivity.this, "Parse Error", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Parse Error");
                    error.printStackTrace();
                } else if (error instanceof NoConnectionError) {
                    Toast.makeText(TransactionHistoryActivity.this, "Server is under maintenance.Please try later.", Toast.LENGTH_LONG).show();
                    Log.d("Error", "No Connection Error");
                    error.printStackTrace();
                } else if (error instanceof NetworkError) {
                    Toast.makeText(TransactionHistoryActivity.this, "Please check your connection.", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Network Error");
                    error.printStackTrace();
                } else if (error instanceof TimeoutError) {
                    Toast.makeText(TransactionHistoryActivity.this, "Timeout Error", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Timeout Error");
                    error.printStackTrace();
                } else {
                    Toast.makeText(TransactionHistoryActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
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
        vendortransactionrequest.setRetryPolicy(new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        LoyaltySingleton.getInstance(getApplicationContext()).addtorequestqueue(vendortransactionrequest);
    }

    private void getalltransactiondetails(JSONArray data) {

        JSONObject transaction;
        for (int i = 0; i < data.length(); i++) {
            try {
                transaction = data.getJSONObject(i);
                String userid = transaction.getString("user_id");
                if (!userid.equals("0")) {
                    JSONObject buyer = transaction.getJSONObject("buyer_details");
                    String name = buyer.getString("name");
                    JSONObject purchaseamount = transaction.getJSONObject("purchase_amount");
                    String amount = purchaseamount.getString("purchase_amount");
                    String prodImg = purchaseamount.getString("prod_img");
                    String prodDescription = purchaseamount.getString("description");
                    String purchaseAmount = purchaseamount.getString("purchase_amount");
                    String pointsawarded = transaction.getString("converted_amount_approved");
                    String closingbalance = transaction.getString("closing_balance");
                    String createddate = transaction.getString("created_at");
                    name = " Points awarded to " + name;
                    mBalance.setText(closingbalance);
                    transactions = new Transactions(closingbalance, name, createddate, pointsawarded, amount, prodImg, prodDescription, purchaseAmount);
                    mTransactionList.add(transactions);
                } else {
                    String name = "You";
                    JSONObject paymentdetails = transaction.getJSONObject("payment_details");
                    String amount = paymentdetails.getString("amount");
                    //String pointsawarded = transaction.getString("converted_amount_approved");
                    String pointsawarded = amount;
                    String closingbalance = transaction.getString("closing_balance");
                    //String createddate = transaction.getString("created_at");
                    String createddate = paymentdetails.getString("paymentdate");
                    name = "Points purchased for Rs." + amount;
                    mBalance.setText(closingbalance);
                    transactions = new Transactions(closingbalance, name, createddate, pointsawarded, amount);
                    mTransactionList.add(transactions);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        final TransactionsAdapter transactionsAdapter = new TransactionsAdapter(TransactionHistoryActivity.this, mTransactionList);
        mTransactions.setAdapter(transactionsAdapter);

        searchView = findViewById(R.id.purchaseSearch);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                transactionsAdapter.getFilter().filter(s.toString());
                return true;
            }
        });

    }

    /*private void getallvendortransactiondetails(JSONArray data) {

        JSONObject transaction;
        for (int i = 0; i < data.length(); i++) {
            try {
                transaction = data.getJSONObject(i);
                String openingbalance = transaction.getString("opening_balance");
                String closingbalance = transaction.getString("closing_balance");
                String createddate = transaction.getString("created_at");
                String status = transaction.getString("status");
                String approvedamount = transaction.getString("converted_amount_approved");
                String userid = transaction.getString("user_id");
                if (!userid.equals("0")) {
                    JSONObject buyer = transaction.getJSONObject("buyer_details");
                    if (buyer.has("name")) {
                        name = buyer.getString("name");
                        transactions = new Transactions(openingbalance, closingbalance, status, name, createddate, approvedamount);
                    }
                    if (buyer.has("message")) {
                        name = buyer.getString("message");
                        transactions = new Transactions(openingbalance, closingbalance, status, name, createddate, approvedamount);
                    }
                }
                else
                {
                    JSONObject vendorpayment = transaction.getJSONObject("payment_details");
                }
                mBalance.setText( closingbalance);
                mTransactionList.add(transactions);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        TransactionsAdapter transactionsAdapter = new TransactionsAdapter(TransactionHistoryActivity.this, mTransactionList);
        mTransactions.setAdapter(transactionsAdapter);
    }*/

    //Handling Volley Error
    public void parseVolleyError(VolleyError error) {
        try {
            String responseBody = new String(error.networkResponse.data, "utf-8");
            JSONObject data = new JSONObject(responseBody);
            String message = data.getString("Message");
            Toast.makeText(TransactionHistoryActivity.this, message, Toast.LENGTH_LONG).show();
            android.app.AlertDialog.Builder loginErrorBuilder = new android.app.AlertDialog.Builder(TransactionHistoryActivity.this);
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

    private void setData() {
        stringArrayList = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            stringArrayList.add("Item " + (i + 1));
        }
    }


}

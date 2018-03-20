package in.siteurl.www.vendorloyalty.Adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import in.siteurl.www.vendorloyalty.API;
import in.siteurl.www.vendorloyalty.AcceptDeclineActivity;
import in.siteurl.www.vendorloyalty.ClaimRequestActivity;
import in.siteurl.www.vendorloyalty.LoyaltySingleton;
import in.siteurl.www.vendorloyalty.Model.AcceptDecline;
import in.siteurl.www.vendorloyalty.Model.ApprovedClaim;
import in.siteurl.www.vendorloyalty.ProfileActivity;
import in.siteurl.www.vendorloyalty.R;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by siteurl on 6/1/18.
 */

public class AcceptDeclineAdapter extends ArrayAdapter<AcceptDecline> {

    private ArrayList<AcceptDecline> mAccepetDeclineList;
    private ArrayList<AcceptDecline> mFilteredList;
    SharedPreferences loginpref;
    String sessionid, uid;
    View finalview;
    RequestOptions imageoptions = new RequestOptions();


    public AcceptDeclineAdapter(@NonNull Context context, int textViewResourceId, @NonNull ArrayList<AcceptDecline> acceptDeclines) {
        super(context, textViewResourceId, acceptDeclines);
        this.mFilteredList = new ArrayList<AcceptDecline>();
        this.mFilteredList.addAll(acceptDeclines);
        this.mAccepetDeclineList = new ArrayList<AcceptDecline>();
        this.mAccepetDeclineList.addAll(acceptDeclines);
        loginpref = getContext().getSharedPreferences("LoginPref", MODE_PRIVATE);
        sessionid = loginpref.getString("sessionid", null);
        uid = loginpref.getString("User-id", null);
        imageoptions.diskCacheStrategy(DiskCacheStrategy.ALL);
        imageoptions.fitCenter();
        imageoptions.placeholder(R.drawable.header);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.acceptdecline_item, parent, false);
        }

        final AcceptDecline acceptDecline = mAccepetDeclineList.get(position);

        TextView buyername = convertView.findViewById(R.id.tv_buyername);
        String name = acceptDecline.getmName();
        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        buyername.setText(name);

        TextView purchasedamount = convertView.findViewById(R.id.tv_amount);
        purchasedamount.setText(acceptDecline.getmAmount() + " Rs");

        TextView description = convertView.findViewById(R.id.tv_description);
        description.setText("Desc : " + acceptDecline.getmDescription());


        Button acceptclaim = convertView.findViewById(R.id.btn_accept);
        acceptclaim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                acceptclaimrequest(acceptDecline.getmPointsEarnedId());
            }
        });

        Button declineclaim = convertView.findViewById(R.id.btn_decline);
        declineclaim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showdeclinemessagedialog(acceptDecline.getmPointsEarnedId());
                //declineclaimrequest(acceptDecline.getmPointsEarnedId());
            }
        });

        ImageView product = convertView.findViewById(R.id.iv_productImage);
        Glide.with(getContext()).load(acceptDecline.getmProductImage())
                .thumbnail(0.5f)
                .apply(imageoptions)
                .into(product);

        finalview = convertView;
        return convertView;
    }

    private void showdeclinemessagedialog(final String pointsearnedid) {

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(finalview.getRootView().getContext());
        builder.setTitle("Decline");
        builder.setMessage("Reason for Decline");
        LayoutInflater li = LayoutInflater.from(getContext());
        View promptsView = li.inflate(R.layout.declinemessage, null);
        final MaterialEditText declineInput = promptsView
                .findViewById(R.id.edtDeclineMessage);
        builder.setView(promptsView);
        builder.setPositiveButton("Decline", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String declinemessage = declineInput.getText().toString();
                declineclaimrequest(pointsearnedid, declinemessage);
            }
        });
        builder.setCancelable(false);
        builder.show();

    }

    private void declineclaimrequest(final String pointsearnedid, final String message) {

        final AlertDialog loadingDialog = new SpotsDialog(getContext(), R.style.Loading);
        loadingDialog.show();

        StringRequest statusupdate_request = new StringRequest(Request.Method.POST, API.declinecliam,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loadingDialog.dismiss();
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String responseerror = jsonObject.getString("Error");
                            if (responseerror.equals("false")) {
                                String responsemessage = jsonObject.getString("Message");
                                Intent statusupdate = new Intent(getContext(), ClaimRequestActivity.class);
                                statusupdate.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                statusupdate.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                getContext().startActivity(statusupdate);
                                ((Activity) getContext()).finish();
                                notifyDataSetChanged();
                                Toast.makeText(getContext(), "Accepted " + responsemessage, Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getContext(), "Server is under maintenance.Please try later.", Toast.LENGTH_LONG).show();
                    Log.d("Error", String.valueOf(error instanceof ServerError));
                    error.printStackTrace();
                } else if (error instanceof AuthFailureError) {
                    Toast.makeText(getContext(), "Authentication Error", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Authentication Error");
                    error.printStackTrace();
                } else if (error instanceof ParseError) {
                    Toast.makeText(getContext(), "Parse Error", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Parse Error");
                    error.printStackTrace();
                } else if (error instanceof NoConnectionError) {
                    Toast.makeText(getContext(), "Server is under maintenance.Please try later.", Toast.LENGTH_LONG).show();
                    Log.d("Error", "No Connection Error");
                    error.printStackTrace();
                } else if (error instanceof NetworkError) {
                    Toast.makeText(getContext(), "Please check your connection.", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Network Error");
                    error.printStackTrace();
                } else if (error instanceof TimeoutError) {
                    Toast.makeText(getContext(), "Timeout Error", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Timeout Error");
                    error.printStackTrace();
                } else {
                    Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_LONG).show();
                    error.printStackTrace();
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new Hashtable<String, String>();
                params.put("user_id", uid);
                params.put("sid", sessionid);
                params.put("points_earned_id", pointsearnedid);
                params.put("api_key", API.APIKEY);
                params.put("message", message);

                return params;
            }
        };

        statusupdate_request.setRetryPolicy(new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        LoyaltySingleton.getInstance(getContext()).addtorequestqueue(statusupdate_request);


    }

    private void acceptclaimrequest(final String pointsearnedid) {

        final AlertDialog loadingDialog = new SpotsDialog(getContext(), R.style.Loading);
        loadingDialog.show();

        StringRequest statusupdate_request = new StringRequest(Request.Method.POST, API.approvepoints,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loadingDialog.dismiss();
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String responseerror = jsonObject.getString("Error");
                            if (responseerror.equals("false")) {
                                String responsemessage = jsonObject.getString("Message");
                                Intent statusupdate = new Intent(getContext(), ClaimRequestActivity.class);
                                statusupdate.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                statusupdate.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                getContext().startActivity(statusupdate);
                                ((Activity) getContext()).finish();
                                notifyDataSetChanged();
                                Toast.makeText(getContext(), "Accepted " + responsemessage, Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getContext(), "Server is under maintenance.Please try later.", Toast.LENGTH_LONG).show();
                    Log.d("Error", String.valueOf(error instanceof ServerError));
                    error.printStackTrace();
                } else if (error instanceof AuthFailureError) {
                    Toast.makeText(getContext(), "Authentication Error", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Authentication Error");
                    error.printStackTrace();
                } else if (error instanceof ParseError) {
                    Toast.makeText(getContext(), "Parse Error", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Parse Error");
                    error.printStackTrace();
                } else if (error instanceof NoConnectionError) {
                    Toast.makeText(getContext(), "Server is under maintenance.Please try later.", Toast.LENGTH_LONG).show();
                    Log.d("Error", "No Connection Error");
                    error.printStackTrace();
                } else if (error instanceof NetworkError) {
                    Toast.makeText(getContext(), "Please check your connection.", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Network Error");
                    error.printStackTrace();
                } else if (error instanceof TimeoutError) {
                    Toast.makeText(getContext(), "Timeout Error", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Timeout Error");
                    error.printStackTrace();
                } else {
                    Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_LONG).show();
                    error.printStackTrace();
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new Hashtable<String, String>();
                params.put("user_id", uid);
                params.put("sid", sessionid);
                params.put("points_earned_id", pointsearnedid);
                params.put("api_key", API.APIKEY);

                return params;
            }
        };

        statusupdate_request.setRetryPolicy(new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        LoyaltySingleton.getInstance(getContext()).addtorequestqueue(statusupdate_request);
    }


    //Handling Volley Error
    public void parseVolleyError(VolleyError error) {
        try {
            String responseBody = new String(error.networkResponse.data, "utf-8");
            JSONObject data = new JSONObject(responseBody);
            String message = data.getString("Message");
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            android.app.AlertDialog.Builder loginErrorBuilder = new android.app.AlertDialog.Builder(getContext());
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
}

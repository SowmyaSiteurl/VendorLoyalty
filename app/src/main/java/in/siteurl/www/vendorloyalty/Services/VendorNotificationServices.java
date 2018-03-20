package in.siteurl.www.vendorloyalty.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
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
import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import in.siteurl.www.vendorloyalty.API;
import in.siteurl.www.vendorloyalty.AcceptDeclineActivity;
import in.siteurl.www.vendorloyalty.App.VendorApplication;
import in.siteurl.www.vendorloyalty.LoyaltySingleton;
import in.siteurl.www.vendorloyalty.R;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by siteurl on 11/1/18.
 */

public class VendorNotificationServices extends Job implements ConnectivityReceiver.ConnectivityReceiverListener {

    static final String TAG = "show_notification_job_tag";
    long interval = JobRequest.MIN_INTERVAL;
    SharedPreferences loginpref;
    String uid;



    @NonNull
    @Override
    protected Result onRunJob(Params params) {

        loginpref = getContext().getSharedPreferences("LoginPref", MODE_PRIVATE);
        uid = loginpref.getString("User-id", null);
        VendorApplication.getInstance().setConnectivityListener(this);
        checkConnection();
        return Result.SUCCESS;
    }

    private void checkConnection() {
        boolean isConnected = ConnectivityReceiver.isConnected();
        showSnack(isConnected);
    }


    private void showSnack(boolean isConnected) {
        if (isConnected) {
            if(!uid.equals(""))
                checkforclaimnotofication();
        }
    }

    public static void schedulePeriodic() {
        new JobRequest.Builder(VendorNotificationServices.TAG)
                .setRequiredNetworkType(JobRequest.NetworkType.ANY)
                .setPeriodic(setperiod())
                .setPersisted(true)
                .build()
                .schedule();
    }

    public static long setperiod()
    {
        int build = Build.VERSION.SDK_INT;
        if(build == Build.VERSION_CODES.M)
        {
            return 60000;
        }
        return 900000;
    }


    //Checking for notification in server
    private void checkforclaimnotofication() {
        StringRequest claimnotificationrequest = new StringRequest(Request.Method.POST, API.notifyvendorCliam, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject responsefromserver = new JSONObject(response);
                    String Error = responsefromserver.getString("Error");
                    String Message = responsefromserver.getString("Message");
                    if(Error.equals("false"))
                    {
                        String data = responsefromserver.getString("data");
                        JSONArray checkdataarray = new JSONArray(data);
                        for (int i = 0; i < checkdataarray.length(); i++) {

                            JSONObject dataobject = checkdataarray.getJSONObject(i);
                            String status = dataobject.getString("status");
                            if (status.equals("New") || status.equals("new")) {
                                createnotificationforvendor();
                            }
                        }
                    }
                    else {
                        //Toast.makeText(getContext(), Message, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

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
                params.put("vendor_id", uid);
                params.put("api_key", API.APIKEY);
                return params;
            }
        };
        claimnotificationrequest.setRetryPolicy(new DefaultRetryPolicy(30000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        LoyaltySingleton.getInstance(getContext()).addtorequestqueue(claimnotificationrequest);
    }


    //To create notifications for vendor
    private void createnotificationforvendor() {

        Intent intent = new Intent(getContext(), AcceptDeclineActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
        final android.support.v4.app.NotificationCompat.Builder notificationBuilder = new android.support.v4.app.NotificationCompat.Builder(getContext());
        notificationBuilder.setContentTitle("Vendor Loyalty");
        notificationBuilder.setContentText("New Claim Request");
        notificationBuilder.setAutoCancel(true);
        notificationBuilder.setSmallIcon(R.drawable.ic_card_giftcard_black_24dp);
        notificationBuilder.setContentIntent(pendingIntent);
        notificationBuilder.setDefaults(Notification.DEFAULT_ALL);
        NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notificationBuilder.build());
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

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {

    }
}

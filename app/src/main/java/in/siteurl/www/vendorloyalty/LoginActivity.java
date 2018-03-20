package in.siteurl.www.vendorloyalty;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
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
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import in.siteurl.www.vendorloyalty.App.VendorApplication;
import in.siteurl.www.vendorloyalty.Services.ConnectivityReceiver;

public class LoginActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener {


    private MaterialEditText mLoginEmail, mLoginPassword;
    private Button mSingIn;
    RelativeLayout LoginRootLayout;
    SharedPreferences loginpref;
    SharedPreferences.Editor editor;
    //Regex for email vaildation
    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    private Toolbar mLoginToolbar;
    private CheckBox mCbShowPwd;
    Dialog alertDialog;
    String email,password;
    private TextView mForgotPassword;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        //Intializing
        LoginRootLayout = findViewById(R.id.loginrootlayout);
        mLoginEmail = findViewById(R.id.edtEmail);
        mLoginPassword = findViewById(R.id.edtPassword);
        mSingIn = findViewById(R.id.btn_signin);
        loginpref = getApplicationContext().getSharedPreferences("LoginPref", MODE_PRIVATE);
        editor = loginpref.edit();
        mLoginToolbar = findViewById(R.id.logintoolbar);
        setSupportActionBar(mLoginToolbar);

        mCbShowPwd = findViewById(R.id.cbShowPwd);
        mCbShowPwd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    mLoginPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
                else {
                    mLoginPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
            }
        });

        alertDialog = new Dialog(this);
        mForgotPassword = findViewById(R.id.tv_forgotpassword);
        mForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showforgotpassworddialog();
            }
        });
    }


    //Method to validate login credentials
    public void validatecredentials(View view) {

         email = mLoginEmail.getText().toString().trim();
         password = mLoginPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            mLoginEmail.setError("Please Enter Email");
            Snackbar.make(LoginRootLayout, "Please Enter Email", Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (!email.matches(EMAIL_PATTERN)) {
            mLoginEmail.setError("Please Enter Valid Email");
            Snackbar.make(LoginRootLayout, "Please Enter Valid Email", Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            mLoginPassword.setError("Please Enter Password");
            Snackbar.make(LoginRootLayout, "Please Enter Password", Snackbar.LENGTH_SHORT).show();
            return;
        }

        checkConnection();
    }

    //Sign in
    private void Signinwithmail(final String email, final String password) {

        final AlertDialog loadingDialog = new SpotsDialog(LoginActivity.this,R.style.Loading);
        loadingDialog.show();

        StringRequest loginrequest = new StringRequest(Request.Method.POST,
                API.login, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                loadingDialog.dismiss();
                try {
                    JSONObject objectsignup = new JSONObject(response);
                    String error = objectsignup.getString("Error");
                    String message = objectsignup.getString("Message");
                    if (error.equals("true")) {
                        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                        Snackbar.make(LoginRootLayout, message, Snackbar.LENGTH_SHORT).show();
                        return;
                    } else if (error.equals("false")) {
                        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                        String role = objectsignup.getString("role");
                        if (role.equals("vendor")) {
                            String data = objectsignup.getString("data");

                            JSONObject dataobject = new JSONObject(data);
                            String name = dataobject.getString("name");
                            String email = dataobject.getString("email");
                            String user_id = dataobject.getString("user_id");
                            String sid = dataobject.getString("sid");
                            String user_group_id = dataobject.getString("user_group_id");
                            String hash= dataobject.getString("hash");

                            editor.putString("loginname", name);
                            editor.putString("loginemail", email);
                            editor.putString("role", role);
                            editor.putString("sessionid", sid);
                            editor.putString("User-id", user_id);
                            editor.putString("user_group_id", user_group_id);
                            editor.putString("hash", hash);
                            editor.commit();

                            mLoginEmail.getText().clear();
                            mLoginPassword.getText().clear();

                            startActivity(new Intent(LoginActivity.this, MainActivity.class).
                                    setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "Invalid Vendor Credentials", Toast.LENGTH_SHORT).show();
                            Snackbar.make(LoginRootLayout, "Invalid Vendor Credentials", Snackbar.LENGTH_SHORT).show();
                            return;
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
                    Toast.makeText(LoginActivity.this, "Server is under maintenance.Please try later.", Toast.LENGTH_LONG).show();
                    Log.d("Error", String.valueOf(error instanceof ServerError));
                    error.printStackTrace();
                } else if (error instanceof AuthFailureError) {
                    Toast.makeText(LoginActivity.this, "Authentication Error", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Authentication Error");
                    error.printStackTrace();
                } else if (error instanceof ParseError) {
                    Toast.makeText(LoginActivity.this, "Parse Error", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Parse Error");
                    error.printStackTrace();
                } else if (error instanceof NoConnectionError) {
                    Toast.makeText(LoginActivity.this, "Server is under maintenance.Please try later.", Toast.LENGTH_LONG).show();
                    Log.d("Error", "No Connection Error");
                    error.printStackTrace();
                } else if (error instanceof NetworkError) {
                    Toast.makeText(LoginActivity.this, "Please check your connection.", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Network Error");
                    error.printStackTrace();
                } else if (error instanceof TimeoutError) {
                    Toast.makeText(LoginActivity.this, "Timeout Error", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Timeout Error");
                    error.printStackTrace();
                } else {
                    Toast.makeText(LoginActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
                    error.printStackTrace();
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new Hashtable<String, String>();
                params.put("email", email);
                params.put("password", password);
                params.put("api_key", API.APIKEY);
                return params;
            }
        };
        loginrequest.setRetryPolicy(new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        LoyaltySingleton.getInstance(getApplicationContext()).addtorequestqueue(loginrequest);
    }


    //To show forgot password dialog
    public void showforgotpassworddialog()
    {
        android.app.AlertDialog.Builder forgotPasswordBuilder = new android.app.AlertDialog.Builder(LoginActivity.this);
        forgotPasswordBuilder.setTitle("Forgot Password");
        forgotPasswordBuilder.setMessage("Please Enter Registered Email.");
        View customLayout = getLayoutInflater().inflate(R.layout.forgot_layout, null);
        final MaterialEditText email = customLayout.findViewById(R.id.edtEmail);
        forgotPasswordBuilder.setView(customLayout);
        forgotPasswordBuilder.setPositiveButton("Reset", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                String forgotemail = email.getText().toString().trim();
                if(TextUtils.isEmpty(forgotemail)||(!forgotemail.matches(EMAIL_PATTERN)))
                {
                    Toast.makeText(LoginActivity.this, "Enter Valid Email", Toast.LENGTH_LONG).show();
                    showforgotpassworddialog();
                    return;
                }
                sendforgotpasswordemailtoserver(forgotemail);

            }
        });

        forgotPasswordBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        forgotPasswordBuilder.show();
    }

    private void sendforgotpasswordemailtoserver(final String forgotemail) {

        final AlertDialog loadingDialog = new SpotsDialog(LoginActivity.this,R.style.Loading);
        loadingDialog.show();

        StringRequest loginrequest = new StringRequest(Request.Method.POST,
                API.forgotpassword, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                loadingDialog.dismiss();
                try {
                    JSONObject objectsignup = new JSONObject(response);
                    String error = objectsignup.getString("Error");
                    String message = objectsignup.getString("Message");
                    if (error.equals("true")) {
                        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                        Snackbar.make(LoginRootLayout, message, Snackbar.LENGTH_SHORT).show();
                        return;
                    } else if (error.equals("false")) {
                        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                        /*startActivity(new Intent(LoginActivity.this, LoginActivity.class).
                                    setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                            finish();*/
                        Snackbar.make(LoginRootLayout, message, Snackbar.LENGTH_SHORT).show();

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
                    Toast.makeText(LoginActivity.this, "Server is under maintenance.Please try later.", Toast.LENGTH_LONG).show();
                    Log.d("Error", String.valueOf(error instanceof ServerError));
                    error.printStackTrace();
                } else if (error instanceof AuthFailureError) {
                    Toast.makeText(LoginActivity.this, "Authentication Error", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Authentication Error");
                    error.printStackTrace();
                } else if (error instanceof ParseError) {
                    Toast.makeText(LoginActivity.this, "Parse Error", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Parse Error");
                    error.printStackTrace();
                } else if (error instanceof NoConnectionError) {
                    Toast.makeText(LoginActivity.this, "Server is under maintenance.Please try later.", Toast.LENGTH_LONG).show();
                    Log.d("Error", "No Connection Error");
                    error.printStackTrace();
                } else if (error instanceof NetworkError) {
                    Toast.makeText(LoginActivity.this, "Please check your connection.", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Network Error");
                    error.printStackTrace();
                } else if (error instanceof TimeoutError) {
                    Toast.makeText(LoginActivity.this, "Timeout Error", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Timeout Error");
                    error.printStackTrace();
                } else {
                    Toast.makeText(LoginActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
                    error.printStackTrace();
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new Hashtable<String, String>();
                params.put("email", forgotemail);
                params.put("api_key", API.APIKEY);
                return params;
            }
        };
        loginrequest.setRetryPolicy(new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        LoyaltySingleton.getInstance(getApplicationContext()).addtorequestqueue(loginrequest);

    }


    //Handling Volley Error
    public void parseVolleyError(VolleyError error) {
        try {
            String responseBody = new String(error.networkResponse.data, "utf-8");
            JSONObject data = new JSONObject(responseBody);
            String message = data.getString("Message");
            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
            android.app.AlertDialog.Builder loginErrorBuilder = new android.app.AlertDialog.Builder(LoginActivity.this);
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


    private void checkConnection() {
        boolean isConnected = ConnectivityReceiver.isConnected();
        showSnack(isConnected);
    }

    // Showing the status in Snackbar
    private void showSnack(boolean isConnected) {
        if (isConnected) {
            //Method to signin
            Signinwithmail(email, password);
            if(alertDialog.isShowing())
            {
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
}

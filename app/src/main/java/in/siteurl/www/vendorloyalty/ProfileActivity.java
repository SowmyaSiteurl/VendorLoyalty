package in.siteurl.www.vendorloyalty;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import in.siteurl.www.vendorloyalty.App.VendorApplication;
import in.siteurl.www.vendorloyalty.Model.VendorProfile;
import in.siteurl.www.vendorloyalty.Services.ConnectivityReceiver;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class ProfileActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener {

    private Toolbar mToolbar;
    SharedPreferences loginpref;
    String sessionid, uid,vendorimageurl;
    RelativeLayout rootLayout;
    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    private MaterialEditText mName, mEmail, mPhone, mAddress, mLatLong;
    private ImageView mVendorStoreImage;
    RequestOptions imageoptions = new RequestOptions();
    private VendorProfile mVendorProfile;
    String mfileName="";
    String productImageBase64="";
    String categoryid;
    public static final int RequestPermissionCode = 1;
    public static final int LoctionPermissionCode = 2;
    private int REQUEST_CAMERA = 0, SELECT_FILE = 1,LOCATION_REQUEST = 2;
    Bitmap vendorStoreBitmap;
    Dialog alertDialog;
    String imageNameOne;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mToolbar = findViewById(R.id.profiletoolbar);
        mToolbar.setTitle("Profile");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        imageoptions.diskCacheStrategy(DiskCacheStrategy.ALL);
        imageoptions.fitCenter();
        imageoptions.placeholder(R.drawable.header);

        //getting details from preferences
        loginpref = getApplicationContext().getSharedPreferences("LoginPref", MODE_PRIVATE);
        sessionid = loginpref.getString("sessionid", null);
        uid = loginpref.getString("User-id", null);

        //Intializing views
        mName = findViewById(R.id.edtName);
        mEmail = findViewById(R.id.edtEmail);
        mPhone = findViewById(R.id.edtPhone);
        mAddress = findViewById(R.id.edtAddress);
        mLatLong = findViewById(R.id.edtLatLong);
        rootLayout = findViewById(R.id.profilerootlayout);
        mVendorStoreImage = findViewById(R.id.iv_vendorshopimage);

        mVendorStoreImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestPermission();
                selectImage();
            }
        });

        alertDialog = new Dialog(this);
        checkConnection();

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemid = item.getItemId();
        if(itemid == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    //To get vendor details from server
    private void getvendordetails() {

        final AlertDialog loadingDialog = new SpotsDialog(ProfileActivity.this, R.style.Loading);
        loadingDialog.show();

        StringRequest vendordetailsrequest = new StringRequest(Request.Method.POST, API.individialvendordetails, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                loadingDialog.dismiss();
                try {
                    JSONObject responsefromserver = new JSONObject(response);
                    String error = responsefromserver.getString("Error");
                    String message = responsefromserver.getString("Message");
                    if (error.equals("false")) {
                        JSONArray data = responsefromserver.getJSONArray("data");
                        JSONObject vendordata = data.getJSONObject(0);
                        String name = vendordata.getString("name");
                        String email = vendordata.getString("email");
                        String phone = vendordata.getString("phone");
                        String address = vendordata.getString("address");
                        vendorimageurl = vendordata.getString("store_image");
                        categoryid = vendordata.getString("category_id");
                        String coordinates = vendordata.getString("gps_location");

                        mVendorProfile = new VendorProfile(name,phone,email,coordinates,vendorimageurl,address,categoryid);

                        mName.setText(name);
                        mEmail.setText(email);
                        mPhone.setText(phone);
                        mAddress.setText(address);
                        mLatLong.setText(coordinates);
                        //mLatLong.setText(getAddress());

                        Glide.with(ProfileActivity.this).load(vendorimageurl)
                                .thumbnail(0.5f)
                                .apply(imageoptions)
                                .into(mVendorStoreImage);

                    } else {
                        Snackbar.make(rootLayout, message, Snackbar.LENGTH_LONG).show();
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
                    Toast.makeText(ProfileActivity.this, "Server is under maintenance.Please try later.", Toast.LENGTH_LONG).show();
                    Log.d("Error", String.valueOf(error instanceof ServerError));
                    error.printStackTrace();
                } else if (error instanceof AuthFailureError) {
                    Toast.makeText(ProfileActivity.this, "Authentication Error", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Authentication Error");
                    error.printStackTrace();
                } else if (error instanceof ParseError) {
                    Toast.makeText(ProfileActivity.this, "Parse Error", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Parse Error");
                    error.printStackTrace();
                } else if (error instanceof NoConnectionError) {
                    Toast.makeText(ProfileActivity.this, "Server is under maintenance.Please try later.", Toast.LENGTH_LONG).show();
                    Log.d("Error", "No Connection Error");
                    error.printStackTrace();
                } else if (error instanceof NetworkError) {
                    Toast.makeText(ProfileActivity.this, "Please check your connection.", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Network Error");
                    error.printStackTrace();
                } else if (error instanceof TimeoutError) {
                    Toast.makeText(ProfileActivity.this, "Timeout Error", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Timeout Error");
                    error.printStackTrace();
                } else {
                    Toast.makeText(ProfileActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
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

        vendordetailsrequest.setRetryPolicy(new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        LoyaltySingleton.getInstance(getApplicationContext()).addtorequestqueue(vendordetailsrequest);
    }


    public void gotomaps(View view)
    {
        LocationPermission();
    }

    //Handling Volley Error
    public void parseVolleyError(VolleyError error) {
        try {
            String responseBody = new String(error.networkResponse.data, "utf-8");
            JSONObject data = new JSONObject(responseBody);
            String message = data.getString("Message");
            Toast.makeText(ProfileActivity.this, message, Toast.LENGTH_LONG).show();
            android.app.AlertDialog.Builder loginErrorBuilder = new android.app.AlertDialog.Builder(ProfileActivity.this);
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

    //To changed vendor details in server
    public void savevendordetails(View view) {
        if (checkpreviousdata()) {
            final AlertDialog loadingDialog = new SpotsDialog(ProfileActivity.this, R.style.Loading);
            loadingDialog.show();
            StringRequest savevendordetailsrequest = new StringRequest(Request.Method.POST, API.editIndividialvendor, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    loadingDialog.dismiss();
                    try {
                        JSONObject responsefromserver = new JSONObject(response);
                        String error = responsefromserver.getString("Error");
                        String message = responsefromserver.getString("Message");
                        showresultdialog(error,message);
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
                        Toast.makeText(ProfileActivity.this, "Server is under maintenance.Please try later.", Toast.LENGTH_LONG).show();
                        Log.d("Error", String.valueOf(error instanceof ServerError));
                        error.printStackTrace();
                    } else if (error instanceof AuthFailureError) {
                        Toast.makeText(ProfileActivity.this, "Authentication Error", Toast.LENGTH_LONG).show();
                        Log.d("Error", "Authentication Error");
                        error.printStackTrace();
                    } else if (error instanceof ParseError) {
                        Toast.makeText(ProfileActivity.this, "Parse Error", Toast.LENGTH_LONG).show();
                        Log.d("Error", "Parse Error");
                        error.printStackTrace();
                    } else if (error instanceof NoConnectionError) {
                        Toast.makeText(ProfileActivity.this, "Server is under maintenance.Please try later.", Toast.LENGTH_LONG).show();
                        Log.d("Error", "No Connection Error");
                        error.printStackTrace();
                    } else if (error instanceof NetworkError) {
                        Toast.makeText(ProfileActivity.this, "Please check your connection.", Toast.LENGTH_LONG).show();
                        Log.d("Error", "Network Error");
                        error.printStackTrace();
                    } else if (error instanceof TimeoutError) {
                        Toast.makeText(ProfileActivity.this, "Timeout Error", Toast.LENGTH_LONG).show();
                        Log.d("Error", "Timeout Error");
                        error.printStackTrace();
                    } else {
                        Toast.makeText(ProfileActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
                        error.printStackTrace();
                    }
                }
            }) {

                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new Hashtable<String, String>();

                    params.put("vendor_id", uid);
                    params.put("name", mName.getText().toString().trim());
                    params.put("email", mEmail.getText().toString().trim());
                    params.put("phone", mPhone.getText().toString().trim());
                    params.put("address", mAddress.getText().toString().trim());
                    params.put("sid",sessionid);
                    params.put("category_id",categoryid);
                    params.put("gps_location",mLatLong.getText().toString().trim());
                    params.put("api_key",API.APIKEY);
                    params.put("store_image",productImageBase64);
                    params.put("filename",mfileName);
                    return params;
                }
            };

            savevendordetailsrequest.setRetryPolicy(new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            LoyaltySingleton.getInstance(getApplicationContext()).addtorequestqueue(savevendordetailsrequest);
        }
        else {
            Snackbar.make(rootLayout,"No Changes Found",Snackbar.LENGTH_SHORT).show();
        }
    }

    //To show vendor edit result in server
    private void showresultdialog(final String error, String message) {

        android.app.AlertDialog.Builder resultBuilder = new android.app.AlertDialog.Builder(ProfileActivity.this);
        resultBuilder.setTitle("Vendor");
        resultBuilder.setMessage(message);
        resultBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if(error.equals("false")){
                    startActivity(new Intent(ProfileActivity.this,MainActivity.class));
                    finish();
                }
                else
                {
                    dialogInterface.dismiss();
                }
            }
        });
        resultBuilder.setCancelable(false);
        resultBuilder.show();

    }


    //check previous data of vendor
    public boolean checkpreviousdata()
    {
        String name,phone,address,email,vendorshopimage,coordinates;
        name = mName.getText().toString();
        phone = mPhone.getText().toString();
        address = mAddress.getText().toString();
        email = mEmail.getText().toString();

        vendorshopimage = vendorimageurl;
        coordinates = mLatLong.getText().toString();
        if(name.equals(mVendorProfile.getmName())&&(phone.equals(mVendorProfile.getmPhone()))&&(address.equals(mVendorProfile.getmAddress()))
                && email.equals(mVendorProfile.getmEmail()) && vendorshopimage.equals(mVendorProfile.getmVendorShopImage())&&coordinates.equals(mVendorProfile.getmLatLong()))
        {
            return false;
        }

        return true;
    }


    //Method to show alertdialog for camera and gallery
    private void selectImage() {
        final CharSequence[] items = { "Take Photo", "Choose from Library",
                "Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    cameraIntent();

                } else if (items[item].equals("Choose from Library")) {
                    galleryIntent();

                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    //To select image from gallery
    private void galleryIntent()
    {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(pickPhoto, "Select File"), SELECT_FILE);
    }

    //To capture image from camera
    private void cameraIntent()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
        }
    }

    //Method to get camera image data
    private void onCaptureImageResult(Intent data)
    {
        vendorStoreBitmap = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        vendorStoreBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

        File destination = new File(Environment.getExternalStorageDirectory(),System.currentTimeMillis()+".jpg");
        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mVendorStoreImage.setImageBitmap(vendorStoreBitmap);
        mfileName = destination.getName();
        vendorimageurl = mfileName;
        productImageBase64 = getStringImage(vendorStoreBitmap);
    }

    //Method to get gallery image data
    private void onSelectFromGalleryResult(Intent data)
    {
        if (data != null) {
            try {
                vendorStoreBitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                Uri selectedImageUri = data.getData();
                imageNameOne = getRealPathFromURI(selectedImageUri);


                File file = new File(imageNameOne);
                mfileName = file.getName();
                Toast.makeText(getApplicationContext(), mfileName + " " + " real ", Toast.LENGTH_LONG).show();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mVendorStoreImage.setImageBitmap(vendorStoreBitmap);
        productImageBase64 = getStringImage(vendorStoreBitmap);
    }

    //Method to get image path from gallery
    public String getRealPathFromURI(Uri contentUri)
    {   // can post image
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(contentUri,proj, // Which columns to return
                null, // WHERE clause; which rows to return (all rows)
                null, // WHERE clause selection arguments (none)
                null); // Order-by clause (ascending by name)
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    //Method to convert bitmap to base64
    public String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }


    //Requesting Run time permissions for read and write in storage
    private void requestPermission() {

        ActivityCompat.requestPermissions(ProfileActivity.this, new String[]
                {
                        WRITE_EXTERNAL_STORAGE,READ_EXTERNAL_STORAGE,CAMERA
                }, RequestPermissionCode);
    }

    //Requesting Run Time permission for location
    private void LocationPermission() {

        ActivityCompat.requestPermissions(ProfileActivity.this, new String[]
                {
                        ACCESS_COARSE_LOCATION,ACCESS_FINE_LOCATION
                }, LoctionPermissionCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {

            case RequestPermissionCode:

                if (grantResults.length > 0) {
                    boolean WriteStoragePermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean ReadStoragePermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean CameraPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (WriteStoragePermission&&ReadStoragePermission&&CameraPermission) {
                        //Toast.makeText(AddorEditOfferActivity.this, "Permission Granted", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(ProfileActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();
                    }
                }
                break;

            case LoctionPermissionCode:

                if (grantResults.length > 0) {
                    boolean LocationPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (LocationPermission) {
                        startActivity(new Intent(this,MapsActivity.class).putExtra("profile",mVendorProfile));
                        //Toast.makeText(AddorEditOfferActivity.this, "Permission Granted", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(ProfileActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }
    //permission ends

    //To check connection
    private void checkConnection() {
        boolean isConnected = ConnectivityReceiver.isConnected();
        showSnack(isConnected);
    }

    // Showing the status in Snackbar
    private void showSnack(boolean isConnected) {
        if (isConnected) {
            //Method to get vendor details
            getvendordetails();
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
                finishAffinity();
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

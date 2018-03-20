package in.siteurl.www.vendorloyalty;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
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
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import in.siteurl.www.vendorloyalty.Model.ViewOffer;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class AddorEditOfferActivity extends AppCompatActivity {


    private Toolbar mAddOfferToolbar;
    private MaterialEditText mProductName, mProductDescription, mProductPrice, mProductTerms, mExpiryDate;
    Calendar newDate;
    int mYear, mMonth, mDay;
    private ImageView mProductImage;
    public static final int RequestPermissionCode = 1;
    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    Bitmap productBitmap;
    String mfileName = "";
    String productImageBase64 = "";
    private RelativeLayout mAddOfferRoot;
    SharedPreferences loginpref;
    String sessionid, uid;
    private Button mAddorEdit;
    ViewOffer editoffer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addoredit_offer);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        //Toolbar
        mAddOfferToolbar = findViewById(R.id.addoffertoolbar);
        setSupportActionBar(mAddOfferToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //Intializing views
        mProductName = findViewById(R.id.edtProductName);
        mProductPrice = findViewById(R.id.edtProductPrice);
        mProductTerms = findViewById(R.id.edtTermsandCondition);
        mProductDescription = findViewById(R.id.edtProductDescription);
        mExpiryDate = findViewById(R.id.edtExpiryDate);
        mProductImage = findViewById(R.id.iv_productImage);
        mAddOfferRoot = findViewById(R.id.addofferrootlayout);
        mAddorEdit = findViewById(R.id.btn_addoffer);

        if (getIntent().hasExtra("editoffer")) {
            mAddOfferToolbar.setTitle("Edit Offer");
            mAddorEdit.setText("Update");
            setOfferDetails();
            mAddorEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    validateEditProductInputs();
                }
            });
        } else {
            mAddOfferToolbar.setTitle("Add Offer");
            mAddorEdit.setText("Add Offer");
            mAddorEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    validateAddProductInputs();
                }
            });
        }

        newDate = Calendar.getInstance();
        loginpref = getApplicationContext().getSharedPreferences("LoginPref", MODE_PRIVATE);
        sessionid = loginpref.getString("sessionid", null);
        uid = loginpref.getString("User-id", null);

        mExpiryDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showdatedialog();
            }
        });

        mProductImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestPermission();
                selectImage();
            }
        });

        //To Fetch image from bundle
        if (savedInstanceState != null) {
            productBitmap = savedInstanceState.getParcelable("product");
            mProductImage.setImageBitmap(productBitmap);
        }
    }

    // To Set all input fields for edit offer
    private void setOfferDetails() {
        editoffer = (ViewOffer) getIntent().getSerializableExtra("editoffer");
        mProductName.setText(editoffer.getmOffername());
        mExpiryDate.setText(editoffer.getmExpirydate());
        mProductDescription.setText(editoffer.getmOfferdescription());
        mProductPrice.setText(editoffer.getmOfferprice());
        mProductTerms.setText(editoffer.getmTermsAndCondtion());
        Glide.with(AddorEditOfferActivity.this)
                .load(editoffer.getmOfferimage())
                .into(mProductImage);
    }

    //Method to show alertdialog for camera and gallery
    private void selectImage() {
        final CharSequence[] items = {"Take Photo", "Choose from Library",
                "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(AddorEditOfferActivity.this);
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
    private void galleryIntent() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(pickPhoto, "Select File"), SELECT_FILE);
    }

    //To capture image from camera
    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
        }
    }

    //Method to get camera image data
    private void onCaptureImageResult(Intent data) {
        productBitmap = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        productBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

        File destination = new File(Environment.getExternalStorageDirectory(), System.currentTimeMillis() + ".jpg");
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
        mProductImage.setImageBitmap(productBitmap);
        mfileName = destination.getName();
        productImageBase64 = getStringImage(productBitmap);
    }

    //Method to get gallery image data
    private void onSelectFromGalleryResult(Intent data) {
        if (data != null) {
            try {
                productBitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                Uri selectedImageUri = data.getData();
                String imageNameOne = getRealPathFromURI(selectedImageUri);

                File file = new File(imageNameOne);
                mfileName = file.getName();
                Toast.makeText(getApplicationContext(), mfileName + " " + " real ", Toast.LENGTH_LONG).show();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mProductImage.setImageBitmap(productBitmap);
        productImageBase64 = getStringImage(productBitmap);
    }

    //Method to get image path from gallery
    public String getRealPathFromURI(Uri contentUri) {   // can post image
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(contentUri, proj, // Which columns to return
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

        ActivityCompat.requestPermissions(AddorEditOfferActivity.this, new String[]
                {
                        WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE, CAMERA
                }, RequestPermissionCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {

            case RequestPermissionCode:

                if (grantResults.length > 0) {
                    boolean WriteStoragePermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean ReadStoragePermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean CameraPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (WriteStoragePermission && ReadStoragePermission && CameraPermission) {
                        //Toast.makeText(AddorEditOfferActivity.this, "Permission Granted", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(AddorEditOfferActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }
    //permission ends

    //Validation of add offer inputs
    public void validateAddProductInputs() {
        String productname = mProductName.getText().toString().trim();
        String productprice = mProductPrice.getText().toString().trim();
        String productdescription = mProductDescription.getText().toString().trim();
        String productterms = mProductTerms.getText().toString().trim();
        String productexpiry = mExpiryDate.getText().toString().trim();

        if (TextUtils.isEmpty(productname)) {
            mProductName.setError("Product Name");
            mProductName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(productprice)) {
            mProductPrice.setError("Product Price");
            return;
        }

        if (TextUtils.isEmpty(productdescription)) {
            mProductDescription.setError("Product Description");
            return;
        }

        if (TextUtils.isEmpty(productterms)) {
            mProductTerms.setError("Product Terms");
            return;
        }

        if (TextUtils.isEmpty(productexpiry)) {
            mExpiryDate.setError("Product Expiry");
            return;
        }

        if (TextUtils.isEmpty(mfileName)) {
            Snackbar.make(mAddOfferRoot, "Please Add Product Image", Snackbar.LENGTH_LONG).show();
            Toast.makeText(this, "Please Add Product Image", Toast.LENGTH_LONG).show();
            return;
        }

        addOffer(productname, productdescription, productprice, productterms, productexpiry, mfileName);

    }

    //Method to add offer in server
    private void addOffer(final String productname, final String productdescription, final String productprice, final String productterms, final String productexpiry, String filename) {

        final AlertDialog loadingDialog = new SpotsDialog(AddorEditOfferActivity.this, R.style.Loading);
        loadingDialog.show();

        StringRequest addofferRequest = new StringRequest(Request.Method.POST, API.offerAddByVendor,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loadingDialog.dismiss();

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String responseError = jsonObject.getString("Error");
                            String responsemessage = jsonObject.getString("Message");
                            if (responseError.equals("false")) {
                                Toast.makeText(AddorEditOfferActivity.this, responsemessage, Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(AddorEditOfferActivity.this, MainActivity.class).
                                        setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK));
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
                    Toast.makeText(AddorEditOfferActivity.this, "Server is under maintenance.Please try later.", Toast.LENGTH_LONG).show();
                    Log.d("Error", String.valueOf(error instanceof ServerError));
                    error.printStackTrace();
                } else if (error instanceof AuthFailureError) {
                    Toast.makeText(AddorEditOfferActivity.this, "Authentication Error", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Authentication Error");
                    error.printStackTrace();
                } else if (error instanceof ParseError) {
                    Toast.makeText(AddorEditOfferActivity.this, "Parse Error", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Parse Error");
                    error.printStackTrace();
                } else if (error instanceof NoConnectionError) {
                    Toast.makeText(AddorEditOfferActivity.this, "Server is under maintenance.Please try later.", Toast.LENGTH_LONG).show();
                    Log.d("Error", "No Connection Error");
                    error.printStackTrace();
                } else if (error instanceof NetworkError) {
                    Toast.makeText(AddorEditOfferActivity.this, "Please check your connection.", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Network Error");
                    error.printStackTrace();
                } else if (error instanceof TimeoutError) {
                    Toast.makeText(AddorEditOfferActivity.this, "Timeout Error", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Timeout Error");
                    error.printStackTrace();
                } else {
                    Toast.makeText(AddorEditOfferActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
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
                params.put("offer_name", productname);
                params.put("offer_description", productdescription);
                params.put("offer_image", productImageBase64);
                params.put("filename", mfileName);
                params.put("offer_price", productprice);
                params.put("expiry_date", productexpiry);
                params.put("terms_and_condtion", productterms);
                return params;
            }
        };
        addofferRequest.setRetryPolicy(new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        LoyaltySingleton.getInstance(getApplicationContext()).addtorequestqueue(addofferRequest);
    }

    //Method to validate edit offer inputs
    public void validateEditProductInputs() {
        String productname = mProductName.getText().toString().trim();
        String productprice = mProductPrice.getText().toString().trim();
        String productdescription = mProductDescription.getText().toString().trim();
        String productterms = mProductTerms.getText().toString().trim();
        String productexpiry = mExpiryDate.getText().toString().trim();

        if (TextUtils.isEmpty(productname)) {
            mProductName.setError("Product Name");
            mProductName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(productprice)) {
            mProductPrice.setError("Product Price");
            return;
        }

        if (TextUtils.isEmpty(productdescription)) {
            mProductDescription.setError("Product Description");
            return;
        }

        if (TextUtils.isEmpty(productterms)) {
            mProductTerms.setError("Product Terms");
            return;
        }

        if (TextUtils.isEmpty(productexpiry)) {
            mExpiryDate.setError("Product Expiry");
            return;
        }

        editOffer(productname, productdescription, productprice, productterms, productexpiry, mfileName);
    }

    //Method to edit offer in server
    private void editOffer(final String productname, final String productdescription, final String productprice, final String productterms, final String productexpiry, String filename) {

        final AlertDialog loadingDialog = new SpotsDialog(AddorEditOfferActivity.this, R.style.Loading);
        loadingDialog.show();

        StringRequest editofferRequest = new StringRequest(Request.Method.POST, API.editvendoroffers,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loadingDialog.dismiss();
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String responseError = jsonObject.getString("Error");
                            String responsemessage = jsonObject.getString("Message");
                            if (responseError.equals("false")) {
                                Toast.makeText(AddorEditOfferActivity.this, responsemessage, Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(AddorEditOfferActivity.this, OffersActivity.class).
                                        setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK));
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
                    Toast.makeText(AddorEditOfferActivity.this, "Server is under maintenance.Please try later.", Toast.LENGTH_LONG).show();
                    Log.d("Error", String.valueOf(error instanceof ServerError));
                    error.printStackTrace();
                } else if (error instanceof AuthFailureError) {
                    Toast.makeText(AddorEditOfferActivity.this, "Authentication Error", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Authentication Error");
                    error.printStackTrace();
                } else if (error instanceof ParseError) {
                    Toast.makeText(AddorEditOfferActivity.this, "Parse Error", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Parse Error");
                    error.printStackTrace();
                } else if (error instanceof NoConnectionError) {
                    Toast.makeText(AddorEditOfferActivity.this, "Server is under maintenance.Please try later.", Toast.LENGTH_LONG).show();
                    Log.d("Error", "No Connection Error");
                    error.printStackTrace();
                } else if (error instanceof NetworkError) {
                    Toast.makeText(AddorEditOfferActivity.this, "Please check your connection.", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Network Error");
                    error.printStackTrace();
                } else if (error instanceof TimeoutError) {
                    Toast.makeText(AddorEditOfferActivity.this, "Timeout Error", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Timeout Error");
                    error.printStackTrace();
                } else {
                    Toast.makeText(AddorEditOfferActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
                    error.printStackTrace();
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", uid);
                params.put("sid", sessionid);
                params.put("api_key", API.APIKEY);
                params.put("offer_name", productname);
                params.put("offer_description", productdescription);
                params.put("offer_image", productImageBase64);
                params.put("filename", mfileName);
                params.put("offer_price", productprice);
                params.put("expiry_date", productexpiry);
                params.put("terms_and_condtion", productterms);
                params.put("offer_id", editoffer.getmOfferid());
                return params;
            }
        };
        editofferRequest.setRetryPolicy(new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        LoyaltySingleton.getInstance(getApplicationContext()).addtorequestqueue(editofferRequest);
    }

    //Handling Volley Error
    public void parseVolleyError(VolleyError error) {
        try {
            String responseBody = new String(error.networkResponse.data, "utf-8");
            JSONObject data = new JSONObject(responseBody);
            String message = data.getString("Message");
            Toast.makeText(AddorEditOfferActivity.this, message, Toast.LENGTH_LONG).show();
            android.app.AlertDialog.Builder loginErrorBuilder = new android.app.AlertDialog.Builder(AddorEditOfferActivity.this);
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

    //To show date picker
    public void showdatedialog() {
        DatePickerDialog callbackdialog = new DatePickerDialog(AddorEditOfferActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        newDate.set(year, month, day);
                        String date = new SimpleDateFormat("yyyy-MM-dd").format(newDate.getTime());
                        mExpiryDate.setText(date);

                        mYear = newDate.get(Calendar.YEAR);
                        mMonth = newDate.get(Calendar.MONTH);
                        mDay = newDate.get(Calendar.DAY_OF_MONTH);
                    }
                }, mYear, mMonth, mDay);

        callbackdialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        callbackdialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == DialogInterface.BUTTON_NEGATIVE) {
                            mExpiryDate.setText("");
                        }
                    }
                });
        callbackdialog.show();
                /*Condition to stop datepicker to current month
        Calendar d = Calendar.getInstance();
        callbackdialog.updateDate(d.get(Calendar.YEAR),d.get(Calendar.MONTH),d.get(Calendar.DAY_OF_MONTH));*/
    }

    //To save image in bundle
    @Override
    public void onSaveInstanceState(Bundle toSave) {
        super.onSaveInstanceState(toSave);
        if (productBitmap != null) {
            toSave.putParcelable("product", productBitmap);
        }
    }
}

package in.siteurl.www.vendorloyalty;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import dmax.dialog.SpotsDialog;
import in.siteurl.www.vendorloyalty.Model.VendorProfile;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    VendorProfile vendorProfile;
    String sessionid, uid;
    SharedPreferences loginpref;
    private String mAddressfrommap;
    Double current_lat, current_long;
    String VendorLatLng;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //getting details from preferences
        loginpref = getApplicationContext().getSharedPreferences("LoginPref", MODE_PRIVATE);
        sessionid = loginpref.getString("sessionid", null);
        uid = loginpref.getString("User-id", null);

        if (getIntent().hasExtra("profile")) {
            vendorProfile = (VendorProfile) getIntent().getSerializableExtra("profile");
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        CameraPosition googlePlex = CameraPosition.builder()
                .target(new LatLng(12.2958, 76.6394))
                .zoom(16)
                .bearing(0)
                .tilt(45)
                .build();


        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(googlePlex));
        mMap.addMarker(new MarkerOptions().position(new LatLng(12.2958, 76.6394)).title("Marker in Mysuru").draggable(true).icon(BitmapDescriptorFactory.fromResource(R.drawable.mapicon)));
        //mMap.addMarker(new MarkerOptions().position(new LatLng(current_long, current_lat)).title("Marker in Mysuru").draggable(true).icon(BitmapDescriptorFactory.fromResource(R.drawable.mapicon)));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16.0f));

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mMap.setMyLocationEnabled(true);
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
            }

            @Override
            public void onMarkerDrag(Marker marker) {
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {

                getAddressfromLatLng(marker.getPosition());
                LatLng location = marker.getPosition();

                VendorLatLng = String.valueOf(location.latitude) + "," + location.longitude;

            }
        });

    }

    private void getAddressfromLatLng(LatLng position) {

        Geocoder geocoder = new Geocoder(MapsActivity.this);
        if (geocoder.isPresent()) {
            try {
                List<Address> addressList = geocoder.getFromLocation(position.latitude, position.longitude, 1);
                if (addressList != null && addressList.size() > 0) {

                    String locality = addressList.get(0).getAddressLine(0);
                    String country = addressList.get(0).getCountryName();

                    String city = addressList.get(0).getLocality();
                    String state = addressList.get(0).getAdminArea();
                    String postalCode = addressList.get(0).getPostalCode();
                    String knownName = addressList.get(0).getFeatureName();


                    if (!locality.isEmpty() && !country.isEmpty()) {
                        showaddressdialog(locality, country);
                    }

                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Sorry:( Not Able Fetch the Address", Toast.LENGTH_SHORT).show();
        }
    }

    private void showaddressdialog(String locality, String country) {

        AlertDialog.Builder adressdialog = new AlertDialog.Builder(MapsActivity.this);
        adressdialog.setTitle("Your Address");
        adressdialog.setIcon(R.drawable.header);
        adressdialog.setMessage(locality);
        mAddressfrommap = locality;
        adressdialog.setPositiveButton("Update Address", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                updatevendorprofile();
            }
        });
        adressdialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                finish();
            }
        });

        adressdialog.setNeutralButton("Select Again", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        adressdialog.setCancelable(true);
        adressdialog.show();

    }

    private void updatevendorprofile() {

        final AlertDialog loadingDialog = new SpotsDialog(MapsActivity.this, R.style.Loading);
        loadingDialog.show();
        StringRequest savevendordetailsrequest = new StringRequest(Request.Method.POST, API.editIndividialvendor, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                loadingDialog.dismiss();
                try {
                    JSONObject responsefromserver = new JSONObject(response);
                    String error = responsefromserver.getString("Error");
                    String message = responsefromserver.getString("Message");
                    showresultdialog(error, message);
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
                    Toast.makeText(MapsActivity.this, "Server is under maintenance.Please try later.", Toast.LENGTH_LONG).show();
                    Log.d("Error", String.valueOf(error instanceof ServerError));
                    error.printStackTrace();
                } else if (error instanceof AuthFailureError) {
                    Toast.makeText(MapsActivity.this, "Authentication Error", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Authentication Error");
                    error.printStackTrace();
                } else if (error instanceof ParseError) {
                    Toast.makeText(MapsActivity.this, "Parse Error", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Parse Error");
                    error.printStackTrace();
                } else if (error instanceof NoConnectionError) {
                    Toast.makeText(MapsActivity.this, "Server is under maintenance.Please try later.", Toast.LENGTH_LONG).show();
                    Log.d("Error", "No Connection Error");
                    error.printStackTrace();
                } else if (error instanceof NetworkError) {
                    Toast.makeText(MapsActivity.this, "Please check your connection.", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Network Error");
                    error.printStackTrace();
                } else if (error instanceof TimeoutError) {
                    Toast.makeText(MapsActivity.this, "Timeout Error", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Timeout Error");
                    error.printStackTrace();
                } else {
                    Toast.makeText(MapsActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
                    error.printStackTrace();
                }
            }
        }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new Hashtable<String, String>();

                params.put("vendor_id", uid);
                params.put("name", vendorProfile.getmName());
                params.put("email", vendorProfile.getmEmail());
                params.put("phone", vendorProfile.getmPhone());
                params.put("address", mAddressfrommap);
                params.put("sid", sessionid);
                params.put("category_id", vendorProfile.getmCategoryId());
                params.put("gps_location", VendorLatLng);
                params.put("api_key", API.APIKEY);
                params.put("store_image", "");
                params.put("filename", "");
                return params;
            }
        };

        savevendordetailsrequest.setRetryPolicy(new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        LoyaltySingleton.getInstance(getApplicationContext()).addtorequestqueue(savevendordetailsrequest);

    }

    //To show vendor edit result in server
    private void showresultdialog(final String error, String message) {

        android.app.AlertDialog.Builder resultBuilder = new android.app.AlertDialog.Builder(MapsActivity.this);
        resultBuilder.setTitle("Vendor");
        resultBuilder.setMessage(message);
        resultBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if (error.equals("false")) {
                    startActivity(new Intent(MapsActivity.this, ProfileActivity.class));
                    finish();
                } else {
                    dialogInterface.dismiss();
                }
            }
        });
        resultBuilder.setCancelable(false);
        resultBuilder.show();

    }

    //Handling Volley Error
    public void parseVolleyError(VolleyError error) {
        try {
            String responseBody = new String(error.networkResponse.data, "utf-8");
            JSONObject data = new JSONObject(responseBody);
            String message = data.getString("Message");
            Toast.makeText(MapsActivity.this, message, Toast.LENGTH_LONG).show();
            android.app.AlertDialog.Builder loginErrorBuilder = new android.app.AlertDialog.Builder(MapsActivity.this);
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

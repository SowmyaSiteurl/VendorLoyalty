package in.siteurl.www.vendorloyalty;

import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import net.glxn.qrgen.android.QRCode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class QRCodeActivity extends AppCompatActivity {

    private ImageView mQrCode;
    private String mVendorHash;
    SharedPreferences loginpref;
    SharedPreferences.Editor editor;
    private Toolbar mToolbar;
    public static final int RequestPermissionCode = 1;
    private Bitmap mQrBitmap;
    private static final String IMAGE_DIRECTORY = "/TrendzVendor";
    private boolean mSaved = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);

        //Intializing views
        mQrCode = findViewById(R.id.iv_vendorqrcode);
        loginpref = getApplicationContext().getSharedPreferences("LoginPref", MODE_PRIVATE);
        editor = loginpref.edit();
        mVendorHash = loginpref.getString("hash", "");
        mToolbar = findViewById(R.id.qrcode_toolbar);
        mToolbar.setTitle("QR Code");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //To generate the qr code of vendor
        generatevendorqrcode();
    }

    //To generate the qr code of vendor
    private void generatevendorqrcode() {
        mQrBitmap = QRCode.from(mVendorHash).bitmap();
        mQrCode.setImageBitmap(mQrBitmap);
    }

    //To create menu options
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.qrcode_menu, menu);
        return true;
    }

    //on menu option selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemid = item.getItemId();
        if (itemid == R.id.saveqrcode_menu) {
            saveqrcodetogallery();
        }
        if (itemid == android.R.id.home) {
            onBackPressed();
        }

        return true;
    }


    private void saveqrcodetogallery() {
        if (ActivityCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermission();
            return;
        } else {
            // boolean check = loginpref.getBoolean("saved",false);

            /* if(!check) {
                saveImage(mQrBitmap);
            }*/

            File fileForCheck = new File(Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);

            if (fileForCheck.length() < 1) {
                saveImage(mQrBitmap);
            } else {
                Toast.makeText(this, "Already Saved To Gallery", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //Method to convert bitmap to base64
    public String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }


    //Requesting storage permissions
    private void requestPermission() {

        ActivityCompat.requestPermissions(this, new String[]
                {
                        WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE
                }, RequestPermissionCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {

            case RequestPermissionCode:

                if (grantResults.length > 0) {
                    boolean WriteStoragePermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean ReadStoragePermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (WriteStoragePermission && ReadStoragePermission) {
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    //To save image in gallery
    public String saveImage(Bitmap myBitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        File vendorDirectory = new File(
                Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);
        // have the object build the directory structure, if needed.

        if (!vendorDirectory.exists()) {
            vendorDirectory.mkdirs();
        }

        try {
            File f = new File(vendorDirectory, Calendar.getInstance()
                    .getTimeInMillis() + ".jpg");
            f.createNewFile();   //give read write permission
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            MediaScannerConnection.scanFile(this,
                    new String[]{f.getPath()},
                    new String[]{"image/jpeg"}, null);
            fo.close();
            /*Log.d("TAG", "File Saved::--->" + f.getAbsolutePath());*/
            editor.putBoolean("saved", true);
            editor.commit();
            Toast.makeText(this, "Saved To Gallery", Toast.LENGTH_SHORT).show();
            return f.getAbsolutePath();

        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return "";
    }
}

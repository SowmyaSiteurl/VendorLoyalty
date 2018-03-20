package in.siteurl.www.vendorloyalty;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.messaging.FirebaseMessaging;

public class SplashActivity extends AppCompatActivity {

    private ImageView mSplashBackground,mTest;
    SharedPreferences.Editor editor;
    SharedPreferences loginpref;
    String loginuserid,sessionid,uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mSplashBackground = findViewById(R.id.iv_splash);
        //mTest  = findViewById(R.id.iv_test);

        //Firebase subscribe
        FirebaseMessaging.getInstance().subscribeToTopic("global");

        //getting login details in preferences
        loginpref = getApplicationContext().getSharedPreferences("LoginPref", MODE_PRIVATE);
        sessionid = loginpref.getString("sessionid", null);
        uid = loginpref.getString("User-id", null);

        //To Load image in all devices
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL);
        Glide.with(this).load(R.drawable.splash).into(mSplashBackground);

        //Go to Login Page or main page
        GotoLoginorMain();

    }

    //Method to go login or main page
    private void GotoLoginorMain() {

        Thread timer = new Thread()
        {
            public void run()
            {
                try {
                    sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finally {
                    if (loginpref.contains("User-id"))
                    {
                        loginuserid = loginpref.getString("User-id", null);
                        if(loginuserid.equals("")||loginuserid.equals(null))
                        {
                            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                        }
                        else
                        {
                            startActivity(new Intent(SplashActivity.this, MainActivity.class));
                        }
                    }
                    else
                    {
                        startActivity(new Intent(SplashActivity.this,LoginActivity.class));
                    }
                }
            }
        };
        timer.start();
    }

    //To finish the activity from background
    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}

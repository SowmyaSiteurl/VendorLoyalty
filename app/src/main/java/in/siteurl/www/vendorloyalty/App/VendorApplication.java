package in.siteurl.www.vendorloyalty.App;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDex;

import com.evernote.android.job.JobManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.HashMap;

import in.siteurl.www.vendorloyalty.Helper.UpdateHelper;
import in.siteurl.www.vendorloyalty.Services.ConnectivityReceiver;
import in.siteurl.www.vendorloyalty.Services.VendorJobCreator;

/**
 * Created by siteurl on 9/1/18.
 */

public class VendorApplication extends Application {
    private static VendorApplication mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        JobManager.create(this).addJobCreator(new VendorJobCreator());
        int build = Build.VERSION.SDK_INT;
        if(build == Build.VERSION_CODES.M)
        {
            JobManager.instance().getConfig().setAllowSmallerIntervalsForMarshmallow(true); // Don't use this in production
        }

        //Checking if app is up-to-date
        final FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        HashMap<String,Object> defaultvalue = new HashMap<>();
        defaultvalue.put(UpdateHelper.KEY_UPDATE_ENABLE,false);
        defaultvalue.put(UpdateHelper.KEY_UPDATE_VERSION,"1.0");
        defaultvalue.put(UpdateHelper.KEY_UPDATE_URL,"https://play.google.com/apps/testing/com.mytrintrin.www.pbs_trintrin");

        firebaseRemoteConfig.setDefaults(defaultvalue);
        firebaseRemoteConfig.fetch(15)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            //Toast.makeText(MyApplication.this, "Checking Updates", Toast.LENGTH_SHORT).show();
                            firebaseRemoteConfig.activateFetched();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    public static synchronized VendorApplication getInstance() {
        return mInstance;
    }

    public void setConnectivityListener(ConnectivityReceiver.ConnectivityReceiverListener listener) {
        ConnectivityReceiver.connectivityReceiverListener = listener;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}

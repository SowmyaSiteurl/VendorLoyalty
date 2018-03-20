package in.siteurl.www.vendorloyalty.Helper;

import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

/**
 * Created by siteurl on 13/1/18.
 */

public class UpdateHelper {

    public static String KEY_UPDATE_ENABLE ="is_update";
    public static String KEY_UPDATE_VERSION ="version";
    public static String KEY_UPDATE_URL =" update_url";

    public interface onUpdateCheckListener
    {
        void onUpdateCheckListener(String urlApp);
    }


    public static Builder with(Context context)
    {
        return new Builder(context);
    }

    private onUpdateCheckListener onUpdateCheckListener;
    private Context context;

    public UpdateHelper( Context context,UpdateHelper.onUpdateCheckListener onUpdateCheckListener) {
        this.onUpdateCheckListener = onUpdateCheckListener;
        this.context = context;
    }

    public void check()
    {
        FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
        if(remoteConfig.getBoolean(KEY_UPDATE_ENABLE))

        {
            String currentversion = remoteConfig.getString(KEY_UPDATE_VERSION);
            String appversion = getAppversion(context);
            String updateurl = remoteConfig.getString(KEY_UPDATE_URL);
            // Toast.makeText(context, ""+currentversion, Toast.LENGTH_SHORT).show();
            if(!TextUtils.equals(currentversion,appversion)&& onUpdateCheckListener != null)
            {
                onUpdateCheckListener.onUpdateCheckListener(updateurl);
            }
        }
    }

    private String getAppversion(Context context) {
        String result = "";
        try {
            result = context.getPackageManager().getPackageInfo(context.getPackageName(),0).versionName;
            result = result.replace("[a-zA-Z]|-","");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static class Builder{

        private Context context;
        private onUpdateCheckListener onUpdateCheckListener;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder onUpdateCheck(onUpdateCheckListener onUpdateCheckListener)
        {
            this.onUpdateCheckListener = onUpdateCheckListener;
            return  this;
        }

        public UpdateHelper build()
        {
            return new UpdateHelper(context,onUpdateCheckListener);
        }

        public UpdateHelper check()
        {
            UpdateHelper updateHelper = build();
            updateHelper.check();

            return updateHelper;
        }
    }
}

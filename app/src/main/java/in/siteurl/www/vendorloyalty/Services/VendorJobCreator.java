package in.siteurl.www.vendorloyalty.Services;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

/**
 * Created by siteurl on 11/1/18.
 */

public class VendorJobCreator implements JobCreator {

    @Override
    public Job create(String tag) {
        switch (tag) {
            case VendorNotificationServices.TAG:
                return new VendorNotificationServices();
            default:
                return null;
        }
    }
}

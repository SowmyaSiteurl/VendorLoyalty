package in.siteurl.www.vendorloyalty;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by siteurl on 26/10/17.
 */

public class LoyaltySingleton {

        private static LoyaltySingleton minstance;
        private static Context mctx;
        private RequestQueue requestQueue;

        private LoyaltySingleton(Context context) {
            mctx = context;
            requestQueue = getRequestQueue();
        }


        public RequestQueue getRequestQueue() {
            if (requestQueue == null) {
                requestQueue = Volley.newRequestQueue(mctx.getApplicationContext());
            }
            return requestQueue;
        }

        public static synchronized LoyaltySingleton getInstance(Context context) {
            if (minstance == null) {
                minstance = new LoyaltySingleton(context);
            }
            return minstance;
        }

        public <T> void addtorequestqueue(Request<T> request) {
            requestQueue.add(request);
        }
}

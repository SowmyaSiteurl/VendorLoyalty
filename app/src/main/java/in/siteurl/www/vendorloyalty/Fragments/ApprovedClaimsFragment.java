package in.siteurl.www.vendorloyalty.Fragments;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import in.siteurl.www.vendorloyalty.API;
import in.siteurl.www.vendorloyalty.Adapters.ApprovedClaimsAdapter;
import in.siteurl.www.vendorloyalty.LoyaltySingleton;
import in.siteurl.www.vendorloyalty.Model.ApprovedClaim;
import in.siteurl.www.vendorloyalty.R;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class ApprovedClaimsFragment extends Fragment {

    private ListView mApprovedClaims;
    private RelativeLayout mRootLayout;
    SharedPreferences loginpref;
    String sessionid, uid;
    private ArrayList<ApprovedClaim> mApprovedClaimList;
    private ApprovedClaimsAdapter mApprovedClaimAdapter;


    public ApprovedClaimsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_approved_claims, container, false);
        mApprovedClaims = view.findViewById(R.id.lv_approvedclaims);
        mRootLayout = view.findViewById(R.id.approvedclaimsrootlayout);
        loginpref = getActivity().getSharedPreferences("LoginPref", MODE_PRIVATE);
        sessionid = loginpref.getString("sessionid", null);
        uid = loginpref.getString("User-id", null);
        mApprovedClaimList = new ArrayList<>();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getAllApprovedClaims();
    }

    private void getAllApprovedClaims() {
        StringRequest getapprovedclaimrequest = new StringRequest(Request.Method.POST,
                API.approvedlist,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject responsefromserver = new JSONObject(response);
                            String error = responsefromserver.getString("Error");
                            String message = responsefromserver.getString("Message");
                            if (error.equals("false")) {
                                JSONArray data = responsefromserver.getJSONArray("data");
                                if (data.length() > 0) {
                                    mApprovedClaimList.clear();
                                    for (int i = 0; i < data.length(); i++) {
                                        JSONObject approvedclaim = data.getJSONObject(i);
                                        String purchaseamount = approvedclaim.getString("purchase_amount");
                                        String pointsearned = approvedclaim.getString("points_earned");
                                        String approvaldate = approvedclaim.getString("approval_date");
                                        JSONObject user = approvedclaim.getJSONObject("user_details");
                                        String username = user.getString("name");

                                        ApprovedClaim approvedClaim = new ApprovedClaim(username, purchaseamount, approvaldate, pointsearned);
                                        mApprovedClaimList.add(approvedClaim);
                                    }

                                    mApprovedClaimAdapter = new ApprovedClaimsAdapter(getActivity(), R.layout.approved_claims_item, mApprovedClaimList);
                                    mApprovedClaims.setAdapter(mApprovedClaimAdapter);

                                } else {
                                    Snackbar.make(mRootLayout, "No Approved Claims Found", Snackbar.LENGTH_SHORT)
                                            .show();
                                }
                            } else {
                                Snackbar.make(mRootLayout, message, Snackbar.LENGTH_LONG)
                                        .show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new Hashtable<String, String>();
                params.put("user_id", uid);
                params.put("api_key", API.APIKEY);
                params.put("sid", sessionid);
                return params;
            }
        };
        getapprovedclaimrequest.setRetryPolicy(new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        LoyaltySingleton.getInstance(getContext()).addtorequestqueue(getapprovedclaimrequest);
    }

}


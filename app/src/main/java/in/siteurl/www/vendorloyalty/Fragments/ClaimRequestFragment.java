package in.siteurl.www.vendorloyalty.Fragments;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

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
import in.siteurl.www.vendorloyalty.AcceptDeclineActivity;
import in.siteurl.www.vendorloyalty.Adapters.AcceptDeclineAdapter;
import in.siteurl.www.vendorloyalty.LoyaltySingleton;
import in.siteurl.www.vendorloyalty.Model.AcceptDecline;
import in.siteurl.www.vendorloyalty.R;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class ClaimRequestFragment extends Fragment {

    SharedPreferences loginpref;
    String sessionid, uid;
    ArrayList<AcceptDecline> mAcceptDeclineArrayList;
    ListView mAcceptDeclineList;


    public ClaimRequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_claim_request, container, false);
        loginpref = getActivity().getSharedPreferences("LoginPref", MODE_PRIVATE);
        sessionid = loginpref.getString("sessionid", null);
        uid = loginpref.getString("User-id", null);
        mAcceptDeclineList = view.findViewById(R.id.lv_claimrequests);
        mAcceptDeclineArrayList= new ArrayList<>();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getAllSeenClaims();
    }

    private void getAllSeenClaims() {
        StringRequest allseenrequest = new StringRequest(Request.Method.POST,
                API.allseen,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("seen response",response);
                        try {
                            JSONObject responsefromserver = new JSONObject(response);
                            String response_error = responsefromserver.getString("Error");
                            String message = responsefromserver.getString("Message");
                            if (response_error.equals("false")) {
                                JSONArray data = responsefromserver.getJSONArray("data");
                                if (data.length() > 0) {
                                    for (int i =0;i<data.length();i++ ) {
                                        JSONObject seenobject = data.getJSONObject(i);
                                        String status = seenobject.getString("status");
                                        if(status.equals("New")||status.equals("Seen"))
                                        {
                                            String description = seenobject.getString("description");
                                            String purchaseamount = seenobject.getString("purchase_amount");
                                            String pointsearnedid = seenobject.getString("points_earned_id");
                                            JSONObject userdetails = seenobject.getJSONObject("user_details");
                                            String name = userdetails.getString("name");
                                            String productimage = seenobject.getString("prod_img");
                                            AcceptDecline acceptDecline = new AcceptDecline(name, pointsearnedid, description, purchaseamount,productimage);
                                            mAcceptDeclineArrayList.add(acceptDecline);
                                        }
                                    }
                                    AcceptDeclineAdapter acceptDeclineAdapter = new AcceptDeclineAdapter(getContext(),R.layout.acceptdecline_item,mAcceptDeclineArrayList);
                                    mAcceptDeclineList.setAdapter(acceptDeclineAdapter);

                                } else {

                                    Toast.makeText(getContext(), "No Claim Request", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new Hashtable<String, String>();
                params.put("user_id", uid);
                params.put("api_key", API.APIKEY);
                params.put("sid",sessionid);
                return params;
            }
        };
        allseenrequest.setRetryPolicy(new DefaultRetryPolicy(30000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        LoyaltySingleton.getInstance(getContext()).addtorequestqueue(allseenrequest);
    }

}

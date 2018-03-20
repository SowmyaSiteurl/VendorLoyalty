package in.siteurl.www.vendorloyalty;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import in.siteurl.www.vendorloyalty.Adapters.ClaimRequestPagerAdapter;
import in.siteurl.www.vendorloyalty.App.VendorApplication;
import in.siteurl.www.vendorloyalty.Fragments.ApprovedClaimsFragment;
import in.siteurl.www.vendorloyalty.Fragments.ClaimRequestFragment;
import in.siteurl.www.vendorloyalty.Services.ConnectivityReceiver;

public class ClaimRequestActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener {

    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private ClaimRequestPagerAdapter mClaimRequestPagerAdapter;
    private Toolbar mToolbar;
    Dialog alertDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_claim_request);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mToolbar = findViewById(R.id.claimrequesttoolbar);
        mToolbar.setTitle("Claims");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mTabLayout = findViewById(R.id.claimtablayout);
        mTabLayout.setSelectedTabIndicatorColor(Color.parseColor("#FFFFFF"));
        alertDialog = new Dialog(this);
        mViewPager = findViewById(R.id.vp_claimrequest);
        mClaimRequestPagerAdapter = new ClaimRequestPagerAdapter(getSupportFragmentManager());
        checkConnection();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemid = item.getItemId();
        if(itemid == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }


    private void checkConnection() {
        boolean isConnected = ConnectivityReceiver.isConnected();
        showSnack(isConnected);
    }

    // Showing the status in Snackbar
    private void showSnack(boolean isConnected) {
        if (isConnected) {

            mClaimRequestPagerAdapter.addFragments(new ClaimRequestFragment(),"Claim Request");
            mClaimRequestPagerAdapter.addFragments(new ApprovedClaimsFragment(),"Approved Claims");
            mViewPager.setAdapter(mClaimRequestPagerAdapter);
            mTabLayout.setupWithViewPager(mViewPager);
            if(alertDialog.isShowing())
            {
                alertDialog.dismiss();
            }
        } else {
            shownointernetdialog();
        }
    }

    //To show no internet dialog
    private void shownointernetdialog() {
        //alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.setContentView(R.layout.nointernet);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.setCancelable(false);
        Button retry = alertDialog.findViewById(R.id.exit_btn);
        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                //checkConnection();
                System.exit(0);
            }
        });
        alertDialog.show();
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        showSnack(isConnected);
    }

    @Override
    protected void onResume() {
        super.onResume();
        VendorApplication.getInstance().setConnectivityListener(this);
    }
}

package in.siteurl.www.vendorloyalty;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import in.siteurl.www.vendorloyalty.Model.Transactions;

public class IndivisualTransactionDetails extends AppCompatActivity {

    TextView trnsDescription, trnsClosingBalance, trnsApprovedAmount, trnsApprovedDate ,purchaseAmount;
    ImageView trnsImage;
    SharedPreferences loginpref;
    String sessionid, uid;
    Transactions transactions;
    private Toolbar mToolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_indivisual_transaction_details);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

      /*  mToolbar = findViewById(R.id.indivisualTranscationtoolbar);
        mToolbar.setTitle("Transaction History");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);*/


        loginpref = getApplicationContext().getSharedPreferences("LoginPref", MODE_PRIVATE);
        sessionid = loginpref.getString("sessionid", null);
        uid = loginpref.getString("User-id", null);

      /*  mToolbar = findViewById(R.id.profiletoolbar);
        mToolbar.setTitle("Transaction");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);*/


        trnsApprovedAmount = findViewById(R.id.trns_points);
        trnsApprovedDate = findViewById(R.id.trnsExpirydate);
        trnsClosingBalance = findViewById(R.id.trns_closingbalance);
        trnsDescription = findViewById(R.id.trans_description);
        purchaseAmount = findViewById(R.id.tv_purchaseAmount);
        trnsImage = findViewById(R.id.trans_image);

        transactions = (Transactions) getIntent().getSerializableExtra("transactionDetails");

        trnsDescription.setText(transactions.getmName());
        trnsClosingBalance.setText("Closing Balance  " + transactions.getmClosingBalance() +" Pts");
        trnsApprovedAmount.setText("Approved Amount  " + transactions.getmApprovedAmount() +" Pts");
        purchaseAmount.setText("Purchase Amount "+ transactions.getmPurchaseAmount());
        trnsApprovedDate.setText(transactions.getmDate());

        //glide image
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(R.drawable.header);
        requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL);
        requestOptions.fitCenter();

        Glide.with(getApplicationContext()).load(transactions.getProdImage())
                .thumbnail(0.5f)
                .apply(requestOptions)
                .into(trnsImage);

    }
}

package in.siteurl.www.vendorloyalty.Adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import in.siteurl.www.vendorloyalty.IndivisualTransactionDetails;
import in.siteurl.www.vendorloyalty.Model.Transactions;
import in.siteurl.www.vendorloyalty.R;

public class TransactionsAdapter extends RecyclerView.Adapter<TransactionsAdapter.ViewHolder> implements Filterable {

    private List<String> friends;
    private Activity activity;

    private ArrayList<Transactions> mTransactionsList;
    private ArrayList<Transactions> mTransactionsList1;
    private Context mContext;

    Date approvedDate;

    public TransactionsAdapter(Activity activity, List<String> friends) {
        this.friends = friends;
        this.activity = activity;
    }

    public TransactionsAdapter(Context context, ArrayList<Transactions> transactions) {
        this.mContext = context;
        this.mTransactionsList = transactions;
        this.mTransactionsList1 = transactions;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.transaction_item_3, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TransactionsAdapter.ViewHolder viewHolder, final int position) {

        String name = mTransactionsList.get(position).getmName();
        //To convert first letter to uppercase
        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        viewHolder.mUserName.setText(name);

//        viewHolder.mOpeningBalance.setText(mTransactionsList.get(position).getmOpeningBalance() + " Rs");
        viewHolder.mClosingBalance.setText(mTransactionsList.get(position).getmClosingBalance() + " Pts");
        viewHolder.mApprovedAmount.setText(mTransactionsList.get(position).getmApprovedAmount() + " Pts");

        String date = mTransactionsList.get(position).getmDate();
        //SimpleDateFormat readDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat readDate = new SimpleDateFormat("yyyy-MM-dd");
        //SimpleDateFormat writeDate = new SimpleDateFormat("E, MMM dd yyyy");
        SimpleDateFormat writeDate = new SimpleDateFormat("dd MMM yyyy");
        try {

            approvedDate = readDate.parse(date);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        date = writeDate.format(approvedDate);

        viewHolder.mApprovedDate.setText(date);


        viewHolder.trnsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               /* Intent intent = new Intent(mContext, IndivisualTransactionDetails.class);
                intent.putExtra("transactionDetails", mTransactionsList.get(position));

                mContext.startActivity(intent);*/

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
                alertDialog.setTitle("History");

                LayoutInflater inflater = LayoutInflater.from(mContext);
                View alertView = inflater.inflate(R.layout.activity_indivisual_transaction_details, null);
                alertDialog.setView(alertView);

                TextView trnsDescription, trnsClosingBalance, trnsApprovedAmount, trnsApprovedDate, purchaseAmount;
                ImageView trnsImage;

                trnsApprovedAmount = alertView.findViewById(R.id.trns_points);
                trnsApprovedDate = alertView.findViewById(R.id.trnsExpirydate);
                trnsClosingBalance = alertView.findViewById(R.id.trns_closingbalance);
                trnsDescription = alertView.findViewById(R.id.trans_description);
                purchaseAmount = alertView.findViewById(R.id.tv_purchaseAmount);
                trnsImage = alertView.findViewById(R.id.trans_image);


                trnsDescription.setText(mTransactionsList.get(position).getmName());
                trnsClosingBalance.setText("Closing Balance  " + mTransactionsList.get(position).getmClosingBalance() + " Pts");
                trnsApprovedAmount.setText("Approved Amount  " + mTransactionsList.get(position).getmApprovedAmount() + " Pts");
                purchaseAmount.setText("Purchase Amount " + mTransactionsList.get(position).getmPurchaseAmount());
                // trnsApprovedDate.setText(mTransactionsList.get(position).getmDate());

                String date = mTransactionsList.get(position).getmDate();
                SimpleDateFormat readDate = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat writeDate = new SimpleDateFormat("dd MMM yyyy");
                try {
                    approvedDate = readDate.parse(date);

                } catch (ParseException e) {
                    e.printStackTrace();
                }
                date = writeDate.format(approvedDate);
                trnsApprovedDate.setText(date);

                //glide image
                RequestOptions requestOptions = new RequestOptions();
                requestOptions.placeholder(R.drawable.header);
                requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL);
                requestOptions.fitCenter();

                Glide.with(mContext).load(mTransactionsList.get(position).getProdImage())
                        .thumbnail(0.5f)
                        .apply(requestOptions)
                        .into(trnsImage);


                alertDialog.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();

                    }
                });

                AlertDialog dialog = alertDialog.create();
                dialog.show();


            }
        });
    }

    @Override
    public int getItemCount() {
        return (null != mTransactionsList ? mTransactionsList.size() : 0);
    }

    /**
     * View holder to display each RecylerView item
     */
    protected class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mUserName;
        private TextView mOpeningBalance;
        private TextView mClosingBalance;
        private TextView mApprovedAmount;
        private TextView mApprovedDate;
        private RelativeLayout trnsLayout;

        public ViewHolder(View view) {
            super(view);
            mUserName = view.findViewById(R.id.tv_pointsdesc);
           /* mOpeningBalance = view.findViewById(R.id.tv_openingbalance);*/

            mApprovedAmount = view.findViewById(R.id.tv_points);
            mClosingBalance = view.findViewById(R.id.tv_closingbalance);
            mApprovedDate = view.findViewById(R.id.tv_date);
            trnsLayout = view.findViewById(R.id.trnsLayout);
        }
    }

    //search functionality
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {

                String charString = charSequence.toString();
                if (charString.isEmpty()) {

                    mTransactionsList = mTransactionsList1;
                } else {
                    ArrayList<Transactions> filteredList = new ArrayList<>();
                    for (Transactions row : mTransactionsList1) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.getmOpeningBalance().toLowerCase().contains(charString.toLowerCase()) || row.getmClosingBalance().contains(charSequence)) {
                            filteredList.add(row);
                        }
                    }

                    mTransactionsList = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = mTransactionsList;
                return filterResults;

            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {

                mTransactionsList = (ArrayList<Transactions>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }
}
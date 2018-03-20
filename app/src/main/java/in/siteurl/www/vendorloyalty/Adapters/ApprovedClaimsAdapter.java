package in.siteurl.www.vendorloyalty.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import in.siteurl.www.vendorloyalty.Model.ApprovedClaim;
import in.siteurl.www.vendorloyalty.R;

/**
 * Created by siteurl on 5/1/18.
 */

public class ApprovedClaimsAdapter extends ArrayAdapter<ApprovedClaim> {

    private ArrayList<ApprovedClaim> mApprovedClaimList;
    private ArrayList<ApprovedClaim> mFilteredList;
    private ApprovedClaimFilter filter;
    Context context;
    Date approvedonDate;

    public ApprovedClaimsAdapter(Context context, int textViewResourceId, ArrayList<ApprovedClaim> approvedList) {
        super(context, textViewResourceId, approvedList);
        this.mFilteredList = new ArrayList<ApprovedClaim>();
        this.mFilteredList.addAll(approvedList);
        this.mApprovedClaimList = new ArrayList<ApprovedClaim>();
        this.mApprovedClaimList.addAll(approvedList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.approved_claims_item, parent, false);
        }

        ApprovedClaim approvedClaim = mApprovedClaimList.get(position);

        TextView buyername = convertView.findViewById(R.id.tv_approvedname);
        String name = approvedClaim.getmName();
        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        buyername.setText(name);

        TextView purchasedamount = convertView.findViewById(R.id.tv_purchasedamount);
        purchasedamount.setText(approvedClaim.getmPurchasedAmount()+" Rs");

        TextView approvedon = convertView.findViewById(R.id.tv_approvedondate);

        String approveddate = approvedClaim.getmApprovedOn();
        SimpleDateFormat readDate = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat writeDate = new SimpleDateFormat("dd MMM yyyy");
        try {
            approvedonDate = readDate.parse(approveddate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        approveddate = writeDate.format(approvedonDate);
        approvedon.setText(approveddate);

        TextView approvedpoints = convertView.findViewById(R.id.tv_approvedpoints);
        approvedpoints.setText(approvedClaim.getmPoints());

        return convertView;
    }

    @Override
    public Filter getFilter() {
        if (filter == null){
            filter  = new ApprovedClaimFilter();
        }
        return filter;
    }

    private class ApprovedClaimFilter extends Filter
    {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            constraint = constraint.toString().toLowerCase();
            FilterResults result = new FilterResults();
            if(constraint != null && constraint.toString().length() > 0)
            {
                ArrayList<ApprovedClaim> filteredItems = new ArrayList<ApprovedClaim>();

                for(int i = 0, l = mApprovedClaimList.size(); i < l; i++)
                {
                    ApprovedClaim mycountry = mApprovedClaimList.get(i);
                    if(mycountry.toString().toLowerCase().contains(constraint))
                        filteredItems.add(mycountry);
                }
                result.count = filteredItems.size();
                result.values = filteredItems;
            }
            else
            {
                synchronized(this)
                {
                    result.values = mApprovedClaimList;
                    result.count = mApprovedClaimList.size();
                }
            }
            return result;
        }
        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint,FilterResults results)
        {
            mFilteredList = (ArrayList<ApprovedClaim>)results.values;
            notifyDataSetChanged();
            clear();
            for(int i = 0, l = mFilteredList.size(); i < l; i++)
                add(mFilteredList.get(i));
            notifyDataSetInvalidated();
        }
    }
}

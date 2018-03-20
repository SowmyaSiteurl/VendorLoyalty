package in.siteurl.www.vendorloyalty.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;

import java.util.ArrayList;

import in.siteurl.www.vendorloyalty.AddorEditOfferActivity;
import in.siteurl.www.vendorloyalty.Model.ViewOffer;
import in.siteurl.www.vendorloyalty.R;

/**
 * Created by siteurl on 18/12/17.
 */

public class ViewOfferAdapter extends ArrayAdapter<ViewOffer> {

    private ArrayList<ViewOffer> mAllOffers;
    private ArrayList<ViewOffer> mSearchedOffers;
    private OffersProductFilter filter;
    RequestOptions imageoptions = new RequestOptions();


    public ViewOfferAdapter(@NonNull Context context, int textViewResourceId, ArrayList<ViewOffer> viewOffers) {
        super(context, textViewResourceId, viewOffers);
        this.mSearchedOffers = new ArrayList<ViewOffer>();
        this.mSearchedOffers.addAll(viewOffers);
        this.mAllOffers = new ArrayList<ViewOffer>();
        this.mAllOffers.addAll(viewOffers);

        imageoptions.diskCacheStrategy(DiskCacheStrategy.ALL);
       // imageoptions.fitCenter();
        imageoptions.placeholder(R.drawable.header);
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new OffersProductFilter();
        }
        return filter;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.vendor_offers, parent, false);
        }

        final ViewOffer viewOffer = mAllOffers.get(position);

        TextView productname = convertView.findViewById(R.id.vendor_offer_productname);
        String product = viewOffer.getmOffername();
        //productname.setText(product.substring(0, 1).toUpperCase() + product.substring(1));
        productname.setText(product);

        TextView productdescription = convertView.findViewById(R.id.vendor_offer_description);
        productdescription.setText(viewOffer.getmOfferdescription());

        final ImageView productImage = convertView.findViewById(R.id.vendor_offer_productimage);

        Glide.with(getContext()).load(viewOffer.getmOfferimage())
                .thumbnail(0.5f)
                .apply(imageoptions)
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {

                        resource.getIntrinsicWidth();
                        resource.getIntrinsicHeight();
                        productImage.setImageDrawable(resource);

                        if (resource.getIntrinsicWidth() < 1080 && resource.getIntrinsicHeight() < 480) {

                            productImage.setScaleType(ImageView.ScaleType.FIT_XY);

                            //  productImage.setMaxHeight(480);
                            //  productImage.setMaxWidth(1080);
                        }

                    }
                });

        TextView expirydate = convertView.findViewById(R.id.vendor_offer_expiry);
        expirydate.setText(viewOffer.getmExpirydate());

        TextView productprice = convertView.findViewById(R.id.vendor_offer_productprice);
        productprice.setText("₹ " + viewOffer.getmOfferprice());

        TextView terms = convertView.findViewById(R.id.vendor_offer_termsandcondition);
        terms.setText(viewOffer.getmTermsAndCondtion());

        TextView status = convertView.findViewById(R.id.vendor_offer_status);
        status.setText(viewOffer.getmStatus());

        ImageView editoffer = convertView.findViewById(R.id.vendor_offer_editoffer);
        editoffer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(getContext(), "Edit", Toast.LENGTH_SHORT).show();
                Intent editIntent = new Intent(getContext(), AddorEditOfferActivity.class);
                editIntent.putExtra("editoffer", viewOffer);
                editIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(editIntent);
            }
        });

        return convertView;
    }

    private class OffersProductFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            constraint = constraint.toString().toLowerCase();
            FilterResults result = new FilterResults();
            if (constraint != null && constraint.toString().length() > 0) {
                ArrayList<ViewOffer> filteredItems = new ArrayList<ViewOffer>();

                for (int i = 0, l = mAllOffers.size(); i < l; i++) {

                    ViewOffer offer = mAllOffers.get(i);
                    if (offer.toString().toLowerCase().contains(constraint))
                        filteredItems.add(offer);
                }
                result.count = filteredItems.size();
                result.values = filteredItems;
            } else {
                synchronized (this) {
                    result.values = mAllOffers;
                    result.count = mAllOffers.size();
                }
            }
            return result;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mAllOffers = (ArrayList<ViewOffer>) results.values;
            notifyDataSetChanged();
            clear();
            for (int i = 0, l = mAllOffers.size(); i < l; i++)
                add(mAllOffers.get(i));
            notifyDataSetInvalidated();
        }
    }
}

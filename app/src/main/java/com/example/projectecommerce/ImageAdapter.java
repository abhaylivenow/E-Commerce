package com.example.projectecommerce;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private Context mContext;
    private List<Upload> mUploads;
    private onClickListener mOnClickListener;

    public ImageAdapter(Context context , List<Upload> upload, onClickListener onClickListener){
        mContext = context;
        mUploads = upload;
        this.mOnClickListener = onClickListener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.image_item_layout, parent, false);
        return new ImageViewHolder(v,mOnClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Upload currentUpload = mUploads.get(position);
        holder.product_name.setText(currentUpload.getmName());
        holder.product_price.setText(currentUpload.getM_price());
        holder.product_des.setText(currentUpload.getM_description());

        Picasso.get().load(currentUpload.getmImageUrl()).placeholder(R.mipmap.ic_launcher)
                .fit().centerCrop().into(holder.product_image);
    }

    @Override
    public int getItemCount() {
        return mUploads.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView product_name, product_des, product_price;
        public ImageView product_image;
        onClickListener onClickListener;

        public ImageViewHolder(@NonNull View itemView, onClickListener onClickListener) {
            super(itemView);
            product_name = itemView.findViewById(R.id.product_name);
            product_price = itemView.findViewById(R.id.product_price);
            product_des = itemView.findViewById(R.id.product_description);
            product_image = itemView.findViewById(R.id.product_image);
            this.onClickListener = onClickListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onClickListener.onProductCLick(getAdapterPosition());
        }
    }
    // custom interface created for listening on click to the recycler view
    public interface onClickListener{
        void onProductCLick(int position);
    }
}

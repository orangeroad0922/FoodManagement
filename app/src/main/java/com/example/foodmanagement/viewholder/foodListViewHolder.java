package com.example.foodmanagement.viewholder;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.foodmanagement.R;

public class foodListViewHolder extends RecyclerView.ViewHolder {
    public TextView titleView;
    public TextView detailView;

    public foodListViewHolder( View itemView ) {
        super( itemView );
        titleView = (TextView) itemView.findViewById( R.id.title );
        detailView = (TextView) itemView.findViewById( R.id.expiration );

    }
}

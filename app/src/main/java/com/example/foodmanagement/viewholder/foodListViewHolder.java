package com.example.foodmanagement.viewholder;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.foodmanagement.R;

public class foodListViewHolder extends RecyclerView.ViewHolder {
    public TextView foodNameView;
    public TextView expirationView;

    public foodListViewHolder( View itemView ) {
        super( itemView );
        foodNameView = (TextView) itemView.findViewById( R.id.foodname );
        expirationView = (TextView) itemView.findViewById( R.id.expiration );

    }
}

package com.example.foodmanagement.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.example.foodmanagement.R;
import com.example.foodmanagement.model.FoodData;
import com.example.foodmanagement.viewholder.foodListViewHolder;

import java.text.SimpleDateFormat;
import java.util.List;

public class foodListRecycleViewAdapter extends RecyclerView.Adapter<foodListViewHolder>{

    private List<FoodData> list;

    public foodListRecycleViewAdapter( List<FoodData> list) {
        this.list = list;
    }

    @Override
    public foodListViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate( R.layout.row, parent,false);
        foodListViewHolder vh = new foodListViewHolder(inflate);
        return vh;
    }

    @Override
    public void onBindViewHolder( foodListViewHolder holder, int position) {
        holder.titleView.setText(list.get(position).getFoodName());
        holder.detailView.setText(new SimpleDateFormat("yyyy/MM/dd").format(list.get(position).getExpiration()));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }}

package com.example.sondre_lab_2;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {


    // Moves the user to the correct article
    static void moveToArticle(int value, View v) {
        // Starts new activity for displaying single item
        Intent intent = new Intent(v.getContext(), ActivityContentDisplay.class);
        intent.putExtra("link", ((RssFeedModel)(MainActivity.newsList.get(value))).link);

        v.getContext().startActivity(intent);
    }

    private List<RssFeedModel> mData;
    private LayoutInflater mInflater;

    // data is passed into the constructor
    MyRecyclerViewAdapter(Context context, List<RssFeedModel> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.list_view_row, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        RssFeedModel data = mData.get(position);
        holder.myTextView.setText(data.title);
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView myTextView;

        @Override
        public void onClick(View v) {
            moveToArticle(returnPosition(v), v);
        }

        ViewHolder(View itemView) {
            super(itemView);
            myTextView = itemView.findViewById(R.id.feedTitle);
            itemView.setOnClickListener(this);
        }

        public int returnPosition(View view) {
            return this.getAdapterPosition();
        }
    }
}
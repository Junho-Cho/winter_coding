package com.example.lgpc.winter_coding;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

/**
 * Created by LGPC on 2018-11-01.
 */

public class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView ivPhoto;
        ImageView ivLargePhoto;

        MyViewHolder(View view) {
            super(view);
            ivPhoto = view.findViewById(R.id.iv_list_thumbnail);
            ivLargePhoto = view.findViewById(R.id.iv_list_large);

            ivPhoto.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (view == ivPhoto) {
                Bitmap bitmap = ((BitmapDrawable)ivLargePhoto.getDrawable()).getBitmap();
                ByteArrayOutputStream bs = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bs);
                byte[] bytes = bs.toByteArray();

                Intent intent = new Intent(MainActivity.mContext, DetailActivity.class);
                intent.putExtra("LargePhoto", bytes);
                MainActivity.mContext.startActivity(intent);
            }
        }
    }

    private ArrayList<PhotoInfo> photoInfoArrayList;
    MyAdapter(ArrayList<PhotoInfo> photoInfoArrayList) {
        this.photoInfoArrayList = photoInfoArrayList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_items, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        MyViewHolder myViewHolder = (MyViewHolder) holder;
        myViewHolder.ivPhoto.setImageBitmap(photoInfoArrayList.get(position).thumbnail);
        myViewHolder.ivLargePhoto.setImageBitmap(photoInfoArrayList.get(position).largeImage);
    }

    @Override
    public int getItemCount() {
        return photoInfoArrayList.size();
    }
}

package com.bene.wintexasholdemfinal.custom_spinner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bene.wintexasholdemfinal.R;

public class ImageAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private int[] img_resources;

    ImageAdapter(Context appContext, int[] img_resources) {
        inflater = (LayoutInflater.from(appContext));
        this.img_resources = img_resources;
    }

    @Override
    public int getCount() {
        return img_resources.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.spinner_custom_layout, null);
        ImageView icon = convertView.findViewById(R.id.imageView);
        icon.setImageResource(img_resources[i]);
        return convertView;
    }
}

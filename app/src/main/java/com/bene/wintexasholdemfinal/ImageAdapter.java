package com.bene.wintexasholdemfinal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter {
    private Context context;
    private final int[] symbol_imgs = {R.drawable.clubs_dark_theme, R.drawable.diamonds_dark_theme, R.drawable.spades_dark_theme, R.drawable.hearts_dark_theme};
    private LayoutInflater inflater;

    ImageAdapter(Context appContext) {
        this.context = appContext;
        inflater = (LayoutInflater.from(appContext));
    }

    @Override
    public int getCount() {
        return symbol_imgs.length;
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
        ImageView icon = (ImageView) convertView.findViewById(R.id.imageView);
        icon.setImageResource(symbol_imgs[i]);
        return convertView;
    }
}

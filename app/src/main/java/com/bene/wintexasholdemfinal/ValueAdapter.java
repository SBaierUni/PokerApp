package com.bene.wintexasholdemfinal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ValueAdapter extends BaseAdapter {
    private Context context;
    private String[] values = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};
    private LayoutInflater inflter;

    public ValueAdapter(Context appContext) {
        this.context = appContext;
        inflter = (LayoutInflater.from(appContext));
    }

    @Override
    public int getCount() {
        return values.length;
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
        convertView = inflter.inflate(R.layout.spinner_custom_layout, null);
        TextView view = (TextView) convertView.findViewById(R.id.textView);
        view.setText(values[i]);
        return convertView;
    }
}

package com.bene.wintexasholdemfinal;

import android.content.Context;
import android.graphics.Color;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

// TODO maybe replace T with 10 for better UI experience
public class CardSpinner {
    private Spinner value;
    private Spinner symbol;
    private final String[] card_values = {"2","3","4","5","6","7","8","9","T","J","Q","K","A"};
    private final char[] card_symbols = {'C','D','S','H'};

    public CardSpinner(Spinner val, Spinner sym) {
        this.value = val;
        this.symbol = sym;
    }

    public boolean compareID(int id) {
        return id == value.getId() || id == symbol.getId();
    }

    public void setVisibility(int visibility) {
        value.setVisibility(visibility);
        symbol.setVisibility(visibility);
    }

    public String getText() {
        return card_symbols[symbol.getSelectedItemPosition()] + "" + value.getSelectedItem().toString();
    }

    // TODO color change on recognition not working
    public void updateTextColor() {
        TextView valSpinner;

        // spinner has no elements yet
        if(value.getChildAt(0) == null)
            valSpinner = (TextView) value.getAdapter().getView(0, null, value);
        else
            valSpinner = (TextView) value.getChildAt(0);

        if (symbol.getSelectedItemPosition() % 2 == 0)
            valSpinner.setTextColor(Color.BLACK);
        else
            valSpinner.setTextColor(Color.parseColor("#cb2529"));   // special color red
    }

    public int getSelectedValuePosition() {
        return value.getSelectedItemPosition();
    }

    public int getSelectedSymbolPosition() {
        return symbol.getSelectedItemPosition();
    }

    public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener listener) {
        value.setOnItemSelectedListener(listener);
        symbol.setOnItemSelectedListener(listener);
    }

    public void setAdapter(Context context) {
        value.setAdapter(new ArrayAdapter<String>(context, R.layout.spinner_custom_numbers_layout, card_values));
        symbol.setAdapter(new ImageAdapter(context));
    }

    public void setValueSelection(int position) {
        value.setSelection(position);
        updateTextColor();
    }

    public void setSymbolSelection(int position) {
        symbol.setSelection(position);
    }

    /** only valid format e.g. H5 */
    public void setSelection(String card) {
        for(int i = 0; i < card_symbols.length; i++)
            if(card_symbols[i] == card.charAt(0))
                setSymbolSelection(i);

        for(int i = 0; i < card_values.length; i++)
            if (card_values[i].charAt(0) == card.charAt(1))
                setValueSelection(i);
    }
}

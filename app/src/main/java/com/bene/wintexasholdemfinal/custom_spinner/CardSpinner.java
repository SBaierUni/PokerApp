package com.bene.wintexasholdemfinal.custom_spinner;

import android.content.Context;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.bene.wintexasholdemfinal.R;

public class CardSpinner {
    private Context context;
    private Spinner value;
    private Spinner symbol;
    private final String[] card_values = {"-","2","3","4","5","6","7","8","9","T","J","Q","K","A"};
    private final int[] card_symbol_images = {R.drawable.none, R.drawable.clubs_dark, R.drawable.diamonds_dark, R.drawable.spades_dark, R.drawable.hearts_dark};
    private final char[] card_symbols = {'-','C','D','S','H'};

    public CardSpinner(Context context, Spinner val, Spinner sym) {
        this.context = context;
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

    /** Adjust the value color to the symbol */
    public void updateTextColor() {
        if (symbol.getSelectedItemPosition() == 0)
            setTextColor(ContextCompat.getColor(context, R.color.theme_white));
        else if (symbol.getSelectedItemPosition() % 2 == 0)
            setTextColor(ContextCompat.getColor(context, R.color.theme_red));
        else
            setTextColor(ContextCompat.getColor(context, R.color.theme_blue));
    }

    public void setTextColor(int color) {
        TextView valSpinner;

        // if spinner has no elements yet
        if(value.getChildAt(0) == null)
            valSpinner = (TextView) value.getAdapter().getView(0, null, value);
        else
            valSpinner = (TextView) value.getChildAt(0);

        valSpinner.setTextColor(color);
    }

    /** Callback if CardSpinner object has been selected */
    public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener listener) {
        value.setOnItemSelectedListener(listener);
        symbol.setOnItemSelectedListener(listener);
    }

    public void setAdapter(Context context) {
        value.setAdapter(new ArrayAdapter<>(context, R.layout.spinner_custom_numbers_layout, card_values));
        symbol.setAdapter(new ImageAdapter(context, card_symbol_images));
    }

    public void setValueSelection(int position) {
        value.setSelection(position);
        updateTextColor();
    }

    public void setSymbolSelection(int position) {
        symbol.setSelection(position);
    }

    public boolean isValid() {
        return symbol.getSelectedItemPosition() != 0 && value.getSelectedItemPosition() != 0;
    }

    public void reset() {
        symbol.setSelection(0);
        value.setSelection(0);
    }

    /** Only valid format e.g. H7 */
    public void setSelection(String card) {
        for(int i = 0; i < card_values.length; i++)
            if (card_values[i].charAt(0) == card.charAt(1))
                setValueSelection(i);

        for(int i = 0; i < card_symbols.length; i++)
            if(card_symbols[i] == card.charAt(0))
                setSymbolSelection(i);
    }
}

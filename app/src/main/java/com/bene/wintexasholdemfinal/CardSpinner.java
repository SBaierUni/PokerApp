package com.bene.wintexasholdemfinal;

import android.widget.Spinner;

// TODO use this class
public class CardSpinner {
    private Spinner value;
    private Spinner symbol;
    private final char[] symbols = {'C', 'D', 'S', 'H'};

    CardSpinner(Spinner val, Spinner sym) {
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
        return symbols[symbol.getSelectedItemPosition()] + "" + value.getSelectedItem().toString();
    }

    public int getSelectedValuePosition() {
        return value.getSelectedItemPosition();
    }

    public int getSelectedSymbolPosition() {
        return symbol.getSelectedItemPosition();
    }
}

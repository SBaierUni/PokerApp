package com.bene.wintexasholdemfinal;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;

import TH_0_99_1.*;

public class ManualEntryActivity extends AppCompatActivity
        implements AdapterView.OnItemSelectedListener {
    ImageView talonCard1Img = null;
    ImageView talonCard2Img = null;
    ImageView talonCard3Img = null;
    ImageView talonCard4Img = null;
    ImageView talonCard5Img = null;
    EditText talonCard1Txt = null;
    EditText talonCard2Txt = null;
    EditText talonCard3Txt = null;
    EditText talonCard4Txt = null;
    EditText talonCard5Txt = null;
    ImageView pocketCard1Img = null;
    ImageView pocketCard2Img = null;
    EditText pocketCard1Txt = null;
    EditText pocketCard2Txt = null;
    TextView probabilityToWin= null;
    TextView handValResult= null;
    Button enterNextCardsButton= null;

    Spinner noOfOpponentsSpinner= null;

    Deck deck= new Deck();
    Talon talon= new Talon();
    PlayerPocket playerPocket= new PlayerPocket();
    int nextRound= 0;
    int noOfOpponents= 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manual_entry_activity);
        // set instance variables to be able to access text fields and images:
        talonCard1Img = (ImageView) findViewById(R.id.talonCard1Img);
        talonCard2Img = (ImageView) findViewById(R.id.talonCard2Img);
        talonCard3Img = (ImageView) findViewById(R.id.talonCard3Img);
        talonCard4Img = (ImageView) findViewById(R.id.talonCard4Img);
        talonCard5Img = (ImageView) findViewById(R.id.talonCard5Img);
        talonCard1Txt = (EditText) findViewById(R.id.talonCard1Txt);
        talonCard2Txt = (EditText) findViewById(R.id.talonCard2Txt);
        talonCard3Txt = (EditText) findViewById(R.id.talonCard3Txt);
        talonCard4Txt = (EditText) findViewById(R.id.talonCard4Txt);
        talonCard5Txt = (EditText) findViewById(R.id.talonCard5Txt);
        pocketCard1Img = (ImageView) findViewById(R.id.pocketCard1Img);
        pocketCard2Img = (ImageView) findViewById(R.id.pocketCard2Img);
        pocketCard1Txt = (EditText) findViewById(R.id.pocketCard1Txt);
        pocketCard2Txt = (EditText) findViewById(R.id.pocketCard2Txt);
        probabilityToWin = (TextView) findViewById(R.id.probabilityToWin);
        handValResult = (TextView) findViewById(R.id.handValResult);
        enterNextCardsButton= (Button) findViewById(R.id.drawNextCardsButton);

        noOfOpponentsSpinner= (Spinner) findViewById(R.id.noOfOpponentsSpinner);
        ArrayList<String> categories = new ArrayList<String>();
        categories.add("1");
        categories.add("2");
        categories.add("3");
        categories.add("4");
        categories.add("5");
        categories.add("6");
        categories.add("7");
        categories.add("8");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        noOfOpponentsSpinner.setAdapter(dataAdapter);
        noOfOpponentsSpinner.setSelection(noOfOpponents - 1);  // default value: 3 (= index position 2) opponents
        noOfOpponentsSpinner.setOnItemSelectedListener(this);

        registerTxtFocusChangeListeners();
        drawRound0();
    }
    @Override
    public void onItemSelected(AdapterView<?> parent,
                               View view, int position, long id) {
        // 'position' contains selected spinner item (counts from 0)
        ; // recalculate probability of winning based on 'position'
        noOfOpponents = position + 1;
        reCalc();
    }
    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // ... in case nothing is selected
    }

    private void drawRound0() {
        deck= new Deck();
        ArrayList<Card> ppCards= deck.draw(2);
        pocketCard1Txt.setText(ppCards.get(0).getCardAsShortString());
        pocketCard2Txt.setText(ppCards.get(1).getCardAsShortString());
        pocketCard1Img.setImageDrawable(getResId(ppCards.get(0).getCardAsShortString()));
        pocketCard2Img.setImageDrawable(getResId(ppCards.get(1).getCardAsShortString()));
        enableTxtRound0();
        enterNextCardsButton.setText("draw/edit next card(s)");
        enterNextCardsButton.setEnabled(true);
        composePlayerPocket();
        reCalc();
        handValResult.setText("–");
    }
    private void enableTxtRound0() {
        enableEditText(pocketCard1Txt); pocketCard1Txt.selectAll();
        enableEditText(pocketCard2Txt);

        enableEditText(talonCard1Txt); talonCard1Txt.setVisibility(View.INVISIBLE);
        enableEditText(talonCard2Txt); talonCard2Txt.setVisibility(View.INVISIBLE);
        enableEditText(talonCard3Txt); talonCard3Txt.setVisibility(View.INVISIBLE);
        enableEditText(talonCard4Txt); talonCard4Txt.setVisibility(View.INVISIBLE);
        enableEditText(talonCard5Txt); talonCard5Txt.setVisibility(View.INVISIBLE);

        talonCard1Img.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.blankcard));
        talonCard2Img.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.blankcard));
        talonCard3Img.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.blankcard));
        talonCard4Img.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.blankcard));
        talonCard5Img.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.blankcard));
    }
    private void enableEditText(EditText txt) {
        txt.setFocusable(true); txt.setFocusableInTouchMode(true);
        txt.setClickable(true); txt.setTextColor(Color.BLACK);
        txt.setEnabled(true);
    }
    private void disableEditText(EditText txt) {
        txt.setFocusable(false); txt.setTextColor(Color.GRAY);
        txt.setEnabled(false);
    }
    private void drawRound1() {
        talon.addCards(deck.draw(3));
        talonCard1Txt.setText(talon.cardAt(0).getCardAsShortString());
        talonCard2Txt.setText(talon.cardAt(1).getCardAsShortString());
        talonCard3Txt.setText(talon.cardAt(2).getCardAsShortString());
        talonCard1Img.setImageDrawable(getResId(talon.cardAt(0).getCardAsShortString()));
        talonCard2Img.setImageDrawable(getResId(talon.cardAt(1).getCardAsShortString()));
        talonCard3Img.setImageDrawable(getResId(talon.cardAt(2).getCardAsShortString()));
        enableTxtRound1();
    }
    private void enableTxtRound1() {
        disableEditText(pocketCard1Txt);
        disableEditText(pocketCard2Txt);
        talonCard1Txt.setVisibility(View.VISIBLE);
        talonCard1Txt.selectAll();
        talonCard2Txt.setVisibility(View.VISIBLE);
        talonCard3Txt.setVisibility(View.VISIBLE);

    }
    private void drawRound2() {
        talon.addCards(deck.draw(1));
        talonCard1Txt.setText(talon.cardAt(3).getCardAsShortString());
        talonCard4Img.setImageDrawable(getResId(talon.cardAt(3).getCardAsShortString()));
        enableTxtRound2();
        composeCardSets();
    }
    private void enableTxtRound2() {
        disableEditText(talonCard1Txt);
        disableEditText(talonCard2Txt);
        disableEditText(talonCard3Txt);
        talonCard4Txt.setVisibility(View.VISIBLE);
        talonCard4Txt.selectAll();
    }
    private void drawRound3() {
        talon.addCards(deck.draw(1));
        talonCard5Txt.setText(talon.cardAt(4).getCardAsShortString());
        talonCard4Img.setImageDrawable(getResId(talon.cardAt(4).getCardAsShortString()));
        enableTxtRound3();
        composeCardSets();

    }
    private void enableTxtRound3() {
        disableEditText(talonCard4Txt);
        talonCard4Txt.setVisibility(View.VISIBLE);
        talonCard4Txt.selectAll();
    }
    public void onClickEnterNextCards(View v) {
        nextRound= nextRound + 1;
        switch (nextRound) {
            case 1: drawRound1();
                break;
            case 2: drawRound2();
                break;
            case 3: drawRound3();
                enterNextCardsButton.setEnabled(false);
                break;
        }
    }
    public void onClickStartAgain(View v) {
        nextRound= 0;
        drawRound0();
    }
    private void reCalc() {
        Player me= new Player(talon, playerPocket);
        deck.reset();
        deck.addToAlreadyDrawnCards(talon);
        deck.addToAlreadyDrawnCards(playerPocket);
        String hVal= me.getHighestHandVal().toString();
        if (nextRound != 0) {
            handValResult.setText(hVal.toString());
        }
        calcAndSetProbability(noOfOpponents);
    }
    private void composePlayerPocket() {
        playerPocket = new PlayerPocket();
        playerPocket.addCard(new Card(pocketCard1Txt.getText().toString()));
        playerPocket.addCard(new Card(pocketCard2Txt.getText().toString()));
        pocketCard1Img.setImageDrawable(getResId(playerPocket.cardAt(0).getCardAsShortString()));
        pocketCard2Img.setImageDrawable(getResId(playerPocket.cardAt(1).getCardAsShortString()));
        talon= new Talon();
    }
    private void composeTalon3() {   // talon with three cards
        talon= new Talon();
        talon.addCard(new Card(talonCard1Txt.getText().toString()));
        talon.addCard(new Card(talonCard2Txt.getText().toString()));
        talon.addCard(new Card(talonCard3Txt.getText().toString()));
        talonCard1Img.setImageDrawable(getResId(talon.cardAt(0).getCardAsShortString()));
        talonCard2Img.setImageDrawable(getResId(talon.cardAt(1).getCardAsShortString()));
        talonCard3Img.setImageDrawable(getResId(talon.cardAt(2).getCardAsShortString()));
    }
    private void composeTalon4() { // talon with four cards
        composeTalon3();
        talon.addCard(new Card(talonCard4Txt.getText().toString()));
        talonCard4Img.setImageDrawable(getResId(talon.cardAt(3).getCardAsShortString()));
    }
    private void composeTalon5() {   // talon with five cards
        composeTalon4();
        talon.addCard(new Card(talonCard5Txt.getText().toString()));
        talonCard5Img.setImageDrawable(getResId(talon.cardAt(4).getCardAsShortString()));
    }
    private void composeCardSets() {
        switch (nextRound) {
            case 0:
                composePlayerPocket();
                reCalc();
                handValResult.setText("–");
                break;
            case 1:
                composePlayerPocket();
                composeTalon3();
                reCalc();
                break;
            case 2:
                composePlayerPocket();
                composeTalon4();
                reCalc();
                break;
            case 3:
                composePlayerPocket();
                composeTalon5();
                reCalc();
                break;
            case 4:
                composePlayerPocket();
                composeTalon5();
                reCalc();
            default:
        }
    }
    private void checkAndChangeCard (EditText txt, ImageView img, boolean newCardDesc) { // newCardDesc = true  => cardDesc has changed
        Card otherCard= new Card(txt.getText().toString());
        if (!otherCard.isCorrect() || isDuplicateCard(otherCard, newCardDesc)) {
            txt.setTextColor(Color.RED);
            enterNextCardsButton.setEnabled(false);
            return;
        }
        txt.setTextColor(Color.BLACK);
        if (nextRound != 3)
            enterNextCardsButton.setEnabled(true);
        composeCardSets();
    }
    private boolean cardDescHasChanged(String cardDesc, CardSet cSet, int pos) {
        return (!cardDesc.equalsIgnoreCase(cSet.cardAt(pos).getCardAsShortString()));
    }
    private boolean isDuplicateCard(Card c, boolean newCardDesc) {
        if (deck.isAlreadyDrawn(c) && newCardDesc) {
            return true;
        }
        return false;
    }
    private void registerTxtFocusChangeListeners() {
        talonCard1Txt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    checkAndChangeCard(talonCard1Txt, talonCard1Img, cardDescHasChanged(talonCard1Txt.getText().toString(), talon, 0));
                }
            }
        });
        talonCard2Txt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    checkAndChangeCard(talonCard2Txt, talonCard2Img, cardDescHasChanged(talonCard2Txt.getText().toString(), talon, 1));
                }
            }
        });
        talonCard3Txt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    checkAndChangeCard(talonCard3Txt, talonCard3Img, cardDescHasChanged(talonCard3Txt.getText().toString(), talon, 2));
                }
            }
        });
        talonCard4Txt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    checkAndChangeCard(talonCard4Txt, talonCard4Img, cardDescHasChanged(talonCard4Txt.getText().toString(), talon, 3));
                }
            }
        });
        talonCard5Txt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    checkAndChangeCard(talonCard5Txt, talonCard5Img, cardDescHasChanged(talonCard5Txt.getText().toString(), talon, 4));
                }
            }
        });
        pocketCard1Txt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    checkAndChangeCard(pocketCard1Txt, pocketCard1Img, cardDescHasChanged(pocketCard1Txt.getText().toString(), playerPocket, 0));
                }
            }
        });
        pocketCard2Txt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    checkAndChangeCard(pocketCard2Txt, pocketCard2Img, cardDescHasChanged(pocketCard2Txt.getText().toString(), playerPocket, 1));
                }
            }
        });
        //========================== the changes should also be checked after the editing is finished without a focus (cursor) change to another EditText field

        pocketCard1Txt.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if (playerPocket.getNoOfCards() == 0)
                    composePlayerPocket();
                checkAndChangeCard(pocketCard1Txt, pocketCard1Img, cardDescHasChanged(pocketCard1Txt.getText().toString(), playerPocket, 0));
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                //checkAndChangeCard(pocketCard1Txt, pocketCard1Img, cardDescHasChanged(pocketCard1Txt.getText().toString(), playerPocket, 0));
            }
        });

        pocketCard2Txt.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if (playerPocket.getNoOfCards() == 0)
                    composePlayerPocket();
                checkAndChangeCard(pocketCard2Txt, pocketCard2Img, cardDescHasChanged(pocketCard2Txt.getText().toString(), playerPocket, 1));
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                //checkAndChangeCard(pocketCard2Txt, pocketCard2Img, cardDescHasChanged(pocketCard2Txt.getText().toString(), playerPocket, 1));
            }
        });

        talonCard1Txt.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                checkAndChangeCard(talonCard1Txt, talonCard1Img, cardDescHasChanged(talonCard1Txt.getText().toString(), talon, 0));
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                //checkAndChangeCard(talonCard1Txt, talonCard1Img, cardDescHasChanged(talonCard1Txt.getText().toString(), talon, 0));
            }
        });
        talonCard2Txt.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                checkAndChangeCard(talonCard2Txt, talonCard2Img, cardDescHasChanged(talonCard2Txt.getText().toString(), talon, 1));
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                //checkAndChangeCard(talonCard2Txt, talonCard2Img, cardDescHasChanged(talonCard2Txt.getText().toString(), talon, 1));
            }
        });
        talonCard3Txt.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                checkAndChangeCard(talonCard3Txt, talonCard3Img, cardDescHasChanged(talonCard3Txt.getText().toString(), talon, 2));
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                //checkAndChangeCard(talonCard3Txt, talonCard3Img, cardDescHasChanged(talonCard3Txt.getText().toString(), talon, 2));
            }
        });
        talonCard4Txt.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                checkAndChangeCard(talonCard4Txt, talonCard4Img, cardDescHasChanged(talonCard4Txt.getText().toString(), talon, 3));
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                //checkAndChangeCard(talonCard4Txt, talonCard4Img, cardDescHasChanged(talonCard4Txt.getText().toString(), talon, 3));
            }
        });
        talonCard5Txt.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                checkAndChangeCard(talonCard5Txt, talonCard5Img, cardDescHasChanged(talonCard5Txt.getText().toString(), talon, 4));
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                //checkAndChangeCard(talonCard5Txt, talonCard5Img, cardDescHasChanged(talonCard5Txt.getText().toString(), talon, 4));
            }
        });
    }
    private void calcAndSetProbability(int noOfOpp) {
        ProbabilityOfWinningCalculator calc= new ProbabilityOfWinningCalculator(talon, playerPocket, deck, noOfOpp);
        double prob= calc.calcProbability(888);
        String probStr= String.format("%.2f", prob);
        probabilityToWin.setText(probStr + "%");
    }
    private Drawable getResId(String cardShort) {  // shorter version to map the card string to the .GIF file representing a card icon
        cardShort= cardShort.toLowerCase();   // .GIF files need to be in lower case letters => we have to convert; eg "CA" -> "ca"
        int resourceId = ManualEntryActivity.this.getResources().getIdentifier(
                cardShort,
                "drawable",
                ManualEntryActivity.this.getPackageName());
        return ContextCompat.getDrawable(this, resourceId);
    }
}

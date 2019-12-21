package com.bene.wintexasholdemfinal;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bene.wintexasholdemfinal.TH_0_99_1.Card;
import com.bene.wintexasholdemfinal.TH_0_99_1.CardSet;
import com.bene.wintexasholdemfinal.TH_0_99_1.Deck;
import com.bene.wintexasholdemfinal.TH_0_99_1.Player;
import com.bene.wintexasholdemfinal.TH_0_99_1.PlayerPocket;
import com.bene.wintexasholdemfinal.TH_0_99_1.ProbabilityOfWinningCalculator;
import com.bene.wintexasholdemfinal.TH_0_99_1.Talon;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Objects;

public class ManualEntryActivity extends AppCompatActivity
        implements AdapterView.OnItemSelectedListener {
    TextView probabilityToWin= null;
    TextView handValResult= null;

    Button scanHand;
    Button scanFlop;
    Button scanTurn;
    Button scanRiver;
    Button hideCards;

    Thread probCalculatorThread;

    Spinner noOfOpponentsSpinner= null;

    Spinner[] symHand = new Spinner[2];
    Spinner[] valHand = new Spinner[2];
    String[] hand_sym_id;
    String[] hand_val_id;

    Spinner[] symTalon = new Spinner[5];
    Spinner[] valTalon = new Spinner[5];

    Deck deck= new Deck();
    Talon talon= new Talon();
    PlayerPocket playerPocket= new PlayerPocket();
    int nextRound= 0;
    int noOfOpponents= 3;
    ArrayList<String> currPrediction;
    ArrayList<String> card_values;
    final char[] symbols = {'C', 'D', 'S', 'H'};

    final int ACTIVITY_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manual_entry_activity);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // set instance variables to be able to access text fields and images:
        probabilityToWin = (TextView) findViewById(R.id.probabilityToWin);
        handValResult = (TextView) findViewById(R.id.handValResult);
        scanFlop = findViewById(R.id.scanFlop);
        scanHand = findViewById(R.id.scanHand);
        scanTurn = findViewById(R.id.scanTurn);
        scanRiver = findViewById(R.id.scanRiver);

        scanFlop.setVisibility(View.INVISIBLE);
        scanTurn.setVisibility(View.INVISIBLE);
        scanRiver.setVisibility(View.INVISIBLE);
        hideCards = findViewById(R.id.hideCards);

        // TODO remove shit
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

        // TODO replace T by 10
        card_values = new ArrayList<String>();
        card_values.add("2");
        card_values.add("3");
        card_values.add("4");
        card_values.add("5");
        card_values.add("6");
        card_values.add("7");
        card_values.add("8");
        card_values.add("9");
        card_values.add("T");
        card_values.add("J");
        card_values.add("Q");
        card_values.add("K");
        card_values.add("A");

        ImageAdapter imgAdapter = new ImageAdapter(getApplicationContext());
        ArrayAdapter<String> valAdapter = new ArrayAdapter<String>(this, R.layout.spinner_custom_numbers_layout, card_values);

        hand_sym_id = getResources().getStringArray(R.array.hand_symbol_ids);
        hand_val_id = getResources().getStringArray(R.array.hand_value_ids);

        String[] talon_sym_id = getResources().getStringArray(R.array.talon_symbol_ids);
        String[] talon_val_id = getResources().getStringArray(R.array.talon_value_ids);

        for(int i = 0; i < hand_sym_id.length; i++) {
            symHand[i] = (Spinner) findViewById(getResources().getIdentifier(hand_sym_id[i], "id", getPackageName()));
            valHand[i] = (Spinner) findViewById(getResources().getIdentifier(hand_val_id[i], "id", getPackageName()));
            symHand[i].setAdapter(imgAdapter);
            valHand[i].setAdapter(valAdapter);
            symHand[i].setOnItemSelectedListener(this);
            valHand[i].setOnItemSelectedListener(this);
        }

        CardSpinner cs = new CardSpinner((Spinner)findViewById(R.id.spinner_numbers_hand_1), (Spinner)findViewById(R.id.spinner_symbols_hand_1));

        for(int i = 0; i < talon_sym_id.length; i++) {
            symTalon[i] = (Spinner) findViewById(getResources().getIdentifier(talon_sym_id[i], "id", getPackageName()));
            valTalon[i] = (Spinner) findViewById(getResources().getIdentifier(talon_val_id[i], "id", getPackageName()));
            symTalon[i].setAdapter(imgAdapter);
            valTalon[i].setAdapter(valAdapter);
            symTalon[i].setOnItemSelectedListener(this);
            valTalon[i].setOnItemSelectedListener(this);
        }

        //randomize();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        int parent_id = parent.getId();
        // 'position' contains selected spinner item (counts from 0);
        // recalculate probability of winning based on 'position'
        if (parent_id == noOfOpponentsSpinner.getId()) {
            noOfOpponents = position + 1;
            reCalc();
        }

        for(int i = 0; i < symHand.length; i++) {
            if(parent_id == symHand[i].getId() || parent_id == valHand[i].getId()) {
                checkAndChangeCard(valHand[i], symHand[i], cardDescHasChanged(getSpinnerText(valHand[i], symHand[i]), playerPocket, i));
            }
        }

        for(int i = 0; i < symTalon.length; i++) {
            if(parent_id == symTalon[i].getId() || parent_id == valTalon[i].getId()) {
                checkAndChangeCard(valTalon[i], symTalon[i], cardDescHasChanged(getSpinnerText(valTalon[i], symTalon[i]), talon, i));
            }
        }
    }

    // TODO check isDuplicate correctness
    private void checkAndChangeCard (Spinner val, Spinner sym, boolean newCardDesc) { // newCardDesc = true  => cardDesc has changed
        Card otherCard = new Card(getSpinnerText(val, sym));
        if (!otherCard.isCorrect() || isDuplicateCard(otherCard, newCardDesc)) {
            // TODO do some error indication
            //txt.setTextColor(Color.RED);
            return;
        }

        // update text color depending on symbol
        if (sym.getSelectedItemPosition() % 2 == 0)
            ((TextView) val.getChildAt(0)).setTextColor(Color.BLACK);
        else
            ((TextView) val.getChildAt(0)).setTextColor(Color.parseColor("#cb2529"));

        composeCardSets();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // ... in case nothing is selected
    }

    private String getPredictedCard(){
        if (currPrediction == null) return null;

        while(!currPrediction.isEmpty()) {
            String card = currPrediction.remove(0);
            if (!isDuplicateCard(new Card(card), true))
                return card;
        }
        return null;
    }

    private void drawCard(Spinner val, Spinner sym, boolean addToTalon) {
        String card = getPredictedCard();

        // if card not recognized in image -> pick random card
        if(card == null)
            card = deck.draw(1).get(0).getCardAsShortString();
        else
            deck.addToAlreadyDrawnCards(new Card(card));


        if(addToTalon)
            talon.addCard(new Card(card));

        for(int i = 0; i < symbols.length; i++)
            if(symbols[i] == card.charAt(0))
                sym.setSelection(i);

        for(int i = 0; i < card_values.size(); i++)
            if (card_values.get(i).charAt(0) == card.charAt(1))
                val.setSelection(i);
    }

    private void drawRound0() {
        //resetGame();
        //deck = new Deck();

        drawCard(valHand[0], symHand[0], false);
        drawCard(valHand[1], symHand[1], false);

        enableTxtRound0();
        //reCalc();
        handValResult.setText("–");
    }

    private void enableTxtRound0() {
        for(int i = 0; i < symTalon.length; i++) {
            valTalon[i].setVisibility(View.INVISIBLE);
            symTalon[i].setVisibility(View.INVISIBLE);
        }
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
        drawCard(valTalon[0], symTalon[0], true);
        drawCard(valTalon[1], symTalon[1], true);
        drawCard(valTalon[2], symTalon[2], true);

        enableTxtRound1();
    }

    private void enableTxtRound1() {
        for(int i = 0; i < 3; i++) {
            symTalon[i].setVisibility(View.VISIBLE);
            valTalon[i].setVisibility(View.VISIBLE);
        }
    }
    private void drawRound2() {
        drawCard(valTalon[3], symTalon[3], true);
        enableTxtRound2();
        composeCardSets();
    }

    private void enableTxtRound2() {
        symTalon[3].setVisibility(View.VISIBLE);
        valTalon[3].setVisibility(View.VISIBLE);
    }
    private void drawRound3() {
        drawCard(valTalon[4], symTalon[4], true);
        symTalon[4].setVisibility(View.VISIBLE);
        valTalon[4].setVisibility(View.VISIBLE);
        composeCardSets();
    }

    private void randomize(){
        deck = new Deck();

        drawCard(symHand[0], valHand[0], false);
        drawCard(symHand[1], valHand[1], false);

        for(int i = 0; i < symTalon.length; i++)
            drawCard(symTalon[i], valTalon[i], true);
        resetGame();
    }

    private void resetGame() {
        deck = new Deck();

        scanFlop.setVisibility(View.INVISIBLE);
        scanTurn.setVisibility(View.INVISIBLE);
        scanRiver.setVisibility(View.INVISIBLE);

        valHand[0].setVisibility(View.INVISIBLE);
        valHand[1].setVisibility(View.INVISIBLE);
        symHand[0].setVisibility(View.INVISIBLE);
        symHand[1].setVisibility(View.INVISIBLE);

        for(int i = 0; i < symTalon.length; i++) {
            valTalon[i].setVisibility(View.INVISIBLE);
            symTalon[i].setVisibility(View.INVISIBLE);
        }

        handValResult.setText("–");
    }

    public void onClickStartAgain(View v) {
        nextRound = 0;
        // TODO delete data, some strange shit happening
        //randomize();
    }

    public void onClickscanFlop(View v){
        if(scanTurn.getVisibility() == View.INVISIBLE)
            scanTurn.setVisibility(View.VISIBLE);
        openCameraView();
    }

    public void onClickscanTurn(View v){
        if(scanRiver.getVisibility() == View.INVISIBLE)
            scanRiver.setVisibility(View.VISIBLE);
        openCameraView();
    }

    public void onClickscanRiver(View v){
        openCameraView();
    }

    public void onClickScanHand(View v) {
        if(scanFlop.getVisibility() == View.INVISIBLE)
            scanFlop.setVisibility(View.VISIBLE);
        openCameraView();
    }

    public void onClickhideCards(View v){
        if(probabilityToWin.getVisibility() == View.VISIBLE){
            probabilityToWin.setVisibility(View.INVISIBLE);
            valHand[0].setVisibility(View.INVISIBLE);
            symHand[0].setVisibility(View.INVISIBLE);
            valHand[1].setVisibility(View.INVISIBLE);
            symHand[1].setVisibility(View.INVISIBLE);
        }
        else {
            probabilityToWin.setVisibility(View.VISIBLE);
            valHand[0].setVisibility(View.VISIBLE);
            symHand[0].setVisibility(View.VISIBLE);
            valHand[1].setVisibility(View.VISIBLE);
            symHand[1].setVisibility(View.VISIBLE);
        }
    }

    // TODO make pretty
    private void drawNextRound() {
        switch (nextRound) {
            case 0: drawRound0(); break;
            case 1: drawRound1(); break;
            case 2: drawRound2(); break;
            case 3: drawRound3(); break;
        }
        nextRound++;
    }

    private void openCameraView() {
        Intent i = new Intent(getApplicationContext(), DetectorActivity.class);
        startActivityForResult(i, ACTIVITY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == ACTIVITY_REQUEST_CODE){
            currPrediction = Objects.requireNonNull(data.getExtras()).getStringArrayList("prediction");
            drawNextRound();
        }
    }

    private void reCalc() {
        Player me = new Player(talon, playerPocket);
        deck.reset();
        deck.addToAlreadyDrawnCards(talon);
        deck.addToAlreadyDrawnCards(playerPocket);
        String hVal = me.getHighestHandVal().toString();
        if (nextRound != 0) {
            handValResult.setText(hVal);
        }
        calcAndSetProbability();
    }

    private String getSpinnerText(Spinner val, Spinner sym) {
        return symbols[sym.getSelectedItemPosition()] + "" + val.getSelectedItem().toString();
    }

    private void composePlayerPocket() {
        playerPocket = new PlayerPocket();
        playerPocket.addCard(new Card(getSpinnerText(valHand[0], symHand[0])));
        playerPocket.addCard(new Card(getSpinnerText(valHand[1], symHand[1])));
        talon = new Talon();
    }

    private void composeTalon3() {   // talon with three cards
        talon = new Talon();
        for(int i = 0; i < 3; i++)
            talon.addCard(new Card(getSpinnerText(valTalon[i], symTalon[i])));
    }

    private void composeTalon4() { // talon with four cards
        composeTalon3();
        talon.addCard(new Card(getSpinnerText(valTalon[3], symTalon[3])));
    }

    private void composeTalon5() {   // talon with five cards
        composeTalon4();
        talon.addCard(new Card(getSpinnerText(valTalon[4], symTalon[4])));
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

    private void composeCardSetsOld() {
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

    private boolean cardDescHasChanged(String cardDesc, CardSet cSet, int pos) {
        if (cSet.getNoOfCards() < 1)
            return false;
        return (!cardDesc.equalsIgnoreCase(cSet.cardAt(pos).getCardAsShortString()));
    }
    private boolean isDuplicateCard(Card c, boolean newCardDesc) {
        System.out.println(deck.isAlreadyDrawn(c));
        return deck.isAlreadyDrawn(c) && newCardDesc;
    }

    private void calcAndSetProbability(){
        if(probCalculatorThread != null){
            probCalculatorThread.interrupt();
        }
        probCalculatorThread = new Thread(new Runnable() {
            public void run(){
                final ProbabilityOfWinningCalculator prob = new ProbabilityOfWinningCalculator(talon, playerPocket, deck, noOfOpponents);
                final DecimalFormat df = new DecimalFormat("#.0");
                double probs = 0;
                for(int i = 0; i < 100; i++){
                    if(Thread.currentThread().isInterrupted()){
                        break;
                    }
                    probs += prob.calcProbability(200);
                    final double transmitProb = probs / (i+1);
                    probabilityToWin.post(new Runnable(){
                        public void run(){
                            probabilityToWin.setText(df.format(transmitProb) +  "%");
                        }
                    });
                }
            }
        });
        probCalculatorThread.start();
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

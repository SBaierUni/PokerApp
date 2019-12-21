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
    private TextView probabilityToWin= null;
    private TextView handValResult= null;

    private Button scanHand;
    private Button scanFlop;
    private Button scanTurn;
    private Button scanRiver;
    private Button hideCards;

    private Thread probCalculatorThread;

    private Spinner noOfOpponentsSpinner= null;

    private CardSpinner[] hand_cards = new CardSpinner[2];
    private CardSpinner[] talon_cards = new CardSpinner[5];

    private Deck deck = new Deck();
    private Talon talon = new Talon();
    private PlayerPocket playerPocket= new PlayerPocket();
    private int nextRound = 0;
    private int noOfOpponents = 3;
    private ArrayList<String> currPrediction;

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

        String[] categories = getResources().getStringArray(R.array.categories);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        noOfOpponentsSpinner= (Spinner) findViewById(R.id.noOfOpponentsSpinner);
        noOfOpponentsSpinner.setAdapter(dataAdapter);
        noOfOpponentsSpinner.setSelection(noOfOpponents - 1);  // default value: 3 (= index position 2) opponents
        noOfOpponentsSpinner.setOnItemSelectedListener(this);

        String[] hand_sym_id = getResources().getStringArray(R.array.hand_symbol_ids);
        String[] hand_val_id = getResources().getStringArray(R.array.hand_value_ids);
        String[] talon_sym_id = getResources().getStringArray(R.array.talon_symbol_ids);
        String[] talon_val_id = getResources().getStringArray(R.array.talon_value_ids);

        for(int i = 0; i < hand_cards.length; i++) {
            hand_cards[i] = new CardSpinner((Spinner) findViewById(getResources().getIdentifier(hand_val_id[i], "id", getPackageName())),
                    (Spinner) findViewById(getResources().getIdentifier(hand_sym_id[i], "id", getPackageName())));
            hand_cards[i].setAdapter(this);
            hand_cards[i].setOnItemSelectedListener(this);
        }

        for(int i = 0; i < talon_cards.length; i++) {
            talon_cards[i] = new CardSpinner((Spinner) findViewById(getResources().getIdentifier(talon_val_id[i], "id", getPackageName())),
                    (Spinner) findViewById(getResources().getIdentifier(talon_sym_id[i], "id", getPackageName())));
            talon_cards[i].setAdapter(this);
            talon_cards[i].setOnItemSelectedListener(this);
        }

        randomize();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        int parent_id = parent.getId();

        if (parent_id == noOfOpponentsSpinner.getId()) {
            noOfOpponents = position + 1;
            reCalc();
        }

        for(int i = 0; i < hand_cards.length; i++)
            if(hand_cards[i].compareID(parent_id))
                checkAndChangeCard(hand_cards[i], cardDescHasChanged(hand_cards[i].getText(), playerPocket, i));


        for(int i = 0; i < talon_cards.length; i++)
            if(talon_cards[i].compareID(parent_id))
                checkAndChangeCard(talon_cards[i], cardDescHasChanged(talon_cards[i].getText(), talon, i));
    }

    // TODO check isDuplicateCard correctness
    private void checkAndChangeCard (CardSpinner cs, boolean newCardDesc) { // newCardDesc = true  => cardDesc has changed
        Card otherCard = new Card(cs.getText());
        if (!otherCard.isCorrect() || isDuplicateCard(otherCard, newCardDesc)) {
            // TODO maybe do some error indication
            //txt.setTextColor(Color.RED);
            return;
        }

        cs.updateTextColor();
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

    // TODO value colour not updated after hand recognition
    private void drawCard(CardSpinner cs, boolean addToTalon) {
        String card = getPredictedCard();

        // if card not recognized in image -> pick random card
        if(card == null)
            card = deck.draw(1).get(0).getCardAsShortString();
        else
            deck.addToAlreadyDrawnCards(new Card(card));

        if(addToTalon) talon.addCard(new Card(card));

        cs.setSelection(card);
    }

    private void drawRound0() {
        resetGame();


        drawCard(hand_cards[0], false);
        drawCard(hand_cards[1], false);

        enableTxtRound0();
    }

    private void enableTxtRound0() {
        hand_cards[0].setVisibility(View.VISIBLE);
        hand_cards[1].setVisibility(View.VISIBLE);

        if(scanFlop.getVisibility() == View.INVISIBLE)
            scanFlop.setVisibility(View.VISIBLE);

        for (CardSpinner talon_card : talon_cards)
            talon_card.setVisibility(View.INVISIBLE);
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
        drawCard(talon_cards[0], true);
        drawCard(talon_cards[1], true);
        drawCard(talon_cards[2], true);

        enableTxtRound1();
    }

    private void enableTxtRound1() {
        for(int i = 0; i < 3; i++)
            talon_cards[i].setVisibility(View.VISIBLE);
    }
    private void drawRound2() {
        drawCard(talon_cards[3], true);
        enableTxtRound2();
        composeCardSets();
    }

    private void enableTxtRound2() {
        talon_cards[3].setVisibility(View.VISIBLE);
    }
    private void drawRound3() {
        drawCard(talon_cards[4], true);
        talon_cards[4].setVisibility(View.VISIBLE);
        composeCardSets();
    }

    private void randomize(){
        deck = new Deck();

        drawCard(hand_cards[0], false);
        drawCard(hand_cards[1], false);

        for (CardSpinner talon_card : talon_cards) drawCard(talon_card, true);
    }

    // TODO reset not fully working, prediction is behind, maybe do recalc
    private void resetGame() {
        nextRound = 0;
        deck = new Deck();

        scanFlop.setVisibility(View.INVISIBLE);
        scanTurn.setVisibility(View.INVISIBLE);
        scanRiver.setVisibility(View.INVISIBLE);

        hand_cards[0].setVisibility(View.INVISIBLE);
        hand_cards[1].setVisibility(View.INVISIBLE);

        for (CardSpinner talon_card : talon_cards)
            talon_card.setVisibility(View.INVISIBLE);

        talon = new Talon();
        playerPocket = new PlayerPocket();
        calcAndSetProbability();
        handValResult.setText("–");
    }

    public void onClickStartAgain(View v) {
        resetGame();
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
        openCameraView();
    }

    public void onClickhideCards(View v){
        if(probabilityToWin.getVisibility() == View.VISIBLE){
            probabilityToWin.setVisibility(View.INVISIBLE);
            hand_cards[0].setVisibility(View.INVISIBLE);
            hand_cards[1].setVisibility(View.INVISIBLE);
        }
        else {
            probabilityToWin.setVisibility(View.VISIBLE);
            hand_cards[0].setVisibility(View.VISIBLE);
            hand_cards[1].setVisibility(View.VISIBLE);
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

    private void composePlayerPocket() {
        playerPocket = new PlayerPocket();
        playerPocket.addCard(new Card(hand_cards[0].getText()));
        playerPocket.addCard(new Card(hand_cards[1].getText()));
        talon = new Talon();
    }

    private void composeTalon3() {   // talon with three cards
        talon = new Talon();
        for(int i = 0; i < 3; i++)
            talon.addCard(new Card(talon_cards[i].getText()));
    }

    private void composeTalon4() { // talon with four cards
        composeTalon3();
        talon.addCard(new Card(talon_cards[3].getText()));
    }

    private void composeTalon5() {   // talon with five cards
        composeTalon4();
        talon.addCard(new Card(talon_cards[4].getText()));
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

package com.bene.wintexasholdemfinal;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
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
import com.bene.wintexasholdemfinal.custom_spinner.CardSpinner;
import com.bene.wintexasholdemfinal.object_detection.DetectorActivity;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Objects;

public class ManualEntryActivity extends AppCompatActivity
        implements AdapterView.OnItemSelectedListener {
    private TextView probabilityToWin= null;
    private TextView handValResult= null;

    private ImageButton scanHand;
    private ImageButton scanFlop;
    private Button hideCards;

    private ProgressBar mProgress;
    private Handler handler = new Handler();

    private Thread probCalculatorThread;

    private Spinner noOfOpponentsSpinner= null;

    private CardSpinner[] hand_cards = new CardSpinner[2];
    private CardSpinner[] talon_cards = new CardSpinner[5];

    private Deck deck = new Deck();
    private Talon talon = new Talon();
    private PlayerPocket playerPocket= new PlayerPocket();
    private int nextRound;
    private int noOfOpponents = 3;
    private ArrayList<String> currPrediction;
    private boolean updateLock = true;   // only start a new calculation thread at the end of a round

    final int ACTIVITY_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manual_entry_activity);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // set instance variables to be able to access text fields and images:
        probabilityToWin = findViewById(R.id.probabilityToWin);
        handValResult = findViewById(R.id.handValResult);
        scanFlop = findViewById(R.id.scanFlop);
        scanHand = findViewById(R.id.scanHand);
        hideCards = findViewById(R.id.hideCards);

        Resources res = getResources();
        Drawable drawable = res.getDrawable(R.drawable.circular_progress);
        mProgress = findViewById(R.id.circularProgressbar);
        mProgress.setProgress(100);
        mProgress.setSecondaryProgress(100);
        mProgress.setMax(100);
        mProgress.setProgressDrawable(drawable);

        // load predefined strings from string.xml
        String[] opponentCategories = getResources().getStringArray(R.array.categories);
        String[] hand_sym_id = getResources().getStringArray(R.array.hand_symbol_ids);
        String[] hand_val_id = getResources().getStringArray(R.array.hand_value_ids);
        String[] talon_sym_id = getResources().getStringArray(R.array.talon_symbol_ids);
        String[] talon_val_id = getResources().getStringArray(R.array.talon_value_ids);

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, R.layout.spinner_custom_numbers_layout, opponentCategories);
        noOfOpponentsSpinner= findViewById(R.id.noOfOpponentsSpinner);
        noOfOpponentsSpinner.setAdapter(dataAdapter);
        noOfOpponentsSpinner.setSelection(noOfOpponents - 1);  // default value: 3 (= index position 2) opponents
        noOfOpponentsSpinner.setOnItemSelectedListener(this);


        for(int i = 0; i < hand_cards.length; i++) {
            hand_cards[i] = new CardSpinner(this, findViewById(getResources().getIdentifier(hand_val_id[i], "id", getPackageName())),
                    findViewById(getResources().getIdentifier(hand_sym_id[i], "id", getPackageName())));
            hand_cards[i].setAdapter(this);
            hand_cards[i].setOnItemSelectedListener(this);
        }

        for(int i = 0; i < talon_cards.length; i++) {
            talon_cards[i] = new CardSpinner(this, findViewById(getResources().getIdentifier(talon_val_id[i], "id", getPackageName())),
                    findViewById(getResources().getIdentifier(talon_sym_id[i], "id", getPackageName())));
            talon_cards[i].setAdapter(this);
            talon_cards[i].setOnItemSelectedListener(this);
        }

        resetGame();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // ... in case nothing is selected
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        int parent_id = parent.getId();

        if (parent_id == noOfOpponentsSpinner.getId()) {
            noOfOpponents = position + 1;
            ((TextView) view).setTextSize(28);
            reCalc();
        }

        for(int i = 0; i < hand_cards.length; i++)
            if (hand_cards[i].compareID(parent_id))
                checkAndChangeCard(hand_cards[i], cardDescHasChanged(hand_cards[i].getText(), playerPocket, i));

        for(int i = 0; i < talon_cards.length; i++)
            if(talon_cards[i].compareID(parent_id))
                checkAndChangeCard(talon_cards[i], cardDescHasChanged(talon_cards[i].getText(), talon, i));
    }

    private void checkAndChangeCard (CardSpinner cs, boolean newCardDesc) {
        Card otherCard = new Card(cs.getText());

        if (!otherCard.isCorrect() || isDuplicateCard(otherCard, newCardDesc)) {
            handValResult.setText(R.string.invalid);
            cs.setTextColor(Color.YELLOW);  // Error indication color
            return;
        }

        cs.updateTextColor();   // set value color to symbol color

        if(!updateLock && newCardDesc) // if card has changed
            composeCardSets();
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

    private void drawCard(CardSpinner cs, boolean addToTalon) {
        String card = getPredictedCard();

        // if card not recognized in image -> pick random card
        if(card == null)
            card = deck.draw(1).get(0).getCardAsShortString();
        else
            deck.addToAlreadyDrawnCards(new Card(card));

        if(addToTalon) talon.addCard(new Card(card));

        updateLock = true;
        cs.setSelection(card);
        updateLock = false;
    }

    private void drawRound0() {
        for (CardSpinner hand_card : hand_cards) drawCard(hand_card, false);

        setPrivateStatsVisibility(probabilityToWin.getVisibility() == View.VISIBLE);
        scanHand.setVisibility(View.INVISIBLE);
        scanFlop.setVisibility(View.VISIBLE);
    }

    private void drawRound1() {
        for(int i = 0; i < 3; i++) {
            drawCard(talon_cards[i], true);
            talon_cards[i].setVisibility(View.VISIBLE);
        }
    }

    private void drawRound2() {
        drawCard(talon_cards[3], true);
        talon_cards[3].setVisibility(View.VISIBLE);
    }

    private void drawRound3() {
        drawCard(talon_cards[4], true);
        talon_cards[4].setVisibility(View.VISIBLE);
        scanFlop.setVisibility(View.INVISIBLE);
    }

    public void onClickStartAgain(View v) {
        resetGame();
    }

    public void onClickScanTalon(View v){
        openCameraView();
    }

    public void onClickScanHand(View v) {
        openCameraView();
    }

    public void onClickHideCards(View v){
        setPrivateStatsVisibility(probabilityToWin.getVisibility() == View.INVISIBLE);
    }

    private void resetGame() {
        nextRound = -1;  // -1 is the default round without any cards
        deck = new Deck();

        scanHand.setVisibility(View.VISIBLE);
        scanFlop.setVisibility(View.INVISIBLE);
        hand_cards[0].setVisibility(View.INVISIBLE);
        hand_cards[1].setVisibility(View.INVISIBLE);
        for (CardSpinner talon_card : talon_cards) talon_card.setVisibility(View.INVISIBLE);

        talon = new Talon();
        playerPocket = new PlayerPocket();
        reCalc();
    }

    /** Visibility of hand cards and winning statistics */
    private void setPrivateStatsVisibility(boolean visible) {
        if(visible){
            probabilityToWin.setVisibility(View.VISIBLE);
            mProgress.setVisibility(View.VISIBLE);
            // -1 is the default round without any cards
            if (nextRound >= 0) {
                hand_cards[0].setVisibility(View.VISIBLE);
                hand_cards[1].setVisibility(View.VISIBLE);
            }
            handValResult.setVisibility(View.VISIBLE);
            hideCards.setBackgroundResource(R.drawable.button_theme);
            hideCards.setText(R.string.hide);
            hideCards.setTextColor(ContextCompat.getColor(this, R.color.theme_black));
        } else {
            probabilityToWin.setVisibility(View.INVISIBLE);
            mProgress.setVisibility(View.INVISIBLE);
            hand_cards[0].setVisibility(View.INVISIBLE);
            hand_cards[1].setVisibility(View.INVISIBLE);
            handValResult.setVisibility(View.INVISIBLE);
            hideCards.setBackgroundResource(R.drawable.button_theme_border);
            hideCards.setText(R.string.show);
            hideCards.setTextColor(ContextCompat.getColor(this, R.color.theme_blue));
        }
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
            nextRound++;
            drawNextRound();
        }
    }

    private void drawNextRound() {
        switch (nextRound) {
            case 0: drawRound0(); break;
            case 1: drawRound1(); break;
            case 2: drawRound2(); break;
            case 3: drawRound3(); break;
        }
        composeCardSets();
    }

    private void reCalc() {
        Player me = new Player(talon, playerPocket);
        deck.reset();
        deck.addToAlreadyDrawnCards(talon);
        deck.addToAlreadyDrawnCards(playerPocket);
        String hVal = me.getHighestHandVal().toString();
        if (nextRound >= 0) {
            handValResult.setText(hVal);
            calcAndSetProbability();
        }
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
            default:
                System.out.println("Something unexpected happened -> resetting game");
                resetGame();
        }
    }

    private boolean cardDescHasChanged(String cardDesc, CardSet cSet, int pos) {
        if (cSet.getNoOfCards() < 1)
            return false;
        return (!cardDesc.equalsIgnoreCase(cSet.cardAt(pos).getCardAsShortString()));
    }
    private boolean isDuplicateCard(Card c, boolean newCardDesc) {
        return deck.isAlreadyDrawn(c) && newCardDesc;
    }

    private void calcAndSetProbability(){
        if(probCalculatorThread != null)
            probCalculatorThread.interrupt();

        probCalculatorThread = new Thread(() -> {
            final ProbabilityOfWinningCalculator prob = new ProbabilityOfWinningCalculator(talon, playerPocket, deck, noOfOpponents);
            final DecimalFormat df = new DecimalFormat("#0.0");
            double probs = 0;
            for(int i = 0; i < 100; i++){
                if(Thread.currentThread().isInterrupted())
                    break;
                probs += prob.calcProbability(200);
                final double transmitProb = probs / (i+1);
                handler.post(() -> {
                    mProgress.setProgress((int) transmitProb);
                    probabilityToWin.setText(df.format(transmitProb) +  "%");
                });
            }
        });
        probCalculatorThread.start();
    }
}

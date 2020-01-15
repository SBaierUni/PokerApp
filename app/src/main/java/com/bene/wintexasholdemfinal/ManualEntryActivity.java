package com.bene.wintexasholdemfinal;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

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

public class ManualEntryActivity extends AppCompatActivity
        implements AdapterView.OnItemSelectedListener {

    public static final int ACTIVITY_REQUEST_CODE = 100;
    private static final int PERMISSIONS_REQUEST = 1;
    private static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
    private static final int DEFAULT_ROUND = -1;   // default round without any cards

    private int nextRound;
    private int noOfOpponents = 3;
    private boolean updateLock = true;  // control generating a new thread on updating view
    private ArrayList<String> curr_prediction_stack;    // first element has the highest confidence

    private TextView probabilityToWin = null;
    private TextView handValResult = null;

    private Button hideCards;
    private ImageButton scanHand;
    private ImageButton scanFlop;

    private TextView bestHand;

    private ProgressBar mProgress;
    private Handler handler = new Handler();

    private Thread probCalculatorThread;

    private Spinner noOfOpponentsSpinner = null;
    private CardSpinner[] hand_cards = new CardSpinner[2];
    private CardSpinner[] talon_cards = new CardSpinner[5];

    private Deck deck = new Deck();
    private Talon talon = new Talon();
    private PlayerPocket playerPocket = new PlayerPocket();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.MainTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manual_entry_activity);

        if (!hasPermission()) requestPermission();

        // set instance variables to be able to access text fields and images:
        probabilityToWin = findViewById(R.id.probabilityToWin);
        handValResult = findViewById(R.id.handValResult);
        scanFlop = findViewById(R.id.scanFlop);
        scanHand = findViewById(R.id.scanHand);
        hideCards = findViewById(R.id.hideCards);
        bestHand = findViewById(R.id.text4);

        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.circular_progress_bar, null);
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
        noOfOpponentsSpinner = findViewById(R.id.noOfOpponentsSpinner);
        noOfOpponentsSpinner.setAdapter(dataAdapter);
        noOfOpponentsSpinner.setSelection(noOfOpponents - 1);  // default value: 3 (= index position 2) opponents
        noOfOpponentsSpinner.setOnItemSelectedListener(this);

        for (int i = 0; i < hand_cards.length; i++) {
            hand_cards[i] = new CardSpinner(this, findViewById(getId(hand_val_id[i])), findViewById(getId(hand_sym_id[i])));
            hand_cards[i].setAdapter(this);
            hand_cards[i].setOnItemSelectedListener(this);
        }

        for (int i = 0; i < talon_cards.length; i++) {
            talon_cards[i] = new CardSpinner(this, findViewById(getId(talon_val_id[i])), findViewById(getId(talon_sym_id[i])));
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

        for (int i = 0; i < hand_cards.length; i++)
            if (hand_cards[i].compareID(parent_id))
                checkAndChangeCard(hand_cards[i], cardDescHasChanged(hand_cards[i].getText(), playerPocket, i));

        for (int i = 0; i < talon_cards.length; i++)
            if (talon_cards[i].compareID(parent_id))
                checkAndChangeCard(talon_cards[i], cardDescHasChanged(talon_cards[i].getText(), talon, i));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == ACTIVITY_REQUEST_CODE) {
            curr_prediction_stack = data.getExtras().getStringArrayList("prediction");  // null is handled separately
            nextRound++;
            drawNextRound();
        }
    }

    /** Start a new card detection activity */
    private void openCameraView() {
        if (hasPermission()) {
            Intent i = new Intent(getApplicationContext(), DetectorActivity.class);
            startActivityForResult(i, ACTIVITY_REQUEST_CODE);
        } else
            requestPermission();
    }

    public void onHelpClick(View v) {
        new AlertDialog.Builder(this)
                .setTitle("Info")
                .setMessage(R.string.help_text)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public void onClickScanHand(View v) {
        openCameraView();
    }

    public void onClickScanTalon(View v) {
        openCameraView();
    }

    public void onClickStartAgain(View v) {
        resetGame();
    }

    public void onClickHideCards(View v) {
        setPrivateStatsVisibility(probabilityToWin.getVisibility() == View.INVISIBLE);
    }

    private void resetGame() {
        nextRound = DEFAULT_ROUND;
        deck = new Deck();
        talon = new Talon();
        playerPocket = new PlayerPocket();

        scanHand.setVisibility(View.VISIBLE);
        scanFlop.setVisibility(View.INVISIBLE);
        for (CardSpinner hand_card : hand_cards) hand_card.setVisibility(View.INVISIBLE);
        for (CardSpinner talon_card : talon_cards) talon_card.setVisibility(View.INVISIBLE);

        reCalc();
    }

    /** Update card value or signal error */
    private void checkAndChangeCard(CardSpinner cs, boolean newCardDesc) {
        Card otherCard = new Card(cs.getText());

        if (!otherCard.isCorrect() || isDuplicateCard(otherCard, newCardDesc)) {
            handValResult.setText(R.string.invalid);
            cs.setTextColor(Color.YELLOW);  // error indication
            return;
        }

        cs.updateTextColor();

        if (!updateLock && newCardDesc) // card has changed
            composeCardSets();
    }

    /** Remove and return a card from the prediction stack */
    private String getPredictedCard() {
        if (curr_prediction_stack == null) return null;

        while (!curr_prediction_stack.isEmpty()) {
            String card = curr_prediction_stack.remove(0);
            if (!isDuplicateCard(new Card(card), true))
                return card;
        }
        return null;
    }

    /** Display next detected card or pick a random card if not recognized */
    private void drawCard(CardSpinner cs, boolean addToTalon) {
        String card = getPredictedCard();

        // Card not recognized in image -> pick random card
        if (card == null)
            card = deck.draw(1).get(0).getCardAsShortString();
        else
            deck.addToAlreadyDrawnCards(new Card(card));

        if (addToTalon) talon.addCard(new Card(card));

        // do not create a new thread when updating the view
        updateLock = true;
        cs.setSelection(card);
        updateLock = false;
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

    /** Drawing 2 hand cards */
    private void drawRound0() {
        for (CardSpinner hand_card : hand_cards) drawCard(hand_card, false);

        setPrivateStatsVisibility(probabilityToWin.getVisibility() == View.VISIBLE);
        scanHand.setVisibility(View.INVISIBLE);
        scanFlop.setVisibility(View.VISIBLE);
    }

    /** Drawing first 3 talon cards */
    private void drawRound1() {
        for (int i = 0; i < 3; i++) {
            drawCard(talon_cards[i], true);
            talon_cards[i].setVisibility(View.VISIBLE);
        }
    }

    /** Drawing 4th talon card */
    private void drawRound2() {
        drawCard(talon_cards[3], true);
        talon_cards[3].setVisibility(View.VISIBLE);
    }

    /** Drawing 5th talon card */
    private void drawRound3() {
        drawCard(talon_cards[4], true);
        talon_cards[4].setVisibility(View.VISIBLE);
        scanFlop.setVisibility(View.INVISIBLE);
    }

    /** Visibility of hand cards and winning statistics */
    private void setPrivateStatsVisibility(boolean visible) {
        if (visible) {
            probabilityToWin.setVisibility(View.VISIBLE);
            mProgress.setVisibility(View.VISIBLE);
            if (nextRound != DEFAULT_ROUND) {
                hand_cards[0].setVisibility(View.VISIBLE);
                hand_cards[1].setVisibility(View.VISIBLE);
            }
            handValResult.setVisibility(View.VISIBLE);
            hideCards.setBackgroundResource(R.drawable.button_theme);
            hideCards.setText(R.string.hide);
            hideCards.setTextColor(ContextCompat.getColor(this, R.color.theme_black));
            bestHand.setVisibility(View.VISIBLE);
        } else {
            probabilityToWin.setVisibility(View.INVISIBLE);
            mProgress.setVisibility(View.INVISIBLE);
            hand_cards[0].setVisibility(View.INVISIBLE);
            hand_cards[1].setVisibility(View.INVISIBLE);
            handValResult.setVisibility(View.INVISIBLE);
            hideCards.setBackgroundResource(R.drawable.button_theme_inverse);
            hideCards.setText(R.string.show);
            hideCards.setTextColor(ContextCompat.getColor(this, R.color.theme_blue));
            bestHand.setVisibility(View.INVISIBLE);
        }
    }

    /** Recalculate the highest hand value and the winning probability */
    private void reCalc() {
        Player me = new Player(talon, playerPocket);
        deck.reset();
        deck.addToAlreadyDrawnCards(talon);
        deck.addToAlreadyDrawnCards(playerPocket);

        if (nextRound >= 0)
            handValResult.setText(me.getHighestHandVal().toString());
        else
            handValResult.setText("-");
        calcAndSetProbability();
    }

    /** Read available hand cards */
    private void composePlayerPocket() {
        playerPocket = new PlayerPocket();
        playerPocket.addCard(new Card(hand_cards[0].getText()));
        playerPocket.addCard(new Card(hand_cards[1].getText()));
        talon = new Talon();
    }

    /** Read first 3 available talon cards */
    private void composeTalon3() {   // talon with three cards
        talon = new Talon();
        for (int i = 0; i < 3; i++)
            talon.addCard(new Card(talon_cards[i].getText()));
    }

    /** Read 4th available talon card */
    private void composeTalon4() {  // talon with four cards
        composeTalon3();
        talon.addCard(new Card(talon_cards[3].getText()));
    }

    /** Read 5th available talon card */
    private void composeTalon5() {   // talon with five cards
        composeTalon4();
        talon.addCard(new Card(talon_cards[4].getText()));
    }

    /** Update card changes and recalculate winning probability */
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

    private void calcAndSetProbability() {
        if (probCalculatorThread != null)
            probCalculatorThread.interrupt();

        probCalculatorThread = new Thread(() -> {
            final ProbabilityOfWinningCalculator prob = new ProbabilityOfWinningCalculator(talon, playerPocket, deck, noOfOpponents);
            final DecimalFormat df = new DecimalFormat("#0.0");
            double probs = 0;
            for (int i = 0; i < 100; i++) {
                if (Thread.currentThread().isInterrupted())
                    break;
                probs += prob.calcProbability(200);
                final double transmitProb = probs / (i + 1);
                handler.post(() -> {
                    mProgress.setProgress((int) transmitProb);
                    probabilityToWin.setText(df.format(transmitProb) + "%");
                });
            }
        });
        probCalculatorThread.start();
    }

    /** Convert String id to resource id*/
    private int getId(String id) {
        return getResources().getIdentifier(id, "id", getPackageName());
    }

    private boolean hasPermission() {
        return checkSelfPermission(PERMISSION_CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        if (shouldShowRequestPermissionRationale(PERMISSION_CAMERA))
            Toast.makeText(ManualEntryActivity.this, "Camera permission is required", Toast.LENGTH_LONG).show();
        else
            requestPermissions(new String[]{PERMISSION_CAMERA}, PERMISSIONS_REQUEST);
    }
}

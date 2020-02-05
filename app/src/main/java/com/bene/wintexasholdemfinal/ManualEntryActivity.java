package com.bene.wintexasholdemfinal;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.bene.wintexasholdemfinal.TH_0_99_1.Card;
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

    private boolean default_round = true;   // default round without any cards
    private int noOfOpponents = 3;
    private ArrayList<String> curr_prediction_stack;    // first element has the highest confidence

    private TextView probabilityToWin = null;
    private TextView handValResult = null;
    private TextView chanceToWin = null;
    private TextView bestHand;

    private Button hideCards;

    private ProgressBar mProgress;
    private Handler handler = new Handler();

    private Thread probCalculatorThread;

    private Spinner noOfOpponentsSpinner = null;
    private CardSpinner[] hand_cards = new CardSpinner[2];
    private CardSpinner[] talon_cards = new CardSpinner[5];

    private Deck deck = new Deck();
    private Talon talon = new Talon();
    private PlayerPocket playerPocket = new PlayerPocket();

    private int hiddenCnt = 0;

    private boolean predictHandCards;           // true if pred. hand, false if pred. talon
    private boolean atLeastOneValid = false;    // at least one valid card available

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.MainTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manual_entry_activity);

        if (!hasPermission()) requestPermission();

        // set instance variables to be able to access text fields and images:
        probabilityToWin = findViewById(R.id.probabilityToWin);
        handValResult = findViewById(R.id.handValResult);
        chanceToWin = findViewById(R.id.chanceToWin);
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

        for (CardSpinner hand_card : hand_cards)
            if (hand_card.compareID(parent_id))
                checkAndChangeCard(hand_card);

        for (CardSpinner talon_card : talon_cards)
            if (talon_card.compareID(parent_id))
                checkAndChangeCard(talon_card);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == ACTIVITY_REQUEST_CODE) {
            curr_prediction_stack = data.getExtras().getStringArrayList("prediction");  // null is handled separately
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

    public void easterEgg(View v) {
        if(hiddenCnt > 7) {
            setContentView(R.layout.manual_entry_activity_light);
        } else
            hiddenCnt++;
    }

    public void onClickScanHand(View v) {
        predictHandCards = true;
        openCameraView();
    }

    public void onClickScanTalon(View v) {
        predictHandCards = false;
        openCameraView();
    }

    public void onClickStartAgain(View v) {
        resetGame();
    }

    public void onClickHideCards(View v) {
        setPrivateStatsVisibility(probabilityToWin.getVisibility() == View.INVISIBLE);
    }

    public void onHelpClick(View v) {
        new AlertDialog.Builder(this)
                .setTitle("INFO")
                .setMessage(R.string.help_text)
                .setIcon(R.drawable.help)
                .setPositiveButton("close", (arg0, arg1) -> arg0.dismiss())
                .show();
    }

    private void resetGame() {
        default_round = true;

        deck = new Deck();
        talon = new Talon();
        playerPocket = new PlayerPocket();

        for (CardSpinner hand_card : hand_cards) hand_card.reset();
        for (CardSpinner talon_card : talon_cards) talon_card.reset();

        stopStatisticThread();
        resetStatistics();
        handValResult.setText("-");
    }

    /** Update card value or signal error */
    private void checkAndChangeCard(CardSpinner cs) {
        Card otherCard = new Card(cs.getText());

        if (!otherCard.isCorrect()) {
            composeCardSet();
            cs.setTextColor(Color.WHITE);  // error indication
            return;
        }

        cs.updateTextColor();
        composeCardSet();
    }

    private void composeCardSet() {
        playerPocket = new PlayerPocket();
        talon = new Talon();
        atLeastOneValid = false;

        for (CardSpinner hand_card : hand_cards) {
            if (hand_card.isValid()) {
                playerPocket.addCard(new Card(hand_card.getText()));
                atLeastOneValid = true;
            }
        }
        for (CardSpinner talon_card : talon_cards) {
            if (talon_card.isValid()) {
                talon.addCard(new Card(talon_card.getText()));
                atLeastOneValid = true;
            }
        }
        reCalc();
    }

    /** Remove and return a card from the prediction stack */
    private String getPredictedCard() {
        if (curr_prediction_stack == null) return null;

        while (!curr_prediction_stack.isEmpty()) {
            String card = curr_prediction_stack.remove(0);
            if (!deck.isAlreadyDrawn(new Card(card)))
                return card;
        }
        return null;
    }

    /** Display next detected card or pick a random card if not recognized */
    private void drawCard(CardSpinner cs) {
        if(cs.isValid()) return;  // card position already manually selected

        String card = getPredictedCard();   // get most likely predicted card

        if (card == null) return;   // no card recognized

        cs.setSelection(card);
    }

    private void drawNextRound() {
        if(predictHandCards)
            for (CardSpinner hand_card : hand_cards) drawCard(hand_card);
        else
            for (CardSpinner talon_card : talon_cards) drawCard(talon_card);
    }

    /** Visibility of hand cards and winning statistics */
    private void setPrivateStatsVisibility(boolean visible) {
        if (visible) {
            probabilityToWin.setVisibility(View.VISIBLE);
            mProgress.setVisibility(View.VISIBLE);
            hand_cards[0].setVisibility(View.VISIBLE);
            hand_cards[1].setVisibility(View.VISIBLE);
            handValResult.setVisibility(View.VISIBLE);
            chanceToWin.setVisibility(View.VISIBLE);
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
            chanceToWin.setVisibility(View.INVISIBLE);
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

        if (atLeastOneValid) {
            if (playerPocket.getCards().size() + talon.getCards().size() >= 5) {
                handValResult.setText(me.getHighestHandVal().toString());
            } else {
                handValResult.setText("-");
            }
            calcAndSetProbability();
        }
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
                probs += prob.calcProbability(50);
                final double transmitProb = probs / (i + 1);
                handler.post(() -> {
                    if (default_round) {
                        resetStatistics();
                        default_round = false;
                    } else if (duplicateCards()) {
                        handValResult.setText("DUPLICATE");
                        resetStatistics();
                    } else {
                        mProgress.setProgress((int) transmitProb);
                        probabilityToWin.setText(df.format(transmitProb) + "%");
                    }
                });
            }
        });
        probCalculatorThread.start();
    }

    private boolean duplicateCards() {
        ArrayList<Integer> alreadyDrawn = deck.getAlreadyDrawnIndices();

        for (int i = 0; i < alreadyDrawn.size()-1; i++)
            for ( int j = i+1; j < alreadyDrawn.size(); j++)
                if (alreadyDrawn.get(i) == (int)alreadyDrawn.get(j))
                    return true;
        return false;
    }

    private void stopStatisticThread() {
        if (probCalculatorThread != null) {
            probCalculatorThread.interrupt();
            try {
                probCalculatorThread.join();
            } catch (InterruptedException e) {
                // do nothing
            }
            probCalculatorThread = null;
        }
    }

    private void resetStatistics() {
        probabilityToWin.setText("");
        mProgress.setProgress(0);
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

package org.tensorflow.lite.examples.detection.TH_0_99_1;

import java.util.ArrayList;
import java.util.Collections;

public class Player {
	private Talon talon;
	private PlayerPocket playerPocket;
	private ArrayList<Card> allCards; // all cards which are in talon + playerPocket
	private ArrayList<Hand> allHands;
	public Player(Talon t, PlayerPocket pp) {
		talon= t;
		playerPocket= pp;
		allCards= new ArrayList<Card>(); // originally: new Card[7];
		allHands= new ArrayList<Hand>();
	}
	public void display() {
		talon.display();
		playerPocket.display();
	}
	// the source code of the following method generateAllCombinations() is from
	//    http://hmkcode.com/calculate-find-all-possible-combinations-of-an-array-using-java/
	public ArrayList<Hand> generateAllCombinations(ArrayList<Card> elements, int K){
		allHands= new ArrayList<Hand>();
		// get the length of the array
		// e.g. for {'A','B','C','D'} => N = 4
		int N = elements.size();
		if(K > N){
			System.out.println("Invalid input, K > N");
			return allHands;
		}
		// calculate the possible combinations c(N,K)  e.g., c(4,2)

		// get the combination by index
		// e.g. 01 --> AB , 23 --> CD
		int combination[] = new int[K];

		// position of current index
		//  if (r = 1)              r*
		//  index ==>        0   |   1   |   2
		//  element ==>      A   |   B   |   C
		int r = 0;
		int index = 0;
		while(r >= 0) {
			// possible indexes for 1st position "r=0" are "0,1,2" --> "A,B,C"
			// possible indexes for 2nd position "r=1" are "1,2,3" --> "B,C,D"

			// for r = 0 ==> index < (4+ (0 - 2)) = 2
			if(index <= (N + (r - K))) {
				combination[r] = index;
				// if we are at the last position 'print' and increase the index
				if(r == K-1) {
					//do something with the combination e.g. add to list or print
					Hand hand= new Hand();
					for (int i= 0; i < K; i++) {
						hand.addCard(allCards.get(combination[i]));
					}
					hand.getHandVal();
					allHands.add(hand);

					index++;
				}
				else {
					// select index for next position
					index = combination[r]+1;
					r++;
				}
			}
			else {
				r--;
				if(r > 0)
					index = combination[r]+1;
				else
					index = combination[0]+1;
			}
		}
		return allHands;
	}
	public void generateHands() {
		allHands= new ArrayList<Hand>();
		allCards= new ArrayList<Card>();
		for (Card c: playerPocket.getCards()) {
			allCards.add(c);
		}
		for (Card c: talon.getCards()) {
			allCards.add(c);
		}
		if (allCards.size() < 5) {
			System.out.println("ERROR: player with less than 5 cards available => hands cannot be generated");
			return;
		}
		allHands= generateAllCombinations(allCards, 5);
	}
	public HandVal getHighestHandVal() {
		generateHands();
		if (allHands.size() == 0) {
			System.out.println("ERROR: NO hands; they have not yet been generated");
			return HandVal.NOTVALID;
		}
		Hand highestHand= allHands.get(0);
		for (Hand h : allHands) {
			if (h.getHandVal().ordinal() > highestHand.getHandVal().ordinal()) {
				highestHand= h;
			}
		}
		return highestHand.getHandVal();
	}
	public ArrayList<Hand> getHighestHands() {
		HandVal highestHandVal= getHighestHandVal();
		ArrayList<Hand> highestHands= new ArrayList<Hand>();
		for (Hand h : allHands) {
			if (h.getHandVal().ordinal() == highestHandVal.ordinal()) {
				highestHands.add(h);
			}
		}
		System.out.println("number of highest hands: " + highestHands.size());
		return highestHands;
	}
	public Hand getHighestHand() {
		generateHands();
		Collections.sort(allHands, Collections.reverseOrder());
		return allHands.get(0);
	}
	public ArrayList<Hand> getAllHands() {
		return allHands;
	}
}
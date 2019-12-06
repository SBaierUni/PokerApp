package TH_0_99_1;
import java.util.ArrayList;

public class Hand extends CardSet implements Comparable<Hand> {
	private int cardsCalculator[][]= new int[5][14];
	private HandVal handVal= HandVal.NOTVALID;
	protected ArrayList<Integer> tieBreakers;  // Rank values of cards in the hand
	public Hand() {
		super("hand", 5);
	}
	public Hand(String cardsAsString) {
		super(cardsAsString, "hand");
	}
	public HandVal getHandVal() {
		if (handVal != HandVal.NOTVALID) {
			return handVal;
		}
		if (currCardIndex != maxNoOfCards - 1) {	// all five cards need to be set ...
			return HandVal.NOTVALID;	//    otherwise the hand value
			//    cannot be calculated
		}
		tieBreakers= new ArrayList<Integer>();
		resetCardsCalculator();
		calcColAndRowSums();
		//printCardsCalculator();
		handVal= assessHand();
		//System.out.println(handVal);
		//System.out.println(tieBreakers);
		return handVal;
	}
	private void resetCardsCalculator() {
		// initialize the 2-dimensional array with zeros
		for (int row= 0; row < 5; row= row + 1) {
			for (int col= 0; col < 14; col= col + 1) {
				cardsCalculator[row][col]= 0;
			}
		}
		// initialize the cardsCalculator with the cards stored in CardSet::cards[]
		for (int i= 0; i < maxNoOfCards; i= i + 1)	{	// for (initialization ; condition ; increment/decrement)
			cardsCalculator[cards[i].getSuit().ordinal()]
					[cards[i].getRank().ordinal()]++;
		}
	}
	private void calcColAndRowSums() {
		// calculate column sums
		int cSum= 0;
		for (int col= 0; col < 13; col++) {
			for (int row= 0; row < 4; row++) {
				cSum= cSum + cardsCalculator[row][col];
			}
			cardsCalculator[4][col]= cSum;
			cSum= 0;
		}
		// calculate row sums
		int rSum= 0;
		for (int row= 0; row < 4; row++) {
			for (int col= 0; col < 13; col++) {
				rSum= rSum + cardsCalculator[row][col];
			}
			cardsCalculator[row][13]= rSum;
			rSum= 0;
		}
	}
	private void printCardsCalculator() {
		for (int row= 0; row < 5; row++) {
			for (int col= 0; col < 13; col++) {
				System.out.print(cardsCalculator[row][col]); System.out.print("  ");
			}
			System.out.println();
		}
	}
	private int rankValOfColSumHighToLow(int colSum) {
		for (int col= 12; col >= 0; col--) {
			if (cardsCalculator[4][col] == colSum) {
				return col;
			}
		}
		return -1;
	}
	private int rankValOfColSumLowToHigh(int colSum) {
		for (int col= 0; col < 14 ; col++) {
			if (cardsCalculator[4][col] == colSum) {
				return col;
			}
		}
		return -1;
	}
	private void addRanksToTiebreaker(int numberOfCards){
		for (int col = 12; col >= 0; col--){
			if(cardsCalculator[4][col] == numberOfCards){
				tieBreakers.add(col);
			}
		}
	}
	private HandVal assessHand() {
		tieBreakers.clear();
		if (isStraight() && isFlush() && cardsCalculator[4][12] == 1 && cardsCalculator[4][0] != 1)
			return HandVal.ROYALFLUSH;
		if (isStraight() && isFlush()){
			int low = rankValOfColSumLowToHigh(1);
			int high = rankValOfColSumLowToHigh(1);
			if(high == 12 && low == 0){
				tieBreakers.add(3);
			} else{
				tieBreakers.add(high);
			}
			return HandVal.STRAIGHTFLUSH;
		}
		if (isPoker()) {
			addRanksToTiebreaker(4);
			addRanksToTiebreaker(1);
			return HandVal.POKER;
		}
		if (isFullHouse()){
			addRanksToTiebreaker(3);
			addRanksToTiebreaker(2);
			return HandVal.FULLHOUSE;
		}
		if (isFlush()){
			addRanksToTiebreaker(1);
			return HandVal.FLUSH;
		}
		if (isStraight()){
			int low = rankValOfColSumLowToHigh(1);
			int high = rankValOfColSumLowToHigh(1);
			if(high == 12 && low == 0){
				tieBreakers.add(3);
			} else{
				tieBreakers.add(high);
			}
			return HandVal.STRAIGHT;
		}
		if (isThreeOfAKind()){
			addRanksToTiebreaker(3);
			addRanksToTiebreaker(1);
			return HandVal.THREEOFAKIND;
		}
		if (isTwoPairs()){
			addRanksToTiebreaker(2);
			addRanksToTiebreaker(1);
			return HandVal.TWOPAIRS;
		}
		if (isOnePair()){
			addRanksToTiebreaker(2);
			addRanksToTiebreaker(1);
			return HandVal.ONEPAIR;
		}
		if (isHighCard()){
			addRanksToTiebreaker(1);
			return HandVal.HIGHCARD;
		}
		return HandVal.NOTVALID;
	}
	private boolean isStraight() {
		String sumOfColsStr="";
		sumOfColsStr+= new Integer(cardsCalculator[4][12]).toString();  //no of aces as first digit in the string
		for (int col= 0; col < 13; col++) {
			sumOfColsStr+= new Integer(cardsCalculator[4][col]).toString();
		}
		// 'sumOfColsStr' is the bottom ∑ line of handCalculator as string,
		//       with the number of aces as first and last digit
		return sumOfColsStr.contains("11111");
	}
	private boolean isFlush() {
		for (int row= 0; row < 4; row++) {
			if (cardsCalculator[row][13] == 5) {
				return true;
			}
		}
		return false;
	}
	private boolean isPoker() {
		for (int col= 0; col < 14; col++) {
			if (cardsCalculator[4][col] == 4) {
				return true;
			}
		}
		return false;
	}
	private boolean isFullHouse() {
		return isThreeOfAKind() && isOnePair();
	}
	private boolean isThreeOfAKind() {
		for (int col= 0; col < 14; col++) {
			if (cardsCalculator[4][col] == 3) {
				return true;
			}
		}
		return false;
	}
	private boolean isTwoPairs() {
		boolean isTwoPairs= (noOfPairs() == 2);
		return  isTwoPairs;
	}
	private boolean isOnePair() {
		boolean isOnePair= (noOfPairs() == 1);
		return  isOnePair;
	}
	private int noOfPairs() {
		int noOfPairs= 0;
		for (int col= 0; col < 14; col++) {
			if (cardsCalculator[4][col] == 2) {
				noOfPairs++;   // noOfPairs= noOfPairs + 1;
			}
		}
		return noOfPairs;
	}
	private boolean isHighCard() {
		boolean moreFound= false;
		for (int col= 0; col < 14; col++) {
			if (cardsCalculator[4][col] > 1)
				moreFound= true;
		}
		return !moreFound;   // not (!) more than ones (1s) found
	}
	@Override
	public int compareTo(Hand otherHand) { // 0 if equal, -1 if I'm < otherHand,
		// 1 if I'm > otherHand
		if (this.getHandVal().ordinal() > otherHand.getHandVal().ordinal()) {
			return 1;
		}
		if (this.getHandVal().ordinal() < otherHand.getHandVal().ordinal()) {
			return -1;
		}
		if (this.getHandVal().ordinal() == otherHand.getHandVal().ordinal()) {
			if (this.tieBreakers.size() != otherHand.tieBreakers.size()) {
				System.out.println("ERROR in compareTo: tieBreakers lists have to be the same length");
				System.out.println(this);
				System.out.println(this.getHandVal());
				System.out.println(this.tieBreakers);
				System.out.println(otherHand);
				System.out.println(otherHand.getHandVal());
				System.out.println(otherHand.tieBreakers);
				return 0;
			}
			for (int i= 0; i < this.tieBreakers.size(); i++) {
				// System.out.println("compareto..." + tieBreakers.get(i) + "   "+otherHand.tieBreakers.get(i) );
				if (tieBreakers.get(i) > otherHand.tieBreakers.get(i))
					return 1;
				if (tieBreakers.get(i) < otherHand.tieBreakers.get(i))
					return -1;
			}
		}
		return 0;
	}
}
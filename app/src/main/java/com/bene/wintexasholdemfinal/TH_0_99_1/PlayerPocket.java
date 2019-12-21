package com.bene.wintexasholdemfinal.TH_0_99_1;

import java.util.ArrayList;

public class PlayerPocket extends CardSet {
	public PlayerPocket() {
		super("player pocket", 2);
	}
	public PlayerPocket(ArrayList<Card> cardList) {
		super("player pocket", 2);
		addCards(cardList);
	}
}
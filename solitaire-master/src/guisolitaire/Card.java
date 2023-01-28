package guisolitaire;


class Card {
	private final Suit suit;
	private final Value value;
	
	private boolean revealed = false, selected = false;
	
	/**
	 * Construct a new card with specified Suit and Value
	 */
	Card(Suit suit, Value value) {
		this.suit = suit;
		this.value = value;
	}
	
	/**
	 * Get the card's Suit
	 */
	Suit getSuit() {
		return suit;
	}
	
	/**
	 * Get the card's value
	 */
	Value getValue() {
		return value;
	}
	
	/**
	 * Get the card's color
	 */
	CardColor getColor() {
		if (suit == Suit.HEART || suit == Suit.DIAMOND) {
			return CardColor.RED;
		} else {
			return CardColor.BLACK;
		}
	}
	
	/**
	 * Set this card's revealed to true
	 */
	void reveal() {
		revealed = true;
	}
	
	/**
	 * Get the value of revealed
	 * @return the value of revealed
	 */
	boolean isRevealed() {
		return revealed;
	}
	
	/**
	 * Toggle whether this card is selected or not
	 */
	void toggleSelected() {
		selected = !selected;
	}
	
	/**
	 * Get whether this card is selected	 */
	boolean isSelected() {
		return selected;
	}
	
	/**
	 * Get the name of this card 	 */
	String getName() {
		return value.toString().toLowerCase() + "of" + suit.toString().toLowerCase() + "s";
	}
	
	

}

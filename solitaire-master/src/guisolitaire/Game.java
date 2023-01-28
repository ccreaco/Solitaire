package guisolitaire;

import java.util.*;

import javafx.geometry.BoundingBox;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.Font;

public class Game {

	// Constants
	private final double CARD_WIDTH = 100, CARD_HEIGHT = 145.2, ROUNDING_FACTOR = 10, PADDING = 25;

	// Variables used to store the cards and bounds for the game
	private Stack<Card> hand = new Stack<>();
	private final Stack<Card> waste = new Stack<>();
	private final List<Stack<Card>> board = new ArrayList<>(), foundations = new ArrayList<>();
	private final BoundingBox handBounds = new BoundingBox(PADDING, PADDING, CARD_WIDTH, CARD_HEIGHT),
			wasteBounds = new BoundingBox(PADDING * 2 + CARD_WIDTH, PADDING, CARD_WIDTH, CARD_HEIGHT);
	private final List<BoundingBox> foundationsBounds = new ArrayList<>();
	private final List<List<BoundingBox>> boardBounds = new ArrayList<>();

	private final Map<String, Image> imageCache = new HashMap<>();
	private final Random random = new Random();
	private final GraphicsContext gc;

	// Variables that can change during the game which need global access
	private String alertText = "", winText = "";
	private Stack<Card> selected = new Stack<>();
	int vegasScore = -52;
	int gameScore = 0;
	int finalScore = 0;
	private ScoreTimer scoreTimer = new ScoreTimer();

	/**
	 * The main constructor which initialises the game in its entirety and handles
	 * all the card movement before drawing the game
	 * 
	 * @param gc the GraphicsContext to write to, should be gotten from a Canvas
	 *           object.
	 */
	Game(GraphicsContext gc) {
		this.gc = gc;
		initVars();
		loadImages();
		fillPack();
		shufflePack();
		layBoard();
		generateBoardBounds();
		revealCards();
		drawGame();
	}

	/**
	 * Initialises the 2D ArrayLists to avoid NullPointer exceptions
	 */
	public void initVars() {
		for (int i = 0; i < 4; i++) {
			foundations.add(new Stack<>());
			foundationsBounds
					.add(new BoundingBox(PADDING + (CARD_WIDTH + PADDING) * (3 + i), PADDING, CARD_WIDTH, CARD_HEIGHT));
		}
	}

	/**
	 * Fill the pack Stack with each card in a 52 card deck
	 */
	public void fillPack() {
		hand.clear();
		for (Suit suit : Suit.values()) {
			for (Value value : Value.values()) {
				hand.push(new Card(suit, value));
			}
		}
	}

	/**
	 * Shuffle the order of the pack. Uses a Fisher-Yates shuffle.
	 */
	public void shufflePack() {
		for (int i = 0; i < 52; i++) {
			swapCard(random.nextInt(52 - i), 51 - i);
		}
	}

	/**
	 * Swap the card at i1 in the pack with the card at i2
	 * 
	 * @param i1 the index of the first card to be swapped
	 * @param i2 the index of the second card to be swapped
	 */
	public void swapCard(int i1, int i2) {
		Card temp = hand.get(i1);
		hand.set(i1, hand.get(i2));
		hand.set(i2, temp);
	}

	/**
	 * Lay out the board from the pack of cards. Essentially places sequentially
	 * increasing stacks of cards on each space of the board
	 */
	public void layBoard() {
		board.clear();
		for (int i = 0; i < 7; i++) {
			Stack<Card> stack = new Stack<>();
			for (int j = 0; j < i + 1; j++) {
				stack.push(hand.pop());
			}
			board.add(stack);
		}
	}

	/**
	 * Generate a BoundingBox for each card on the board.
	 */
	public void generateBoardBounds() {
		boardBounds.clear();
		for (int i = 0; i < 7; i++) {
			boardBounds.add(new ArrayList<>());
			Stack<Card> stack = board.get(i);
			boardBounds.get(i).add(new BoundingBox(PADDING + (CARD_WIDTH + PADDING) * i, PADDING * 2 + CARD_HEIGHT,
					CARD_WIDTH, CARD_HEIGHT));
			for (int j = 1; j < stack.size(); j++) {
				boardBounds.get(i).add(new BoundingBox(PADDING + (CARD_WIDTH + PADDING) * i,
						PADDING * (2 + j) + CARD_HEIGHT, CARD_WIDTH, CARD_HEIGHT));
			}
		}
	}

	/**
	 * Turns the hand by taking the top card of the pack and placing it in the waste
	 * face up.
	 */
	public void turnHand() {
		waste.push(hand.pop());
	}

	/**
	 * Place all the cards in the waste back into the pack.
	 */
	public void resetHand() {
		int size = waste.size();
		for (int i = 0; i < size; i++) {
			hand.push(waste.pop());
		}
	}

	/**
	 * Check whether placing the child on the parent would be a valid move according
	 * to the board rules
	 * 
	 * @param parent the parent Card to check
	 * @param child  the child Card to check
	 * @return true if the move is valid
	 */
	public boolean isValidBoardMove(Card parent, Card child) {
		// Explicit if statements preserved for clarity
		if (parent == null) {
			return child.getValue() == Value.KING;
		}
		if (parent.getColor() == child.getColor()) {
			return false;
		}
		if (parent.getValue().ordinal() != child.getValue().ordinal() + 1) {
			return false;
		}
		return true;
	}

	/**
	 * Check whether placing the child on the parent would be a valid move according
	 * to the foundation rules
	 * 
	 * @param parent the parent Card to check
	 * @param child  the child Card to check
	 * @return true if the move is valid
	 */
	public boolean isValidFoundationsMove(Card parent, Card child) {
		// Explicit if statements preserved for clarity
		if (parent == null) {
			return child.getValue() == Value.ACE;
		}
		if (parent.getSuit() != child.getSuit()) {
			return false;
		}
		if (parent.getValue().ordinal() != child.getValue().ordinal() - 1) {
			return false;
		}
		return true;
	}

	/**
	 * Move the selected card to the specified Stack TODO: Make more efficient than
	 * checking each Stack for the presence of the selected card
	 * 
	 * @param stack the Stack to move to
	 */
	public void moveCards(Stack<Card> stack) {
		for (Card card : selected) {
			waste.remove(card);
			for (Stack<Card> boardStack : board) {
				boardStack.remove(card);
			}
			for (Stack<Card> foundStack : foundations) {
				foundStack.remove(card);
			}
			stack.push(card);
		}
		if (isGameWon()) {
			winText = "You win! ";
		}
	}

	private boolean isGameWon() {
		for (Stack<Card> stack : foundations) {
			if (stack.size() != 13) {
				return false;
			}
			int bonus = (2 * gameScore) - (10 * scoreTimer.secondsPassed);
			finalScore = gameScore + bonus;
			System.out.print("Klonkdie Score: " + finalScore);
			System.out.print("Vegas Score: " + vegasScore);
			
		}
		return true;
	}

	/**
	 * Draw the game to the provided GraphicsContext
	 */
	private void drawGame() {
		// Clear the game canvas
		gc.clearRect(0, 0, 1000, 900);

		// Draw the hand
		double x = PADDING, y = PADDING;
		if (hand.isEmpty()) {
			drawEmpty(x, y);
		} else {
			drawCardBack(x, y);
		}

		// Draw the waste
		x = PADDING * 2 + CARD_WIDTH;
		y = PADDING;
		if (waste.isEmpty()) {
			drawEmpty(x, y);
		} else {
			drawCard(waste.peek(), x, y);
		}

		// Draw the board
		for (int i = 0; i < 7; i++) {
			x = PADDING + (CARD_WIDTH + PADDING) * i;
			Stack<Card> stack = board.get(i);
			if (stack.isEmpty()) {
				drawEmpty(x, PADDING * 2 + CARD_HEIGHT);
			} else {
				for (int j = 0; j < stack.size(); j++) {
					y = PADDING * (3 + j) + CARD_HEIGHT;
					Card card = stack.get(j);
					if (card.isRevealed()) {
						drawCard(card, x, y);
					} else {
						drawCardBack(x, y);
					}
				}
			}
		}

		// Draw the foundations
		y = PADDING;
		for (int i = 0; i < 4; i++) {
			x = PADDING + (CARD_WIDTH + PADDING) * (3 + i);
			Stack<Card> stack = foundations.get(i);
			if (stack.isEmpty()) {
				drawEmpty(x, y);
			} else {
				drawCard(stack.peek(), x, y);
			}
		}

		// Draw the alert text
		drawText(alertText, 100, 900, 12, Color.RED, TextAlignment.RIGHT);

		// Draw the win text
		drawText(winText, 450, 350, 50, Color.BLACK, TextAlignment.CENTER);
	}

	/**
	 * Draw the specified Card to the game canvas
	 * 
	 * @param card the card to draw
	 * @param x    the x coordinate to draw at
	 * @param y    the y coordinate to draw at
	 */
	private void drawCard(Card card, double x, double y) {
		gc.drawImage(imageCache.get(card.getName()), x, y, CARD_WIDTH, CARD_HEIGHT);
		if (card.isSelected()) {
			gc.setStroke(Color.LIGHTBLUE);
			gc.setLineWidth(3);
			gc.strokeRoundRect(x, y, CARD_WIDTH, CARD_HEIGHT, ROUNDING_FACTOR, ROUNDING_FACTOR);
		}
	}

	/**
	 * Draw a cardback to the game canvas
	 * 
	 * @param x the x coordinate to draw at
	 * @param y the y coordinate to draw at
	 */
	private void drawCardBack(double x, double y) {
		gc.drawImage(imageCache.get("cardback"), x, y, CARD_WIDTH, CARD_HEIGHT);
	}

	/**
	 * Draw an empty card space to the game canvas
	 * 
	 * @param x the x coordinate to draw at
	 * @param y the y coordinate to draw at
	 */
	private void drawEmpty(double x, double y) {
		gc.setStroke(Color.WHITE);
		gc.setLineWidth(1);
		gc.strokeRoundRect(x, y, CARD_WIDTH, CARD_HEIGHT, ROUNDING_FACTOR, ROUNDING_FACTOR);
	}

	/**
	 * Load the images from the resource folder into the image cache for later use
	 */
	private void loadImages() {
		for (Suit suit : Suit.values()) {
			for (Value value : Value.values()) {
				String filename = value.toString().toLowerCase() + "of" + suit.toString().toLowerCase() + "s";
				imageCache.put(filename,
						new Image(this.getClass().getResourceAsStream("/res/cards/" + filename + ".png")));
			}
		}
		imageCache.put("cardback", new Image(this.getClass().getResourceAsStream("/res/cards/cardback.png")));
	}

	/**
	 * Handle the mouse being clicked
	 * 
	 * @param me the MouseEvent passed from the GUISolitaire class
	 */
	void handleMouseClicked(MouseEvent me) {
		double x = me.getX(), y = me.getY();

		// Reset the alert text
		alertText = null;

		// Handle hand interactivity
		if (handBounds.contains(x, y) && me.getClickCount() != 2) {
			handClicked();
			finish(me);
			return;
		}

		// Handle waste interactivity
		if (wasteBounds.contains(x, y) && me.getClickCount() != 2) {
			wasteClicked();
			finish(me);
			return;
		}

		// Handle board interactivity. Checks the bounds for each card on the board to
		// see if any have been clicked. If
		// the mouse was clicked at an overlap of bounds it selects the topmost one.
		boolean boardClicked = false;

		int indexX = -1, indexY = -1;
		for (int i = 0; i < 7; i++) {
			List<BoundingBox> boundsList = boardBounds.get(i);
			for (int j = 0; j < boundsList.size(); j++) {
				if (boundsList.get(j).contains(x, y)) {
					if (board.get(i).isEmpty() || board.get(i).get(j).isRevealed()) {
						indexX = i;
						indexY = j;
						boardClicked = true;
					}
				}
			}

			if (boardClicked && me.getClickCount() != 2) {
				boardClicked(indexX, indexY);
				finish(me);
				return;
			}
			if (boardClicked && me.getClickCount() == 2) {
				autoStackCard(indexX);
				finish(me);
				return;
			}
		}

		// waste double click
		boolean wasteBound = false;
		int index = -1;

		for (int t = 0; t < 7; t++) {
			if (wasteBounds.contains(x, y) && me.getClickCount() == 2) {
				Stack<Card> test = board.get(t);
				index = t;
				// indexX2 = 0;
				wasteBound = true;

			}
		}

		if (wasteBound) {
			autoStackWaste(index);
			finish(me);
			deselect();
			return;
		}

		// Handle foundations interactivity

		for (int i = 0; i < 4; i++) {
			if (foundationsBounds.get(i).contains(x, y) && me.getClickCount() != 2) {
				foundationsClicked(i);
				finish(me);
				return;
			}
		}

		// If nothing was clicked
		deselect();
		finish(me);
	}

	/**
	 * Deselect the currently selected card
	 */
	private void deselect() {
		if (!selected.isEmpty()) {
			for (Card card : selected) {
				card.toggleSelected();
			}
			selected.clear();
		}
	}

	/**
	 * Set the selected variable to be equal to a card
	 * 
	 * @param card the card to select
	 */
	private void select(Card card) {
		card.toggleSelected();
		selected.add(card);

	}

	/**
	 * Handle the hand being clicked
	 */
	private void handClicked() {
		if (hand.isEmpty()) {
			resetHand();
			gameScore = gameScore - 20;
			vegasScore = vegasScore - 50;
		} else {
			turnHand();
		}
		deselect();

	}

	/**
	 * Handle the waste being clicked
	 * 
	 * @return
	 */
	private boolean wasteClicked() {
		boolean wasteClicked = false;
		if (!waste.isEmpty()) {
			Card card = waste.peek();
			if (!selected.isEmpty() && selected.contains(card)) {
				deselect();
				wasteClicked = true;
			} else {
				deselect();
				select(card);
			}
		} else {
			deselect();
		}
		return wasteClicked;
	}

	/**
	 * Handles the board being clicked
	 * 
	 * @param indexX the column on the board clicked
	 * @param indexY the card in the column clicked
	 */
	private void boardClicked(int indexX, int indexY) {
		Stack<Card> stack = board.get(indexX);
		Card card = null;
		if (!stack.isEmpty()) {
			card = stack.get(indexY);

		}

		if (!selected.isEmpty()) {
			if (selected.contains(card)) {
				deselect();
			} else if (isValidBoardMove(card, selected.get(0)) && (indexY == stack.size() - 1 || indexY == 0)) {
				moveCards(stack);
				gameScore = gameScore + 5;
				vegasScore = vegasScore + 5;
				generateBoardBounds();
				deselect();
			} else {
				alertText = "Invalid move!";
				deselect();
			}
		} else {
			deselect();
			for (int i = indexY; i < stack.size(); i++) {
				select(stack.get(i));
			}
		}
	}

	/*
	 * Handles the card being double clicked
	 */
	public void autoStackCard(int index) {

		Stack<Card> boardStack = board.get(index);
		Card card = null;
		
		card = boardStack.peek();

		if (boardStack.isEmpty()) {
			card = boardStack.get(index);
		}

		if (card.getValue() != Value.ACE) {
			for (int i = 0; i < 4; i++) {
				Stack<Card> stack = foundations.get(i);
				Card f = null;

				if (!stack.isEmpty()) {
					f = stack.peek();
				}

				if (!selected.isEmpty()) {
					if (isValidFoundationsMove(f, card)) {
						moveCards(stack);
						vegasScore = vegasScore + 5;
						gameScore = gameScore + 10;
						generateBoardBounds();
						deselect();
					} else {
						System.out.println("not valid foundations move, checking board");
						for (int b = 0; b <= 6; b++) {
							Stack<Card> stackB = board.get(b);
							Card boardCards = null;
							
							if (!stackB.isEmpty()) {
								boardCards = stackB.peek();
							} 

							if (!selected.isEmpty()) {
								if (isValidBoardMove(boardCards, selected.get(0))) {
									System.out.println("move card to index: " + b + " " + boardCards.getName());
									moveCards(stackB);
									vegasScore = vegasScore + 5;
									gameScore = gameScore + 5;
									generateBoardBounds();
									deselect();
									break;
								}

							}

						}
					}
				}

			}
			deselect();
		}

		if (card.getValue() == Value.ACE) {
			for (int i = 0; i < 4; i++) {
				Stack<Card> stack = foundations.get(i);
				Card f = null;

				if (!stack.isEmpty()) {
					f = stack.peek();
				} 

				if (!selected.isEmpty()) {
					if (isValidFoundationsMove(f, card)) {
						moveCards(stack);
						vegasScore = vegasScore + 5;
						gameScore = gameScore + 10;
						generateBoardBounds();
						deselect();
					}

				}
			}
		}

	}

	private void autoStackWaste(int index) {

		Card card = waste.peek();

		if (card.getValue() != Value.ACE) {
			for (int i = 0; i < 4; i++) {
				Stack<Card> stack = foundations.get(i);
				Card f = null;

				if (!stack.isEmpty()) {
					f = stack.peek();
				} if(stack.isEmpty()) { 
					System.out.println("STACK EMPTY PILE");
				}

				if (!selected.isEmpty()) {
					if (isValidFoundationsMove(f, card)) {
						moveCards(stack);
						vegasScore = vegasScore + 5;
						gameScore = gameScore + 10;
						System.out.println(gameScore);
						generateBoardBounds();
						deselect();
					} else {

						for (int b = 0; b <= 6; b++) {
							Stack<Card> stackCheck = board.get(b);
							Card boardCards = null;
							
							if(!stackCheck.isEmpty()) { 
							boardCards = stackCheck.peek();
							}
							
							if (isValidBoardMove(boardCards, card)) {
								System.out.println("move " + card.getName() + " to index: " + b);
								moveCards(stackCheck);
								vegasScore = vegasScore + 5;
								gameScore = gameScore + 5;
								generateBoardBounds();
								deselect();

							}
						}

					}

				}
			}
			deselect();
		}

		if (card.getValue() == Value.ACE) {
			for (int i = 0; i < 4; i++) {
				Stack<Card> stack = foundations.get(i);
				Card f = null;

				if (!stack.isEmpty()) {
					f = stack.peek();
				}

				if (!selected.isEmpty()) {
					if (isValidFoundationsMove(f, card)) {
						moveCards(stack);
						gameScore = gameScore + 10;
						vegasScore = vegasScore + 5;
						generateBoardBounds();
						deselect();
					} 

				}
			}
		}

		deselect();
	}

	/**
	 * Handle the foundations being clicked
	 * 
	 * @param index the index of the foundation clicked
	 */
	private void foundationsClicked(int index) {
		Stack<Card> stack = foundations.get(index);
		System.out.println(stack);
		Card card = null;
		if (!stack.isEmpty()) {
			card = stack.peek();

		}

		if (!selected.isEmpty()) {
			if (selected.contains(card)) {
				deselect();
			} else if (isValidFoundationsMove(card, selected.get(0))) {
				System.out.println(stack);
				moveCards(stack);
				gameScore = gameScore + 10;
				vegasScore = vegasScore + 5;
				generateBoardBounds();
				deselect();
			} else {
				alertText = "Invalid move!";
				deselect();
			}
		} else if (card != null) {
			deselect();
			select(card);
		} else {
			deselect();
		}
	}

	/**
	 * Draw the specified text to the game canvas at the specified coordinates.
	 * 
	 */
	private void drawText(String text, double x, double y) {
		drawText(text, x, y, Font.getDefault().getSize(), Color.BLACK, TextAlignment.LEFT);
	}

	/**
	 * Draw the specified text to the game canvas at the specified coordinates with
	 * additional attributes.
	 * 

	 */
	private void drawText(String text, double x, double y, double size, Paint paint, TextAlignment textAlignment) {
		gc.setFont(new Font(size));
		gc.setFill(paint);
		gc.setTextAlign(textAlignment);
		gc.fillText(text, x, y);
	}

	/**
	 * Reveal any cards which are not yet revealed and on the bottom of their board
	 * stack
	 */
	private void revealCards() {
		for (Stack<Card> stack : board) {
			if (!stack.isEmpty()) {
				Card card = stack.peek();
				if (!card.isRevealed()) {
					card.reveal();
					
				}
			}
		}
	}

	/**
	 * Perform final cleanup for the handleMouseClicked() function
	 * 
	 * @param me the MouseEvent passed by handleMouseClicked()
	 */
	private void finish(MouseEvent me) {
		revealCards();
		drawGame();
		me.consume();

	}

	public String gameScore() {
		Label label = new Label();
		String score = String.valueOf(gameScore);
		label.setText(score);
		return score;
	}
	
	public String vegasScore() { 
		Label label = new Label();
		String score = String.valueOf(vegasScore);
		label.setText(score);
		return score;
	}

}
package guisolitaire;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

public class GUISolitaire extends Application {

	Game game;
	Pane root;
	ScoreTimer timer = new ScoreTimer();
	Stage savedStage;

	@Override
	public void start(Stage primaryStage) {

		Canvas canvas = new Canvas(1000, 1000);

		root = new Pane(canvas);
		root.setStyle("-fx-background-color: green");

		Scene scene = new Scene(root, Color.GREEN);

		game = new Game(canvas.getGraphicsContext2D());

		canvas.setOnMouseClicked(game::handleMouseClicked);

		timer.restart();
		timer.start();


		// timer label
		Label l = new Label();
		Timeline timeline = new Timeline(
				new KeyFrame(Duration.ZERO, actionEvent -> l.setText("Time elapsed: " + timer.s)),
				new KeyFrame(Duration.seconds(1)));
		timeline.setCycleCount(Timeline.INDEFINITE);
		timeline.play();

		l.setTranslateX(800);
		l.setTranslateY(950);
		root.getChildren().add(l);


		// New game button
		Button newGameButton = new Button();
		newGameButton.setText("New Game");
		newGameButton.setTranslateX(10);
		newGameButton.setTranslateY(950);
		root.getChildren().add(newGameButton);

		EventHandler<ActionEvent> newGameEvent = new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				timer.stop();
				timer.restart();
				start(primaryStage);
			}

		};

		newGameButton.setOnAction(newGameEvent);

		// vegas score button
		Button vegasScore = new Button();
		vegasScore.setText("Vegas Scoring");
		vegasScore.setTranslateX(100);
		vegasScore.setTranslateY(950);
		root.getChildren().add(vegasScore);
		EventHandler<ActionEvent> vegasScoreEvent = new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				Label score = new Label();
				Timeline scoreTimeline = new Timeline(new KeyFrame(Duration.ZERO, actionEvent -> score.setText("Score: " + game.vegasScore())),
						new KeyFrame(Duration.seconds(1)));
				scoreTimeline.setCycleCount(Timeline.INDEFINITE);
				scoreTimeline.play();
				score.setTranslateX(500);
				score.setTranslateY(950);
				root.getChildren().add(score);
				
			}

		};

		vegasScore.setOnAction(vegasScoreEvent);

		// regular score button
		Button regularScore = new Button();
		regularScore.setText("Klondike Scoring");
		regularScore.setTranslateX(200);
		regularScore.setTranslateY(950);
		root.getChildren().add(regularScore);

		EventHandler<ActionEvent> regularScoreEvent = new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				Label score = new Label();
				Timeline scoreTimeline = new Timeline(new KeyFrame(Duration.ZERO, actionEvent -> score.setText("Score: " + game.gameScore())),
						new KeyFrame(Duration.seconds(1)));
				scoreTimeline.setCycleCount(Timeline.INDEFINITE);
				scoreTimeline.play();
				score.setTranslateX(500);
				score.setTranslateY(950);
				root.getChildren().add(score);
				
				

				
			}

		};

		regularScore.setOnAction(regularScoreEvent);

		Button instructions = new Button();
		instructions.setText("How to play");
		instructions.setTranslateX(325);
		instructions.setTranslateY(950);
		root.getChildren().add(instructions);

		instructions.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				showAlertWithHeaderText();
			}
		});
		

		primaryStage.setScene(scene);
		primaryStage.setTitle("Solitaire");
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);

	}

	private void showAlertWithHeaderText() {
		Alert alert = new Alert(AlertType.NONE);
		alert.setTitle("How to Play");
		// alert.setHeaderText("Standard Instructions:");
		alert.setContentText("Welcome to Solitaire! " + "\r\n"
				+ "The first objective is to release and play into position certain cards to build up each foundation, in sequence and in suit, from the ace through the king. The ultimate objective is to build the whole pack onto the foundations, and if that can be done, the Solitaire game is won.\r\n"
				+ "\r\n" + "The Play" + "\r\n"
				+ "The initial array may be changed by \"building\" - transferring cards among the face-up cards in the tableau. Certain cards of the tableau can be played at once, while others may not be played until certain blocking cards are removed. For example, of the seven cards facing up in the tableau, if one is a nine and another is a ten, you may transfer the nine to on top of the ten to begin building that pile in sequence. Since you have moved the nine from one of the seven piles, you have now unblocked a face down card; this card can be turned over and now is in play.\r\n"
				+ "\r\n"
				+ "As you transfer cards in the tableau and begin building sequences, if you uncover an ace, the ace should be placed in one of the foundation piles. The foundations get built by suit and in sequence from ace to king.\r\n"
				+ "\r\n"
				+ "Continue to transfer cards on top of each other in the tableau in sequence. If you can’t move any more face up cards, you can utilize the stock pile by flipping over the first card. This card can be played in the foundations or tableau. If you cannot play the card in the tableau or the foundations piles, move the card to the waste pile and turn over another card in the stock pile.\r\n"
				+ "\r\n"
				+ "If a vacancy in the tableau is created by the removal of cards elsewhere it is called a “space”, and it is of major importance in manipulating the tableau. If a space is created, it can only be filled in with a king. Filling a space with a king could potentially unblock one of the face down cards in another pile in the tableau.\r\n"
				+ "\r\n"
				+ "Continue to transfer cards in the tableau and bring cards into play from the stock pile until all the cards are built in suit sequences in the foundation piles to win!"
				+ "");

		alert.getDialogPane().getButtonTypes().add(ButtonType.OK);

		alert.showAndWait();
	}

}

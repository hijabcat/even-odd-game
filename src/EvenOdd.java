import javafx.application.Application;
import javafx.animation.Timeline;			//Timeline animations
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import java.text.DecimalFormat;
import javafx.event.ActionEvent;				//keyboard & events
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;						//gui stuff
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.control.Label;
import java.util.Random;						//actually creating random numbers
import java.io.File;								//mostly for saving high scores
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
 
public class EvenOdd extends Application{
	// GUI components only
	private final int windowWidth = 360;
	private final int windowHeight = 440;
	private Stage primaryStage;
	private Scene gameScene;
	private Scene gameOverScene;
	private Label timeLabel;
	private VBox gameOverPane;
	private GridPane mainGamePane;
	private Label randNumLabel;
	private Label scoreLabel;
	private Label bonusTimeLabel;
	private Label actualFinalScore;
	private Label actualHighScore;
	
	
	private static final double MILLISEC = 100;
	private Timeline timerAnimation;
	private static final double BONUS_TIME_MILLISEC = 2000;
	private Timeline bonusTimeAnimation;
	private final int INITIAL_TIME_REMAINING=10;
	private double timeRemaining = INITIAL_TIME_REMAINING;
	private DecimalFormat dFormatter = new DecimalFormat("0.0");
	
	private int randomNumber = (int)(Math.random()*10);
	private Random generator = new Random();
	private final int  RANDOM_LOWER_BOUND = 1;
	private final int RANDOM_UPPER_BOUND = 1000;
	private String currentUserGuess;
	
	private String gameMode = "waiting";
	
	private int finalScore=0;
	private int highScore=0;
	private int bonusTimerCounter=0;

	

	@Override
	public void start(Stage paramStage) throws Exception {
		primaryStage=paramStage;
		setUpGUI();
		
	}
	
	public static void main(String[] args) {
		Application.launch(args);

	}
	public void setUpGUI() {
		VBox timeScorePane = new VBox();
		timeScorePane.getStyleClass().addAll("align-center");
		timeScorePane.setId("timeScorePane");
		timeLabel = new Label(timeRemaining + "");
		timeLabel.setId("timeLabel");
		timeLabel.getStyleClass().addAll("largeText");
		bonusTimeLabel = new Label("");
		bonusTimeLabel.setId("bonusTimelabel");
		bonusTimeLabel.getStyleClass().addAll("smallText");
		scoreLabel = new Label(finalScore + "");
		scoreLabel.getStyleClass().addAll("mediumText");
		scoreLabel.setId("scoreLabel");
		timeScorePane.getChildren().addAll(timeLabel, bonusTimeLabel, scoreLabel);
		
		StackPane numberAreaPane = new StackPane();
		numberAreaPane.setPrefHeight(200);
		numberAreaPane.setId("numberAreaPane");
		randNumLabel = new Label("Press any key to start");
		randNumLabel.getStyleClass().addAll("mediumText");
		randNumLabel.setId("randNumlabel");
		numberAreaPane.getChildren().add(randNumLabel);
		
		Label evenLabel = new Label("	Even\n(right arrow)");
		evenLabel.getStyleClass().addAll("oddEvenLabels");
		StackPane evenPane = new StackPane();
		evenPane.setPrefWidth(windowWidth/2.0);
		evenPane.setId("evenPane");
		evenPane.getChildren().add(evenLabel);
		Label oddLabel = new Label("	Odd\n(left arrow)");
		oddLabel.getStyleClass().addAll("oddEvenLabels");
		StackPane oddPane = new StackPane();
		oddPane.setPrefWidth(windowWidth/2.0);
		oddPane.getStyleClass().addAll("odd-and-even-game");
		oddPane.setId("oddPane");
		oddPane.getChildren().add(oddLabel);
		GridPane evenOddPane = new GridPane();
		evenOddPane.addRow(0, oddPane, evenPane);
		StackPane evenOddContainerPane = new StackPane();
		evenOddContainerPane.getChildren().add(evenOddPane);
		
		mainGamePane = new GridPane();
		mainGamePane.addColumn(0, timeScorePane, numberAreaPane, evenOddContainerPane);
		gameScene = new Scene(mainGamePane);
		gameScene.getStylesheets().add("style.css");
		
		
		
		gameOverPane = new VBox();
		gameOverPane.getStyleClass().addAll("align-center");
		gameOverPane.setId("gameOverPane");
		VBox finalScorePane = new VBox();
		finalScorePane.setId("finalScorePane");
		finalScorePane.setPrefWidth(50);
		Label gameOverLabel = new Label ("Game Over");
		gameOverLabel.getStyleClass().addAll("largeText");
		Label finalScoreLabel = new Label("Score: ");
		finalScoreLabel.getStyleClass().addAll("mediumText");
		actualFinalScore = new 	Label(finalScore + "");
		actualFinalScore.getStyleClass().addAll("largeText");
		Label highScoreLabel = new Label("High Score:");
		highScoreLabel.getStyleClass().addAll("mediumText");
		actualHighScore = new Label (highScore + "");
		actualHighScore.getStyleClass().addAll("largeText");
		finalScorePane.getChildren().addAll(finalScoreLabel, actualFinalScore, highScoreLabel
				, actualHighScore);
		VBox finalScorePaneContainer = new VBox();
		finalScorePaneContainer.getChildren().addAll(finalScorePane);
		finalScorePaneContainer.getStyleClass().addAll("align-center");
		finalScorePaneContainer.setId("finalScorePaneContainer");
		Label restartInstructionsLabel = new Label("Press SPACE to restart");
		restartInstructionsLabel.getStyleClass().addAll("mediumText");
		restartInstructionsLabel.setId("restartInstructionsLabel");
		gameOverPane.getChildren().addAll(gameOverLabel, finalScorePaneContainer, 
				restartInstructionsLabel);
		gameOverScene = new Scene(gameOverPane);
		gameOverScene.getStylesheets().add("style.css");
		
		
		primaryStage.setTitle("Even Odd");
		primaryStage.setScene(gameScene);
		primaryStage.setScene(gameScene);		//sets the initial scene when the game is "waiting". Can easily be changed to gameOverScene to test GUI
	    primaryStage.setHeight(windowHeight);
	    primaryStage.setWidth(windowWidth);
	    primaryStage.setResizable(false);		
	    primaryStage.show();
	    
	    setUpAnimation();
	    setUpKeyAssociation();
	    setPaneFocus();
	    
		}
	private void setUpAnimation() {
		EventHandler<ActionEvent>timerEventHandler = (ActionEvent e) -> {
			updateTimer();
		};
		//animation for countdown timer
		timerAnimation = new Timeline(new KeyFrame(Duration.millis(MILLISEC), timerEventHandler));
		timerAnimation.setCycleCount(Timeline.INDEFINITE);
		
		EventHandler<ActionEvent>bonusTimeEventHandler = (ActionEvent e) -> {
			bonusTimeLabel.setText("+10 Sec");
		};
		EventHandler<ActionEvent>bonusTimeEventHandler2 = (ActionEvent e) -> {
			bonusTimeLabel.setText("");
		};
		
		bonusTimeAnimation = new Timeline(new KeyFrame (Duration.millis(0), bonusTimeEventHandler), new KeyFrame(Duration.millis(BONUS_TIME_MILLISEC), bonusTimeEventHandler2));
		bonusTimeAnimation.setCycleCount(1);
	}
		
		
	}

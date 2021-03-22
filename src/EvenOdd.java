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
	private Scene gameOverScene;			// scene display the score
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
	private int bonusTimeCounter=0;

	

	@Override
	public void start(Stage paramStage) throws Exception {
		primaryStage=paramStage;			
		setUpGUI();					// call method which creates the GUI
		
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
	    
	    setUpAnimation();					// methods which creates animation 
	    setUpKeyAssociation();
	    setPaneFocus();
	    
		}
	private void setUpAnimation() {								// sets up animation timelines
		EventHandler<ActionEvent>timerEventHandler = (ActionEvent e) -> {			// creates a handler 
			updateTimer();
		};
		//animation for countdown timer
		timerAnimation = new Timeline(new KeyFrame(Duration.millis(MILLISEC), timerEventHandler));
		timerAnimation.setCycleCount(Timeline.INDEFINITE);
		
		EventHandler<ActionEvent>bonusTimeEventHandler = (ActionEvent e) -> {		// handler to add bonus meesage
			bonusTimeLabel.setText("+10 Sec");
		};
		EventHandler<ActionEvent>bonusTimeEventHandler2 = (ActionEvent e) -> {		// handler to set back text to void
			bonusTimeLabel.setText("");
		};
		
		bonusTimeAnimation = new Timeline(new KeyFrame (Duration.millis(0), bonusTimeEventHandler), new KeyFrame(Duration.millis(BONUS_TIME_MILLISEC), bonusTimeEventHandler2));
		bonusTimeAnimation.setCycleCount(1);
	}
	public void setUpKeyAssociation() {				// calls the identifyKeyPress in specific pane.
		gameOverPane.setOnKeyPressed(e -> {
			identifyKeyPress(e);
		});
		mainGamePane.setOnKeyPressed(e -> {
			identifyKeyPress(e);
		});
	}
	public void identifyKeyPress(KeyEvent e) {
		if(gameMode.equals("waiting")){
			startAGame();
		}
		else if (gameMode.equals("running")) {
			switch(e.getCode()) {
				case LEFT:							// left arrow key and call method to check if its correct
					currentUserGuess = "ODD";
					isUserGuessCorrect();
					break;	
				case RIGHT:							// right arrow key and call method to check if its correct
					currentUserGuess ="EVEN";
					isUserGuessCorrect();
					break;
			}
		}
		else if (gameMode.equals("over")) {			// if game is over it can restart by pressing space
			if(e.getCode()== KeyCode.SPACE) {
				startAGame();
			}
		}
			
	}
	public void setPaneFocus() {					// sets the focus to the correct pane
		if(gameMode.equals("over")) {				// if game is over, gameoverpane should be the focus
			gameOverPane.requestFocus();
		}else {
			mainGamePane.requestFocus();
		}
	}
	public void startAGame() {
		finalScore = 0;								// resets score and timer
		bonusTimeCounter = 0;
		timeRemaining = INITIAL_TIME_REMAINING;
		
		primaryStage.setScene(gameScene);
		gameMode="running";
		timerAnimation.play();
		
		scoreLabel.setText(finalScore + "");
		randNumLabel.setId("randNumLabelRun");
		displayNewNumber();						// calls method which picks a new number and starts the game
	}
	public void updateTimer() {
		if(!gameMode.equals("over")) {			
			if(timeRemaining>0) {									// execute while>0
				timeRemaining -=.1;									//decrement by .01 for every 100th of second
				timeRemaining = (Math.round(timeRemaining*10))/10.0;
				timeLabel.setText(dFormatter.format(timeRemaining));		// update the  time remaining, and do string concatenation
			}
			else {															// game is over once time remaining is 0
				gameMode = "over";											
				showGameOver();												// call method which switch to game over scene
			}
				
		}
	}
	public void displayNewNumber() {								// creates a new random number and must be different from previous
		int tempOldRand = randomNumber;
		randomNumber = generator.nextInt(RANDOM_UPPER_BOUND)+ RANDOM_LOWER_BOUND;
		while(randomNumber == tempOldRand) {
			randomNumber = generator.nextInt(RANDOM_UPPER_BOUND);
		}
		randNumLabel.setText(randomNumber + "");
		
	}
	public void isUserGuessCorrect() {							// checks if the keypress matches even/odd, update the score & display a new number. Ignores all key except left/right arrow
		if(currentUserGuess.equals("EVEN")&&(randomNumber%2==0)) {			// checks if the number is even & their guess is correct
			updateScore();													// updates the score 
			displayNewNumber();												// display a new random number
		}
		else if(currentUserGuess.equals("ODD")&&(randomNumber%2!=0)) {		// checks if the number is odd & their guess is correct
			updateScore();
			displayNewNumber();
		}
		else {				// if the user enters the wrong answer, the game will end
			gameMode ="over";
			showGameOver();
		}
	}
	public void updateScore() {							// increase the score & also add 10 seconds more if the plaeyr gets 10 corrrect answers
		finalScore++;
		scoreLabel.setText(finalScore + "");
		bonusTimeCounter++;
		if(bonusTimeCounter == 10) {
			bonusTimeAnimation.play();					// display a bonus message 
			timeRemaining+=10;
			bonusTimeCounter=0;
		}
	}
	public void showGameOver() {					// shows the game over scene and calls the method readWriteHighScore
		readWriteHighScore();
		primaryStage.setScene(gameOverScene);
		actualFinalScore.setText(finalScore + "");
		setPaneFocus();
	}
	public void readWriteHighScore() {
		boolean needToUpdateFile = false;
		String scoresFileName = ".CONFIG_DO_NOT_MODIFY";
		Path highScoreFilePath = Paths.get(scoresFileName);
		File scoresFile = new File(scoresFileName);
		
		if (scoresFile.exists()) {
			needToUpdateFile = false;
			try (Scanner inputFile = new Scanner(scoresFile);){
				highScore = inputFile.nextInt();
				if(finalScore > highScore) {
					highScore = finalScore;
					needToUpdateFile = true;
					}
			}
			catch(FileNotFoundException e) {
				System.out.print("Error reading File");
			}
		}
		else
			highScore=finalScore;
		
		actualHighScore.setText(highScore + "");
		
		if(needToUpdateFile) {
			if(scoresFile.exists()) {
				try {
					Files.delete(highScoreFilePath);
				}
				catch (IOException e) {
					System.out.print("Error: Couldn't delete file before updating high score");
				}
			}
			try (PrintWriter actualScoreFile = new PrintWriter(scoresFile);){
				actualScoreFile.println(highScore);
				Files.setAttribute(highScoreFilePath, "dos:hidden", true);
			}
			catch(FileNotFoundException e) {
				System.out.println("Error saving high score, FileNotFoundException");
			} catch (IOException e) {
				System.out.println("Invalid permissions when saving hidden file");
			}
		}
	}
		
		
}

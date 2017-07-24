package view;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Observable;
import java.util.ResourceBundle;

import Model.DB.SokobanDBManager;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import javafx.util.StringConverter;
import view.canvases.BoxesCanvas;
import view.canvases.PlayerCanvas;
import view.canvases.SolidCanvas;

public class SokobanView extends Observable implements View, Initializable {
	private LinkedList<String> params;
	private Media[] sound = new Media[5];
	private Media completed;
	private MediaPlayer soundFXPlayer = null;
	private MediaPlayer bgPlayer = null;
	@FXML
	private Label timerLbl;
	@FXML
	private Label stepsLbl;
	// Set key codes.
	private KeyObject upK = new KeyObject(KeyCode.UP);
	private KeyObject downK = new KeyObject(KeyCode.DOWN);
	private KeyObject leftK = new KeyObject(KeyCode.LEFT);
	private KeyObject rightK = new KeyObject(KeyCode.RIGHT);
	@FXML
	MenuItem saveMenuItem;
	@FXML
	private SolidCanvas levelDisplayerC;
	@FXML
	private BoxesCanvas boxesC;
	@FXML
	private PlayerCanvas playerC;
	@FXML
	private Slider soundSlider;
	@FXML
	private Slider soundSlider1;
	@FXML
	private Label statusLbl;
	private LevelPaintManager paintManager;
	private ObservableList<String> players;
	private ObservableList<String> levels;
	private ObservableList<String> scores;
	private String lvlName;
	// private SokobanDBManager dbm;
	private String playerName = "";
	private File file;
	// private String currentLevelName="";
	private String levelStr;
	private int hintCount;
	private HashMap<Character, String> movesMap = new HashMap<>();

	@Override
	public void setDbm(SokobanDBManager dbm) {
		// this.dbm = dbm;
	}

	@Override
	public void loadLevel() {
		FileChooser fc = new FileChooser();
		hintCount = 0;
		fc.setTitle("Load level");
		fc.getExtensionFilters()
		.add(new FileChooser.ExtensionFilter("Level files (*.txt *.xml *.obj)", "*.txt", "*.xml", "*.obj"));
		fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt"));// Set
																									// txt
																									// filetr.
		fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml"));// Set
																									// xml
																									// filter.
		fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Object files (*.obj)", "*.obj"));// Set
																										// obj
																										// filter.
		fc.setInitialDirectory(new File("./"));
		file = fc.showOpenDialog(null);
		notifyLoad(file);

	}

	@Override
	public void saveLevel() {
		FileChooser fc = new FileChooser();
		fc.setTitle("Save level");
		fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt"));// Set
																									// txt
																									// filetr.
		fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml"));// Set
																									// xml
																									// filter.
		fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Object files (*.obj)", "*.obj"));// Set
																										// obj
																										// filter.
		fc.setInitialDirectory(new File("./"));
		File file = fc.showSaveDialog(null);
		if (file != null) {
			levelDisplayerC.requestFocus();
			params = new LinkedList<>();
			params.add("save");
			params.add(file.toString());
			setChanged();
			notifyObservers(params);
		}

	}

	private void notifyLoad(File file) {
		if (file != null) {
			levelDisplayerC.requestFocus();
			params = new LinkedList<>();
			params.add("load");
			params.add(file.toString());
			setChanged();
			notifyObservers(params);
			setKeyListener();
			timerLbl.setText("0");
			stepsLbl.setText("0");
			statusLbl.setText("level has loaded.");
			saveMenuItem.setDisable(false);
		}
	}

	private void soundManager(int soundNumber) {
		soundNumber++;
		if (soundNumber == 0) {// Check if loaded new level.
			bgPlayer.seek(Duration.ZERO);
			bgPlayer.setVolume((soundSlider1.getValue() / 100));
			bgPlayer.play();
		} else {
			soundFXPlayer = new MediaPlayer(sound[soundNumber]);
			soundFXPlayer.setVolume((soundSlider.getValue() / 100));
			soundFXPlayer.play();
		}
	}

	public void paintLevel(LinkedList<String> arg) {
		if (arg.get(1) == "-1") {
			levelStr = arg.get(0);

		}
		soundManager(paintManager.paint(arg));// the paint manager return the
												// move number.
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		paintManager = new LevelPaintManager(levelDisplayerC, boxesC, playerC);
		setFocusListener();
		playerC.addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> levelDisplayerC.requestFocus());
		saveMenuItem.setDisable(true);
		setKeyListener();

		movesMap.put('u', "up");
		movesMap.put('d', "down");
		movesMap.put('l', "left");
		movesMap.put('r', "right");

		// Create media. TODO media location by property.
		sound[0] = new Media(new File("./res/Sound/background.mp3").toURI().toString());
		sound[1] = new Media(new File("./res/Sound/block.mp3").toURI().toString());
		sound[2] = new Media(new File("./res/Sound/step.mp3").toURI().toString());
		sound[3] = new Media(new File("./res/Sound/box.mp3").toURI().toString());
		sound[4] = new Media(new File("./res/Sound/destination.mp3").toURI().toString());
		completed = new Media(new File("./res/Sound/completed.wav").toURI().toString());
		bgPlayer = new MediaPlayer(sound[0]);
		soundFXPlayer = new MediaPlayer(sound[1]);
		bgPlayer.setOnEndOfMedia(new Runnable() {
			public void run() {
				bgPlayer.seek(Duration.ZERO);
			}
		});

		setSoundSliderListener();

	}

	private void setKeyListener() {

		// Key Press listener.
		levelDisplayerC.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (event.getCode() == upK.getKey())
					keyPressed("up");
				else if (event.getCode() == downK.getKey())
					keyPressed("down");
				else if (event.getCode() == leftK.getKey())
					keyPressed("left");
				else if (event.getCode() == rightK.getKey())
					keyPressed("right");
			}
		});
	}

	private void setSoundSliderListener() {
		setSliderLabeles(soundSlider, "SoundFX");
		setSliderLabeles(soundSlider1, "Background music");
		// Set sound slider listener. for sound fx.
		setBindPlayerAndSlider(soundSlider, soundFXPlayer);
		// For background music.
		setBindPlayerAndSlider(soundSlider1, bgPlayer);
	}

	private void setBindPlayerAndSlider(Slider soundSlider, MediaPlayer player) {
		soundSlider.valueProperty().addListener(new InvalidationListener() {

			@Override
			public void invalidated(javafx.beans.Observable arg0) {
				player.setVolume(soundSlider.getValue() / 100);
			}
		});
	}

	private void setSliderLabeles(Slider soundSlider, String midLable) {
		soundSlider.setShowTickLabels(true);
		soundSlider.setMinorTickCount(3);
		soundSlider.setLabelFormatter(new StringConverter<Double>() {
			@Override
			public String toString(Double n) {
				if (n == 0)
					return "Min";
				else if (n == 50)
					return midLable;

				return "Max";

			}

			@Override
			public Double fromString(String arg0) {
				return null;
			}
		});

	}

	@Override
	public void keyPressed(String direction) {
		params = new LinkedList<>();
		params.add("move");
		params.add(direction);
		setChanged();
		notifyObservers(params);
	}

	@Override
	public void LevelCompleted(LinkedList<String> params) {

		bgPlayer.stop();
		soundFXPlayer.stop();
		soundFXPlayer = new MediaPlayer(completed);
		soundFXPlayer.play();
		levelDisplayerC.setOnKeyPressed(null);
		 Platform.runLater(()->statusLbl.setText("level completed!"));
		// Platform.runLater(()->popUpHighScore());

	}

	private void popUpHighScore() {

		FXMLLoader loader = new FXMLLoader(getClass().getResource("AddNewHighScore.fxml"));
		try {
			BorderPane root = (BorderPane) loader.load();
			HighScoreController hsController = (HighScoreController) loader.getController();
			Scene scene = new Scene(root, 250, 200);
			Stage stage = new Stage();
			// hsController.setDbm(dbm);
			hsController.setSteps(this.stepsLbl.getText());
			hsController.setTime(this.timerLbl.getText());
			hsController.setLvlName(this.lvlName);
			stage.setScene(scene);
			stage.setAlwaysOnTop(true);
			stage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void exit() {
		bgPlayer.stop();
		params = new LinkedList<>();
		params.add("exit");
		setChanged();
		notifyObservers(params);
		Platform.exit();
	}

	public void setFocusListener() {
		levelDisplayerC.focusedProperty().addListener(new ChangeListener<Boolean>() {
			public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
				Platform.runLater(new Runnable() {
					public void run() {
						levelDisplayerC.requestFocus();
					}
				});
			}
		});
	}

	@Override
	public boolean isAlive() {
		return true;
	}

	public void setKeys() {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("KeySetting.fxml"));
		try {
			BorderPane root = (BorderPane) loader.load();
			KeySettingWindowController keyWindow = (KeySettingWindowController) loader.getController();
			Scene scene = new Scene(root, 250, 200);
			Stage stage = new Stage();
			keyWindow.setDownKey(downK);
			keyWindow.setLeftKey(leftK);
			keyWindow.setRightKey(rightK);
			keyWindow.setUpKey(upK);
			stage.setScene(scene);
			stage.setAlwaysOnTop(true);
			stage.showAndWait();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void updateTimer(LinkedList<String> timer) {
		Platform.runLater(() -> timerLbl.setText(timer.removeFirst()));
	}

	@Override
	public void updateStepCount(LinkedList<String> steps) {
		Platform.runLater(() -> stepsLbl.setText(steps.removeFirst()));
	}

	@Override
	public void setList(LinkedList<String> list) {
		for (String s : list)
			System.out.println(s);
	}

	public void showTable() {

		FXMLLoader loader = new FXMLLoader(getClass().getResource("showTable.fxml"));
		try {
			BorderPane root = (BorderPane) loader.load();
			ShowTableController tableController = (ShowTableController) loader.getController();
			Stage stage = new Stage();
			stage.setTitle("Scores Table");
			Scene scene = new Scene(root, 450, 400);
			stage.setScene(scene);
			stage.setAlwaysOnTop(true);
			// tableController.initialize(dbm, currentLevelName);
			stage.showAndWait();
			// tableController.getManager().close();
			// Set override X button exit.
			stage.setOnHiding(new EventHandler<WindowEvent>() {

				@Override
				public void handle(WindowEvent event) {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							tableController.getManager().close();
						}
					});
				}
			});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String getSolution() {
		if (levelStr == null || levelStr.equals(""))
			return "";
		Socket theServer;
		try {
			theServer = new Socket("127.0.0.1", 1234);

			BufferedReader serverInput = new BufferedReader(new InputStreamReader(theServer.getInputStream()));
			ObjectOutputStream outToServer = new ObjectOutputStream(theServer.getOutputStream());
			// #####Strin to char[][]#####
			String[] row = levelStr.split("\r\n");
			for (String sa : row)
				System.out.println(sa);
			char[][] carr = new char[row.length][];
			int i = 0;

			for (String s : row)
				carr[i++] = s.toCharArray();

			outToServer.writeObject(carr);
			outToServer.flush();

			String solution = serverInput.readLine();
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(solution);

			return solution;
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			System.out.println("ERROR: could not get solution from server.");
		}

		return "";
	}

	public void getHint() {
		String sol = getSolution();
		System.out.println(sol);
		if (!sol.equals("unsolvable")&&!sol.equals("null")) {
			 Platform.runLater(()->statusLbl.setText("hint "+hintCount+"/"+sol.length()));
			notifyLoad(file);
			if(hintCount<sol.length())
				hintCount++;
			makeMoves(sol,hintCount);
		
		}
	}

	public void solve() {
		 Platform.runLater(()->statusLbl.setText("Trying to solve.."));

		String sol = getSolution();
		System.out.println(sol);
		if (!sol.equals("unsolvable")&&!sol.equals("null")) {
			notifyLoad(file);
			makeMoves(sol,sol.length());
			 Platform.runLater(()->statusLbl.setText("Solving..."));

		}else
			 Platform.runLater(()->statusLbl.setText("level is not solveable!"));


	}

	private void makeMoves(String moves,int size) {

		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				char[] carr = moves.toCharArray();
				for (int i=0;i<size;i++) {
					keyPressed(movesMap.get(carr[i]));
					try {
						Thread.sleep(300);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}

			}
		});
		t.start();

	}
}

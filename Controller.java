// Shaan Jalal and Stephen Kolluri
package view;

import java.io.*;
import java.util.*;
import com.google.gson.*;
import javafx.stage.Stage;
import javafx.fxml.FXML;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class Controller {

	//variables of the current song
	@FXML
	TextField myName;
	@FXML
	TextField myArtist;
	@FXML
	TextField myAlbum;
	@FXML
	TextField myYear;

	//variables for when adding a new song
	@FXML
	TextField newName;
	@FXML
	TextField newArtist;
	@FXML
	TextField newAlbum;
	@FXML
	TextField newYear;

	//buttons in the UI
	@FXML
	Button AddToPlaylist;
	@FXML
	Button DeleteButton;
	@FXML
	Button EditButton;
	@FXML
	Button ConfirmButton;
	@FXML
	Button CancelButton;

	//variables for when editing songs 
	String dummyName = "", dummyArtist = "", dummyAlbum = "", dummyYear = "";
	String name = "name", artist = "artist", album = "album", year = "year";

	//Strings to simplify error messages and accessing the JSON input
	String ohno = "Error", ohyes = "Success!";
	String yearError = "Please input a valid song year this time!";
	String positiveYearError = "The year can not be less than 0!";
	String nameArtError = "Both the name and artist fields have to be filled out!";
	String VerticalError = "Vertical bar | is not permitted in the name, artist, or album fields!";
	String dup = "Looks like there's a duplicate song in the list!";
	String path = "src/songs.json";
	int dummyIndex = 0;

	// variables for when dealing with all the songs in the list
	private ObservableList<Song> songlist;
	@FXML
	ListView<Song> listView;

	// JSON/GSON variables for getting/writing/saving data to json files
	JsonElement File;
	JsonObject FileObj;
	JsonArray FileArr;

	public void start(Stage mainStage) throws FileNotFoundException {
		int i = 0;
		Gson Red = new Gson();
		File = JsonParser.parseReader(new FileReader(path));
		FileObj = File.getAsJsonObject();
		FileArr = FileObj.getAsJsonArray("Songs");
		songlist = FXCollections.observableArrayList();

		while(i < FileArr.size()) {
			songlist.add(Red.fromJson(FileArr.get(i), Song.class));
			i++;
		}
		sorting();
		updating();
		listView.setItems(songlist);
		listView.getSelectionModel().select(i);

		StatusQuo();
		OnDisplay(mainStage);
		listView.getSelectionModel().selectedIndexProperty().addListener((num) -> OnDisplay(mainStage));
	}

	public void OnDisplay(Stage mainStage) {
		Song Classic = listView.getSelectionModel().getSelectedItem();
		if (Classic == null) {
			setDefaults(myName, myArtist, myAlbum, myYear, "");
		} else {
			setSongDefaults(myName, myArtist, myAlbum, myYear, Classic);
		}
	}

	// AddToPlaylist button for a new song is pressed

	public void AddtoPlaylist(ActionEvent action) {
		Button button = (Button) action.getSource();
		if (button == AddToPlaylist) {
			String songName = newName.getText();
			String songArtist = newArtist.getText();
			String songYear = newYear.getText();
			String songAlbum = newAlbum.getText();
			Alert alert = new Alert(AlertType.CONFIRMATION);

			alert.setContentText("Are you sure you want to add this song?");
			Optional<ButtonType> option = alert.showAndWait();

			if (option.isPresent() && option.get() == ButtonType.OK){
			if (songName.equals("") || songArtist.equals("")) {
				alertPing(ohno, nameArtError);
			} else if (songName.contains("|") || songArtist.contains("|") || songAlbum.contains("|")) {
				alertPing(ohno, VerticalError);
			} else if (DuplicateCheck(songName, songArtist)) {
				alertPing(ohno, dup);	
			} else if (!songYear.equals("") && !Numeric(songYear)) {
				alertPing(ohno, yearError);
			} else if (Numeric(songYear) && Integer.parseInt(songYear) <= 0) {
				alertPing(ohno, positiveYearError);
			} else {
				JsonObject Temp = new JsonObject();
				setJsonProperties(Temp, songName, songArtist, songAlbum, songYear);
				FileArr.add(Temp);

				try (FileWriter fp = new FileWriter(path)) {
					Gson Son = new GsonBuilder().setPrettyPrinting().create();
					Son.toJson(File, fp);
				} catch (IOException D) {
					D.printStackTrace();
				}

				Song newSong = new Song(songName, songArtist, songAlbum, songYear);
				songlist.add(newSong);
				String The = newSong.name;
				String Black = newSong.artist;
				String Eyed = newSong.album;
				String Peas = newSong.year;
				sorting();
				updating();
				listView.getSelectionModel().select(newSong);
				alertPing(ohyes, The + " by " + Black + " - " + Eyed + "(" + Peas + ") has been added!");
			}
		}
		}
	}

	public void Delete(ActionEvent action) {
		Button button = (Button)action.getSource();
		if (button == DeleteButton) {
			Song Slide = listView.getSelectionModel().getSelectedItem();
			String name = Slide.name;
			String artist = Slide.artist;
			String album = Slide.album;
			String year = Slide.year;
			Alert alert = new Alert(AlertType.CONFIRMATION);
			
			alert.setContentText("Are you sure you want to delete this song?");
			Optional<ButtonType> choice = alert.showAndWait();

				if (choice.isPresent() && choice.get() == ButtonType.OK) {
					FileArr.remove(locating(name, artist));
					try (FileWriter fp = new FileWriter(path)) {
						Gson Son = new GsonBuilder().setPrettyPrinting().create();
						Son.toJson(File, fp);
					} catch (IOException E) {
						E.printStackTrace();
					}
					songlist.remove(Slide);
					sorting();
					updating();
					alertPing(ohyes, "Successfully deleted " + name + " by " + artist + " - " + album + "(" + year + ")");
				}
			}
		}

	public void Edit(ActionEvent action) {
		Button button = (Button) action.getSource();
		Song Moonlight = listView.getSelectionModel().getSelectedItem();
		if (Moonlight == null) alertPing(ohno, "You've got to select a song to edit!");
		
		if (button == EditButton) {
			dummyName = Moonlight.name;
			dummyArtist = Moonlight.artist;
			dummyIndex = locating(dummyName, dummyArtist);
			dummyAlbum = Moonlight.album;
			dummyYear = Moonlight.year;

			settingMyName(myName, true, 1);
			settingMyArtist(myArtist, true, 1);
			settingMyAlbum(myAlbum, true, 1);
			settingMyYear(myYear, true, 1);
			ConCanOpacity(ConfirmButton, CancelButton, 1);
			DelEditOpacity(DeleteButton, EditButton, 0);
			listView.setFocusTraversable(true);
			listView.setMouseTransparent(false);

		} else if (button == CancelButton) {
			myName.setText(dummyName);
			myArtist.setText(dummyArtist);
			myAlbum.setText(dummyAlbum);
			myYear.setText(dummyYear);
			StatusQuo();

		} else if (button == ConfirmButton) {
			String newName = myName.getText();
			String newArtist = myArtist.getText();
			String newYear = myYear.getText();
			String newAlbum = myAlbum.getText();
			String lowerArtist = newArtist.toLowerCase();
			String lowerDummyArt = dummyArtist.toLowerCase();
			String lowerName = newName.toLowerCase();
			String lowerDummyName = dummyName.toLowerCase();
			Alert alert = new Alert(AlertType.CONFIRMATION);

			alert.setContentText("Are you sure you want to edit this song?");
			Optional<ButtonType> choice = alert.showAndWait();

			if (choice.isPresent() && choice.get() == ButtonType.OK){
			if (!newYear.equals("") && !Numeric(newYear)) {
				alertPing(ohno, yearError);
			} else if (newName.contains("|") || newArtist.contains("|") || newAlbum.contains("|")) {
				alertPing(ohno, VerticalError);
			} else if (newName.equals("") || newArtist.equals("")) {
				alertPing(ohno, nameArtError);
			} else if (Numeric(newYear) && Integer.parseInt(newYear) <= 0) {
				alertPing(ohno, positiveYearError);
			} else {
				if (!(Objects.equals(lowerArtist, lowerDummyArt) && Objects.equals(lowerName, lowerDummyName)) && (DuplicateCheck(newName, newArtist))) {
					alertPing(ohno, dup);
				} else {
					JsonObject Riptide = (JsonObject) FileArr.get(dummyIndex);
					setJsonProperties(Riptide, newName, newArtist, newAlbum, newYear);

					Moonlight.name = newName;
					Moonlight.album = newAlbum;
					Moonlight.year = newYear;
					Moonlight.artist = newArtist;
					try (FileWriter fp = new FileWriter(path)) {
						Gson Son = new GsonBuilder().setPrettyPrinting().create();
						Son.toJson(File, fp);
					} catch (IOException F) {
						F.printStackTrace();
					}
					sorting();
					updating();
					alertPing(ohyes, "The song has been edited!");
					StatusQuo();
				}
			}
		}
	}
	}

	public void alertPing(String title, String message) {
		if (Objects.equals(title, ohyes)){
			Alert alert = new Alert(AlertType.INFORMATION);
			setAlerts(alert, title, message);
		}
		if (Objects.equals(title, ohno)){
			if(Objects.equals(message, yearError)){
				Alert alert = new Alert(AlertType.WARNING);
				setAlerts(alert, title, message);
			}
			else if (Objects.equals(message, nameArtError)){
				Alert alert = new Alert(AlertType.WARNING);
				setAlerts(alert, title, message);
			}
			else if (Objects.equals(message, dup)){
				Alert alert = new Alert(AlertType.ERROR);
				setAlerts(alert, title, message);
			}
			else if (Objects.equals(message, positiveYearError)){
				Alert alert = new Alert(AlertType.ERROR);
				setAlerts(alert, title, message);
			}
			else if (Objects.equals(message, VerticalError)){
				Alert alert = new Alert(AlertType.ERROR);
				setAlerts(alert, title, message);
			}
			else{
				Alert alert = new Alert(AlertType.WARNING);
				setAlerts(alert, title, message);
			}
		}
	}

	public void setAlerts(Alert alert, String title, String message){
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}

	public boolean Numeric(String possiblenum) {
		try {
			Integer.parseInt(possiblenum);
			return true;
		} catch (NumberFormatException EX) {
			return false;
		}
	}

	public boolean DuplicateCheck(String name, String artist) {
		artist = artist.toLowerCase();
		name = name.toLowerCase();
		int i = 0;
		while(i < songlist.size()) {
			String otherArtist = songlist.get(i).artist;
			String lowerArtist = otherArtist.toLowerCase();
			String otherName = songlist.get(i).name;
			String lowerName = otherName.toLowerCase();
			if ((Objects.equals(artist, lowerArtist) && Objects.equals(name, lowerName))) return true;
			i++;
		}
		return false;
	}

	public int locating(String name, String artist) {
		int j = 0;
		while(j < FileArr.size()) {
			JsonObject obj = (JsonObject) FileArr.get(j);
			JsonElement Taylor = obj.get("artist");
			JsonElement Swift = obj.get("name");
			String Zayn = Taylor.getAsString();
			String Malik = Swift.getAsString();
			if ((Objects.equals(artist, Zayn) && Objects.equals(name, Malik))) return j;
			j++;
		}
		return -1;
	}

	public void sorting() {
		Collections.sort(songlist, new Comparator<Song>() {
			@Override
			public int compare(Song song1, Song song2) {
				int num = 0;
				String name1 = song1.name;
				String name2 = song2.name;
				String art1 = song1.artist;
				String art2 = song2.artist;
				num = name1.compareToIgnoreCase(name2);
				if (num < 1 && num > -1) num = art1.compareToIgnoreCase(art2);
				return num;
			}
		});
	}

	public void updating() {
		listView.setCellFactory(list -> new ListCell<Song>() {
			@Override
			public void updateItem(Song song, boolean bool) {
				super.updateItem(song, bool);
				if (song != null) setText(song.name + " | " + song.artist);
				else setText("");
			}
		});
	}

	public void settingMyName(TextField myName, boolean TF, double num){
		myName.setEditable(TF);
		myName.setOpacity(num);
	}

	public void settingMyArtist(TextField myArtist, boolean TF, double num){
		myArtist.setEditable(TF);
		myArtist.setOpacity(num);
	}

	public void settingMyAlbum(TextField myAlbum, boolean TF, double num){
		myAlbum.setEditable(TF);
		myAlbum.setOpacity(num);
	}

	public void settingMyYear(TextField myYear, boolean TF, double num){
		myYear.setEditable(TF);
		myYear.setOpacity(num);
	}

	public void ConCanOpacity(Button ConfirmButton, Button CancelButton, double dig){
		ConfirmButton.setOpacity(dig);
		CancelButton.setOpacity(dig);
	}

	public void DelEditOpacity(Button DeleteButton, Button EditButton, double radix){
		DeleteButton.setOpacity(radix);
		EditButton.setOpacity(radix);
	}

	public void setDefaults(TextField myName, TextField myArtist, TextField myAlbum, TextField myYear, String string){
		myName.setText(string);
		myArtist.setText(string);
		myAlbum.setText(string);
		myYear.setText(string);
	}

	public void setSongDefaults(TextField myName, TextField myArtist, TextField myAlbum, TextField myYear, Song song){
		myName.setText(song.name);
		myArtist.setText(song.artist);
		myAlbum.setText(song.album);
		myYear.setText(song.year);
	}

	public void setJsonProperties(JsonObject obj, String aName, String aArtist, String aAlbum, String aYear){
		obj.addProperty(name, aName);
		obj.addProperty(artist, aArtist);
		obj.addProperty(album, aAlbum);
		obj.addProperty(year, aYear);
	}

	public void StatusQuo(){
		settingMyName(myAlbum, false, 0.5);
		settingMyArtist(myArtist, false, 0.5);
		settingMyAlbum(myAlbum, false, 0.5);
		settingMyYear(myYear, false, 0.5);
		ConCanOpacity(ConfirmButton, CancelButton, 0);
		DelEditOpacity(DeleteButton, EditButton, 1);
		listView.setMouseTransparent(false);
		listView.setFocusTraversable(true);
	}
}
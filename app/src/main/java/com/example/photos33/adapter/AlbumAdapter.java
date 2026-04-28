package com.example.photos33.adapter;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Controller for the Album List screen, the primary dashboard for non-admin users.
 * This class manages the display of a user's album collection, providing
 * summary information for each album (photo count and date range) and
 * facilitating album-level operations like creation, deletion, and renaming.
 * @author Christopher Tanudjaja
 */
public class AlbumAdapter{

    /**
     * Default constructor for AlbumListController.
     */
    public AlbumAdapter() {}

    /** ListView displaying the user's albums. */
    @FXML private ListView<Album> albumListView;

    /** The master data container for the application. */
    private UserData allData;
    /** The currently logged-in user. */
    private User currentUser;
    /** The observable list backing the album list view. */
    private ObservableList<Album> obsList;

    /** Formatter for displaying photo date ranges. */
    private final SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");

    /**
     * Injects data from the {@link LoginController} and populates the view.
     * Uses a custom CellFactory to fulfill the project requirement of
     * displaying name, count, and date range for every album.
     * @param allData The master {@link UserData} container.
     * @param currentUser The {@link User} currently logged into the session.
     */
    public void initData(UserData allData, User currentUser) {
        this.allData = allData;
        this.currentUser = currentUser;

        obsList = FXCollections.observableArrayList();

        try {
            if (currentUser instanceof RegularUser) {
                obsList.addAll(((RegularUser) currentUser).getAlbums());
            }
            albumListView.setItems(obsList);

            // Cell factory showing name, count, date range
            albumListView.setCellFactory(lv -> new ListCell<Album>() {
                @Override
                protected void updateItem(Album album, boolean empty) {
                    super.updateItem(album, empty);
                    if (empty || album == null) { setText(null); return; }

                    int count = album.getPhotoCount();
                    String dateRange = getDateRange(album);

                    setText(album.getAlbumName() +
                            "  |  " + count + (count == 1 ? " photo" : " photos") +
                            "  |  " + dateRange);
                }
            });

        } catch (Exception e) {
            showError("Load Error", "Could not load albums.");
        }
    }

    /**
     * Calculates the earliest and latest photo dates within an album.
     * @param album The album to analyze.
     * @return A string representing the date range, or "No photos" if empty.
     */
    private String getDateRange(Album album) {
        if (album.getPhotos().isEmpty()) return "No photos";

        Calendar earliest = null;
        Calendar latest   = null;

        for (Photo photo : album.getPhotos()) {
            Calendar d = photo.getDateTime();
            if (earliest == null || d.before(earliest)) earliest = d;
            if (latest   == null || d.after(latest))    latest   = d;
        }

        if (earliest == latest) {
            return sdf.format(earliest.getTime());
        }
        return sdf.format(earliest.getTime()) + " – " + sdf.format(latest.getTime());
    }

    /**
     * Transitions the application to the {@link AlbumViewController} upon
     * a double-click event on a specific album.
     * @param event The mouse event triggered by the user.
     */
    @FXML
    public void handleOpenAlbum(MouseEvent event) {
        if (event.getClickCount() == 2) {
            Album selected = albumListView.getSelectionModel().getSelectedItem();
            if (selected == null) return;

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/photos/view/albumView.fxml"));
                if (loader.getLocation() == null) {
                    showError("Navigation Error", "Could not find album view.");
                    return;
                }
                Parent root = loader.load();
                AlbumViewController ctrl = loader.getController();
                ctrl.initData(allData, selected, currentUser);

                Stage stage = (Stage) albumListView.getScene().getWindow();
                stage.setScene(new Scene(root));
            } catch (Exception e) {
                e.printStackTrace();
                showError("Open Error", "Could not open album.");
            }
        }
    }

    /**
     * Creates a new album for the current user.
     */
    @FXML
    public void handleCreateAlbum() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create Album");
        dialog.setHeaderText("Enter a name for the new album:");
        dialog.showAndWait().ifPresent(name -> {
            name = name.trim();
            if (name.isEmpty()) { showError("Invalid Input", "Album name cannot be empty."); return; }

            try {
                RegularUser regUser = (RegularUser) currentUser;
                if (regUser.getAlbum(name) != null) {
                    showError("Duplicate Album", "An album named '" + name + "' already exists.");
                    return;
                }
                Album newAlbum = new Album(name);
                regUser.addAlbum(newAlbum);
                obsList.add(newAlbum);
                DataManager.save(allData);
            } catch (Exception e) {
                showError("Create Error", "Could not create album.");
            }
        });
    }

    /**
     * Deletes the currently selected album after user confirmation.
     */
    @FXML
    public void handleDeleteAlbum() {
        Album selected = albumListView.getSelectionModel().getSelectedItem();
        if (selected == null) { showWarning("No Selection", "Please select an album to delete."); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete album '" + selected.getAlbumName() + "'?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    ((RegularUser) currentUser).removeAlbum(selected.getAlbumName());
                    obsList.remove(selected);
                    DataManager.save(allData);
                } catch (Exception e) {
                    showError("Delete Error", "Could not delete album.");
                }
            }
        });
    }

    /**
     * Logic for renaming an album.
     */
    @FXML
    public void handleRenameAlbum() {
        Album selected = albumListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("No Selection", "Please select an album to rename.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(selected.getAlbumName());
        dialog.setTitle("Rename Album");
        dialog.setHeaderText("Enter a new name:");

        dialog.showAndWait().ifPresent(newName -> {
            newName = newName.trim();
            if (newName.isEmpty()) {
                showError("Invalid Input", "Album name cannot be empty.");
                return;
            }
            if (newName.equals(selected.getAlbumName())) return;

            try {
                RegularUser regUser = (RegularUser) currentUser;

                if (regUser.getAlbum(newName) != null) {
                    showError("Duplicate Album", "An album named '" + newName + "' already exists.");
                    return;
                }

                Album renamed = new Album(newName);
                for (Photo p : selected.getPhotos()) renamed.addPhoto(p);

                regUser.removeAlbum(selected.getAlbumName());
                regUser.addAlbum(renamed);

                int index = obsList.indexOf(selected);
                obsList.set(index, renamed);

                albumListView.getSelectionModel().select(renamed);

                DataManager.save(allData);
            } catch (Exception e) {
                showError("Rename Error", "Could not rename album.");
            }
        });
    }

    /**
     * Navigates to the search subsystem.
     */
    @FXML
    public void handleSearch() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/photos/view/search.fxml"));
            if (loader.getLocation() == null) { showError("Navigation Error", "Could not find search view."); return; }
            Parent root = loader.load();
            SearchController ctrl = loader.getController();
            ctrl.initData(allData, currentUser);

            Stage stage = (Stage) albumListView.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            showError("Navigation Error", "Could not open search.");
        }
    }

    /**
     * Saves the current application state and returns to the login screen.
     */
    @FXML
    public void handleLogout() {
        try {
            DataManager.save(allData);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/photos/view/login.fxml"));
            if (loader.getLocation() == null) { showError("Navigation Error", "Could not find login view."); return; }
            Parent root = loader.load();
            LoginController ctrl = loader.getController();
            ctrl.setMainData(allData);

            Stage stage = (Stage) albumListView.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            showError("Logout Error", "Could not logout.");
        }
    }

    /**
     * Displays an error alert.
     * @param title Alert title.
     * @param content Error message content.
     */
    private void showError(String title, String content) {
        new Alert(Alert.AlertType.ERROR, content).showAndWait();
    }

    /**
     * Displays a warning alert.
     * @param title Alert title.
     * @param content Warning message content.
     */
    private void showWarning(String title, String content) {
        new Alert(Alert.AlertType.WARNING, content).showAndWait();
    }
}

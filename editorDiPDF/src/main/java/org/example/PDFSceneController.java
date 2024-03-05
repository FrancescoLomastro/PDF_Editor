package org.example;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class PDFSceneController implements Initializable {
    private static final float SCROLLBAR_SIZE = 15;
    private static final Paint OVERLAY_COLOR = Color.BLACK;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private VBox vBox;

    @FXML
    private StackPane errorPopUp;

    @FXML
    private Button popUpButton;

    @FXML
    private Label errorLabel;
    @FXML
    private Label savedLabel;
    @FXML
    private TextField pageNumberTextField;
    @FXML
    private Label pageMaxNumberLabel;
    @FXML
    private Button buttonNext;
    @FXML
    private Button buttonBack;
    private double width;
    private Pane globalPane;
    private boolean scrollEventEnabled = true;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        saveMenuItem.setDisable(true);
        setupPopUpButton();
        displayPDF();
        setupControls();
        updateTotalPageNumber();
        PDFView.getInstance().setupCloseAction();
    }

    private void setupControls() {
        scrollPane.vvalueProperty().addListener((observable, oldValue, newValue) -> {
            if(scrollEventEnabled) {
                int numPages = PDFModel.getInstance().getNumberOfPages();
                int currentPage = (int) (newValue.doubleValue() * numPages) + 1;
                if(currentPage <= numPages)
                    pageNumberTextField.setText(Integer.toString(currentPage));
            }
        });
        pageNumberTextField.setOnAction(event -> {
            scrollEventEnabled = false;

            String text = pageNumberTextField.getText();
            try {
                int page = Integer.parseInt(text);
                if(page < 1 || page > PDFModel.getInstance().getNumberOfPages()) {
                    pageNumberTextField.setText("1");
                    moveToPage(0);
                }
                else {
                    moveToPage(page);
                }
            } catch (NumberFormatException e) {
                pageNumberTextField.setText("1");
                moveToPage(0);
            }

            scrollEventEnabled = true;
        });
        pageNumberTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            buttonBack.setDisable(newValue.equals(String.valueOf(1)));
            buttonNext.setDisable(newValue.equals(String.valueOf(PDFModel.getInstance().getNumberOfPages())));
        });
        buttonNext.setOnAction(event -> {
            scrollEventEnabled = false;

            String text = pageNumberTextField.getText();
            try {
                int page = Integer.parseInt(text);
                if(page < PDFModel.getInstance().getNumberOfPages()) {
                    page++;
                    pageNumberTextField.setText(Integer.toString(page));
                    moveToPage(page);
                }
            } catch (NumberFormatException e) {
                pageNumberTextField.setText("1");
                moveToPage(0);
            }

            scrollEventEnabled = true;
        });
        buttonBack.setOnAction(event -> {
            scrollEventEnabled = false;

            String text = pageNumberTextField.getText();
            try {
                int page = Integer.parseInt(text);
                if(page > 1) {
                    page--;
                    pageNumberTextField.setText(Integer.toString(page));
                    moveToPage(page);
                }
            } catch (NumberFormatException e) {
                pageNumberTextField.setText("1");
                moveToPage(0);
            }

            scrollEventEnabled = true;
        });

        pageNumberTextField.setText("1");
    }



    private void moveToPage(int page) {
        int normalizedPage = page - 1;
        double pageHeight = ((Pane) vBox.getChildren().getFirst()).getHeight();
        double visibleZoneHeight = scrollPane.getViewportBounds().getHeight();
        double x = pageHeight/(vBox.getHeight()-visibleZoneHeight);
        scrollPane.setVvalue(normalizedPage*(x));
    }

    public void displayPDF() {
        int numPages = PDFModel.getInstance().getNumberOfPages();
        width = scrollPane.getPrefWidth()-SCROLLBAR_SIZE;
        for (int i = 0; i < numPages; i++) {
            try {
                setPage(i);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        setEndPage();
        Platform.runLater(() -> {
            PDFView.getInstance().show();
        });
    }
    private void setPage(int i) throws IOException {

        float ratio = PDFModel.getInstance().getAspectRatio();
        byte[] imageData = PDFModel.getInstance().getPageAsImage(i, 72);
        Image image = new Image(new ByteArrayInputStream(imageData));
        ImageView imageView = new ImageView(image);
        Pane pane = new Pane();


        pane.setMaxWidth(width);
        pane.setPrefWidth(width);
        pane.setMinHeight(width*ratio);
        pane.setMaxHeight(width*ratio);
        pane.setPrefHeight(width*ratio);
        imageView.setFitWidth(pane.getPrefWidth());
        imageView.setPreserveRatio(true);
        pane.getChildren().add(imageView);

        Rectangle overlay = new Rectangle(pane.getPrefWidth(), pane.getPrefHeight(), OVERLAY_COLOR);
        overlay.setOpacity(0.0);
        pane.getChildren().add(overlay);

        pane.setOnMouseEntered(event -> {
            overlay.setOpacity(0.3);
            pane.setCursor(Cursor.HAND);
        });
        pane.setOnMouseExited(event -> {
            overlay.setOpacity(0.0);
            pane.setCursor(Cursor.DEFAULT);
        });
        pane.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                handleLeftClick((Pane) event.getSource(),0);
            } else if (event.getButton() == MouseButton.SECONDARY) {
                handleRightClick((Pane) event.getSource());
            }
        });
        vBox.getChildren().add(pane);
    }
    public void setEndPage() {
        float ratio = PDFModel.getInstance().getAspectRatio();
        AnchorPane endPage;
        try {
            endPage = FXMLLoader.load(getClass().getResource("/fxml/endPage.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        globalPane = endPage;

        endPage.setMaxWidth(width);
        endPage.setPrefWidth(width);
        endPage.setMinHeight(width*ratio);
        endPage.setMaxHeight(width*ratio);
        endPage.setPrefHeight(width*ratio);

        Rectangle overlay = new Rectangle(endPage.getPrefWidth(), endPage.getPrefHeight(), OVERLAY_COLOR);
        overlay.setOpacity(0.0);
        endPage.getChildren().add(overlay);

        endPage.setOnMouseEntered(event -> {
            overlay.setOpacity(0.3);
            endPage.setCursor(Cursor.HAND);
        });
        endPage.setOnMouseExited(event -> {
            overlay.setOpacity(0.0);
            endPage.setCursor(Cursor.DEFAULT);
        });
        endPage.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                handleLeftClick((Pane) event.getSource(),1);
            }
        });
        vBox.getChildren().add(endPage);
    }
    public void updateTotalPageNumber() {
        int numPages = PDFModel.getInstance().getNumberOfPages();
        pageMaxNumberLabel.setText("/"+numPages);
    }



    private void setupPopUpButton() {
        popUpButton.setOnAction(event -> {
            errorPopUp.setVisible(false);
            errorLabel.setText("");
        });
    }
    private void setUnsaved() {
        saveMenuItem.setDisable(false);
        savedLabel.setText("Unsaved");
        savedLabel.getStyleClass().clear();
        savedLabel.getStyleClass().add("unsaved");
    }
    private void setSaved() {
        saveMenuItem.setDisable(true);
        savedLabel.setText("Saved");
        savedLabel.getStyleClass().clear();
        savedLabel.getStyleClass().add("saved");
    }
    public void handleLeftClick(Pane pane, int lastPage) {
        setUnsaved();
        Pane whitePane = new Pane();
        float ratio = PDFModel.getInstance().getAspectRatio();
        whitePane.setPrefWidth(scrollPane.getPrefWidth());
        whitePane.setMinHeight(scrollPane.getPrefWidth()*ratio);
        whitePane.setMaxHeight(scrollPane.getPrefWidth()*ratio);
        whitePane.setPrefHeight(scrollPane.getPrefWidth()*ratio);
        whitePane.setStyle("-fx-background-color: white;");

        Rectangle overlay = new Rectangle(whitePane.getPrefWidth(), whitePane.getPrefHeight(), OVERLAY_COLOR);
        overlay.setOpacity(0.0);
        whitePane.getChildren().add(overlay);

        whitePane.setOnMouseEntered(event -> {
            overlay.setOpacity(0.3);
            whitePane.setCursor(Cursor.HAND);
        });
        whitePane.setOnMouseExited(event -> {
            overlay.setOpacity(0.0);
            whitePane.setCursor(Cursor.DEFAULT);
        });
        whitePane.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                handleLeftClick((Pane) event.getSource(),0);
            } else if (event.getButton() == MouseButton.SECONDARY) {
                handleRightClick((Pane) event.getSource());
            }
        });

        int index = vBox.getChildren().indexOf(pane);
        vBox.getChildren().add(index, whitePane);
        if (lastPage == 0)
            PDFModel.getInstance().addBlankPage(index);
        else
            PDFModel.getInstance().addBlankPage(-1);
        updateTotalPageNumber();
    }
    private void handleRightClick(Pane pane) {
        setUnsaved();
        int index = vBox.getChildren().indexOf(pane);
        PDFModel.getInstance().removePage(index);
        vBox.getChildren().remove(pane);
        updateTotalPageNumber();
    }
    private void handleError(String errorMsg) {
        errorLabel.setText(errorMsg);
        errorPopUp.setVisible(true);
    }


    @FXML
    private MenuItem homeMenuItem;

    @FXML
    private MenuItem saveMenuItem;

    @FXML
    private MenuItem undoMenuItem;

    @FXML
    private MenuItem redoMenuItem;

    @FXML
    private void handleHome() {
        try {
            PDFModel.getInstance().closeDocument();
        } catch (IOException e) {
            handleError(e.getMessage());
            return;
        }
        PDFView.getInstance().changeScene("/fxml/fileSelector.fxml");
    }

    @FXML
    private void handleSave() {
        try {
            PDFModel.getInstance().saveDocument();
        } catch (IOException e) {
            handleError(e.getMessage());
            return;
        }
        setSaved();
    }

    @FXML
    private void handleUndo() {
        System.out.println("Undo");
    }

    @FXML
    private void handleRedo() {
        System.out.println("Redo");
    }

}
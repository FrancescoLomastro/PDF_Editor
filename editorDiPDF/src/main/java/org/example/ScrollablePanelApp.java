package org.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class ScrollablePanelApp extends Application {
    private static final int NUM_PANELS = 100; // Numero di pannelli
    private static final int PANEL_HEIGHT = 200; // Altezza dei pannelli

    private VBox panelContainer;
    private ScrollPane scrollPane;
    private Label pageLabel;
    private TextField pageNumberTextField;
    int is = 1;

    @Override
    public void start(Stage primaryStage) {
        // Creazione dei pannelli allineati verticalmente
        panelContainer = new VBox();
        for (int i = 0; i < NUM_PANELS; i++) {
            Pane panel = createPanel(i + 1);
            panelContainer.getChildren().add(panel);
        }

        // Creazione del componente ScrollPane
        scrollPane = new ScrollPane();
        scrollPane.setContent(panelContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(PANEL_HEIGHT * 3);


        Button nextPageButton = new Button("Next Page");
        Pane p = (Pane) panelContainer.getChildren().getFirst();


        nextPageButton.setOnAction(e -> {
            double x;
            System.out.println(scrollPane.getVmax()+"   "+scrollPane.getVmin());
            double pageHeight = ((Pane) panelContainer.getChildren().getFirst()).getHeight();
            double visibleZoneHeight = scrollPane.getViewportBounds().getHeight();
            x = pageHeight/(panelContainer.getHeight()-visibleZoneHeight);
            scrollPane.setVvalue(is*(x));
            is++;
        });
        Button backPageButton = new Button("Back Page");
        backPageButton.setOnAction(e -> scrollPane.setVvalue(scrollPane.getVvalue() - 0.1));

        // Creazione del componente TextField e gestione evento Invio
        pageNumberTextField = new TextField();
        pageNumberTextField.setOnAction(e -> {
            try {
                int panelNumber = Integer.parseInt(pageNumberTextField.getText());
                double scrollPosition = (double) (panelNumber - 1) / (NUM_PANELS - 1);
                scrollPane.setVvalue(Math.max(0, scrollPosition));
            } catch (NumberFormatException ex) {
                // Ignora se l'input non è un numero
            }
        });

        // Creazione della label per il numero di pannelli
        pageLabel = new Label("Number of Panels: " + NUM_PANELS);

        // Creazione della navigation bar
        HBox navigationBar = new HBox(10, backPageButton, nextPageButton, pageLabel, new Label("Go to Page:"), pageNumberTextField);
        navigationBar.setPadding(new Insets(10));

        // Creazione del layout principale
        VBox mainLayout = new VBox(navigationBar, scrollPane);
        Scene scene = new Scene(mainLayout, 400, 300);

        // Impostazione della finestra
        primaryStage.setScene(scene);
        primaryStage.setTitle("Scrollable Panel App");
        primaryStage.show();
    }

    // Metodo per creare un pannello
    private Pane createPanel(int panelNumber) {
        Pane panel = new Pane();
        panel.setPrefSize(200, PANEL_HEIGHT);
        panel.setStyle("-fx-background-color: lightblue;");
        Label label = new Label("Panel " + panelNumber);
        panel.getChildren().add(label);
        return panel;
    }

    public static void main(String[] args) {
        launch(args);
    }
}

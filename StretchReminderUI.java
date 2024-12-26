import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import java.awt.Toolkit;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

public class StretchReminderUI extends Application {

    private static final long REMINDER_INTERVAL = 20 * 60 * 1000; // 20 minutes in milliseconds
    private Label statusLabel;
    private Timer timer;
    private boolean count;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Stretch Reminder");

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        statusLabel = new Label("Reminder Running");
        Button startButton = new Button("Start Reminders");
        Button stopButton = new Button("Stop Reminders");

        startReminders();
        startButton.setOnAction(e -> startReminders());
        stopButton.setOnAction(e -> stopReminders());

        root.getChildren().addAll(statusLabel, startButton, stopButton);

        Scene scene = new Scene(root, 150, 150);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void startReminders() {
        count = true;
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> showReminder());
            }
        }, REMINDER_INTERVAL, REMINDER_INTERVAL);
        statusLabel.setText("Reminder On");
    }

    private void stopReminders() {
        count = false;

        if (timer != null) {
            timer.cancel();
            timer = null;
            statusLabel.setText("Reminders stopped.");
        }
    }

    private void showReminder() {

        if (!count) {
            return;
        }

        Image image = new Image(getClass().getResourceAsStream("neck.png"));

        Platform.runLater(() -> {
            Dialog<String> dialog = new Dialog<>();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initStyle(StageStyle.UNDECORATED);

            ImageView imageView = new ImageView(image);
            imageView.setFitHeight(182);
            imageView.setFitWidth(277);

            Label timerLabel = new Label("20");
            timerLabel.setStyle("-fx-font-size: 30px;");
            timerLabel.setTextFill(Color.RED);

            BorderPane contentPane = new BorderPane();
            contentPane.setCenter(imageView);
            contentPane.setBottom(timerLabel);
            BorderPane.setAlignment(timerLabel, Pos.CENTER);
            contentPane.setPadding(new Insets(0, 0, 0, 0)); // 10 pixels padding at the bottom

            dialog.getDialogPane().setContent(contentPane);

            // Remove all buttons
            dialog.getDialogPane().getButtonTypes().clear();

            // Play a beep sound
            Toolkit.getDefaultToolkit().beep();

            // Set up the timer
            Timeline timeline = new Timeline();
            count = false;
            timeline.setCycleCount(20);
            timeline.getKeyFrames().add(
                    new KeyFrame(Duration.seconds(1), event -> {
                        int remainingSeconds = Integer.parseInt(timerLabel.getText()) - 1;
                        timerLabel.setText(String.valueOf(remainingSeconds));
                        if (remainingSeconds == 0) {
                            dialog.setResult("Timer finished");

                            dialog.close();
                            count = true;
                        }
                    }));

            // Ensure the dialog is always on top and brought to the front
            dialog.setOnShowing(event -> {
                Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
                stage.setAlwaysOnTop(true);
                stage.toFront();
                timeline.play();
            });

            // Add event filters to close the dialog on key press or mouse click
            dialog.getDialogPane().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                timeline.stop();
                dialog.setResult("Dialog closed by key press");
                dialog.close();
                event.consume();
                count = true;
            });

            dialog.getDialogPane().addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                    timeline.stop();
                    dialog.setResult("Dialog closed by mouse click");
                    dialog.close();
                    event.consume();
                    count = true;
                }
            });

            // Show the dialog and handle the result
            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                String value = result.get();
                System.out.println("Dialog result: " + value);
            }
        });
    }

    @Override
    public void stop() {
        if (timer != null) {
            timer.cancel();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

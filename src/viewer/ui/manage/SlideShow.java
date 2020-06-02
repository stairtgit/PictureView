package viewer.ui.manage;//

import java.io.File;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 *      幻灯片播放类
 */

public class SlideShow {
//    public static ArrayList<ImageView> imageList = new ArrayList();
    private Timeline animation;
    private int currentImageViewIndex = 0;
    private BorderPane borderPane = new BorderPane();
    private Timeline imageLargeAnimation;
    private double size = 50;
    private ImageView imageView = new ImageView();
    private int imgsLength;
    private File file;
    private File[] files;
    private Image image;

    SlideShow(File[] files, int length) {
        this.files = files;
        this.file = this.files[this.currentImageViewIndex];
        this.image = new Image(this.file.toURI().toString());
        this.imgsLength = length;
        this.imageView.setImage(image);
        System.gc();
        this.imageView.setFitHeight(this.size);
        this.imageView.setFitWidth(this.size);
        this.borderPane.setCenter(this.imageView);
        this.animation = new Timeline(new KeyFrame[]{new KeyFrame(Duration.millis(3000.0D), (e) -> {
            this.imagePlay();
        }, new KeyValue[0])});
        this.animation.setCycleCount(-1);
        this.animation.play();
        this.imageLargeAnimation = new Timeline(new KeyFrame[]{new KeyFrame(Duration.millis(50.0D), (e) -> {
            this.imageLarge();
        }, new KeyValue[0])});
        this.imageLargeAnimation.setCycleCount(-1);
        this.imageLargeAnimation.play();
        Scene scene = new Scene(this.borderPane, 500, 500);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("幻灯片播放");
        stage.show();
    }

    private void imagePlay() {
        this.size = 50;
        if (this.currentImageViewIndex == this.imgsLength - 1) {
            this.animation.stop();
            this.imageLargeAnimation.stop();
        }

        if (this.currentImageViewIndex + 1 != this.imgsLength) {
//            System.out.println(this.currentImageViewIndex);
//            System.out.println(this.imgsLength);
            this.currentImageViewIndex++;
            this.file = this.files[this.currentImageViewIndex];
            this.image = new Image(this.file.toURI().toString());
            this.imageView.setImage(image);
            this.borderPane.setCenter(this.imageView);
//            this.borderPane.setCenter(imageList.get(++this.currentImageViewIndex));
        }

    }

    private void imageLarge() {
        try {
            this.imageView.setFitHeight(this.size);
            this.imageView.setFitWidth(this.size);
        } catch (IndexOutOfBoundsException var2) {
        }

        this.size += 4;
    }


}

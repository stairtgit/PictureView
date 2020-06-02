package viewer.ui.manage;

import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;

public class PreviewWindow extends Stage implements EventHandler {

    // 当前显示的缩略图
    private Thumbnail currentThumbnail;
    // 当前目录所有缩略图
    private ArrayList<Thumbnail> thumbnails;
    // 当前显示缩略图下标
    private int currentThumbnailIndex = 0;
    // 图片显示器
    private ImageView mainImageView;
    // 上一张
    private Button prevBtn;
    // 下一张
    private Button nextBtn;
    // 左转
    private Button rightRotateBtn;
    // 右转
    private Button leftRotateBtn;
    // 放大
    private Button amplifyBtn;
    // 缩小
    private Button shrinkBtn;
    // 当前角度 共360度
    private int currentAngle = 0;
    // 每次旋转角度
    private static int rotateAngle = 90;
    // 当前缩放值
    private double currentScale = 0.5;
    // 每次缩放的值
    private double scale = 0.1;

//    private double w;
//    private double h;

    public PreviewWindow(Thumbnail currentThumbnail, ObservableList<Node> thumbnails) {
        this.currentThumbnail = currentThumbnail;
        this.thumbnails = new ArrayList<Thumbnail>();
        for (Node node : thumbnails) {
            if (node instanceof Thumbnail) {
                this.thumbnails.add(((Thumbnail) node));
            }
        }
        this.currentThumbnailIndex = this.thumbnails.indexOf(this.currentThumbnail);
        this.initModality(Modality.APPLICATION_MODAL);
        this.mainImageView = new ImageView(this.currentThumbnail.getImageFile().toURI().toString());
        initUI();
    }

    /**
     * 初始化界面
     */
    private void initUI() {
        this.setTitle(this.currentThumbnail.getImageFile().getName());
        Image image = this.mainImageView.getImage();
        this.prevBtn = new Button("", new ImageView(new Image("/image/lastOneButton.gif")));
        this.prevBtn.setOnMouseClicked(this);
        this.nextBtn = new Button("", new ImageView(new Image("/image/nextOneButton.gif")));
        this.nextBtn.setOnMouseClicked(this);
        this.leftRotateBtn = new Button("", new ImageView(new Image("/image/antiClockwise.gif")));
        this.leftRotateBtn.setOnMouseClicked(this);
        this.rightRotateBtn = new Button("", new ImageView(new Image("/image/clockwise.gif")));
        this.rightRotateBtn.setOnMouseClicked(this);
        this.amplifyBtn = new Button("", new ImageView(new Image("/image/enlarge.gif")));
        this.amplifyBtn.setOnMouseClicked(this);
        this.shrinkBtn = new Button("", new ImageView(new Image("/image/narrow.gif")));
        this.shrinkBtn.setOnMouseClicked(this);

        FlowPane bottomPane = new FlowPane();
        bottomPane.getChildren().add(this.amplifyBtn);
        bottomPane.getChildren().add(this.shrinkBtn);
        bottomPane.getChildren().add(this.leftRotateBtn);
        bottomPane.getChildren().add(this.rightRotateBtn);

//        this.w = image.getWidth() * this.currentScale;
//        this.h = image.getHeight() * this.currentScale;
//        System.out.println(this.w);
//        System.out.println(this.h);
//        this.mainImageView.setX(this.w);
        this.mainImageView.setScaleX(this.currentScale);
        this.mainImageView.setScaleY(this.currentScale);

        AnchorPane anchorPane = new AnchorPane();
        anchorPane.getChildren().addAll(this.mainImageView, this.prevBtn, this.nextBtn, bottomPane);

        bottomPane.prefWidthProperty().bind(anchorPane.widthProperty());
        bottomPane.setStyle("-fx-alignment :center");
        this.prevBtn.prefHeightProperty().bind(anchorPane.heightProperty());
        this.nextBtn.prefHeightProperty().bind(anchorPane.heightProperty());
        this.prevBtn.setStyle("-fx-opacity:0.5");
        this.nextBtn.setStyle("-fx-opacity:0.5");
        this.mainImageView.fitHeightProperty().bind(anchorPane.heightProperty());
        this.mainImageView.fitWidthProperty().bind(anchorPane.widthProperty());
        this.mainImageView.setLayoutX(0.0);
        this.mainImageView.setLayoutY(0.0);
        AnchorPane.setLeftAnchor(this.prevBtn, 0.0);
        AnchorPane.setRightAnchor(this.nextBtn, 0.0);
        AnchorPane.setBottomAnchor(bottomPane, 0.0);

        Scene scene = new Scene(anchorPane, 800, 600);
        this.setScene(scene);
    }

    // 上一张
    private void prev() {
        this.currentThumbnailIndex -= 1;
        if (this.currentThumbnailIndex < 0) this.currentThumbnailIndex = this.thumbnails.size() - 1;
        this.currentThumbnail = this.thumbnails.get(this.currentThumbnailIndex);
        this.mainImageView.setImage(new Image(this.currentThumbnail.getImageFile().toURI().toString()));
        this.setTitle(this.currentThumbnail.getImageFile().getName());
    }

    // 下一张
    private void next() {
        this.currentThumbnailIndex += 1;
        if (this.currentThumbnailIndex == this.thumbnails.size()) this.currentThumbnailIndex = 0;
        this.currentThumbnail = this.thumbnails.get(this.currentThumbnailIndex);
        this.mainImageView.setImage(new Image(this.currentThumbnail.getImageFile().toURI().toString()));
        this.setTitle(this.currentThumbnail.getImageFile().getName());
    }

    // 左转 每一次90度
    private void leftRotate() {
        this.currentAngle = this.currentAngle - rotateAngle;
        if (this.currentAngle < 0) this.currentAngle = 360 - rotateAngle;
        this.mainImageView.setRotate(this.currentAngle);
    }

    // 右转 每一次90度
    private void rightRotate() {
        this.currentAngle = this.currentAngle + rotateAngle;
        if (this.currentAngle > 360) this.currentAngle = rotateAngle;
        this.mainImageView.setRotate(this.currentAngle);
    }

    // 放大
    private void amplify() {
        this.currentScale += this.scale;
        this.mainImageView.setScaleX(this.currentScale);
        this.mainImageView.setScaleY(this.currentScale);
    }

    // 缩小
    private void shrink() {
        this.currentScale -= this.scale;
        this.mainImageView.setScaleX(this.currentScale);
        this.mainImageView.setScaleY(this.currentScale);
    }

    @Override
    public void handle(Event event) {
        if (event.getSource() == this.prevBtn) {
            prev();
        } else if (event.getSource() == this.nextBtn) {
            next();
        } else if (event.getSource() == this.amplifyBtn) {
            amplify();
        } else if (event.getSource() == this.shrinkBtn) {
            shrink();
        } else if (event.getSource() == this.leftRotateBtn) {
            leftRotate();
        } else if (event.getSource() == this.rightRotateBtn) {
            rightRotate();
        }
    }

}

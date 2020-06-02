package viewer.ui.manage;

import java.io.File;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

/**
 * 缩略图类，继承BorderPane
 * 
 * <pre>
 * 文件名用Label显示，放置在BorderPane的BOTTOM；
 * 图片用Canvas绘制；CheckBox用于控制是否被选中；
 * Canvas和CheckBox放置在一个Pane上；
 * Pane放在BorderPane的CENTER。
 * </pre>
 *
 *
 */
public class Thumbnail extends BorderPane {
	// 缩略图尺寸
	public static final double THUMBNAIL_WIDTH = 160;
	public static final double THUMBNAIL_HEIGHT = 120;

	// 缩略图关联的文件
	private File imageFile;

	// 缩略图所在的父容器
	private ViewerPane viewerPane;

	// 缩略图的构成组件
	private CheckBox selectedBox; // 缩略图的选中状态
	private Label nameLabel; // 缩略图的文件名
	private Canvas canvas; // 绘制缩略图的画布
	private Pane imagePane; // 组合selecte dBox和canvas的pane

	// 构造方法
	public Thumbnail(File imageFile, ViewerPane viewerPane) {
		super();
		this.imageFile = imageFile;
		this.viewerPane = viewerPane;
		buildThumbnail();
	}

//	public boolean play()

	// 重命名
	public boolean rename(String newName) {
		String absName = imageFile.getParent() + "/" + newName;
		File dest = new File(absName);
		if (imageFile.renameTo(dest)) {
			imageFile = dest;
			this.nameLabel.setText(imageFile.getName());

			return true;
		}
		return false;
	}

	// 删除并判断是否成功——————增加余浩
	public boolean deleted(String Name) {
		String absName = imageFile.getParent() + "/" + Name;
		File dest = new File(absName);
		if (dest.delete()) {

			return true;
		}
		return false;
	}

	// 构造缩略图
	private void buildThumbnail() {
		// 1. 选择框
		selectedBox = new CheckBox(); // 创建选择框
		selectedBox.setVisible(false);// 初始为不可见
		selectedBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (newValue) {
					viewerPane.selectedThumbnailsProperty().add(Thumbnail.this);
				} else {
					viewerPane.selectedThumbnailsProperty().remove(Thumbnail.this);
				}
			}
		});

		// 2. 画布Canvas
		canvas = new Canvas(Thumbnail.THUMBNAIL_WIDTH + 10, THUMBNAIL_HEIGHT + 10); // 创建绘制图片的画布

		GraphicsContext gc = canvas.getGraphicsContext2D(); // 获得画笔对象

		Image image = new Image(imageFile.toURI().toString(), Thumbnail.THUMBNAIL_WIDTH - 2,
				Thumbnail.THUMBNAIL_HEIGHT - 2, true, true); // 读取图片文件，按照缩略图大小读取，并保持宽高比例

		// 计算绘制图片的坐标
		double x = (Thumbnail.THUMBNAIL_WIDTH - image.getWidth()) / 2;
		double y = (Thumbnail.THUMBNAIL_HEIGHT - image.getHeight()) / 2;
		// gc.setStroke(Color.BLACK);
		// gc.strokeRect(0, 0, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);
		gc.drawImage(image, x, y); // 绘制图片到Canvas
		gc.applyEffect(new DropShadow(5, 5, 5, Color.GRAY));//对绘制的图片施加阴影特效

		// 3. 文件名Label
		nameLabel = new Label(imageFile.getName()); // 使用文件名创建Label
		nameLabel.prefWidthProperty().bind(canvas.widthProperty());
		nameLabel.setAlignment(Pos.CENTER);
		nameLabel.setTooltip(new Tooltip(imageFile.getName()));

		// 4. 绘图的Pane
		imagePane = new Pane(); // 创建绘图Pane
		imagePane.setStyle("-fx-background-color: transparent; -fx-hover: #D8E9F9;");

		imagePane.getChildren().add(canvas); // 绘图Pane放置Canvas
		imagePane.getChildren().add(selectedBox); // 绘图Pane放置selectBox

		// 5. 组合缩略图的BorderPane
		this.setStyle("-fx-background-color: transparent; -fx-hover: #D8E9F9;");
		this.setMaxWidth(Thumbnail.THUMBNAIL_WIDTH + 5);
		this.setHover(true);
		this.setCenter(imagePane);
		this.setBottom(nameLabel);

		// 6. 鼠标事件
		this.setMouseTransparent(false);
		// 6.1 进入缩略图
		this.setOnMouseEntered(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				selectedBox.setVisible(true);
				imagePane.setStyle("-fx-background-color: #CCE8FF;");
				Thumbnail.this.setStyle("-fx-background-color: #CCE8FF;");
//				Thumbnail.this.setStyle("-fx-background-color: -fx-shadow-highlight-color, -fx-outer-border, -fx-inner-border, -fx-body-color;");
			}
		});

		// 6.2 离开缩略图
		this.setOnMouseExited(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				selectedBox.setVisible(selectedBox.isSelected());
				imagePane.setStyle("-fx-background-color: transparent");
				Thumbnail.this.setStyle("-fx-background-color: transparent");
			}
		});

		// 6.3 鼠标单击，选中当前缩略图，取消其他缩略图的选中状态
		this.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				if (event.getButton() == MouseButton.PRIMARY) { // 左键
					if(event.getClickCount() == 1){  // 单击
						ObservableSet<Thumbnail> thumbnails = viewerPane.selectedThumbnailsProperty().get();
						// System.out.println(thumbnails.contains(Thumbnail.this));
						if (thumbnails.contains(Thumbnail.this)) {
							thumbnails.retainAll(FXCollections.observableSet(Thumbnail.this));
						} else {
							thumbnails.clear();
							thumbnails.add(Thumbnail.this);
						}
						//林思贤修改
					} else if(event.getClickCount() == 2) { //双击
						new PreviewWindow(Thumbnail.this, Thumbnail.this.viewerPane.getThumbnailPane().getChildren()).show();
					} //林思贤修改
				} else{

				}
			}
		});

	} // end of buildThumbnail

	// setters && getters -------------------
	public ViewerPane getViewerPane() {
		return viewerPane;
	}

	public void setViewerPane(ViewerPane viewerPane) {
		this.viewerPane = viewerPane;
	}

	public File getImageFile() {
		return imageFile;
	}

	// 返回选中状态
	public boolean isSelected() {
		return selectedBox.isSelected();
	}

	public void setSelected(boolean selected) {
		selectedBox.setSelected(selected);
		selectedBox.setVisible(selected);
	}

}

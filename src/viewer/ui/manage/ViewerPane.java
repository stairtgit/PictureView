package viewer.ui.manage;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Callback;
import javafx.util.Pair;
import javafx.util.StringConverter;
import viewer.ui.control.TitledToolBar;
import viewer.ui.util.ViewerUtil;

/**
 * 显示缩略图及进行操作的Pane
 *
 *
 */
public class ViewerPane extends BorderPane {
	// 缩略图之间的间隔
	public static final double THUNMBNAIL_GAP = 15;
	// 默认按钮的大小
	public static final double BUTTON_SIZE = 40;
	// 表示当前操作状态的常量
	public static final int OP_BROWSE = 0;
	public static final int OP_COPY = 1;
	public static final int OP_CUT = 2;

	// 标题区域的数据域
	private TitledToolBar titleBox; // 标题区域
	private ProgressIndicator progressIndicator; // 加载图片的进度指示器

	// 状态栏区域的数据域
	private TitledToolBar statusBar; // 状态栏
	private Label numberOfThumbnailsLabel; // 显示当前目录中图片个数的标签
	private Label numberOfSelectedThumbnailsLabel; // 显示当前选中缩略图个数的Label
	private Button btnPlay; // 播放幻灯片按钮

	//copy文件list————————余浩增加
	private ArrayList<File> copyPictureFiles = new  ArrayList<>();
	//cut文件list————————余浩增加
	private ArrayList<File> cutPictureFiles = new  ArrayList<>();

	// 缩略图的Pane
	private FlowPane thumbnailPane;

	// 存放当前选中的目录的属性，使用属性绑定解决在目录树中选定一个目录后，通知相关功能进行工作
	// 更新pathLabel, buildThumbnailPane
	private SimpleObjectProperty<File> selectedFolderProperty = new SimpleObjectProperty<File>();

	// 存放当前被选中的缩略图的属性
	private SimpleSetProperty<Thumbnail> selectedThumbnailsProperty = new SimpleSetProperty<>(
			FXCollections.observableSet(new HashSet<Thumbnail>()));

	// 表示当前操作状态的数据域
	private int operateStatus = OP_BROWSE;

	// 加载图片的任务
	private LoadThumbnailTask task;
	private File[] imgFiles;

	// Constructor-------------------------------------------------------------------------------------
	public ViewerPane() {
		buildTitleBox(); // 构建标题区域
		ScrollPane scrollPane = buildThumbnailPane(); // 构建缩略图区域
		buildStatusBar(); // 构建状态栏

		// 组合3个部分
		this.setTop(titleBox);
		this.setCenter(scrollPane);
		this.setBottom(statusBar);
	}
	// 方法---------------------------------------------------------------------------------------------

	// 构造标题区域
	private void buildTitleBox() {
		titleBox = new TitledToolBar(); // 创建标题区域的ToolBar

		// titleBox的标题显示当前目录, bind到selectedFolderProperty
		// 使用 titleBox.textProperty().bind(selectedFolderProperty.asString());
		// 直接绑定, null值处理不好
		// 自定义一个绑定
		titleBox.titleTextProperty().bindBidirectional(selectedFolderProperty, new StringConverter<File>() {

			@Override
			public String toString(File object) {
				return object == null ? "没有选择目录" : object.toString();
			}

			@Override
			public File fromString(String string) {
				// 该部分不需要
				return null;
			}
		});

		// 删除按钮
		Button btnDelete = new Button("删除");
		btnDelete.setPrefHeight(BUTTON_SIZE);
		btnDelete.getStyleClass().add("left-pill");
		btnDelete.setOnAction(event -> DeleteAction() );

		// 重命名按钮
		Button btnRename = new Button("重命名");
		btnRename.setPrefHeight(BUTTON_SIZE);
		btnRename.getStyleClass().add("right-pill");
		btnRename.setOnAction(event -> renameProcess());

		// 组合 删除按钮 和 重命名按钮 到一个间距为0的HBox
		HBox buttonBox1 = new HBox(0, btnDelete, btnRename);
		buttonBox1.disableProperty().bind(selectedThumbnailsProperty.emptyProperty());

		// 复制按钮———————修改余浩
		Button btnCopy = new Button("复制");
		btnCopy.setPrefHeight(BUTTON_SIZE);
		btnCopy.getStyleClass().add("left-pill");
		btnCopy.disableProperty().bind(selectedThumbnailsProperty.emptyProperty());
		btnCopy.setOnAction( event -> CopyAction()  );

		// 剪切按钮
		Button btnCut = new Button("剪切");
		btnCut.setPrefHeight(BUTTON_SIZE);
		btnCut.getStyleClass().add("center-pill");
		btnCut.disableProperty().bind(selectedThumbnailsProperty.emptyProperty());
		btnCut.setOnAction(event -> CutAction());

		// 粘贴按钮
		Button btnPaste = new Button("粘贴");
		btnPaste.setPrefHeight(BUTTON_SIZE);
		btnPaste.getStyleClass().add("right-pill");
		btnCut.disableProperty().bind(selectedThumbnailsProperty.emptyProperty());
		btnPaste.setOnAction(event -> PasteAction());

		// 组合 复制按钮 , 剪切按钮 和 粘贴按钮 到一个间距为0的HBox
		HBox buttonBox2 = new HBox(0, btnCopy, btnCut, btnPaste);

		// 播放幻灯片按钮
		btnPlay = new Button("", new ImageView(new Image("/image/mffilm.png")));
		btnPlay.setPrefHeight(BUTTON_SIZE);
//		btnPlay.setEffect(new DropShadow());
		btnPlay.setTooltip(new Tooltip("播放幻灯片"));
		btnPlay.setDisable(true);
		btnPlay.setOnAction(event -> playProcess());


		// 加载进度显示
		progressIndicator = new ProgressIndicator();
		progressIndicator.setPrefSize(BUTTON_SIZE, BUTTON_SIZE);
		progressIndicator.setProgress(0);
		progressIndicator.setTooltip(new Tooltip("点击停止缩略图的加载"));
		// 点击进度条，取消加载缩略图
		progressIndicator.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				if (task != null && task.isRunning()) {
					task.cancel();
				}
			}

		});

		// 把标题 区域的组件放置到titleBox中
		titleBox.addLeftItems(buttonBox1, buttonBox2, btnPlay); // 按钮组靠左放
		titleBox.addRightItems(new Separator(Orientation.VERTICAL), progressIndicator); // 进度指示器靠右放

	} // end of buildTitleBox

	// 构造缩略图部分
	public ScrollPane buildThumbnailPane() {
		thumbnailPane = new FlowPane(ViewerPane.THUNMBNAIL_GAP, ViewerPane.THUNMBNAIL_GAP); // 创建pane
		thumbnailPane.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, new Insets(0))));
		thumbnailPane.setPadding(new Insets(ViewerPane.THUNMBNAIL_GAP)); // 设置padding
//        thumbnailPane.setMargin(thumbnailPane,new Insets(10));

		// 当前选中目录发生变化的事件处理
		selectedFolderProperty.addListener(new ChangeListener<File>() {

			@Override
			public void changed(ObservableValue<? extends File> observable, File oldValue, File newValue) {
				// 在目录树选中节点变化中，每次节点变化，先检查有无正在运行的线程(任务)
				// 如果有，就取消它。
				if (task != null && task.isRunning()) {
					task.cancel();
				}

				// 如果当前处理浏览状态，则切换目录时清空选中缩略图
				if (operateStatus == OP_BROWSE) {
					selectedThumbnailsProperty.get().clear();
				}
				btnPlay.setDisable(true); // 播放幻灯片按钮不可用

				File folder = observable.getValue(); // 获得当前选中目录

				// 创建文件过滤器，只保留4种图片文件
				FileFilter filter = new FileFilter() {
					@Override
					public boolean accept(File file) {
						boolean result = false;
						if (file.isFile()) {
							String filename = file.getName().toLowerCase();
							if (filename.endsWith(".bmp") || filename.endsWith(".jpg") || filename.endsWith(".jpeg")
									|| filename.endsWith(".gif") || filename.endsWith(".png")) {
								result = true;
							}
						}
						return result;
					}
				};

				File[] imageFiles = folder.listFiles(filter); // 获得当前目录中符合filter要求的文件数组
				imgFiles = imageFiles;
				thumbnailPane.getChildren().clear(); // 清空原来的缩略图，为加载图片准备
				numberOfThumbnailsLabel.setText("共 0 张图片"); // 数量设置为0

				task = new LoadThumbnailTask(imageFiles); // 以当前选中目录创建任务
				progressIndicator.progressProperty().bind(task.progressProperty()); // 进度条绑定到任务进度
				if (imageFiles != null && imageFiles.length > 0) {
					btnPlay.setDisable(false); // 播放幻灯片按钮可用

					Thread thread = new Thread(task); // 新建一个执行任务task的线程
					thread.setDaemon(true);
					thread.start(); // 启动线程
				}
			}

		}); // end of selectedFolderProperty addListener

		// 选中的缩略图发生变化时的事件处理
		selectedThumbnailsProperty.addListener(new SetChangeListener<Thumbnail>() {

			@Override
			public void onChanged(Change<? extends Thumbnail> change) {
				// System.out.println(change);
				// System.out.println("------------------");
				if (change.wasAdded()) {
					change.getElementAdded().setSelected(true);
				}
				if (change.wasRemoved()) {
					change.getElementRemoved().setSelected(false);
				}
			}

		}); // end of selectedThumbnailsProperty addListener

		// 缩略图Pane的鼠标点击事件，如果直接点击缩略图Pane，则清除所有选中状态
		thumbnailPane.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				// 如果直接点击在thumbnailPane上面
				if (event.getPickResult().getIntersectedNode() == thumbnailPane) {
					selectedThumbnailsProperty.get().clear();
				}
			}

		});
		
		thumbnailPane.setOnKeyReleased(new EventHandler<KeyEvent>() {

			@Override
			public void handle(KeyEvent event) {
				System.out.println(event.getCode());
				System.out.println(event.isControlDown());
			}
		});

		// 把缩略图的Pane放入一个滚动Pane中
		ScrollPane scrollPane = new ScrollPane();
		// 取消scorllPane默认边框
		scrollPane.setStyle(
				"-fx-background-color: white; -fx-background-insets: 0; -fx-control-inner-background: transparent;"); // 取消默认的边框颜色

		scrollPane.setFitToHeight(true);
		scrollPane.setFitToWidth(true);
		scrollPane.setContent(thumbnailPane);

		return scrollPane;
	} // end of buildThumbnailPane

	// 状态栏区域
	private void buildStatusBar() {
		statusBar = new TitledToolBar();

		// 加载缩略图个数的Label
		numberOfThumbnailsLabel = new Label("共 0 张图片");
		numberOfThumbnailsLabel.setPrefHeight(20);
		numberOfThumbnailsLabel.setFont(new Font("Microsoft YaHei", 12.0));

		// 选中缩略图个数的Label
		numberOfSelectedThumbnailsLabel = new Label("选中 0 张图片");
		numberOfSelectedThumbnailsLabel.setPrefHeight(20);
		numberOfSelectedThumbnailsLabel.setFont(new Font("Microsoft YaHei", 12.0));
		numberOfSelectedThumbnailsLabel.textProperty().bindBidirectional(selectedThumbnailsProperty,
				new StringConverter<ObservableSet<Thumbnail>>() {

					@Override
					public String toString(ObservableSet<Thumbnail> object) {
						return String.format(" 选中 %d 张图片", object.size());
					}

					@Override
					public ObservableSet<Thumbnail> fromString(String string) {
						return null;
					}

				});

		statusBar.getChildren().addAll(numberOfThumbnailsLabel, numberOfSelectedThumbnailsLabel);

	} // end of buildStatusBar

	//处理复制的方法————————————余浩增加
	private boolean CopyAction() {
		if(selectedThumbnailsProperty.getSize() <= 0) {
			return false;
		}
		if (selectedThumbnailsProperty.getSize() > 0) {
			Iterator<Thumbnail> it = selectedThumbnailsProperty.get().iterator();
			copyPictureFiles.clear();
			while (it.hasNext()) {
				Thumbnail thumbnail = it.next();
				String Name = thumbnail.getImageFile().getName();
				String absName = thumbnail.getImageFile().getParent() + "/" + Name;
				File dest = new File(absName);
				copyPictureFiles.add(dest);
			}
		}
		Clipboard clipboard = Clipboard.getSystemClipboard();
		ClipboardContent clipboardContent = new ClipboardContent();
		clipboard.clear();
		clipboardContent.putFiles(copyPictureFiles);
		clipboard.setContent(clipboardContent);
		clipboard = null;
		clipboardContent = null;
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("提示信息");
		alert.setHeaderText(null);
		alert.setContentText("复制成功！");

		alert.showAndWait();
		return true;
	}
	//处理剪切的方法————————————余浩增加
	private boolean CutAction() {
		if(selectedThumbnailsProperty.getSize()<=0) {
			return false;
		}
		if(selectedThumbnailsProperty.getSize() > 0) {
			Iterator<Thumbnail> it = selectedThumbnailsProperty.get().iterator();
			cutPictureFiles.clear();
			while (it.hasNext()) {
				Thumbnail thumbnail = it.next();
				String Name = thumbnail.getImageFile().getName();
				String absName = thumbnail.getImageFile().getParent() + "/" + Name;
				File dest = new File(absName);
				cutPictureFiles.add(dest);
			}
		}
		Clipboard clipboard = Clipboard.getSystemClipboard();
		ClipboardContent clipboardContent = new ClipboardContent();
		clipboard.clear();
		clipboardContent.putFiles(cutPictureFiles);
		clipboard.setContent(clipboardContent);
		clipboard = null;
		clipboardContent = null;
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("提示信息");
		alert.setHeaderText(null);
		alert.setContentText("剪切成功！");

		alert.showAndWait();
		return true;
	}

	//处理粘贴的方法————————————余浩增加
	private void PasteAction(){
		Clipboard clipboard = Clipboard.getSystemClipboard();
		List<File> files =  (List<File>) clipboard.getContent(DataFormat.FILES);
		if (files.size() <= 0) {
			return;
		}
		if(cutPictureFiles.size()>0) {
			File first = files.get(0);
			if(first.getParentFile().getAbsolutePath().compareTo(selectedFolderProperty.get().getAbsolutePath())==0){
				cutPictureFiles.clear();
				clipboard.clear();
			}
		}
		for(File oldFile : files) {
			String newName = Pasterename(selectedFolderProperty.get().getAbsolutePath(),oldFile.getName());
			File newFile = new File(selectedFolderProperty.get().getAbsolutePath()+File.separator+newName);
			try {
				newFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(newFile.exists()) {
				try {
					copyFile(oldFile,newFile);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		/*	try {
				electedFolderProperty.addListener(new ChangeListener<File>() );
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
			if(cutPictureFiles.size()>0) {
				oldFile.delete();
			}

		}
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("提示信息");
		alert.setHeaderText(null);
		alert.setContentText("粘贴完成！");

		alert.showAndWait();
	}


	//粘贴复制————————余浩
	private void copyFile(File fromFile, File toFile) throws IOException {
		FileInputStream inputStream = new FileInputStream(fromFile);
		FileOutputStream outputStream = new FileOutputStream(toFile);
		byte[] b = new byte[1024];
		int byteRead;
		while ((byteRead = inputStream.read(b)) > 0) {
			outputStream.write(b, 0, byteRead);
		}
		inputStream.close();
		outputStream.close();

	}

	//粘贴命名————————余浩
	private String Pasterename(String theFilePath, String name) {
		String newName = name;
		File fatherPathFile = new File(theFilePath);
		File[] filesInFatherPath = fatherPathFile.listFiles();
		for (File fileInFatherPath : filesInFatherPath) {
			String fileName = fileInFatherPath.getName();
			int cmp = newName.compareTo(fileName);
			if (cmp == 0) {
				String str = null;
				int end = newName.lastIndexOf("."), start = newName.lastIndexOf("_副本");
				if (start != -1) {
					str = newName.substring(start, end);
					int num = 1;
					try {
						num = Integer.parseInt(str.substring(str.lastIndexOf("_副本") + 3)) + 1;
						int cnt = 0, d = num - 1;
						while (d != 0) {
							d /= 10;
							cnt++;
						}
						newName = newName.substring(0, end - cnt) + num + newName.substring(end);
					} catch (Exception e) {
						newName = newName.substring(0, end) + "_副本1" + newName.substring(end);
					}

				} else {
					newName = newName.substring(0, end) + "_副本1" + newName.substring(end);
				}
			}
		}
		return newName;
	}


	//处理删除的方法————————————余浩增加
	private void DeleteAction() {

		if(selectedThumbnailsProperty.getSize()<=0) {
			return;
		}
		else if(selectedThumbnailsProperty.getSize()==1) {
			Iterator<Thumbnail> it = selectedThumbnailsProperty.get().iterator();
			Thumbnail thumbnail = it.next();
			String Name = thumbnail.getImageFile().getName();
			if (!thumbnail.deleted(Name)) {
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("提示信息");
				alert.setHeaderText(null);
				alert.setContentText("删除失败！");

				alert.showAndWait();
			}else {
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("提示信息");
				alert.setHeaderText(null);
				alert.setContentText("删除成功！");

				alert.showAndWait();
			}

		}
		else {
			Iterator<Thumbnail> it = selectedThumbnailsProperty.get().iterator();
			int successful = 0;
			int failure = 0;
			while (it.hasNext()) {
				Thumbnail thumbnail = it.next();
				String Name = thumbnail.getImageFile().getName();

				if (thumbnail.deleted(Name)) {
					successful++;
				} else {
					failure++;
				}
			}

			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("提示信息");
			alert.setHeaderText(null);
			alert.setContentText(String.format("批量删除完成, %d个成功, %d个失败！", successful, failure));

			alert.showAndWait();
		}

	}

	// 处理重命名的方法
	private void renameProcess() {
		if (selectedThumbnailsProperty.getSize() == 1) {
			Iterator<Thumbnail> it = selectedThumbnailsProperty.get().iterator();
			Thumbnail thumbnail = it.next();

			String mainName = ViewerUtil.getMainFileName(thumbnail.getImageFile().getName());
			String extName = ViewerUtil.getExtFileName(thumbnail.getImageFile().getName());

			TextInputDialog dialog = new TextInputDialog(mainName);
			dialog.setHeaderText(null);
			dialog.setTitle("重命名");
			dialog.setContentText("新文件名(不需要扩展名):");

			dialog.showAndWait().ifPresent(response -> {
				if (!thumbnail.rename(response + extName)) {
					Alert alert = new Alert(AlertType.INFORMATION);
					alert.setTitle("提示信息");
					alert.setHeaderText(null);
					alert.setContentText("图片文件重命名失败，请检查是否有同名文件！");

					alert.showAndWait();
				}
			});
		} else {
			Dialog<Pair<String, Pair<Integer, Integer>>> dialog = new Dialog<>();
			dialog.setTitle("批量重命名");
			dialog.setHeaderText(null);

			GridPane grid = new GridPane();
			grid.setPadding(new Insets(20, 150, 10, 10));

			TextField textFieldFilename = new TextField("NewImage");
			Spinner<Integer> spinnerStartNumber = new Spinner<>(1, 100, 1);
			Spinner<Integer> spinnerNumberWidth = new Spinner<>(1, 8, 3);

			Platform.runLater(() -> textFieldFilename.requestFocus());

			grid.add(new Label("统一文件名:"), 0, 0);
			grid.add(textFieldFilename, 1, 0);
			grid.add(new Label("起始序号:"), 0, 1);
			grid.add(spinnerStartNumber, 1, 1);
			grid.add(new Label("序号位数:"), 0, 2);
			grid.add(spinnerNumberWidth, 1, 2);

			dialog.getDialogPane().setContent(grid);
			dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
			textFieldFilename.textProperty().addListener((observable, oldValue, newValue) -> {
				dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(newValue.trim().isEmpty());
			});

			dialog.setResultConverter(new Callback<ButtonType, Pair<String, Pair<Integer, Integer>>>() {
				@Override
				public Pair<String, Pair<Integer, Integer>> call(ButtonType buttonType) {
					if (buttonType == ButtonType.OK) {
						Integer start = spinnerStartNumber.getValue();
						Integer width = spinnerNumberWidth.getValue();
						String newName = textFieldFilename.getText();
						return new Pair<String, Pair<Integer, Integer>>(newName,
								new Pair<Integer, Integer>(start, width));
					}
					return null;
				}
			});

			dialog.showAndWait().ifPresent(renameValue -> {
				String newName = renameValue.getKey();
				Integer start = renameValue.getValue().getKey();
				Integer width = renameValue.getValue().getValue();

				Iterator<Thumbnail> it = selectedThumbnailsProperty.get().iterator();
				int successful = 0;
				int failure = 0;
				while (it.hasNext()) {
					Thumbnail thumbnail = it.next();
					String extName = ViewerUtil.getExtFileName(thumbnail.getImageFile().getName());

					String newFilename = String.format(newName + "%0" + width + "d" + extName, start);
					System.out.println(newFilename);
					start++;
					if (thumbnail.rename(newFilename)) {
						successful++;
					} else {
						failure++;
					}
				}

				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("提示信息");
				alert.setHeaderText(null);
				alert.setContentText(String.format("批量图片文件完成, %d个成功, %d个失败！", successful, failure));

				alert.showAndWait();

			});

		}
	}

	//处理幻灯片播放方法
	private void playProcess(){
		new SlideShow(imgFiles,imgFiles.length);
//		SlideShow slideShow = new SlideShow(imgFiles);
	}

	// 属性的封装
	public File getSelectedFolder() {
		return selectedFolderProperty.get();
	}

	public void setSelectedFolder(File selectedFolder) {
		this.selectedFolderProperty.set(selectedFolder);
	}

	public SimpleObjectProperty<File> selectedFolderProperty() {
		return selectedFolderProperty;
	}

	public SimpleSetProperty<Thumbnail> selectedThumbnailsProperty() {
		return selectedThumbnailsProperty;
	}

	public ArrayList<File> getCutPictureFiles() {
		return cutPictureFiles;
	}

	public void setCutPictureFiles(ArrayList<File> cutPictureFiles) {
		this.cutPictureFiles = cutPictureFiles;
	}

	public ArrayList<File> getCopyPictureFiles() {
		return copyPictureFiles;
	}

	public void setCopyPictureFiles(ArrayList<File> copyPictureFiles) {
		this.copyPictureFiles = copyPictureFiles;
	}


	/**
	 * 内部类，加载缩略图的任务
	 */

	private class LoadThumbnailTask extends Task<Integer> {
		private File[] imageFiles; // 目录中的图片文件数组

		public LoadThumbnailTask(File[] imageFiles) {
			this.imageFiles = imageFiles;
			this.updateProgress(0, 1);
		}


		@Override
		protected Integer call() throws Exception {
			int i = 1, max = imageFiles.length;

			for (File file : imageFiles) {
				// 检测到线程(任务)被中断，退出循环
				if (this.isCancelled()) {
					break;
				}
				Platform.runLater(() -> {
					/*
					 * 由于这里的代码是由JavaFX Application线程在特定时间执行的，因此有可能发生task被Cancel之后，
					 * 这个线程还执行几个，这样就会在新目录的缩略图中绘制上个目录的几张图片。 因此，在检测到task被取消后就直接返回。
					 */
					if (this.isCancelled()) {
						return;
					}
					Thumbnail thumbnail = new Thumbnail(file, ViewerPane.this); // 创建一个缩略图

					thumbnailPane.getChildren().add(thumbnail);
					numberOfThumbnailsLabel.setText(String.format("共 %d 张图片", thumbnailPane.getChildren().size()));
				});
				this.updateProgress(i, max); // 更新进度
				i++;
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// 为了避免线程(任务)在sleep过程中被中断，使用异常处理做二次检测
					if (this.isCancelled()) {
						break;
					}
				}
			}
			return thumbnailPane.getChildren().size();
		}
	}

	//林思贤修改
	public FlowPane getThumbnailPane() {
		return thumbnailPane;
	}
	public void setThumbnailPane(FlowPane thumbnailPane) {
		this.thumbnailPane = thumbnailPane;
	}//林思贤修改
	public File[] getImgFiles() {
		return imgFiles;
	}

}

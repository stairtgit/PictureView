package viewer.main;

import viewer.ui.manage.TreeBody;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.stage.Stage;
import viewer.ui.manage.ViewerPane;


public class Main extends Application {
	//public static TreeBody  folderPane =new TreeBody();
	public static TreeBody  folderPane ;
	@Override
	public void start(Stage primaryStage) throws Exception {
		SplitPane sp = new SplitPane();  // 分隔Pane


		ViewerPane viewerPane = new ViewerPane();
		folderPane = new TreeBody(viewerPane);


		sp.getItems().addAll(folderPane, viewerPane);

		sp.setDividerPositions(0.25);
		SplitPane.setResizableWithParent(folderPane, false);

		Scene scene = new Scene(sp, 1024, 600);

		primaryStage.setScene(scene);
		primaryStage.setTitle("Picture Viewer");
		primaryStage.setMinWidth(800);
		primaryStage.setMinHeight(600);
		primaryStage.show();

	}

	public static void main(String[] args) {
		Application.launch(args);
	}

}

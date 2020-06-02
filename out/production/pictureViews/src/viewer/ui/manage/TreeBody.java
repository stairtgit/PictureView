package viewer.ui.manage;



import java.io.File;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;

public class  TreeBody extends TreeView {
	  private ImageView imageViewDisk = new ImageView("image/disk.gif");
	// 关联的ViewerPane	
	  private ViewerPane viewerPane;  
	  public TreeBody(ViewerPane viewerPane) {	  
		  this.viewerPane=viewerPane;	  
    	   TreeItem<String> root=new TreeItem<String>("我的电脑");
    	   this.imageViewDisk.setFitHeight(20.0D);
    	   this.imageViewDisk.setFitWidth(20.0D);
    	   root.setGraphic(imageViewDisk);
   	       this.setRoot(root);
   	       
   	   this.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeUnit>() {

			@Override
			public void changed(ObservableValue<? extends TreeUnit> observable, TreeUnit oldValue,
					TreeUnit newValue) {
		      viewerPane.setSelectedFolder(newValue.getFile());
			}

		});
   	       
    	  File[] files=File.listRoots();  
// 	   File  file =new File("D:\\新建文件夹 (3)");
// 	   File[] files=file.listFiles();
     	   if (files != null) {//访问了无访问权限得文件会导致程序终止
    		   for(int i=0;i<files.length;i++) {   		   
    		   File f=files[i];
    		   if(f.isDirectory()) {
    			   TreeUnit treeUnit =new TreeUnit(f,1);
    			   treeUnit.upgrade();
    			     root.getChildren().add(treeUnit);	    
    		   }else {
    			   continue;
    		   }   		  	     
    	    }
    	   }
    	   
       }
}


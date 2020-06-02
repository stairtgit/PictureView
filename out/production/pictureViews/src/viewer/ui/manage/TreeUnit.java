package viewer.ui.manage;




import java.io.File;
import java.util.ArrayList;

import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;

public class TreeUnit extends TreeItem {	
		  private ImageView imageViewDir = new ImageView("image/dir.jpg");
	  
		  private Label label;
		  private  Button button;
		  private  File  file;
		  private  boolean havachildren=false;
	
	public TreeUnit() {				
	}
	public TreeUnit(File file,int type) {		
		
		super(file.getPath().charAt(0));
		this.imageViewDir.setFitHeight(20.0D);
		this.imageViewDir.setFitWidth(20.0D);
		
		this.setGraphic(imageViewDir);
		this.file=file;
	
	}
	public TreeUnit(File file) {
		 super(file.getName());
		 this.imageViewDir.setFitHeight(20.0D);
			this.imageViewDir.setFitWidth(20.0D);		
			this.setGraphic(imageViewDir);
		 this.file=file;
	}
	public void upgrade() {
	
		if(!havachildren) {
			havachildren=true;
			//System.out.println("更新层"+this);
	
		 ArrayList<TreeUnit> arrayList = new ArrayList();
			File[] files=file.listFiles();
			if (files != null) {
				int  filelen =files.length;
			    for(int i=0;i<filelen;i++) {
			    
				  File f=files[i];
				  if(f.isDirectory()&&!f.isHidden()) {
					  TreeUnit unit=new TreeUnit(f);
				//	  System.out.println("增加单元"+unit);
					  
					
					
					  this.addEventHandler(TreeUnit.branchExpandedEvent(),new EventHandler<TreeItem.TreeModificationEvent>() {

						@Override
						public void handle(TreeModificationEvent event) {			
								unit.upgrade();					
						}
					});
					  
					    this.getChildren().add(unit);
				  }		
				
			    }

			}
		}
	
	}
	public File getFile() {
		return file;
	}
	

	
}

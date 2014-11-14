package models;

import java.io.File;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import play.Play;
import play.db.ebean.Model;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import scala.annotation.meta.param;
import static fahmi.lib.Constants.IM_DEFAULT_PROFILE;
import static fahmi.lib.Constants.IM_MODIFED;
import static fahmi.lib.Constants.PATH_IMAGE;
import static fahmi.lib.Constants.UPLOADS_FOLDER;
@Entity
public class ImagePath extends Model {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long id;
	@Column
	public String path;
	@Column
	public String fileName;	
	@Column
	public String keterangan = IM_DEFAULT_PROFILE;
	
	public static Finder<Long, ImagePath> finder = new Finder<>(Long.class, ImagePath.class);
	public static String PATH = PATH_IMAGE;
	public String getUrlImange(){
		ServerAddress serverAddress = ServerAddress.finder.byId(new Long(1));
		if(serverAddress == null){
			return "server address not implement yet";
		}
		else {
			return "http://"+serverAddress.address+path;
		}
	}
	
	public void updateImage(ImagePath imagePath){
		if(keterangan.equalsIgnoreCase(IM_MODIFED)){
			File file = new File(Play.application().configuration().getString(PATH_IMAGE), fileName);
	    	file.delete();	
		}
    	this.path = imagePath.path;
    	this.fileName = imagePath.fileName;
    	this.keterangan = IM_MODIFED;
	}
	
	public static ImagePath setImageFromRequest(String key){
		MultipartFormData body = Controller.request().body().asMultipartFormData();
		FilePart picture = body.getFile(key);
		if (picture != null) {
			String fileName = picture.getFilename();
			fileName = UUID.randomUUID().toString().replace("-", "_");
			String contentType = picture.getContentType();
			File file = picture.getFile();
			file.renameTo(new File(Play.application().configuration()
					.getString(PATH_IMAGE), fileName));
			ServerAddress address = ServerAddress.finder.byId(new Long(1));
			ImagePath imagePath = new ImagePath();
			imagePath.fileName = fileName;
			imagePath.path = UPLOADS_FOLDER + fileName;
			return imagePath;
		} else {
			Controller.flash("error", "Missing file");
			return null;
		}
	}
	
	public static void deleteImage(ImagePath imagePath){
		File file = new File(Play.application().configuration().getString(PATH_IMAGE), imagePath.fileName);
    	file.delete();
    	imagePath.delete();
	}
	
	
	public static String deleteImageByid(Long id){
		ImagePath imagePath = finder.byId(id);
		if(imagePath == null){
			return "Image not found";
		}
		File file = new File(Play.application().configuration().getString(PATH_IMAGE), imagePath.fileName);
    	file.delete();
    	imagePath.delete();
    	return "success delete image";
	}
	
}

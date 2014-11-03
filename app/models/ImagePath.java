package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import play.db.ebean.Model;
import scala.annotation.meta.param;
import static fahmi.lib.Constants.IM_DEFAULT_PROFILE;
@Entity
public class ImagePath extends Model {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long id;
	@Column
	public String path;
	@Column
	public String keterangan = IM_DEFAULT_PROFILE;
	
	public static Finder<Long, ImagePath> finder = new Finder<>(Long.class, ImagePath.class);
	
	public String getUrlImange(){
		ServerAddress serverAddress = ServerAddress.finder.byId(new Long(1));
		if(serverAddress == null){
			return "server address not implement yet";
		}
		else {
			return "http://"+serverAddress.address+path;
		}
	}
	
}

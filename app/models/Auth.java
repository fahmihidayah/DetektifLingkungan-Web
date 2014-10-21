package models;

import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import play.db.ebean.Model;

@Entity
public class Auth extends Model {
	@Id
	@Column
	public Long idAuth;
	@Column
	public String authToken;

	public static Finder<Long,Auth> finder = new Finder<Long,Auth>(Long.class,
			Auth.class);

	public static User findUser(String userName, String password) {
		return User.finder.where().eq("userName", userName)
				.eq("password", password).findUnique();
	}
	
	public static Auth findAuth(String auth){
		return finder.where().eq("authToken", auth).findUnique();
	}
	
	public void createToken() {
		authToken = UUID.randomUUID().toString();
	}
}

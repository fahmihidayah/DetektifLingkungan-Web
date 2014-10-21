package models;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.UUID;

import javax.management.RuntimeErrorException;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.validation.Constraint;

import com.fasterxml.jackson.annotation.JsonIgnore;

import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

@Entity
public class User extends Model {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long id;

	@Column(unique = true, length = 256, nullable = false)
	public String userName;

	@JsonIgnore
	@Column(length = 64, nullable = false)
	public byte[] shaPassword;
// uncomment the transient to delete password column and more secure 
//	@Transient
//	@JsonIgnore
	@Column
	public String password ;
	
	@Column
	public String type = "ADMIN";
//	@Column(nullable = false)
//	public Date dateCreation = new Date();

	public static Finder<Long, User> finder = new Finder<Long, User>(
			Long.class, User.class);

	
//    public User() {
//		super();
//		this.setPassword(password);
//		this.dateCreation = new Date();
//	}

	public User(String userName, String password) {
		super();
		this.userName = userName;
		this.setPassword(password);
	}

	public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
        shaPassword = getSha512(password);
    }

//	
//	public String createToken() {
//		authToken = UUID.randomUUID().toString();
//		save();
//		return authToken;
//	}
//
//	public void deleteAuthToken() {
//		authToken = null;
//		save();
//	}

	public static byte[] getSha512(String value) {

		try {
			return MessageDigest.getInstance("SHA-512").digest(
					value.getBytes("UTF-8"));
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
//	public static User findByAuthToken(String authToken){
//		if(authToken == null){
//			return null;
//		}
//		try{
//			return finder.where().eq("authToken",authToken).findUnique();	
//		}
//		catch(Exception e){
//			return null;	
//		}
//	}
	
//	public static User findByEmailAddressAndPassword(String userName, String password){
//		return finder.where().eq("userName", userName).eq("shaPassword", getSha512(password)).findUnique();
//	}
//	
	public static User findByUserName(String userName){
		return finder.where().eq("userName", userName).findUnique();
	}
}

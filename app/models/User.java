package models;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.management.RuntimeErrorException;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.validation.Constraint;

import com.fasterxml.jackson.annotation.JsonIgnore;

import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

@Entity
public class User extends Model {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long idUser;
	@JsonIgnore
	@Column(unique = true, length = 256, nullable = false)
	public String userName;

	@JsonIgnore
	@Column(length = 64, nullable = false)
	public byte[] shaPassword;
	
// uncomment the transient to delete password column and more secure 
//	@Transient
	@JsonIgnore
	@Column
	public String password;
	
	@Column
	public String type = "PEMANTAU";
	@Column
	public String name;
	@Column
	public String email;
	@Column
	public String status = "i'm a detective";
	@JsonIgnore
	@ManyToMany(cascade = CascadeType.ALL)
	public List<Laporan> listPantauLaporan ;
	@JsonIgnore
	@OneToMany(cascade = CascadeType.ALL)
	public List<Notif> listNotif = new ArrayList<Notif>();
	
	// pilih salah satu (masih dalam tahap riset)
	@JsonIgnore
	@ManyToMany(mappedBy = "listFollowingUser")
	public List<User> listFollowedUser = new ArrayList<User>();
	@JsonIgnore
	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(
			name = "follower_user",
			joinColumns = @JoinColumn(name = "followed_user_id"),
			inverseJoinColumns = @JoinColumn(name = "following_user_id")
			)
	public List<User> listFollowingUser = new ArrayList<User>();
	
	@Column
	public Integer jumlahFollowedUser = 0;
	@Column
	public Integer jumlahFollowingUser = 0;
	
	@OneToOne(cascade = CascadeType.ALL)
	public ImagePath imageProfilePath;
	
	@Column
	@JsonIgnore
	public String gcmId ;
	
	@Transient
	public boolean isFollowing;
	
	public static Finder<Long, User> finder = new Finder<Long, User>(
			Long.class, User.class);

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
    @JsonIgnore
    public List<User> getListFollowerUser() {
    	for (User user : listFollowedUser) {
			user.isFollowing = true;
		}
		return listFollowedUser;
	}
    
    public List<User> getListFollowingUser() {
    	for (User user : listFollowingUser) {
    		if(this.listFollowedUser.contains(user)){
    			user.isFollowing = true;	
    		}
		}
		return listFollowingUser;
	}
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

	public static User findByUserName(String userName){
		return finder.where().eq("userName", userName).findUnique();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((idUser == null) ? 0 : idUser.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (idUser == null) {
			if (other.idUser != null)
				return false;
		} else if (!idUser.equals(other.idUser))
			return false;
		return true;
	}
	
	
}

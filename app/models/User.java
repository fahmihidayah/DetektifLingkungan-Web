package models;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
	public List<Laporan> listPantauLaporan;
	@JsonIgnore
	@OneToMany(cascade = CascadeType.ALL)
	public List<Notif> listNotif;
	
	// pilih salah satu (masih dalam tahap riset)
	@JsonIgnore
	@ManyToMany( mappedBy = "listFollowingUser")
	public List<User> listFollowerUser;
	@JsonIgnore
	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(
			name = "user_follow_user",
			joinColumns = @JoinColumn(name = "follower_user_id"),
			inverseJoinColumns = @JoinColumn(name = "following_user_id")
			)
	public List<User> listFollowingUser;
	
	@Column
	public Integer jumlahFollowerUser = 0;
	@Column
	public Integer jumlahFollowingUser = 0;
	
	public void tambahFollowerUser(User user){
		listFollowerUser.add(user);
		jumlahFollowerUser = listFollowerUser.size();
	}
	
	public void tambahFollowingUser(User user){
		listFollowingUser.add(user);
		jumlahFollowingUser = listFollowingUser.size();
	}
	
	public void hapusFollowerUser(User user){
		listFollowerUser.remove(user);
		jumlahFollowerUser = listFollowerUser.size();
	}
	
	public void hapusFollowingUser(User user){
		listFollowingUser.remove(user);
		jumlahFollowingUser = listFollowingUser.size();
	}
	
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
}

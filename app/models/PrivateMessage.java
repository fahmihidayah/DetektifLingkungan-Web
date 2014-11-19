package models;

import java.util.Calendar;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import play.db.ebean.Model;

@Entity
public class PrivateMessage extends Model{
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long id ;
	@Column
	public String title;
	@Column
	public String message;
	@Column
	public Calendar date;
	@OneToOne(cascade = CascadeType.ALL)
	public User userSender;
	@OneToOne(cascade = CascadeType.ALL)
	public User userReceiver;
	@Column
	public Boolean isRead;
	
	public static Finder<Long, PrivateMessage> finder = new  Finder<>(Long.class, PrivateMessage.class);

}

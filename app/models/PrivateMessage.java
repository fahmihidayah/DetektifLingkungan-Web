package models;

import java.util.ArrayList;
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
	public Long idPrivateMessage;
	@OneToOne(cascade = CascadeType.ALL)
	public User userSender;
	@OneToOne(cascade = CascadeType.ALL)
	public User userReceiver;
	@Column(nullable = false)
	public String messageData;
	@Column
	public Calendar time;
	@Column
	public String status;
	
	public static Finder<Long, PrivateMessage> finder = new Finder<>(Long.class, PrivateMessage.class);
	
}

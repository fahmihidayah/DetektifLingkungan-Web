package models;

import java.util.ArrayList;
import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import play.db.ebean.Model;
@Entity
public class PrivateMessage extends Model{
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long id;
	@Column
	public User userSender;
	@Column
	public User userReceiver;
	@Column
	public String messageData;
	@Column
	public Calendar time;
	
	public static Finder<Long, PrivateMessage> finder = new Finder<>(Long.class, PrivateMessage.class);
	
}

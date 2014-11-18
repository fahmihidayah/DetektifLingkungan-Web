package models;

import java.util.Calendar;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

import play.db.ebean.Model;
@Entity
public class Notif extends Model {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long id;
	@OneToOne(cascade = CascadeType.ALL)
	public Laporan laporan;
	@Column
	public Calendar time;
	
	@JsonIgnore
	@ManyToOne(cascade = CascadeType.ALL)
	public User user;
	
	public static Finder<Long, Notif> finder = new Finder<>(Long.class, Notif.class);
}

package models;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import play.db.ebean.Model;
@Entity
public class Notif extends Model {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long id;
	@OneToOne(cascade = CascadeType.ALL)
	public Laporan laporan;
	@ManyToOne(cascade = CascadeType.ALL)
	public User user;
	
	public static Finder<Long, Notif> finder = new Finder<>(Long.class, Notif.class);
}

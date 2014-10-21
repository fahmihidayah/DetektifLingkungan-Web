package models;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import play.db.ebean.Model;
@Entity
public class Tanggapan extends Model {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long id;
	@Column
	public String dataTanggapan;
	@OneToOne(cascade = CascadeType.ALL)
	public User user;
	
	public static Finder<Long, Tanggapan> finder = new Finder<>(Long.class, Tanggapan.class);

}

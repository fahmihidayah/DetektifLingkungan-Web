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
public class Komentar extends Model {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long idKomentar;
	@Column
	public String dataKomentar;
	@OneToOne(cascade = CascadeType.ALL)
	public User user;
	@Column
	public Calendar time;
	@JsonIgnore
	@ManyToOne(cascade = CascadeType.ALL)
	public Laporan laporan;
	
	public static Finder<Long, Komentar> finder = new Finder<>(Long.class, Komentar.class);
}

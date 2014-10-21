package models;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

import play.db.ebean.Model;
// kurang data imagenya
@Entity
public class Laporan extends Model {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long id;
	@Column
	public String dataLaporan;
	@OneToOne(cascade = CascadeType.ALL)
	public Tanggapan tanggapan;
	@OneToOne(cascade = CascadeType.ALL)
	public User user;
	@OneToMany(cascade = CascadeType.ALL)
	public List<Komentar> listKomentar;
	@JsonIgnore
	@ManyToMany(mappedBy = "listPantauLaporan", cascade = CascadeType.ALL)
	public List<User> listUserPemantau;
	@Column
	public String katagoriLaporan;
	
	public static Finder<Long, Laporan> finder = new Finder<>(Long.class, Laporan.class);

}

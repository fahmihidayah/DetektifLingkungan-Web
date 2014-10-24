package models;

import java.util.ArrayList;
import java.util.Calendar;
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
	@JsonIgnore
	@OneToMany(cascade = CascadeType.ALL)
	public List<Komentar> listKomentar = new ArrayList<>();
	@JsonIgnore
	@ManyToMany(mappedBy = "listPantauLaporan", cascade = CascadeType.ALL)
	public List<User> listUserPemantau = new ArrayList<>();
	@Column 
	public Integer jumlahKomentar = 0;
	@Column 
	public Integer jumlahUserPemantau = 0;
	@Column
	public String katagoriLaporan;
	@Column
	public Double longitude;
	@Column
	public Double latitude;
	@Column
	public Calendar time;
	
	public static Finder<Long, Laporan> finder = new Finder<>(Long.class, Laporan.class);

	public void tambahKomentar(Komentar komentar){
		listKomentar.add(komentar);
		jumlahKomentar = listKomentar.size();
	}
	
	public void hapusKomentar(Komentar komentar){
		listKomentar.remove(komentar);
		jumlahKomentar = listKomentar.size();
	}
	
	public void tambahUserPemantau(User user){
		listUserPemantau.add(user);
		jumlahUserPemantau = listUserPemantau.size();
	}
	
	public void hapusUserPemantau(User user){
		listUserPemantau.remove(user);
		jumlahUserPemantau = listUserPemantau.size();
	}
}

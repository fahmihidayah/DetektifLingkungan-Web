package models;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

import play.db.ebean.Model;
// kurang data imagenya
@Entity
public class Laporan extends Model {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long idLaporan;
	
	@Column
	public String judulLaporan;
	
	@Column
	public String dataLaporan;
	@OneToOne(cascade = CascadeType.ALL)
	public Tanggapan tanggapan;
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	public User user;
	@JsonIgnore
	@OneToMany(cascade = CascadeType.ALL)
	public List<Komentar> listKomentar = new ArrayList<>();
	@JsonIgnore
	@ManyToMany(mappedBy = "listPantauLaporan", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	public List<User> listUserPemantau ;
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
	@OneToMany(cascade = CascadeType.ALL)
	public List<ImagePath> listImagePath = new ArrayList<ImagePath>();
	
	@Column
	public BigInteger viwer = new BigInteger("0");
	
	@Transient
	public boolean pantau = false;
	
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
		user.listPantauLaporan.remove(this);
		user.save();
		System.out.println(user.listPantauLaporan.size());
		jumlahUserPemantau = listUserPemantau.size();
	}
}

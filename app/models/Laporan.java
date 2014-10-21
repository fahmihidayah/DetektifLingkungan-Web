package models;

import java.util.List;

import javax.persistence.Entity;

import play.db.ebean.Model;
// kurang data imagenya
@Entity
public class Laporan extends Model {
	
	public Long id;
	public String dataLaporan;
	public Tanggapan tanggapan;
	public User user;
	public List<Komentar> listKomentar;
	public List<User> listUserPemantau;
	public String katagoriLaporan;
	
	public static Finder<Long, Laporan> finder = new Finder<>(Long.class, Laporan.class);

}

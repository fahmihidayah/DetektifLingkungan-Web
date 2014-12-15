package models;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

import com.avaje.ebean.annotation.Sql;

@Entity
@Sql
public class UserLaporan {
	@OneToOne(cascade = CascadeType.ALL)
	public User user;
	@OneToOne(cascade = CascadeType.ALL)
	public Laporan laporan;
}

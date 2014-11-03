package model_helper;

import models.ImagePath;

/*
 * id: 1
userName: "f"
password: "f"
type: "PEMANTAU"
name: "fahmi"
email: "fahmi@rembugan.com"
status: "i'm a detective"
jumlahFollowerUser: 0
jumlahFollowingUser: 0
}
 */
public class UserHelper {
	public Long id;
	public String userName;
	public String password;
	public String type;
	public String name;
	public String email;
	public String status;
	public Integer jumlahFollowerUser;
	public Integer jumlahFollowingUser;
	public boolean isFollowing;
	public ImagePath imageProfilePath;

}

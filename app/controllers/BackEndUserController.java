package controllers;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.sql.SQLInput;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;

import com.avaje.ebean.CallableSql;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;
import com.avaje.ebean.SqlRow;
import com.avaje.ebean.SqlUpdate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.ning.http.client.Request;
import com.ning.http.client.Response;
import com.ning.http.multipart.MultipartRequestEntity;
import com.ning.http.multipart.Part;
import com.ning.http.multipart.StringPart;

import model_response.SendMessageResponse;
import models.Auth;
import models.ImagePath;
import models.Komentar;
import models.Laporan;
import models.Notif;
import models.PrivateMessage;
import models.ServerAddress;
import models.User;
import models.UserLaporan;
import fahmi.lib.Constants;
import fahmi.lib.JsonHandler;
import fahmi.lib.RequestHandler;
import play.*;
import play.data.Form;
import play.libs.Akka;
import play.libs.F;
import play.libs.F.Promise;
import play.libs.Json;
import play.libs.WS;
import play.libs.WS.WSRequestHolder;
import play.mvc.*;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import views.html.*;

/**
 * 
 * @author fahmi catatan : untuk perubahan data pada laporan seperti tanggapan
 *         komentar dsb.
 *         
 *         catatan : untuk semua table follower_user harus diubah nama table nya jadi 
 *         followed user id dan following user id
 */
public class BackEndUserController extends Controller implements Constants {
	public static Form<User> frmUser = Form.form(User.class);

	public static Result index() {
		return ok(index.render("Your new application is ready."));
	}

	/**
	 * login api. require userName, password
	 * 
	 * @return
	 * 
	 *         public static Promise<Result>
	 */
	public static Promise<Result> login() {

		Promise<Result> promise = Promise.promise(new F.Function0<Result>() {

			@Override
			public Result apply() throws Throwable {
				String key[] = { "userName", "password" };
				RequestHandler requestHandler = new RequestHandler(frmUser);
				requestHandler.setArrayKey(key);
				if (requestHandler.isContainError()) {
					System.out.println("error 1");
					return badRequest(JsonHandler.getSuitableResponse(
							requestHandler.getErrorMessage(), false));
				}
				User user = Auth.findUser(
						requestHandler.getStringValue("userName"),
						requestHandler.getStringValue("password"));
				if (user == null) {
					System.out.println("error 2");
					return badRequest(JsonHandler.getSuitableResponse(
							"User not found", false));
				}
				Auth auth = new Auth();
				auth.createToken();
				auth.save();
				ObjectNode data = Json.newObject();
				data.put("authKey", auth.authToken);
				data.put("user", Json.toJson(user));
				return ok(JsonHandler.getSuitableResponse(data, true));
			}
		});
		return promise;

	}

	/**
	 * update user gcm id api. require authKey. userId, gcmId
	 * 
	 * @return
	 */
	public static Promise<Result> updateGcmId() {
		Promise<Result> promise = Promise.promise(new F.Function0<Result>() {

			@Override
			public Result apply() throws Throwable {
				String key[] = { "userId", "gcmId" };
				RequestHandler requestHandler = new RequestHandler(true,
						frmUser);
				requestHandler.setArrayKey(key);
				if (requestHandler.isContainError()) {
					return badRequest(JsonHandler.getSuitableResponse(
							requestHandler.getErrorMessage(), false));
				}
				User user = User.finder.byId(requestHandler
						.getLongValue("userId"));
				String gcmId = requestHandler.getStringValue("gcmId");

				String query = "update user set gcm_id=:gcm_id where id_user=:id_user";
				SqlUpdate update = Ebean.createSqlUpdate(query)
						.setParameter("gcm_id", gcmId)
						.setParameter("id_user", user.idUser);
				update.execute();
				return ok(JsonHandler.getSuitableResponse(
						"success update user", true));
			}
		});
		return promise;
	}

	/**
	 * logout api. require authKey
	 * 
	 * @return
	 */
	public static Promise<Result> logout() {
		Promise<Result> promise = Promise.promise(new F.Function0<Result>() {

			@Override
			public Result apply() throws Throwable {
				String key[] = { "authKey" };
				RequestHandler requestHandler = new RequestHandler(frmUser);
				requestHandler.setArrayKey(key);
				if (requestHandler.isContainError()) {
					return badRequest(JsonHandler.getSuitableResponse(
							requestHandler.getErrorMessage(), false));
				}

				Auth auth = Auth.findAuth(requestHandler
						.getStringValue("authKey"));
				auth.delete();
				return ok(JsonHandler.getSuitableResponse("logout", true));
			}
		});
		return promise;
	}

	/**
	 * register user api require "name", "userName", "password", "email"
	 * 
	 * @return
	 */
	public static Promise<Result> registerUser() {
		Promise<Result> promise = Promise.promise(new F.Function0<Result>() {

			@Override
			public Result apply() throws Throwable {
				String key[] = { "name", "userName", "password", "email" };
				RequestHandler requestHandler = new RequestHandler(frmUser);
				requestHandler.setArrayKey(key);
				if (requestHandler.isContainError()) {
					return badRequest(JsonHandler.getSuitableResponse(
							requestHandler.getErrorMessage(), false));
				}
				User user = new User(requestHandler.getStringValue("userName"),
						requestHandler.getStringValue("password"));
				user.name = requestHandler.getStringValue("name");
				user.email = requestHandler.getStringValue("email");
				user.imageProfilePath = ImagePath.finder.where()
						.eq("keterangan", IM_DEFAULT_PROFILE).findUnique();
				user.save();

				return ok(JsonHandler.getSuitableResponse(user, true));
			}
		});
		return promise;
	}

	/**
	 * insert laporan. require "authKey" "dataLaporan", "userId",
	 * "katagoriLaporan", "longitude", "latitude", "judulLaporan"
	 * 
	 * @return
	 */
	public static Promise<Result> insertLaporan() {

		Promise<Result> promise = Promise.promise(new F.Function0<Result>() {

			@Override
			public Result apply() throws Throwable {
				String key[] = { "dataLaporan", "userId", "katagoriLaporan",
						"longitude", "latitude", "judulLaporan"/* , "time" */};
				RequestHandler requestHandler = new RequestHandler(true,
						frmUser);
				requestHandler.setArrayKey(key);
				if (requestHandler.isContainError()) {
					return badRequest(JsonHandler.getSuitableResponse(
							requestHandler.getErrorMessage(), false));
				}

				User user = User.finder.byId(requestHandler
						.getLongValue("userId"));
				if (user == null) {
					return badRequest(JsonHandler.getSuitableResponse(
							"User not found", false));
				}
				Laporan laporan = new Laporan();
				laporan.user = user;
				laporan.judulLaporan = requestHandler
						.getStringValue("judulLaporan");
				laporan.dataLaporan = requestHandler
						.getStringValue("dataLaporan");
				laporan.katagoriLaporan = requestHandler
						.getStringValue("katagoriLaporan");
				laporan.longitude = requestHandler.getDoubleValue("longitude");
				laporan.latitude = requestHandler.getDoubleValue("latitude");
				laporan.time = Calendar.getInstance();
				laporan.save();
				return ok(JsonHandler.getSuitableResponse(laporan, true));
			}
		});
		return promise;

	}

	/**
	 * insert image laporan api. require authKey "laporanId" "picture" (file)
	 * 
	 * call this api as many as image count
	 * 
	 * @return
	 */
	public static Promise<Result> insertLaporanImage() {

		Promise<Result> promise = Promise.promise(new F.Function0<Result>() {

			@Override
			public Result apply() throws Throwable {
				String key[] = { "laporanId" };
				RequestHandler requestHandler = new RequestHandler(true,
						frmUser);
				requestHandler.setArrayKey(key);
				if (requestHandler.isContainError()) {
					return badRequest(JsonHandler.getSuitableResponse(
							requestHandler.getErrorMessage(), false));
				}
				Laporan laporan = Laporan.finder.byId(requestHandler
						.getLongValue("laporanId"));

				ImagePath imagePath = ImagePath.setImageFromRequest("picture");
				imagePath.laporan = laporan;
				imagePath.keterangan = IM_LAPORAN;
				imagePath.save();
				laporan.listImagePath.add(imagePath);
				return ok(JsonHandler.getSuitableResponse(
						"success insert image", true));
			}
		});
		return promise;
	}

	/*
	 * kekurangan - kurang difilter perbagian untuk diambil. contoh 10 item
	 * terupdate berdasarkan tanggal.
	 */
	public static Promise<Result> listLaporan() {
		Promise<Result> promise = Promise.promise(new F.Function0<Result>() {
			@Override
			public Result apply() throws Throwable {
				List<Laporan> listLaporan = Laporan.finder.all();
				return ok(JsonHandler.getSuitableResponse(listLaporan, true));
			}
		});
		return promise;
	}

	/**
	 * get list laporan api. require authKey "userId" "type" h, l, f, d
	 * 
	 * option laporanId
	 * 
	 * @return
	 */
	public static Promise<Result> getListLaporan() {
		Promise<Result> promise = Promise.promise(new F.Function0<Result>() {
			@Override
			public Result apply() throws Throwable {
				String key[] = { "userId", "type" };
				RequestHandler requestHandler = new RequestHandler(true,
						frmUser);
				requestHandler.setArrayKey(key);
				requestHandler.checkError();
				if (requestHandler.isContainError()) {
					return badRequest(JsonHandler.getSuitableResponse(
							requestHandler.getErrorMessage(), false));
				}
				String type = requestHandler.getStringValue("type");
				Laporan laporan = null;
				if (type.equalsIgnoreCase("h") || type.equalsIgnoreCase("l")) {
					laporan = Laporan.finder.byId(requestHandler
							.getOptionalLongValue("laporanId"));
					if (laporan == null) {
						return badRequest(JsonHandler.getSuitableResponse(
								"laporan not found", false));
					}
				}
				User user = User.finder.byId(requestHandler
						.getLongValue("userId"));

				List<Laporan> listUpdateLaporan = null;

				if (type.equalsIgnoreCase("h")) {
					listUpdateLaporan = Laporan.finder.where()
							.gt("time", laporan.time.getTime())/*
																 * .order(
																 * "time desc")
																 */.findList();

				} else if (type.equalsIgnoreCase("l")) {
					listUpdateLaporan = Laporan.finder.where()
							.lt("time", laporan.time.getTime())
							.order("time desc").setMaxRows(2).findList();
				} else if (type.equalsIgnoreCase("o")) {
					listUpdateLaporan = Laporan.finder
							.where()
							.eq("user_id_user",
									requestHandler.getLongValue("userId") + "")
							.order("time desc").setMaxRows(5).findList();
				} else if (type.equalsIgnoreCase("d")){
					listUpdateLaporan = Laporan.finder.where().order("jumlah_user_pemantau desc").setMaxRows(5).findList();
				}
				
				else {
					listUpdateLaporan = Laporan.finder.where()
							.order("time desc")/* .setMaxRows(5) */.findList();
				}

				for (Laporan eLaporan : listUpdateLaporan) {
					String select = "select user_id_user, laporan_id_laporan from user_laporan "
							+ "where user_id_user=:id_user and laporan_id_laporan=:id_laporan";
					SqlRow sqlRow = Ebean.createSqlQuery(select)
							.setParameter("id_user", user.idUser)
							.setParameter("id_laporan", eLaporan.idLaporan)
							.findUnique();
					if (sqlRow != null) {
						eLaporan.pantau = true;
					}
				}
				// if(eLaporan.listUserPemantau.contains(user)){
				// eLaporan.pantau = true;
				// }
				// }

				for (Laporan laporan2 : listUpdateLaporan) {
					User user2 = User.finder.byId(laporan2.user.idUser);
					laporan2.user = user2;
				}

				return ok(JsonHandler.getSuitableResponse(listUpdateLaporan,
						true));
			}
		});
		return promise;
	}

	/**
	 * pantau api. require authKey "userId", "laporanId"
	 * 
	 * @return
	 */
	public static Promise<Result> pantau() {
		Promise<Result> promise = Promise.promise(new F.Function0<Result>() {
			@Override
			public Result apply() throws Throwable {
				String key[] = { "userId", "laporanId" };
				RequestHandler requestHandler = new RequestHandler(true,
						frmUser);
				requestHandler.setArrayKey(key);
				if (requestHandler.isContainError()) {
					return badRequest(JsonHandler.getSuitableResponse(
							requestHandler.getErrorMessage(), false));
				}
				Laporan laporan = Laporan.finder.byId(requestHandler
						.getLongValue("laporanId"));
				User userPemantau = User.finder.byId(requestHandler
						.getLongValue("userId"));

				laporan.jumlahUserPemantau++;
				String updateQuery = "update laporan set jumlah_user_pemantau="
						+ laporan.jumlahUserPemantau + " where id_laporan="
						+ laporan.idLaporan + "";
				SqlUpdate sqlUpdate = Ebean.createSqlUpdate(updateQuery);
				sqlUpdate.execute();

				String insertQuery = "insert into user_laporan (user_id_user, laporan_id_laporan) values ("
						+ userPemantau.idUser + ", " + laporan.idLaporan + ")";
				SqlUpdate sqlInsert = Ebean.createSqlUpdate(insertQuery);
				sqlInsert.execute();
				laporan.pantau = true;
				createNotif(laporan, userPemantau, userPemantau.name + " memantau laporan " + laporan.judulLaporan, TYPE_LAPORAN);
//				createNotif(laporan, userPemantau, userPemantau.name +" memantau ");
				return ok(JsonHandler.getSuitableResponse(laporan, true));
			}
		});
		return promise;
	}

	/**
	 * unpantau api. require authKey "userId", "laporanId"
	 * 
	 * @return
	 */
	public static Promise<Result> unpantau() {
		Promise<Result> promise = Promise.promise(new F.Function0<Result>() {
			@Override
			public Result apply() throws Throwable {
				String key[] = { "userId", "laporanId" };
				RequestHandler requestHandler = new RequestHandler(true,
						frmUser);
				requestHandler.setArrayKey(key);
				if (requestHandler.isContainError()) {
					return badRequest(JsonHandler.getSuitableResponse(
							requestHandler.getErrorMessage(), false));
				}
				Laporan laporan = Laporan.finder.byId(requestHandler
						.getLongValue("laporanId"));
				User userPemantau = User.finder.byId(requestHandler
						.getLongValue("userId"));

				laporan.jumlahUserPemantau--;
				String updateQuery = "update laporan set jumlah_user_pemantau="
						+ laporan.jumlahUserPemantau + " where id_laporan="
						+ laporan.idLaporan + "";
				SqlUpdate sqlUpdate = Ebean.createSqlUpdate(updateQuery);
				sqlUpdate.execute();

				String deleteQuery = "delete from user_laporan where user_id_user = "
						+ userPemantau.idUser
						+ " and laporan_id_laporan = "
						+ laporan.idLaporan + "";
				SqlUpdate sqlInsert = Ebean.createSqlUpdate(deleteQuery);
				sqlInsert.execute();
				laporan.pantau = false;
				return ok(JsonHandler.getSuitableResponse(laporan, true));
			}
		});
		return promise;

	}

	/**
	 * list komentar api. requre authKey, laporanId
	 * 
	 * @return
	 */
	public static Promise<Result> listKomentar() {
		Promise<Result> promise = Promise.promise(new F.Function0<Result>() {
			@Override
			public Result apply() throws Throwable {
				String key[] = { "laporanId" };
				RequestHandler requestHandler = new RequestHandler(true,
						frmUser);
				requestHandler.setArrayKey(key);
				if (requestHandler.isContainError()) {
					return badRequest(JsonHandler.getSuitableResponse(
							requestHandler.getErrorMessage(), false));
				}
				List<Komentar> listKomentar = Komentar.finder
						.where()
						.eq("laporan_id_laporan",
								requestHandler.getStringValue("laporanId"))
						.findList();
				for (Komentar komentar : listKomentar) {
					User user = User.finder.byId(komentar.user.idUser);
					komentar.user = user;
				}
				return ok(JsonHandler.getSuitableResponse(listKomentar, true));
			}
		});
		return promise;
	}

	/**
	 * insert komentar api. require authKey, "laporanId", "dataKomentar",
	 * "userId"
	 * 
	 * @return
	 */

	public static Promise<Result> insertKomentar() {

		Promise<Result> promise = Promise.promise(new F.Function0<Result>() {
			@Override
			public Result apply() throws Throwable {
				String key[] = { "laporanId", "dataKomentar", "userId" };
				RequestHandler requestHandler = new RequestHandler(true,
						frmUser);
				requestHandler.setArrayKey(key);
				if (requestHandler.isContainError()) {
					return badRequest(JsonHandler.getSuitableResponse(
							requestHandler.getErrorMessage(), false));
				}
				Laporan laporan = Laporan.finder.byId(requestHandler
						.getLongValue("laporanId"));
				User user = User.finder.byId(requestHandler
						.getLongValue("userId"));
				Komentar komentar = new Komentar();
				komentar.user = user;
				komentar.dataKomentar = requestHandler
						.getStringValue("dataKomentar");
				komentar.laporan = laporan;
				komentar.time = Calendar.getInstance();
				komentar.save();
				komentar.laporan.jumlahKomentar++;
				String updateQuery = "update laporan set jumlah_komentar=:jumlah_komentar where "
						+ "id_laporan=:id_laporan";
				SqlUpdate sqlUpdate = Ebean
						.createSqlUpdate(updateQuery)
						.setParameter("jumlah_komentar",
								komentar.laporan.jumlahKomentar)
						.setParameter("id_laporan", komentar.laporan.idLaporan);
				sqlUpdate.execute();
				createNotif(laporan, user, user.name + " mengomentari laporan " + laporan.judulLaporan, TYPE_LAPORAN);
				return ok(JsonHandler.getSuitableResponse(komentar, true));
			}
		});
		return promise;

	}

	/**
	 * get user profile api. require authKey, userId, followerUserId
	 * 
	 * error
	 * @return
	 */
	public static Promise<Result> getUserProfile() {
		Promise<Result> promise = Promise.promise(new F.Function0<Result>() {
			@Override
			public Result apply() throws Throwable {
				String key[] = { "userId", "followingUserId" };
				RequestHandler requestHandler = new RequestHandler(true,
						frmUser);
				requestHandler.setArrayKey(key);
				if (requestHandler.isContainError()) {
					return badRequest(JsonHandler.getSuitableResponse(
							requestHandler.getErrorMessage(), false));
				}
				User user = User.finder.byId(requestHandler
						.getLongValue("userId"));
				User userFollower = User.finder.byId(requestHandler
						.getLongValue("followingUserId"));
				if (user == null) {
					return badRequest(JsonHandler.getSuitableResponse(
							"user not found", false));
				}
				String selectQuery = "select * "
						+ "from follower_user where followed_user_id="+user.idUser+""
						+ " and following_user_id="+ userFollower.idUser+"";
				SqlRow sqlRow = Ebean.createSqlQuery(selectQuery)
						.findUnique();
				if (sqlRow != null) {
					user.isFollowing = true;
				}

				return ok(JsonHandler.getSuitableResponse(user, true));
			}
		});
		return promise;

	}

	/**
	 * follow api. require authKey, userId, followerUserId
	 * 
	 * @return
	 */
	public static Promise<Result> follow() {

		Promise<Result> promise = Promise.promise(new F.Function0<Result>() {
			@Override
			public Result apply() throws Throwable {
				String key[] = {"followedUserId", "followingUserId" };
				RequestHandler requestHandler = new RequestHandler(true,
						frmUser);
				requestHandler.setArrayKey(key);
				if (requestHandler.isContainError()) {
					return badRequest(JsonHandler.getSuitableResponse(
							requestHandler.getErrorMessage(), false));
				}
				User followingUser = User.finder.byId(requestHandler
						.getLongValue("followingUserId"));
				User followedUser = User.finder.byId(requestHandler
						.getLongValue("followedUserId"));
				if (followingUser == null || followedUser == null) {
					return badRequest(JsonHandler.getSuitableResponse(
							"user not found", false));
				}

				followingUser.jumlahFollowedUser++;
				followedUser.jumlahFollowingUser++;
				
				String insertQuery = "insert into follower_user (followed_user_id, following_user_id) values (:followed_user_id, :following_user_id)";
				SqlUpdate sqlUpdate = Ebean.createSqlUpdate(insertQuery)
						.setParameter("followed_user_id", followedUser.idUser )
						.setParameter("following_user_id", followingUser.idUser) 
						.setParameter("id_user", followingUser.idUser);
				sqlUpdate.execute();

				String updateQuery = "update user set "
						+ "jumlah_followed_user =:jumlah_followed_user, "
						+ "jumlah_following_user =:jumlah_following_user where"
						+ " id_user=:id_user";
				SqlUpdate sqlUpdate2 = Ebean
						.createSqlUpdate(updateQuery)
						.setParameter("jumlah_followed_user",
								followingUser.jumlahFollowedUser)
						.setParameter("jumlah_following_user",
								followingUser.jumlahFollowingUser)
						.setParameter("id_user", followingUser.idUser);
				sqlUpdate2.execute();
				sqlUpdate2 = Ebean
						.createSqlUpdate(updateQuery)
						.setParameter("jumlah_followed_user",
								followedUser.jumlahFollowedUser)
						.setParameter("jumlah_following_user",
								followedUser.jumlahFollowingUser)
						.setParameter("id_user", followedUser.idUser);
				sqlUpdate2.execute();

				// userFollow.listFollowerUser.add(user);
				// userFollow.jumlahFollowerUser++;
				// userFollow.update();
				// user.jumlahFollowingUser++;
				// user.update();
				
				followedUser = User.finder.byId(requestHandler
						.getLongValue("followedUserId"));
				followedUser.isFollowing = true;
				return ok(JsonHandler.getSuitableResponse(followedUser,
						true));
			}
		});
		return promise;

	}

	/**
	 * unfollow api. require authKey, userId, followerUserId
	 * 
	 * @return
	 */
	public static Promise<Result> unfollow() {
		Promise<Result> promise = Promise.promise(new F.Function0<Result>() {
			@Override
			public Result apply() throws Throwable {
				String key[] = {"followedUserId", "followingUserId" };
				RequestHandler requestHandler = new RequestHandler(true,
						frmUser);
				requestHandler.setArrayKey(key);
				if (requestHandler.isContainError()) {
					return badRequest(JsonHandler.getSuitableResponse(
							requestHandler.getErrorMessage(), false));
				}
				User followingUser = User.finder.byId(requestHandler
						.getLongValue("followingUserId"));
				User followedUser = User.finder.byId(requestHandler
						.getLongValue("followedUserId"));
				if (followingUser == null || followedUser == null) {
					return badRequest(JsonHandler.getSuitableResponse(
							"user not found", false));
				}

				followingUser.jumlahFollowedUser--;
				followedUser.jumlahFollowingUser--;
				
				String insertQuery = "delete from follower_user where followed_user_id =:followed_user_id and following_user_id=:following_user_id";
				SqlUpdate sqlUpdate = Ebean.createSqlUpdate(insertQuery)
						.setParameter("followed_user_id", followedUser.idUser)
						.setParameter("following_user_id",  followingUser.idUser );
				sqlUpdate.execute();

				String updateQuery = "update user set "
						+ "jumlah_followed_user =:jumlah_followed_user, "
						+ "jumlah_following_user =:jumlah_following_user where"
						+ " id_user=:id_user";
				SqlUpdate sqlUpdate2 = Ebean
						.createSqlUpdate(updateQuery)
						.setParameter("jumlah_followed_user",
								followingUser.jumlahFollowedUser)
						.setParameter("jumlah_following_user",
								followingUser.jumlahFollowingUser)
						.setParameter("id_user", followingUser.idUser);
				sqlUpdate2.execute();
				sqlUpdate2 = Ebean
						.createSqlUpdate(updateQuery)
						.setParameter("jumlah_followed_user",
								followedUser.jumlahFollowedUser)
						.setParameter("jumlah_following_user",
								followedUser.jumlahFollowingUser)
						.setParameter("id_user", followedUser.idUser);
				sqlUpdate2.execute();

				// userFollow.listFollowerUser.remove(user);
				// userFollow.jumlahFollowerUser--;
				// userFollow.update();
				// user.listFollowingUser.remove(userFollow);
				// user.jumlahFollowingUser--;
				// user.update();
				followedUser = User.finder.byId(requestHandler
						.getLongValue("followedUserId"));
				followedUser.isFollowing = false;
				return ok(JsonHandler.getSuitableResponse(followedUser,
						true));
			}
		});
		return promise;

	}

	/**
	 * change status api require authKey, userId, status
	 * 
	 * @return
	 */
	public static Promise<Result> changeStatus() {
		Promise<Result> promise = Promise.promise(new F.Function0<Result>() {
			@Override
			public Result apply() throws Throwable {
				String key[] = { "userId", "status" };
				RequestHandler requestHandler = new RequestHandler(true,
						frmUser);
				requestHandler.setArrayKey(key);
				if (requestHandler.isContainError()) {
					return badRequest(JsonHandler.getSuitableResponse(
							requestHandler.getErrorMessage(), false));
				}
				User user = User.finder.byId(requestHandler
						.getLongValue("userId"));
				if (user == null) {
					return badRequest(JsonHandler.getSuitableResponse(
							"user not found", false));
				}
				String status = requestHandler.getStringValue("status");
				String query = "update user set status=:status where id_user=:id";
				SqlUpdate sqlUpdate = Ebean.createSqlUpdate(query)
						.setParameter("status", status)
						.setParameter("id", user.idUser);
				sqlUpdate.execute();

				// user.update();
				return ok(JsonHandler.getSuitableResponse(
						"success update status", true));
			}
		});
		return promise;
	}

	public static Result test() {
		return ok();
	}

	/**
	 * bisa di jadikan framework
	 * 
	 * @param key
	 * @return
	 */
	public static ImagePath uploadFile(String key) {
		MultipartFormData body = request().body().asMultipartFormData();
		FilePart picture = body.getFile(key);
		if (picture != null) {
			String fileName = picture.getFilename();
			fileName = UUID.randomUUID().toString().replace("-", "_");
			String contentType = picture.getContentType();
			File file = picture.getFile();
			file.renameTo(new File(Play.application().configuration()
					.getString(PATH_IMAGE), fileName));
			ServerAddress address = ServerAddress.finder.byId(new Long(1));
			ImagePath imagePath = new ImagePath();
			imagePath.fileName = fileName;
			imagePath.path = UPLOADS_FOLDER + fileName;
			return imagePath;
		} else {

			flash("error", "Missing file");
			return null;
		}
	}

	/**
	 * change user prof pict api. require authKey, userId, picture
	 * 
	 * @return
	 */
	public static Promise<Result> changeProfilePicture() {

		Promise<Result> promise = Promise.promise(new F.Function0<Result>() {
			@Override
			public Result apply() throws Throwable {
				String[] key = { "userId" };
				RequestHandler requestHandler = new RequestHandler(true,
						frmUser);
				requestHandler.setArrayKey(key);
				if (requestHandler.isContainError()) {
					return badRequest(JsonHandler.getSuitableResponse(
							requestHandler.getErrorMessage(), false));
				}
				User user = User.finder.byId(requestHandler
						.getLongValue("userId"));
				ImagePath imagePath = ImagePath.setImageFromRequest("picture");
				if (imagePath == null) {
					return badRequest(JsonHandler.getSuitableResponse(
							"require image", false));
				}
				ImagePath oldUserImage = user.imageProfilePath;
				imagePath.keterangan = IM_MODIFED;
				if (oldUserImage.keterangan
						.equalsIgnoreCase(IM_DEFAULT_PROFILE)) {
					imagePath.save();
					String updateQuery = "update user set image_profile_path_id_image_path =:id_image "
							+ "where id_user =:id_user";
					SqlUpdate sqlUpdate = Ebean.createSqlUpdate(updateQuery)
							.setParameter("id_image", imagePath.idImagePath)
							.setParameter("id_user", user.idUser);
					sqlUpdate.execute();
				} else {
					String updateQuery = "update image_path set path=:path, "
							+ "file_name=:file_name, "
							+ "keterangan=:keterangan where "
							+ "id_image_path=:id_image_path";
					SqlUpdate sqlUpdate = Ebean
							.createSqlUpdate(updateQuery)
							.setParameter("path", imagePath.path)
							.setParameter("file_name", imagePath.fileName)
							.setParameter("keterangan", imagePath.keterangan)
							.setParameter("id_image_path",
									oldUserImage.idImagePath);
					sqlUpdate.execute();
					ImagePath.deleteImage(oldUserImage);
				}
				//
				// user.imageProfilePath = imagePath;
				// Ebean.save(user);
				user = User.finder.byId(requestHandler.getLongValue("userId"));
				return ok(JsonHandler.getSuitableResponse(user, true));
			}
		});
		return promise;

	}

	/**
	 * get follower api. require authKey, userId, mode. mode 0 = follower mode 1
	 * = following
	 * 
	 * @return
	 */
	public static Promise<Result> getFollower() {

		Promise<Result> promise = Promise.promise(new F.Function0<Result>() {
			@Override
			public Result apply() throws Throwable {
				String key[] = { "userId", "mode" };
				RequestHandler requestHandler = new RequestHandler(true,
						frmUser);
				requestHandler.setArrayKey(key);
				if (requestHandler.isContainError()) {
					return badRequest(JsonHandler.getSuitableResponse(
							requestHandler.getErrorMessage(), false));
				}
				User user = User.finder.byId(requestHandler
						.getLongValue("userId"));
				String mode = requestHandler.getStringValue("mode");
				if (user == null) {
					return badRequest(JsonHandler.getSuitableResponse(
							"user not found", false));
				}
				if (mode.equalsIgnoreCase("0")) {
					return ok(JsonHandler.getSuitableResponse(
							user.getListFollowerUser(), true));
				} else {
					return ok(JsonHandler.getSuitableResponse(
							user.getListFollowingUser(), true));
				}
			}
		});
		return promise;

	}

	/**
	 * view laporan api. require authKey, laporanId;
	 * 
	 * @return
	 */
	public static Promise<Result> viewLaporan() {
		Promise<Result> promise = Promise.promise(new F.Function0<Result>() {
			@Override
			public Result apply() throws Throwable {
				String key[] = { "laporanId" };
				RequestHandler requestHandler = new RequestHandler(true,
						frmUser);
				requestHandler.setArrayKey(key);
				if (requestHandler.isContainError()) {
					return ok(JsonHandler.getSuitableResponse(
							requestHandler.getErrorMessage(), false));
				}
				Laporan laporan = Laporan.finder.byId(requestHandler
						.getLongValue("laporanId"));
				laporan.viwer = laporan.viwer.add(new BigInteger("1"));
				// laporan.update();
				String updateQuery = "update laporan set viwer=:viewer where id_laporan=:id_laporan";
				SqlUpdate sqlUpdate = Ebean.createSqlUpdate(updateQuery)
						.setParameter("viewer", laporan.viwer)
						.setParameter("id_laporan", laporan.idLaporan);
				sqlUpdate.execute();
				return ok(JsonHandler.getSuitableResponse("success add view",
						true));
			}
		});
		return promise;

	}

	/**
	 * send message api. require authKey "senderUserId", "receiverUserId",
	 * "title", "message"
	 * 
	 * @return
	 */
	public static Result sendMessage() {
		// Promise<Result> resultPromise = Promise.promise(new
		// play.libs.F.Function<Result>() {
		//
		// @Override
		// public Object apply(Object arg0) throws Throwable {
		// // TODO Auto-generated method stub
		// return null;
		// }
		// });

		// Promise<Result> promise = Promise.promise(new F.Function0<Result>() {
		//
		// @Override
		// public Result apply() throws Throwable {
		// String key [] = {"userSenderId", "userReceiverId", "message"};
		// RequestHandler requestHandler = new RequestHandler(true,frmUser);
		// requestHandler.setArrayKey(key);
		// if(requestHandler.isContainError()){
		// return
		// badRequest(JsonHandler.getSuitableResponse(requestHandler.getErrorMessage(),
		// false));
		// }
		//
		//
		// return null;
		// }
		// });
		// return promise;
		String key[] = { "userSenderId", "userReceiverId", "messageData" };
		RequestHandler requestHandler = new RequestHandler(true, frmUser);
		requestHandler.setArrayKey(key);
		if (requestHandler.isContainError()) {
			return badRequest(JsonHandler.getSuitableResponse(
					requestHandler.getErrorMessage(), false));
		}
		final User userSender = User.finder.byId(requestHandler
				.getLongValue("userSenderId"));
		final User userReceiver = User.finder.byId(requestHandler
				.getLongValue("userReceiverId"));
		final String messageData = requestHandler.getStringValue("messageData");
		String arrayGcmId[] = { userReceiver.gcmId };

		PrivateMessage privateMessage = new PrivateMessage();
		privateMessage.idPrivateMessage = java.util.UUID.randomUUID()
				.getLeastSignificantBits();
		privateMessage.messageData = messageData;
		privateMessage.userReceiver = userReceiver;
		privateMessage.userSender = userSender;
		privateMessage.time = Calendar.getInstance();
		privateMessage.status = STATUS_UNREAD;
		ObjectNode messageJson = Json.newObject();
		messageJson.put("message", Json.toJson(privateMessage));

		sendGcm(arrayGcmId, messageJson);

		// Promise<Result> promise = WS.url("http://127.0.0.1:9000/api/login")
		// .post(node).map(
		// new play.libs.F.Function<WS.Response, Result>() {
		//
		// @Override
		// public Result apply(play.libs.WS.Response arg0)
		// throws Throwable {
		// System.out.println(arg0.asJson().toString());
		// return ok(arg0.asJson().toString());
		// }
		// });
		return ok(messageJson);
		// return ok(JsonHandler.getSuitableResponse(messageJson, true));

	}

	/**
	 * login asycronus
	 * 
	 * @return
	 */
	public static Promise<Result> risetAsychronusApi() {
		JsonNode node = Json.newObject().put("userName", "f")
				.put("password", "f");

		// Promise<JsonNode> holder = WS.url("http://127.0.0.1:9000/api/login")
		// .post(node).map(
		// new play.libs.F.Function<WS.Response, JsonNode>() {
		//
		// @Override
		// public JsonNode apply(play.libs.WS.Response arg0)
		// throws Throwable {
		// System.out.println(arg0.asJson().toString());
		// return arg0.asJson();
		// }
		// });
		Promise<Result> holder = WS.url("http://127.0.0.1:9000/api/login")
				.post(node)
				.map(new play.libs.F.Function<WS.Response, Result>() {

					@Override
					public Result apply(play.libs.WS.Response arg0)
							throws Throwable {
						// System.out.println(arg0.asJson().toString());
						return ok(arg0.asJson().toString());
					}
				});

		return holder;
	}

	/**
	 * get list private message api. require authKey userId, type
	 * 
	 * @return
	 */
	public static Result getListPrivateMessage() {
		return ok();
	}
	/*
	
	 */
	public static Promise<Result> find(){
		Promise<Result> promise = Promise.promise(new F.Function0<Result>() {

			@Override
			public Result apply() throws Throwable {
				String key[] = {"key_word", "type", "userId"};
				RequestHandler requestHandler = new RequestHandler(true,frmUser);
				requestHandler.setArrayKey(key);
				if(requestHandler.isContainError()){
					return badRequest(JsonHandler.getSuitableResponse(requestHandler.getErrorMessage(), false));
				}
				String type = requestHandler.getStringValue("type");
				String keyWord = requestHandler.getStringValue("key_word");
				
				if(type.equalsIgnoreCase("laporan")){
					List<Laporan> listLaporan = Laporan.finder.where().like("judul_laporan", "%"+ keyWord + "%").findList();
					User user = User.finder.byId(requestHandler.getLongValue("userId"));
					for (Laporan eLaporan : listLaporan) {
						String select = "select user_id_user, laporan_id_laporan from user_laporan "
								+ "where user_id_user=:id_user and laporan_id_laporan=:id_laporan";
						SqlRow sqlRow = Ebean.createSqlQuery(select)
								.setParameter("id_user", user.idUser)
								.setParameter("id_laporan", eLaporan.idLaporan)
								.findUnique();
						if (sqlRow != null) {
							eLaporan.pantau = true;
						}
					}
					// if(eLaporan.listUserPemantau.contains(user)){
					// eLaporan.pantau = true;
					// }
					// }

					for (Laporan laporan2 : listLaporan) {
						User user2 = User.finder.byId(laporan2.user.idUser);
						laporan2.user = user2;
					}
					
					return ok(JsonHandler.getSuitableResponse(listLaporan, true));
				}
				else {
					List<User> listUser = User.finder.where().like("name", "%" +keyWord + "%").findList();
					return ok(JsonHandler.getSuitableResponse(listUser, true));
				}
			}
		});
		return promise;
	}
	
	public static Promise<Result> getListNotif(){
		Promise<Result> promise = Promise.promise(new F.Function0<Result>() {
			@Override
			public Result apply() throws Throwable {
				String key [] = {"userId"};
				RequestHandler requestHandler = new RequestHandler(true, frmUser);
				requestHandler.setArrayKey(key);
				if(requestHandler.isContainError()){
					return badRequest(JsonHandler.getSuitableResponse(requestHandler.getErrorMessage(), false));
				}
				String userId = requestHandler.getStringValue("userId");
				
				List<Notif> listNotif = Ebean.find(Notif.class).fetch("laporan.user").where().eq("id_user", userId).findList();//Notif.finder.where().eq("user_id_user", userId).setOrderBy("time desc").setMaxRows(5).findList();
				
				return ok(JsonHandler.getSuitableResponse(listNotif, true));
			}
		});
		return promise;
	}
	
	public static void createNotif(Laporan laporan, User user, String infoLaporan, String type){
		if(!user.idUser.equals(laporan.user.idUser)){
		Laporan laporanPemantau = Ebean.find(Laporan.class).fetch("user").fetch("listUserPemantau").where().idEq(laporan.idLaporan).findUnique();
					
			Notif notif = new Notif();
			notif.laporan = laporanPemantau;
			notif.user = User.finder.byId(user.idUser);
			notif.notifInfo = infoLaporan;
			notif.time = Calendar.getInstance();
			notif.typeNotif = type;
			notif.save();
		
		
		
		ObjectNode objectNode = Json.newObject();
		objectNode.put("message", Json.toJson(notif));
		
//		String laporanId = laporan.idLaporan + "";
		List<Komentar> komen = Ebean.find(Komentar.class).select("dataKomentar").fetch("user","name").where().eq("laporan_id_laporan", laporanPemantau.idLaporan).findList();
		Set<String> gcm = new HashSet<String>();
//		List<String> gcm = new ArrayList<String>();
		for (Komentar k : komen) {
			if(!k.user.idUser.equals(user.idUser) && k.user.idUser != null){
				gcm.add(k.user.gcmId);	
			}
			System.out.println(k.user.gcmId);
			
		}
//		Laporan laporanPemantau = Ebean.find(Laporan.class).fetch("user").fetch("listUserPemantau").where().idEq(laporanId).findUnique();
		List<User> userPemantau = laporanPemantau.listUserPemantau;
		for (User eU : userPemantau) {
			if(!eU.idUser.equals(user.idUser) && eU.idUser != null){
				gcm.add(eU.gcmId);	
			}
			
		}
		List<String> gcmList = new ArrayList<String>();
		gcmList.addAll(gcm);
		String gcmids[] = new String[gcmList.size()];
		gcmList.toArray(gcmids);
		sendGcm(gcmids, objectNode);
		}
	}
	
	public static void sendGcm(String [] arrayGcmId, ObjectNode messageJson){
		ObjectNode node = Json.newObject();
		node.put("registration_ids", Json.toJson(arrayGcmId));
		node.put("data", messageJson);
		Promise<String> promise = WS
				.url("https://android.googleapis.com/gcm/send")
				.setHeader("Authorization", "key=" + API_KEY)
				.setHeader("Content-Type", "application/json").post(node)
				.map(new F.Function<WS.Response, String>() {

					@Override
					public String apply(play.libs.WS.Response arg0)
							throws Throwable {
						System.out.println(arg0.asJson().toString());
						SendMessageResponse messageResponse = new Gson()
								.fromJson(arg0.asJson().toString(),
										SendMessageResponse.class);

						// System.out.println(arg0.asJson().toString());
						return arg0.asJson().toString();
					}
				});
	}
	
	/**
	 * just test
	 * @return
	 */
	public static Result getListGcm(){
		String [] key = {"laporanId"};
		RequestHandler requestHandler = new RequestHandler(frmUser);
		requestHandler.setArrayKey(key);
		if(requestHandler.isContainError()){
			return badRequest(JsonHandler.getSuitableResponse(requestHandler.getErrorMessage(), false));
		}
		String laporanId = requestHandler.getStringValue("laporanId");
		List<Komentar> komen = Ebean.find(Komentar.class).select("dataKomentar").fetch("user","name").where().eq("laporan_id_laporan", laporanId).findList();
		Set<String> gcm = new HashSet<String>();
//		List<String> gcm = new ArrayList<String>();
		for (Komentar k : komen) {
			gcm.add(k.user.gcmId);
		}
		Laporan laporan = Ebean.find(Laporan.class).fetch("user").fetch("listUserPemantau").where().idEq(laporanId).findUnique();
		List<User> userPemantau = laporan.listUserPemantau;
		for (User user : userPemantau) {
			gcm.add(user.gcmId);
		}
		return ok(JsonHandler.getSuitableResponse(gcm,true));
	}

}

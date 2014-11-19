package controllers;

import java.io.File;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ning.http.client.Request;

import models.Auth;
import models.ImagePath;
import models.Komentar;
import models.Laporan;
import models.PrivateMessage;
import models.ServerAddress;
import models.User;
import fahmi.lib.Constants;
import fahmi.lib.JsonHandler;
import fahmi.lib.RequestHandler;
import play.*;
import play.data.Form;
import play.libs.Json;
import play.mvc.*;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import views.html.*;
/**
 * 
 * @author fahmi
 * catatan :
 * untuk perubahan data pada laporan seperti tanggapan komentar dsb.
 */
public class BackEndUserController extends Controller implements Constants {
	public static Form<User> frmUser = Form.form(User.class);
    public static Result index() {
        return ok(index.render("Your new application is ready."));
    }
    /**
     * login api.
     * require userName, password
     * @return
     */
    public static Result login(){
    	String key[] = {"userName", "password"};
    	RequestHandler requestHandler = new RequestHandler(frmUser);
    	requestHandler.setArrayKey(key);
    	if(requestHandler.isContainError()){
    		System.out.println("error 1");
    		return badRequest(JsonHandler.getSuitableResponse(requestHandler.getErrorMessage(), false));
    	}
    	User user = Auth.findUser(requestHandler.getStringValue("userName"),
    			requestHandler.getStringValue("password"));
    	if(user == null){
    		System.out.println("error 2");
    		return badRequest(JsonHandler.getSuitableResponse("User not found", false));
    	}
    	Auth auth = new Auth();
    	auth.createToken();
    	auth.save();
    	ObjectNode data = Json.newObject();
    	data.put("authKey", auth.authToken);
    	data.put("user", Json.toJson(user));
    	return ok(JsonHandler.getSuitableResponse(data, true));
    }
    /**
     * logout api.
     * require authKey
     * @return
     */
    public static Result logout(){
    	String key[] = {"authKey"};
    	RequestHandler requestHandler = new RequestHandler(frmUser);
    	requestHandler.setArrayKey(key);
    	if(requestHandler.isContainError()){
    		return badRequest(JsonHandler.getSuitableResponse(requestHandler.getErrorMessage(), false));
    	}
    	
    	Auth auth = Auth.findAuth(requestHandler.getStringValue("authKey"));
    	auth.delete();
    	return ok(JsonHandler.getSuitableResponse("logout", true));
    }
    /**
     * register user api
     * require 
     * "name", "userName", 
     * "password", "email"
     * @return
     */
    public static Result registerUser(){
    	String key[] = {"name", "userName", "password", "email"};
    	RequestHandler requestHandler = new RequestHandler(frmUser);
    	requestHandler.setArrayKey(key);
    	if(requestHandler.isContainError()){
    		return badRequest(JsonHandler.getSuitableResponse(requestHandler.getErrorMessage(), false));
    	}
    	User user = new User(requestHandler.getStringValue("userName"), 
    			requestHandler.getStringValue("password"));
    	user.name = requestHandler.getStringValue("name");
    	user.email = requestHandler.getStringValue("email");
    	user.imageProfilePath = ImagePath.finder.where().eq("keterangan", IM_DEFAULT_PROFILE).findUnique();
    	user.save();
    	return ok(JsonHandler.getSuitableResponse(user, true));
    }
    /**
     * insert laporan.
     * require 
     * "authKey"
     * "dataLaporan", "userId", 
     * "katagoriLaporan", "longitude", 
     * "latitude", "judulLaporan" 
     * @return
     */
    public static Result insertLaporan(){
    	String key[] = {"dataLaporan", "userId", "katagoriLaporan", "longitude", "latitude", "judulLaporan"/*, "time"*/};
    	RequestHandler requestHandler = new RequestHandler(true,frmUser);
    	requestHandler.setArrayKey(key);
    	if(requestHandler.isContainError()){
    		return badRequest(JsonHandler.getSuitableResponse(requestHandler.getErrorMessage(), false));
    	}
    	
    	
    	User user = User.finder.byId(requestHandler.getLongValue("userId"));
    	if(user == null){
    		return badRequest(JsonHandler.getSuitableResponse("User not found", false));
    	}
    	Laporan laporan = new Laporan();
    	laporan.user = user;
    	laporan.judulLaporan = requestHandler.getStringValue("judulLaporan");
    	laporan.dataLaporan = requestHandler.getStringValue("dataLaporan");
    	laporan.katagoriLaporan = requestHandler.getStringValue("katagoriLaporan");
    	laporan.longitude = requestHandler.getDoubleValue("longitude");
    	laporan.latitude = requestHandler.getDoubleValue("latitude");
    	laporan.time = Calendar.getInstance();
    	laporan.save();
    	return ok(JsonHandler.getSuitableResponse("Success insert laporan", true));
    }
    /**
     * insert image laporan api.
     * require 
     * authKey
     * "laporanId"
     * "picture" (file)
     * 
     * call this api as many as image count
     * @return
     */
    public static Result insertLaporanImage(){
    	String key[] = {"laporanId"};
    	RequestHandler requestHandler = new RequestHandler(true,frmUser);
    	requestHandler.setArrayKey(key);
    	if(requestHandler.isContainError()){
    		return badRequest(JsonHandler.getSuitableResponse(requestHandler.getErrorMessage(), false));
    	}
    	Laporan laporan = Laporan.finder.byId(requestHandler.getLongValue("laporanId"));
    	
    	ImagePath imagePath = ImagePath.setImageFromRequest("picture");
    	imagePath.laporan= laporan; 	
    	imagePath.keterangan = IM_LAPORAN;
    	imagePath.save();
    	laporan.listImagePath.add(imagePath);
    	return ok(JsonHandler.getSuitableResponse("success insert image", true));
    }
    
    /*
     * kekurangan - kurang difilter perbagian untuk diambil.
     * contoh 10 item terupdate berdasarkan tanggal.
     */
    public static Result listLaporan(){
    	List<Laporan> listLaporan = Laporan.finder.all();
    	return ok(JsonHandler.getSuitableResponse(listLaporan, true));
    }
    
    /**
     * get list laporan api.
     * require 
     * authKey
     * "userId"
     * "type" h, l, f
     * 
     * option 
     * laporanId
     * @return
     */
    public static Result getListLaporan(){
    	String key[] = {"userId","type"};
    	RequestHandler requestHandler = new RequestHandler(true,frmUser);
    	requestHandler.setArrayKey(key);
    	requestHandler.checkError();
    	if(requestHandler.isContainError()){
    		return badRequest(JsonHandler.getSuitableResponse(requestHandler.getErrorMessage(), false));
    	}
    	String type = requestHandler.getStringValue("type");
    	Laporan laporan = null;
    	if(type.equalsIgnoreCase("h") || type.equalsIgnoreCase("l")){
    		laporan = Laporan.finder.byId(requestHandler.getOptionalLongValue("laporanId"));
    		if(laporan == null){
    			return badRequest(JsonHandler.getSuitableResponse("laporan not found", false));
    		}
    	}
    	User user = User.finder.byId(requestHandler.getLongValue("userId"));
    	
    	List<Laporan> listUpdateLaporan = null; 
    	
    	if(type.equalsIgnoreCase("h")){
    		listUpdateLaporan = Laporan.finder.where().gt("time", laporan.time.getTime()).order("time desc").findList();
    	}
    	else if (type.equalsIgnoreCase("l")){
    		listUpdateLaporan = Laporan.finder.where().lt("time", laporan.time.getTime()).order("time desc").setMaxRows(2).findList();
    	}
    	else if(type.equalsIgnoreCase("o")){
    		listUpdateLaporan = Laporan.finder.where().eq("user_id", requestHandler.getLongValue("userId") + "").order("time desc").setMaxRows(5).findList();
    	}
    	else {
    		listUpdateLaporan = Laporan.finder.where().order("time desc")/*.setMaxRows(5)*/.findList();
    	}
    	
    	for (Laporan eLaporan : listUpdateLaporan) {
			if(eLaporan.listUserPemantau.contains(user)){
				eLaporan.pantau = true;
			}
		}
    	
    	return ok(JsonHandler.getSuitableResponse(listUpdateLaporan, true));
    }
    /**
     * pantau api.
     * require
     * authKey
     * "userId", "laporanId"
     * @return
     */
    public static Result pantau(){
    	String key[] = {"userId", "laporanId"};
    	RequestHandler requestHandler = new RequestHandler(true, frmUser);
    	requestHandler.setArrayKey(key);
    	if(requestHandler.isContainError()){
    		return badRequest(JsonHandler.getSuitableResponse(requestHandler.getErrorMessage(), false));
    	}
    	Laporan laporan = Laporan.finder.byId(requestHandler.getLongValue("laporanId"));
    	User userPemantau = User.finder.byId(requestHandler.getLongValue("userId"));
    	laporan.tambahUserPemantau(userPemantau);
    	laporan.update();
    	laporan.pantau = true;
    	return ok(JsonHandler.getSuitableResponse(laporan, true));
    }
    /**
     * unpantau api.
     * require 
     * authKey
     * "userId", "laporanId"
     * @return
     */
    public static Result unpantau(){
    	String key[] = {"userId", "laporanId"};
    	RequestHandler requestHandler = new RequestHandler(true,frmUser);
    	requestHandler.setArrayKey(key);
    	if(requestHandler.isContainError()){
    		return badRequest(JsonHandler.getSuitableResponse(requestHandler.getErrorMessage(), false));
    	}
    	Laporan laporan = Laporan.finder.byId(requestHandler.getLongValue("laporanId"));
    	User userPemantau = User.finder.byId(requestHandler.getLongValue("userId"));
    	laporan.hapusUserPemantau(userPemantau);
    	Ebean.save(laporan);
    	laporan.pantau = false;
    	return ok(JsonHandler.getSuitableResponse(laporan, true));
    }
    /**
     * list komentar api.
     * requre 
     * authKey, laporanId
     * @return
     */
    public static Result listKomentar(){
    	String key[] = {"laporanId"};
    	RequestHandler requestHandler = new RequestHandler(true,frmUser);
    	requestHandler.setArrayKey(key);
    	if(requestHandler.isContainError()){
    		return badRequest(JsonHandler.getSuitableResponse(requestHandler.getErrorMessage(), false));
    	}
    	List<Komentar> listKomentar = Komentar.finder.where().eq("laporan.id", requestHandler.getStringValue("laporanId")).findList();
    	return ok(JsonHandler.getSuitableResponse(listKomentar, true));
    }
    
    /**
     * insert komentar api.
     * require 
     * authKey,
     * "laporanId", "dataKomentar", "userId"
     * @return
     */
    
    public static Result insertKomentar(){
    	String key[] = {"laporanId", "dataKomentar", "userId"};
    	RequestHandler requestHandler = new RequestHandler(true,frmUser);
    	requestHandler.setArrayKey(key);
    	if(requestHandler.isContainError()){
    		return badRequest(JsonHandler.getSuitableResponse(requestHandler.getErrorMessage(), false));
    	}
    	Komentar komentar = new Komentar();
    	komentar.user = User.finder.byId(requestHandler.getLongValue("userId"));
    	komentar.dataKomentar = requestHandler.getStringValue("dataKomentar");
    	komentar.laporan = Laporan.finder.byId(requestHandler.getLongValue("laporanId"));
    	komentar.laporan.tambahKomentar(komentar);
    	komentar.laporan.update();
    	return ok(JsonHandler.getSuitableResponse(komentar, true));
    }
    /**
     * get user profile api.
     * require 
     * authKey, userId, followerUserId
     * @return
     */
    public static Result getUserProfile(){
    	String key[] = {"userId","followerUserId"};
    	RequestHandler requestHandler = new RequestHandler(true,frmUser);
    	requestHandler.setArrayKey(key);
    	if(requestHandler.isContainError()){
    		return badRequest(JsonHandler.getSuitableResponse(requestHandler.getErrorMessage(), false));
    	}
    	User user = User.finder.byId(requestHandler.getLongValue("userId"));
    	User userFollower = User.finder.byId(requestHandler.getLongValue("followerUserId"));
    	if(user == null){
    		return badRequest(JsonHandler.getSuitableResponse("user not found", false));
    	}
    	
    	user.isFollowing = user.listFollowerUser.contains(userFollower);
    	    
    	return ok(JsonHandler.getSuitableResponse(user, true));
    }
    /**
     * follow api.
     * require
     * authKey,  userId, followerUserId
     * @return
     */
    public static Result follow(){
    	String key[] = {"userId", "followerUserId"};
    	RequestHandler requestHandler = new RequestHandler(true,frmUser);
    	requestHandler.setArrayKey(key);
    	if(requestHandler.isContainError()){
    		return badRequest(JsonHandler.getSuitableResponse(requestHandler.getErrorMessage(), false));
    	}
    	User user = User.finder.byId(requestHandler.getLongValue("userId"));
    	User userFollow = User.finder.byId(requestHandler.getLongValue("followerUserId"));
    	if(user == null || userFollow == null){
    		return badRequest(JsonHandler.getSuitableResponse("user not found", false));
    	}
    	userFollow.tambahFollowerUser(user);
    	user.tambahFollowingUser(userFollow);
    	userFollow.update();
    	return ok(JsonHandler.getSuitableResponse("success follow", true));
    }
    /**
     * unfollow api.
     * require 
     * authKey, userId, followerUserId
     * @return
     */
    public static Result unfollow(){
    	String key[] = {"userId", "followerUserId"};
    	RequestHandler requestHandler = new RequestHandler(true,frmUser);
    	requestHandler.setArrayKey(key);
    	if(requestHandler.isContainError()){
    		return badRequest(JsonHandler.getSuitableResponse(requestHandler.getErrorMessage(), false));
    	}
    	User user = User.finder.byId(requestHandler.getLongValue("userId"));
    	User userFollow = User.finder.byId(requestHandler.getLongValue("followerUserId"));
    	if(user == null || userFollow == null){
    		return badRequest(JsonHandler.getSuitableResponse("user not found", false));
    	}
    	userFollow.hapusFollowerUser(user);
    	userFollow.update();
    	user.hapusFollowingUser(userFollow);
    	user.update();
    	return ok(JsonHandler.getSuitableResponse("success unfollow", true));
    }
    /**
     * change status api
     * require 
     * authKey, userId, status
     * @return
     */
    public static Result changeStatus(){
    	String key[] = {"userId", "status"};
    	RequestHandler requestHandler = new RequestHandler(true,frmUser);
    	requestHandler.setArrayKey(key);
    	if(requestHandler.isContainError()){
    		return badRequest(JsonHandler.getSuitableResponse(requestHandler.getErrorMessage(), false));
    	}
    	User user = User.finder.byId(requestHandler.getLongValue("userId"));
    	if(user == null ){
    		return badRequest(JsonHandler.getSuitableResponse("user not found", false));
    	}
    	user.status = requestHandler.getStringValue("status");
    	user.update();
    	return ok(JsonHandler.getSuitableResponse("success update status", true));
    }
    
    
    public static Result test(){
    	return ok();
    }
    
    /**
     * bisa di jadikan framework
     * @param key
     * @return
     */
    public static ImagePath uploadFile(String key){
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
     * change user prof pict api.
     * require 
     * authKey, userId, picture 
     * @return
     */
    public static Result changeProfilePicture(){
    	String [] key = {"userId"};
    	RequestHandler requestHandler = new RequestHandler(true, frmUser);
    	requestHandler.setArrayKey(key);
    	if(requestHandler.isContainError()){
    		return badRequest(JsonHandler.getSuitableResponse(requestHandler.getErrorMessage(), false));
    	}
    	User user = User.finder.byId(requestHandler.getLongValue("userId"));
    	ImagePath imagePath = ImagePath.setImageFromRequest("picture");
    	if(imagePath == null){
    		return badRequest(JsonHandler.getSuitableResponse("require image", false));
    	}
    	if(!user.imageProfilePath.keterangan.equalsIgnoreCase(IM_DEFAULT_PROFILE)){
    		ImagePath.deleteImage(user.imageProfilePath);
    	}
    	imagePath.keterangan = IM_MODIFED;
    	imagePath.save();
    	user.imageProfilePath = imagePath;
    	user.update();
    	return ok(JsonHandler.getSuitableResponse(user, true));
    }
    
    /**
     * get follower api.
     * require 
     * authKey, userId, mode.
     * mode 0 = follower
     * mode 1 = following
     * @return
     */
    public static Result getFollower(){
    	String key[] = {"userId", "mode"};
    	RequestHandler requestHandler = new RequestHandler(true, frmUser);
    	requestHandler.setArrayKey(key);
    	if(requestHandler.isContainError()){
    		return badRequest(JsonHandler.getSuitableResponse(requestHandler.getErrorMessage(), false));
    	}
    	User user = User.finder.byId(requestHandler.getLongValue("userId"));
    	String mode = requestHandler.getStringValue("mode");
    	if(user == null){
    		return badRequest(JsonHandler.getSuitableResponse("user not found", false));
    	}
    	if(mode.equalsIgnoreCase("follower")){
    		return ok(JsonHandler.getSuitableResponse(user.listFollowerUser, true));	
    	}
    	else {
    		return ok(JsonHandler.getSuitableResponse(user.listFollowingUser, true)); 
    	}
    }
    
    /**
     * view laporan api.
     * require 
     * authKey, laporanId;
     * @return
     */
    public static Result viewLaporan(){
    	String key[] = {"laporanId"};
    	RequestHandler requestHandler = new RequestHandler(true, frmUser);
    	requestHandler.setArrayKey(key);
    	if(requestHandler.isContainError()){
    		return ok(JsonHandler.getSuitableResponse(requestHandler.getErrorMessage(),false));
    	}
    	Laporan laporan = Laporan.finder.byId(requestHandler.getLongValue("laporanId"));
    	laporan.viwer= laporan.viwer.add(new BigInteger("1"));
    	laporan.update();
    	return ok(JsonHandler.getSuitableResponse("success add view", true));
    }
    /**
     * send message api.
     * require 
     * authKey
     * "senderUserId", "receiverUserId",
     * "title", "message"
     * @return
     */
    
    public static Result sendPrivateMessage(){
    	String key [] = {"senderUserId", "receiverUserId", "title", "message"};
    	RequestHandler requestHandler = new RequestHandler(true, frmUser);
    	requestHandler.setArrayKey(key);
    	if(requestHandler.isContainError()){
    		return badRequest(JsonHandler.getSuitableResponse(requestHandler.getErrorMessage(), false));
    	}
    	
    	User userSender = User.finder.byId(requestHandler.getLongValue("senderUserId"));
    	if(userSender == null){
    		return badRequest(JsonHandler.getSuitableResponse("user sender not found", false));
    	}
    	User userReceiver = User.finder.byId(requestHandler.getLongValue("receiverUserId"));
    	if(userReceiver == null){
    		return badRequest(JsonHandler.getSuitableResponse("user receiver not found", false));
    	}
    	
    	PrivateMessage privateMessage = new PrivateMessage();
    	privateMessage.date = Calendar.getInstance();
    	privateMessage.title = requestHandler.getStringValue("title");
    	privateMessage.message = requestHandler.getStringValue("message");
    	privateMessage.userReceiver = userReceiver;
    	privateMessage.userSender = userSender;
    	privateMessage.isRead = false;
    	privateMessage.save();
    	return ok(JsonHandler.getSuitableResponse(privateMessage, true));
    }
    
    /**
     * get list private message api.
     * require 
     * authKey
     * userId, type
     * @return
     */
    public static Result getListPrivateMessage(){
    	String key [] = {"userId", "type"};
    	RequestHandler requestHandler = new RequestHandler(true, frmUser);
    	requestHandler.setArrayKey(key);
    	if(requestHandler.isContainError()){
    		return badRequest(JsonHandler.getSuitableResponse(requestHandler.getErrorMessage(), false));
    	}
    	
    	User userSender = User.finder.byId(requestHandler.getLongValue("userId"));
    	if(userSender == null){
    		return badRequest(JsonHandler.getSuitableResponse("user not found", false));
    	}
    	String type = requestHandler.getStringValue("type");
    	List<PrivateMessage> listPrivateMessage = new ArrayList<>();
    	if(type.equalsIgnoreCase("i")){
    		listPrivateMessage = PrivateMessage.finder.where().eq("user_receiver_id", requestHandler.getStringValue("userId")).findList();
    	}
    	else {
    		listPrivateMessage = PrivateMessage.finder.where().eq("user_sender_id", requestHandler.getStringValue("userId")).findList();
    	}
    	return ok(JsonHandler.getSuitableResponse(listPrivateMessage, true));
    }
    
}

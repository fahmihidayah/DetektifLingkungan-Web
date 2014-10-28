package controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import model_helper.UserHelper;
import models.Auth;
import models.Komentar;
import models.Laporan;
import models.User;
import fahmi.lib.Constants;
import fahmi.lib.JsonHandler;
import fahmi.lib.RequestHandler;
import play.*;
import play.data.Form;
import play.libs.Json;
import play.mvc.*;
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
    	user.save();
    	return ok(JsonHandler.getSuitableResponse(user, true));
    }
    
    public static Result insertLaporan(){
    	String key[] = {"dataLaporan", "userId", "katagoriLaporan", "longitude", "latitude"/*, "time"*/};
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
    	laporan.dataLaporan = requestHandler.getStringValue("dataLaporan");
    	laporan.katagoriLaporan = requestHandler.getStringValue("katagoriLaporan");
    	laporan.longitude = requestHandler.getDoubleValue("longitude");
    	laporan.latitude = requestHandler.getDoubleValue("latitude");
    	laporan.time = Calendar.getInstance();
    	laporan.save();
    	return ok(JsonHandler.getSuitableResponse("Success insert laporan", true));
    }
    /*
     * kekurangan - kurang difilter perbagian untuk diambil.
     * contoh 10 item terupdate berdasarkan tanggal.
     */
    public static Result listLaporan(){
    	List<Laporan> listLaporan = Laporan.finder.all();
    	return ok(JsonHandler.getSuitableResponse(listLaporan, true));
    }
    
    public static Result tambahKomentar(){
    	
    	return ok();
    }
    /**
     * type h, l, f
     * @return
     */
    public static Result getListLaporan(){
    	String key[] = {"idUser","type"};
    	RequestHandler requestHandler = new RequestHandler(true,frmUser);
    	requestHandler.setArrayKey(key);
    	requestHandler.checkError();
    	if(requestHandler.isContainError()){
    		return badRequest(JsonHandler.getSuitableResponse(requestHandler.getErrorMessage(), false));
    	}
    	String type = requestHandler.getStringValue("type");
    	Laporan laporan = null;
    	if(!type.equalsIgnoreCase("f")){
    		laporan = Laporan.finder.byId(requestHandler.getOptionalLongValue("idLaporan"));
    		if(laporan == null){
    			return badRequest(JsonHandler.getSuitableResponse("laporan not found", false));
    		}
    	}
    	User user = User.finder.byId(requestHandler.getLongValue("idUser"));
    	
    	List<Laporan> listUpdateLaporan = null; 
    	
    	if(type.equalsIgnoreCase("h")){
    		listUpdateLaporan = Laporan.finder.where().gt("time", laporan.time.getTime()).order("time desc").findList();
    	}
    	else if (type.equalsIgnoreCase("l")){
    		listUpdateLaporan = Laporan.finder.where().lt("time", laporan.time.getTime()).order("time desc").setMaxRows(2).findList();
    	}
    	else {
    		listUpdateLaporan = Laporan.finder.where().order("time desc").setMaxRows(5).findList();
    	}
    	
    	for (Laporan eLaporan : listUpdateLaporan) {
			if(eLaporan.listUserPemantau.contains(user)){
				eLaporan.pantau = true;
			}
		}
    	
    	return ok(JsonHandler.getSuitableResponse(listUpdateLaporan, true));
    }
    
    public static Result pantau(){
    	String key[] = {"idUser", "idLaporan"};
    	RequestHandler requestHandler = new RequestHandler(true, frmUser);
    	requestHandler.setArrayKey(key);
    	if(requestHandler.isContainError()){
    		return badRequest(JsonHandler.getSuitableResponse(requestHandler.getErrorMessage(), false));
    	}
    	Laporan laporan = Laporan.finder.byId(requestHandler.getLongValue("idLaporan"));
    	User userPemantau = User.finder.byId(requestHandler.getLongValue("idUser"));
    	laporan.tambahUserPemantau(userPemantau);
    	laporan.update();
    	return ok(JsonHandler.getSuitableResponse("laporan dipantau", true));
    }
    public static Result unpantau(){
    	String key[] = {"idUser", "idLaporan"};
    	RequestHandler requestHandler = new RequestHandler(true,frmUser);
    	requestHandler.setArrayKey(key);
    	if(requestHandler.isContainError()){
    		return badRequest(JsonHandler.getSuitableResponse(requestHandler.getErrorMessage(), false));
    	}
    	Laporan laporan = Laporan.finder.byId(requestHandler.getLongValue("idLaporan"));
    	User userPemantau = User.finder.byId(requestHandler.getLongValue("idUser"));
    	laporan.hapusUserPemantau(userPemantau);
    	laporan.update();
    	return ok(JsonHandler.getSuitableResponse("laporan dipantau", true));
    }
    
    
    public static Result listKomentar(){
    	String key[] = {"idLaporan"};
    	RequestHandler requestHandler = new RequestHandler(true,frmUser);
    	requestHandler.setArrayKey(key);
    	if(requestHandler.isContainError()){
    		return badRequest(JsonHandler.getSuitableResponse(requestHandler.getErrorMessage(), false));
    	}
    	List<Komentar> listKomentar = Komentar.finder.where().eq("laporan.id", requestHandler.getStringValue("idLaporan")).findList();
    	return ok(JsonHandler.getSuitableResponse(listKomentar, true));
    }
    
    public static Result insertKomentar(){
    	String key[] = {"idLaporan", "dataKomentar", "idUser"};
    	RequestHandler requestHandler = new RequestHandler(true,frmUser);
    	requestHandler.setArrayKey(key);
    	if(requestHandler.isContainError()){
    		return badRequest(JsonHandler.getSuitableResponse(requestHandler.getErrorMessage(), false));
    	}
    	Komentar komentar = new Komentar();
    	komentar.user = User.finder.byId(requestHandler.getLongValue("idUser"));
    	komentar.dataKomentar = requestHandler.getStringValue("dataKomentar");
    	komentar.laporan = Laporan.finder.byId(requestHandler.getLongValue("idLaporan"));
    	komentar.laporan.tambahKomentar(komentar);
    	komentar.laporan.update();
    	return ok(JsonHandler.getSuitableResponse(komentar, true));
    }
    
    public static Result getUserProfile(){
    	String key[] = {"idUser","idUserFollower"};
    	RequestHandler requestHandler = new RequestHandler(true,frmUser);
    	requestHandler.setArrayKey(key);
    	if(requestHandler.isContainError()){
    		return badRequest(JsonHandler.getSuitableResponse(requestHandler.getErrorMessage(), false));
    	}
    	User user = User.finder.byId(requestHandler.getLongValue("idUser"));
    	User userFollower = User.finder.byId(requestHandler.getLongValue("idUserFollower"));
    	if(user == null){
    		return badRequest(JsonHandler.getSuitableResponse("user not found", false));
    	}
    	UserHelper helper = new UserHelper();
    	helper.id = user.id;
    	helper.email = user.email;
    	helper.isFollowing = user.listFollowerUser.contains(userFollower);
    	helper.jumlahFollowerUser = user.jumlahFollowerUser;
    	helper.jumlahFollowingUser = user.jumlahFollowingUser;
    	helper.name = user.name;
    	helper.password = user.password;
    	helper.status = user.status;
    	helper.type = user.type;
    	helper.userName = user.userName;
    	return ok(JsonHandler.getSuitableResponse(helper, true));
    }
    
    public static Result follow(){
    	String key[] = {"idUser", "idUserFollow"};
    	RequestHandler requestHandler = new RequestHandler(true,frmUser);
    	requestHandler.setArrayKey(key);
    	if(requestHandler.isContainError()){
    		return badRequest(JsonHandler.getSuitableResponse(requestHandler.getErrorMessage(), false));
    	}
    	User user = User.finder.byId(requestHandler.getLongValue("idUser"));
    	User userFollow = User.finder.byId(requestHandler.getLongValue("idUserFollow"));
    	if(user == null || userFollow == null){
    		return badRequest(JsonHandler.getSuitableResponse("user not found", false));
    	}
    	userFollow.tambahFollowerUser(user);
    	userFollow.update();
    	return ok(JsonHandler.getSuitableResponse("success follow", true));
    }
    
    public static Result unfollow(){
    	String key[] = {"idUser", "idUserFollow"};
    	RequestHandler requestHandler = new RequestHandler(true,frmUser);
    	requestHandler.setArrayKey(key);
    	if(requestHandler.isContainError()){
    		return badRequest(JsonHandler.getSuitableResponse(requestHandler.getErrorMessage(), false));
    	}
    	User user = User.finder.byId(requestHandler.getLongValue("idUser"));
    	User userFollow = User.finder.byId(requestHandler.getLongValue("idUserFollow"));
    	if(user == null || userFollow == null){
    		return badRequest(JsonHandler.getSuitableResponse("user not found", false));
    	}
    	userFollow.hapusFollowerUser(user);
    	userFollow.update();
    	return ok(JsonHandler.getSuitableResponse("success unfollow", true));
    }
    
    public static Result changeStatus(){
    	String key[] = {"idUser", "status"};
    	RequestHandler requestHandler = new RequestHandler(true,frmUser);
    	requestHandler.setArrayKey(key);
    	if(requestHandler.isContainError()){
    		return badRequest(JsonHandler.getSuitableResponse(requestHandler.getErrorMessage(), false));
    	}
    	User user = User.finder.byId(requestHandler.getLongValue("idUser"));
    	if(user == null ){
    		return badRequest(JsonHandler.getSuitableResponse("user not found", false));
    	}
    	user.status = requestHandler.getStringValue("status");
    	user.update();
    	return ok(JsonHandler.getSuitableResponse("success update status", true));
    }
    
    public static Result test(){
//    	String key[] = {"idUser", "idUserFollow"};
//    	RequestHandler requestHandler = new RequestHandler(true,frmUser);
//    	requestHandler.setArrayKey(key);
//    	if(requestHandler.isContainError()){
//    		return badRequest(JsonHandler.getSuitableResponse(requestHandler.getErrorMessage(), false));
//    	}
//    	User user = User.finder.byId(requestHandler.getLongValue("idUser"));
//    	User userFollow = User.finder.byId(requestHandler.getLongValue("idUserFollow"));
//    	if(user == null || userFollow == null){
//    		return badRequest(JsonHandler.getSuitableResponse("user not found", false));
//    	}
//    	userFollow.tambahFollowerUser(user);
//    	return ok(JsonHandler.getSuitableResponse(user, true));
    	return ok();
    }
}

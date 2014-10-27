package controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import models.Auth;
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
    
    public static Result getListLaporan(){
    	String key[] = {"idLaporan", "type"};
    	RequestHandler requestHandler = new RequestHandler(true,frmUser);
    	requestHandler.setArrayKey(key);
    	requestHandler.checkError();
    	if(requestHandler.isContainError()){
    		return badRequest(JsonHandler.getSuitableResponse(requestHandler.getErrorMessage(), false));
    	}
    	Laporan laporan = Laporan.finder.byId(requestHandler.getLongValue("idLaporan"));
    	String type = requestHandler.getStringValue("type");
    	List<Laporan> listUpdateLaporan; 
    	
    	if(type.equalsIgnoreCase("h")){
    		listUpdateLaporan = Laporan.finder.where().gt("time", laporan.time.getTime()).order("time desc").findList();
    	}
    	else {
    		listUpdateLaporan = Laporan.finder.where().lt("time", laporan.time.getTime()).order("time desc").setMaxRows(2).findList();
    	}
    	return ok(JsonHandler.getSuitableResponse(listUpdateLaporan, true));
    }
    
    
    
    public static Result test(){
    	String key[] = {"idLaporan"};
    	RequestHandler requestHandler = new RequestHandler(frmUser);
    	requestHandler.setArrayKey(key);
    	requestHandler.checkError();
    	if(requestHandler.isContainError()){
    		return badRequest(JsonHandler.getSuitableResponse(requestHandler.getErrorMessage(), false));
    	}
    	Laporan laporan = Laporan.finder.byId(requestHandler.getLongValue("idLaporan"));
    	List<Laporan> listUpdateLaporan = Laporan.finder.where().lt("time", laporan.time.getTime()).order("time desc").setMaxRows(2).findList();
    	return ok(JsonHandler.getSuitableResponse(listUpdateLaporan, true));
    }
}

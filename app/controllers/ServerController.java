package controllers;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

//import org.apache.commons.io.FileUtils;



import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import models.Auth;
import models.ImagePath;
import models.Komentar;
import models.Laporan;
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
 */
public class ServerController extends Controller implements Constants {
	public static Form<ServerAddress> form = Form.form(ServerAddress.class);
	public static Result setserver(){
		String [] key  = {"serverAddress"};
		RequestHandler requestHandler = new RequestHandler(form);
		requestHandler.setArrayKey(key);
		if(requestHandler.isContainError()){
			return badRequest(JsonHandler.getSuitableResponse(requestHandler.getErrorMessage(), false));
		}
		ServerAddress serverAddress = new ServerAddress();
		serverAddress.address = requestHandler.getStringValue("serverAddress");
		serverAddress.save();
		return ok(JsonHandler.getSuitableResponse("success insert", true));
	}
	
	public static Result changeServerAddress(){
		String [] key  = {"serverAddress", "idServer"};
		RequestHandler requestHandler = new RequestHandler(form);
		requestHandler.setArrayKey(key);
		if(requestHandler.isContainError()){
			return badRequest(JsonHandler.getSuitableResponse(requestHandler.getErrorMessage(), false));
		}
		ServerAddress serverAddress = ServerAddress.finder.byId(requestHandler.getLongValue("idServer"));
		if(serverAddress == null){
			serverAddress = new ServerAddress();
			serverAddress.address = requestHandler.getStringValue("serverAddress");
			serverAddress.save();
		}
		else {	
			serverAddress.address = requestHandler.getStringValue("serverAddress");
			serverAddress.update();	
		}
		
		return ok(JsonHandler.getSuitableResponse("success change server", true));
	}
	
	public static Result setDefaultProfilePicture(){
		ImagePath imagePath = ImagePath.setImageFromRequest("picture");
    	if(imagePath == null){
    		return badRequest(JsonHandler.getSuitableResponse("error insert image", false));
    	}
    	imagePath.save();
    	return ok(JsonHandler.getSuitableResponse(imagePath, true));
	}
	
}

package controllers;

import models.User;
import fahmi.lib.JsonHandler;
import fahmi.lib.RequestHandler;
import play.*;
import play.data.Form;
import play.mvc.*;
import views.html.*;

public class BackEndUserController extends Controller {
	public static Form<User> frmUser = Form.form(User.class);
    public static Result index() {
        return ok(index.render("Your new application is ready."));
    }
    
    public static Result login(){
    	return ok();
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
}

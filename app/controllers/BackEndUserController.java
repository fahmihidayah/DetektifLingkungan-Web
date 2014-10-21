package controllers;

import com.sun.jna.platform.win32.Netapi32Util.User;

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
    	return ok();
    }
}

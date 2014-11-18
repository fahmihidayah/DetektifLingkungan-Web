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
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import views.html.*;

/**
 * 
 * @author fahmi
 */
public class TestController extends Controller implements Constants {

	public static Result upload() {
		MultipartFormData body = request().body().asMultipartFormData();
		FilePart picture = body.getFile("picture");
		if (picture != null) {
			String fileName = picture.getFilename();
			String contentType = picture.getContentType();
			File file = picture.getFile();
			file.renameTo(new File(Play.application().configuration()
					.getString("myUploadPath"), fileName));
			return ok("File uploaded");
		} else {
			flash("error", "Missing file");
			return ok("ok");
		}
	}
}

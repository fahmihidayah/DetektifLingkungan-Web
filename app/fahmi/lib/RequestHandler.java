package fahmi.lib;

import java.util.HashMap;
import java.util.Map;

import models.Auth;
import play.data.Form;

public class RequestHandler implements Constants {
	private boolean checkAuth = false;
	private Form form;
	/**
	 * ini diganti dengan map. format :
	 * map.put("key-atau-field", "1-atau-0");
	 * 1 wajib diisi 0 optional
	 */
	private String[] arrayKey;
	private Map<String, Object> result = null;

	public RequestHandler(Form form) {
		super();
		this.form = form.bindFromRequest();
	}

	public String[] getArrayKey() {
		return arrayKey;
	}

	public void setArrayKey(String[] arrayKey) {
		this.arrayKey = arrayKey;
		checkError();
	}

	public RequestHandler(boolean checkAuth, Form form) {
		super();
		this.checkAuth = checkAuth;
		this.form = form.bindFromRequest();
	}

	public String findAuth(Map map) {
		String auth_key = (String) map.get(AUTH_KEY);
		if (auth_key == null) {
			return AUTH_NOT_FOUND;
		}
		Auth auth = Auth.findAuth(auth_key);
		if (auth == null) {
			return USER_NOT_FOUND;
		}
		return SUCCESS;
	}

	public void checkError() {
		result = new HashMap<String, Object>();
		Map<String, Object> data = form.bindFromRequest().data();
		if (checkAuth) {
			String errorCheckAuth = findAuth(data);
			if (!errorCheckAuth.equalsIgnoreCase(SUCCESS)) {
				result.put(ERROR, errorCheckAuth);
				return;
			}
		}

		for (int i = 0; i < arrayKey.length; i++) {
			String key = arrayKey[i];
			if (!data.containsKey(key)) {
				result.put(ERROR, "Require " + key);
				return;
			}
			result.put(key, data.get(key));
		}
	}

	public boolean isContainError() {
		if (result == null) {
			return false;
		}
		return result.containsKey(ERROR);
	}

	public String getErrorMessage() {
		if (result == null) {
			return "Server internal error, you not call check Error method yet";
		}
		if (!result.containsKey(ERROR)) {
			return "There is no error";
		}
		return result.get(ERROR).toString();
	}

	public String getStringValue(String key) {
		return result.get(key).toString();
	}

	public Long getLongValue(String key) {
		return Long.parseLong(result.get(key).toString());
	}
	
	public Double getDoubleValue(String key){
		return Double.parseDouble(result.get(key).toString());
	}
	
	public String getOptionalStringValue(String key){
		return form.data().get(key).toString();
	}
	public Long getOptionalLongValue(String key){
		return Long.parseLong(form.data().get(key).toString());
	}
}

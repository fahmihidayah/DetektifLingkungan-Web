package fahmi.lib;

import java.util.HashMap;
import java.util.Map;
/**
 * 
 * @author fahmi
 * masih dalam tahap riset diperlukan atau tidak
 */
public class ParamterHandler {
	private Map<String, String> parameter = new HashMap<>();
	public static String MUST = "1";
	public static String NOT_MUST = "0";
	
	public void putMustParameter(String parameterStr){
		parameter.put(parameterStr, MUST);
	}
	
	public void putNotMustParameter(String parameterStr){
		parameter.put(parameterStr, NOT_MUST);
	}
	
	
}

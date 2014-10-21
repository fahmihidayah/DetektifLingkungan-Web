package fahmi.lib;

import play.data.Form;
import play.libs.Json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonHandler {

    public static JsonNode getSuitableResponse(Object data, boolean success){
    	ObjectNode node = (success? getSuccessObjectNode(): getFailureObjectNode());
    	if(data instanceof Form){
    		node.put("data", getErrorMessage(((Form) data)));
    	}
    	else if( data instanceof String){
    		node.put("data", (String) data);
    	}
    	else {
    		node.put("data", Json.toJson(data));
    	}
    	return Json.toJson(node);
    }
    
    private static ObjectNode getSuccessObjectNode(){
    	ObjectNode node = Json.newObject();
    	node.put("status", "200");
    	node.put("message", "success");
    	return node;
    }
    private static ObjectNode getFailureObjectNode(){
    	ObjectNode node = Json.newObject();
    	node.put("status", "404");
    	node.put("message", "error");
    	return node;
    }
    private static String getErrorMessage(Form errorForm){
    	String error = errorForm.errorsAsJson().toString();
    	error = error.replace("{", "");
    	error = error.replace("]", "");
    	error = error.replace("[", "");
    	error = error.replace("}", "");
    	error = error.replace("\"", "");
    	return error;
    }
}

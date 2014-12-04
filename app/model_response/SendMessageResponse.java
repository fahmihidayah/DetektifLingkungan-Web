package model_response;

import java.util.ArrayList;

/**
 * 
 * @author fahmi
 *
 *{
  "multicast_id": 7617842188201294163,
  "success": 1,
  "failure": 0,
  "canonical_ids": 0,
  "results": [
    {
      "message_id": "0:1416838048058435%20e472c0f9fd7ecd"
    }
  ]
}
 *
 */
public class SendMessageResponse {
	private String multicast_id;
	private Integer success;
	private Integer failure;
	private Integer canonical_ids;
	private ArrayList<MessageId> results;
	
	public class MessageId{
		private String message_id;

		public String getMessage_id() {
			return message_id;
		}

		public void setMessage_id(String message_id) {
			this.message_id = message_id;
		}
		
	}

	public String getMulticast_id() {
		return multicast_id;
	}

	public void setMulticast_id(String multicast_id) {
		this.multicast_id = multicast_id;
	}

	public Integer getSuccess() {
		return success;
	}

	public void setSuccess(Integer success) {
		this.success = success;
	}

	public Integer getFailure() {
		return failure;
	}

	public void setFailure(Integer failure) {
		this.failure = failure;
	}

	public Integer getCanonical_ids() {
		return canonical_ids;
	}

	public void setCanonical_ids(Integer canonical_ids) {
		this.canonical_ids = canonical_ids;
	}

	public ArrayList<MessageId> getResults() {
		return results;
	}

	public void setResults(ArrayList<MessageId> results) {
		this.results = results;
	}
	
	
}

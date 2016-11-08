package transaction;

import org.json.*;

public class Transaction {


	/*
	 * To be added 
	 * 			Timestamp 
	 * 			sender ip address
	 * 			receiver ip address 
	 */
	
	private int txType;
	private String txInput;
	private String txOutput;

	public Transaction(final int txType, String txInput, String txOutput){
		this.txType = txType;
		this.txInput = txInput;
		this.txOutput = txOutput;
		
	}
	
	public static JSONObject txToJSON(Transaction tx) throws JSONException{
		JSONObject txJSON = new JSONObject();
		txJSON.put("txType", tx.txType);
		txJSON.put("txInput", tx.txInput);
		txJSON.put("txOutput", tx.txOutput);
		
		return txJSON; 
	}
	
	
}

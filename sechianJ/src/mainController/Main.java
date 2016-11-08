package mainController;


import org.json.JSONException;
import org.json.JSONObject;

import crypto.KeyGenerator;
import transaction.Transaction;

public class Main {
	
	static JSONObject txJSON = new JSONObject();
	
	public static void main(String args[]){
		
		System.out.println("Start");
		
		KeyGenerator.getKeyGen();
		Transaction tx = new Transaction(1, "transaction input", "transaction output");
		
		try {
			
			txJSON = Transaction.txToJSON(tx);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String jsonTx = txJSON.toString();
		
		System.out.println(jsonTx);
	}
}



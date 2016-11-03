package transaction;

public class Transaction {


	private int txType;
	private byte[] txInput;
	private byte[] txOutput;
//	public enum transactionType{
//		NORMAL, SMART_CONTRACT, TEST
//	}
	
	
	public Transaction(final int txType, byte[] txInput, byte[] txOutput){
		this.txType = txType;
		this.txInput = txInput;
		this.txOutput = txOutput;
		
	}
	
	
}

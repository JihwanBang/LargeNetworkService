package bin;

public class MessageProtocol{
	private int clientID;
	private int sequence;
	private short command;
	private short code;
	private short keyLength;
	private short valueLength;
	private String key;
	private String value;

	public MessageProtocol(int clientID, int sequence, String command, String code, String key, String value){
		this.clientID = clientID; 
		this.sequence = sequence;
		this.key = key;
		this.value = value;
		this.keyLength = (short) key.length();
		this.valueLength = (short) value.length();

		if (command.equals("put")){
			this.command = 1;
		}
		else if (command.equals("put-ack")){
			this.command = 2;
		}
		else if (command.equals("get")){
			this.command = 3;
		}
		else if (command.equals("get-ack")){
			this.command = 4;
		}
		else if (command.equals("del")){
			this.command = 5;
		}
		else if (command.equals("del-ack")){
			this.command = 6;
		}
		else{
			System.out.println("command usage : [put, put-ack, get, get-ack, del, del-ack]");
		}

		if (code.equals("none")){
			this.code = 0;
		}
		else if (code.equals("success")){
			this.code = 1;
		}
		else if (code.equals("not exist")){
			this.code = 2;
		}
		else if (code.equals("already exist")){
			this.code = 3;
		}
		else{
			System.out.println("code usage : [none, success, not exist, already exist]");
		}

	}
	

}
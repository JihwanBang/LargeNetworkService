package bin;

import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;

public class MessageProtocol implements Serializable{
	public int clientID=-1;
	public int sequence=-1;
	public short command=-1;
	public short code=-1;
	private short keyLength = 0;
	private short valueLength=0;
	public String key="" ;
	public String value="" ;

	public void messageUpdate(int clientID, int sequence, short command, short code, String key, String value){
		this.clientID = clientID; 
		this.sequence = sequence;
		this.command = command;
		this.code = code;
		this.key = key;
		this.value = value;
		this.keyLength = (short) key.length();
		this.valueLength = (short) value.length();


	}

	public void send(Socket socket) throws IOException{
		ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
		oos.writeObject(this);
	}
	
	public void receive(Socket socket) throws IOException, ClassNotFoundException{
		ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
		MessageProtocol msg = (MessageProtocol) ois.readObject();
		messageUpdate(msg.clientID, msg.sequence, msg.command, msg.code, msg.key, msg.value);
	}

	public void ackProcess(Socket socket){
		if(this.code == (short) 1){ //success
			System.out.println("Success");
		}
		else if (this.code == (short) 2){ //not exist
			System.out.println("Fail (reason: not exist)");
		}
		else if (this.code == (short) 3){ //already exist
			System.out.println("Fail (reason : already exist");
		}
		else{
			System.out.println("Wrong ACK!");
		}

	}

	public void replyAck(Socket socket) throws IOException{
		if (this.command == (short) 1){
			System.out.println("put success!");
			reply(socket,(short) 2, (short) 1, "", "");
		}
		else if (this.command == (short) 3){
			System.out.println("get success!");
			reply(socket,(short) 4, (short) 1, this.key, "");
		}
		else if (this.command == (short) 5){
			System.out.println("del success!");
			reply(socket,(short) 6, (short) 1, this.key, "");
		}
		else {
			System.out.println("wrong command!");
			reply(socket,(short) -1, (short) -1, "", "");
		}
	}

	private void reply(Socket socket,short cmd, short code, String key, String value) throws IOException{
		MessageProtocol messageSend = new MessageProtocol();
		messageSend.messageUpdate(this.clientID, this.sequence, cmd, code, key, value);
		messageSend.send(socket);		
	}
		
	public void print(){
		System.out.println(this.clientID + " " + this.sequence + " " + this.command + " " + this.code + " " + this.keyLength + " " + this.valueLength + 
			" " + this.key + " "+ this.value);
	}

}
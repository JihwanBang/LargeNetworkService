/*
	Network System and Security Assignment #1 
	Due date : April 7th, 2017
	Author : Jihwan Bang
*/
package bin;

import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.util.Date;
import java.text.SimpleDateFormat;
/*
	This class is used when communicate amongs clients, loadbalance, and handlers.
	componenets : clientID, sequence, command, code, keyLength, valueLength, key, value 
*/
public class MessageProtocol implements Serializable{
	public int clientID=-1;
	public int sequence=-1;
	public short command=-1;
	public short code=-1;
	private short keyLength = 0;
	private short valueLength=0;
	public String key="" ;
	public String value="" ;
	/*
	Usage : Update the message 
	Input : 
		clientID 	clientID in a message
		sequence 	sequence in a message 
		command 	command in a message
		code 		code in a message 
		key 		key in a message
		value 		value in a message 
	Output 
		none 
	*/
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
	/* 
	Usage : send the message through socket 
	Input 	
		socket 		socket 
	Output 
		none
	*/
	public void send(Socket socket) throws IOException{
		ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
		oos.writeObject(this);
	}
	/*
	Usage : receive the message through socket 
	Input 
		socket 		socket 
	Output 
		none
	*/
	public void receive(Socket socket) throws IOException, ClassNotFoundException{
		ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
		MessageProtocol msg = (MessageProtocol) ois.readObject();
		messageUpdate(msg.clientID, msg.sequence, msg.command, msg.code, msg.key, msg.value);
	}

	/*
	Usage : process the ack when ack is received 
	Input 
		socket 		socket 
	Output 
		none 
	*/
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

	/*
	Usage : log all the elements of a message and time.
	Input 
		fw 		Filewriter 
	Output  
		none 
	*/
	public void print(FileWriter fw) throws IOException{
		long time = System.currentTimeMillis(); 
		SimpleDateFormat dayTime = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
		String str = dayTime.format(new Date(time));

		fw.write(String.format("%s %d %d %d %d %d %d %s %s\n", str, this.clientID, this.sequence, this.command,
			this.code, this.keyLength, this.valueLength, this.key, this.value));
		fw.flush();
		//System.out.println(this.clientID + " " + this.sequence + " " + this.command + " " + this.code + " " + this.keyLength + " " + this.valueLength + 
		//	" " + this.key + " "+ this.value);
	}

}




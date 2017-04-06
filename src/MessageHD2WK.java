/*
	Network System and Security Assignment #1 
	Due date : April 7th, 2017
	Author : Jihwan Bang
*/
package bin;

import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.util.Date;
import java.text.SimpleDateFormat;


/*
Usage : When handlers send to workers or workers send to handlers, they do not need to follow 
MeesageProtocol. Therefore, this class is made for protocol between handlers and workers.
*/
public class MessageHD2WK implements Serializable{
	int command;
	int hashValue;
	String key;
	String value;
	/*
	Usage : message data update 
	Input 
		command 	"put" : 1, "get" :3, "del" :5
		hashValue 	hash value of key 
		key 		message key 
		value 		message value 
	Output 
		none
	*/
	public void messageUpdate(int command, int hashValue, String key, String value){
		this.command = command;
		this.hashValue = hashValue;
		this.key = key;
		this.value = value;
	}
	/*
	Usage : send an object(MessageHD2WK) through socket 
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
	Usage : receive an object(MessageHD2WK) through socket. receiving message is stored
	in current messageHD2WK. 
	Input 
		socket 		socket
	Output 
		none 
	*/
	public void receive(Socket socket) throws IOException, ClassNotFoundException{
		ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
		MessageHD2WK msg = (MessageHD2WK) ois.readObject();
		messageUpdate(msg.command, msg.hashValue, msg.key, msg.value);
	}
	/*
	Usage : log all elements of an object(MessageHD2WK) and time .
	Input 
		fw 		File writer 
	Output 
		none
	*/
	public void print(FileWriter fw) throws IOException{
		long time = System.currentTimeMillis(); 
		SimpleDateFormat dayTime = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
		String str = dayTime.format(new Date(time));
		fw.write(String.format("%s %d %s %s\n", str, hashValue, key, value));
		fw.flush();
	}
	
}

package bin;

import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;



public class MessageHD2WK implements Serializable{
	int hashValue;
	String key;
	String value;

	public void messageUpdate(int hashValue, String key, String value){
		this.hashValue = hashValue;
		this.key = key;
		this.value = value;
	}

	public void send(Socket socket) throws IOException{
		ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
		oos.writeObject(this);
	}

	public void receive(Socket socket) throws IOException, ClassNotFoundException{
		ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
		MessageHD2WK msg = (MessageHD2WK) ois.readObject();
		messageUpdate(msg.hashValue, msg.key, msg.value);
	}
	public void print(){
		System.out.println(String.format("%d %s %s",hashValue, key, value));
	}
	
}
/*
	Network System and Security Assignment #1 
	Due date : April 7th, 2017
	Author : Jihwan Bang
*/
package bin;

import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.IOException;
import java.io.File;
import java.io.FileWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import bin.MessageProtocol;
import bin.Client;

/*
	This is for Handler0. we use 2 threads which are HDCLI, which is CLI handler, and Synchronization, which
	is a server for receiving updated message from other handlers. 
*/
public class Handler0{
	public static void main(String[] args){
		int num = 0;
		Thread cli = new HDCLI(num);
		cli.start();
		HDsync sync = new HDsync();
		Thread syncServer = new Synchronization(sync, num);
		syncServer.start();
		handlerConnection(sync, num);
			
	}
	/*
	Usage : handle the received messages. It includes that it's processed case by case("put", "get", "del")
	Input 
		sync 	class that includes syncList 
		num 	worker ID  
	Output 
		none 
	*/
	public static void handlerConnection(HDsync sync,int num){
		try{
			File file = new File(String.format("Handler_%d.log",num));
			FileWriter fw = new FileWriter(file);

			ServerSocket listener = new ServerSocket(10000+num);
			try{
				while(true){
					Socket socket = listener.accept();
					try{
						MessageProtocol messageRecv = new MessageProtocol();
						messageRecv.receive(socket);
						
						messageRecv.print(fw);
						
						int hashValue = joaat_hash(messageRecv.key.getBytes());
						System.out.println(String.format("hashValue remain %d", (hashValue%5 +5)%5));	


						if (messageRecv.command == 1){
							put(socket, hashValue, num, sync, messageRecv, fw);
						}
						else if (messageRecv.command == 3){
							get(socket, hashValue, sync, messageRecv, fw);
						}
						else if (messageRecv.command == 5){
							del(socket, hashValue, num, sync, messageRecv, fw);
						}

						

					}
					catch (ClassNotFoundException c){
						c.printStackTrace();
					}
								
				}	
			}
			finally{
				listener.close();
			}	
			

		}
		catch (IOException e){
			e.printStackTrace();
		}

		
	}
	/*
	Usage : jenkins hash function 
	hash fuction reference : https://en.wikibooks.org/wiki/Algorithm_Implementation/Hashing#Jenkins_one-at-a-time_hash
	*/
	private static int joaat_hash(byte[] key) {
	    int hash = 0;

	    for (byte b : key) {
	        hash += (b & 0xFF);
	        hash += (hash << 10);
	        hash ^= (hash >>> 6);
	    }
	    hash += (hash << 3);
	    hash ^= (hash >>> 11);
	    hash += (hash << 15);
	    return hash;
	}
	/*
	Usage : when receive "put" command, synchronize their syncList among handlers and send an message to a 
	specific worker. After that, it replys ack to a LoadBalance 
	Input 	
		socketLB 	socket between handler and LoadBalance 
		hashValue	hashValue of message key 
		num 		handler ID 
		sync 		class including syncList 
		msg 		received message from LoadBalance 
	Output 
		none
	*/
	private static void put(Socket socketLB, int hashValue, int num, HDsync sync, MessageProtocol msg,
		FileWriter fw) throws ClassNotFoundException, IOException, UnknownHostException{
		boolean success = true;
		for (int i=0; i<sync.syncList.size(); i++){
			if (sync.syncList.get(i).hashValue == hashValue){
				success = false;
			}
		}
		if (success){
			HDelement elem = new HDelement();
			elem.msgUpdate("put", hashValue, (hashValue%5 +5)%5);
			sync.append(elem);
			
			Socket socket1 = new Socket("127.0.0.1",5000+(num+1)%3);
			elem.send(socket1);
			Socket socket2 = new Socket("127.0.0.1", 5000+(num+2)%3);
			elem.send(socket2);
			

			handler2WK(1, hashValue, msg.key, msg.value, fw);
	
		}
		
		
		MessageProtocol reply = new MessageProtocol();
		if (success){
			reply.messageUpdate(msg.clientID, msg.sequence, (short) 2, (short) 1, "", "");
		}

		else {
			reply.messageUpdate(msg.clientID, msg.sequence, (short) 2, (short) 3, "", "");	
		}

		reply.send(socketLB);
	
		
	}
	/*
	Usage : when receive "get" command, request value to a specific worker. Also, reply ack to the LoadBalance.
	Input 
		socketLB 	socket between current handler and LoadBalance 
		hashValue 	hash value of message key 
		sync 		HDsync including syncList 
		msg 		received message from LoadBalane 
	Output 
		none 
	*/
	private static void get(Socket socketLB, int hashValue, HDsync sync, MessageProtocol msg,
		FileWriter fw) throws ClassNotFoundException, UnknownHostException, IOException{
		boolean success = false;
		for (int i=0; i<sync.syncList.size(); i++){
			if (sync.syncList.get(i).hashValue == hashValue){
				success = true;
				break;
			}
		}
		String value="";
		if (success){
			value = handler2WK(3, hashValue, msg.key, msg.value, fw); 
		}
		
		MessageProtocol reply = new MessageProtocol();
		if (success){
			reply.messageUpdate(msg.clientID, msg.sequence, (short)4, (short) 1, msg.key, value);
		}
		else {
			reply.messageUpdate(msg.clientID, msg.sequence, (short)4, (short) 2, "", "");
		}
		reply.send(socketLB);
		
	}
	/*
	Usage : when receiving "del" command, synchronize their syncList among handlers and send an message to a 
	specific worker. After that, it replys ack to a LoadBalance.
	Input 	
		socketLB 	socket between handler and LoadBalance 
		hashValue	hashValue of message key 
		num 		handler ID 
		sync 		class including syncList 
		msg 		received message from LoadBalance 
	Output 
		none	
	*/
	private static void del(Socket socketLB, int hashValue, int num, HDsync sync, MessageProtocol msg, 
		FileWriter fw) throws ClassNotFoundException, UnknownHostException, IOException{
		
		boolean success = false; 
		for (int i=0; i<sync.syncList.size(); i++){
			if (sync.syncList.get(i).hashValue == hashValue){
				success = true; 
				sync.syncList.remove(sync.syncList.get(i));
				break;
			}
		}
		
		if (success){
			HDelement elem = new HDelement();
			elem.msgUpdate("del", hashValue, (hashValue%5 +5)%5);

			Socket socket1= new Socket("127.0.0.1", 5000+(num+1)%3);
			elem.send(socket1);
			Socket socket2 = new Socket("127.0.0.1", 5000+(num+2)%3);
			elem.send(socket2);

			handler2WK(5, hashValue, msg.key, msg.value, fw);
		}

		MessageProtocol reply = new MessageProtocol();
		if (success){
			reply.messageUpdate(msg.clientID, msg.sequence, (short)6, (short) 1, "", "");
		}
		else {
			reply.messageUpdate(msg.clientID, msg.sequence, (short)6, (short) 2, "", "");
		}
		reply.send(socketLB);
	}
	/*
	Usage : handler sends a message to a worker and after that, it receives a message from that worker.
	Input 
		command 	command 
		hashValue 	hash value of message key 
		key 		message key 
		value 		message value 
	Output 
		value 		message value corresponding the key in a specific worker list (It's only used when command
					is "get")
	*/
	private static String handler2WK(int command, int hashValue, String key, String value,
		FileWriter fw) throws ClassNotFoundException{
		
		try{
			Socket socket = new Socket("127.0.0.1", 20000 + (hashValue%5+5)%5);
			MessageHD2WK msg = new MessageHD2WK();
			msg.messageUpdate(command, hashValue, key, value);
			msg.send(socket);
			MessageHD2WK ack = new MessageHD2WK();
			ack.receive(socket);
			ack.print(fw);
			return ack.value;
		}
		catch (IOException e){
			e.printStackTrace();
		}
		return "";
	}

}




/*
Usage : When typing some commands in handler CLI (such as "list", "show"), it can deal with the commands that 
we typed. 
*/
class HDCLI extends Thread{
	int id ;
	public HDCLI(int id){
		this.id = id;
	}
	public void run(){
		while(true){
			Client c = new Client();
			BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
			String cmd = c.request(String.format("HD# %d>",id), bufferRead);
			String[] cmdSplit = cmd.split(" ");
			
			HDsync hd = new HDsync();
			//when typing "list"
			if (cmdSplit[0].equals("list") && cmdSplit.length == 1){
				System.out.println("[worker id] / [hash values for keys stored in a worker");
				for (int i=0; i<5; i++){
					listPrint(hd, i);
				}
			}
			//when typing "show [worker ID]"
			else if (cmdSplit[0].equals("show") && cmdSplit.length == 2){
				System.out.println("[hash values for keys stored in the worker]");
				listPrint(hd, Integer.parseInt(cmdSplit[1]));
				
			}
			//otherwise 
			else {
				System.out.println("usage : list \n\t show [worker ID]");
			}

		}
	}
	/*
	Usage : print all the syncList elements only corresponding to worker ID.
	Input 
		hd 		HDsync including syncList 
		id 		worker ID 
	Output 
		none 
	*/
	private void listPrint(HDsync hd, int id){
		System.out.print(String.format("%d \t",id));
		for (int i=0; i<hd.syncList.size(); i++){
			if(hd.syncList.get(i).workerID == id){
				System.out.print(String.format("%d ", hd.syncList.get(i).hashValue));
			}
		}
		System.out.println("");
	}
}

/*
Usage : synchronize syncList among handlers. This class is a thread that receive the updated message from 
other handlers and process the message ("put" : add the element into syncList, "del" : remove the element in
syncList). 

*/
class Synchronization extends Thread{
	HDsync sync;
	int id;
	public Synchronization(HDsync sync, int id){
		this.sync = sync; 
		this.id = id;
	}
	public void run(){
		try{
			ServerSocket listener = new ServerSocket(5000+id);
			try{
				while (true){
					Socket socket = listener.accept();
					try{
						HDelement elem = new HDelement();
						elem.receive(socket);
						if (elem.command.equals("put")){
							sync.append(elem);
						}
						else if (elem.command.equals("del")){
							sync.remove(elem);
						}
						else {
							System.out.println("[Synchronization] Wrong syntax!");
						}
					}

					catch (ClassNotFoundException c){
						c.printStackTrace();
					}
					finally{
						socket.close();
					}

				}		
			}
			finally{
				listener.close();
			}
			
		}
		catch (IOException e){
			e.printStackTrace();
		}
		
	}
}
/*
Usage : class that have syncList. It has functions that remove some elements in syncList or 
add some elements in syncList
*/
class HDsync{
	public static ArrayList<HDelement> syncList = new ArrayList<HDelement>();
	/*
	Usage : add an element into syncList 
	Input : 
		HDelement e 	HDelement 
	Output : 
		none 
	*/
	public void append(HDelement e){
		syncList.add(e);
	}
	/*
	Usage : remove an element in syncList 
	Input 
		HDelement e 	HDelement 
	Output : 
		none 
	*/
	public void remove(HDelement e){
		for (int i=0; i<syncList.size(); i++){
			if (e.hashValue == syncList.get(i).hashValue){
				syncList.remove(syncList.get(i));
				break;
			}
		}
	}
}
/*
Usage : class that uses syncList elements and message protocol between handlers and workers 
*/
class HDelement implements Serializable{
	String command;
	int hashValue;
	int workerID;
	/*
	Usage : Update message 
	Input : 
		command 	message command (such as "put", "get", "del")
		hashValue 	hash value of message key 
		worker ID 	worker ID 
	Output : 
		none 
	*/
	public void msgUpdate(String command, int hashValue, int workerID){
		this.hashValue = hashValue;
		this.workerID = workerID;
		this.command = command;
	}
	/*
	Usage : send the object through socket 
	Input : 
		socket 		socket 
	Output :
		none 
	*/
	public void send(Socket socket) throws IOException{
		ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
		oos.writeObject(this);
	}
	/*
	Usage : receive the object through socket 
	Input : 
		socket 		socket 
	Output : 
		none 
	*/
	public void receive(Socket socket) throws IOException, ClassNotFoundException{
		ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
		HDelement msg = (HDelement) ois.readObject();
		msgUpdate(msg.command, msg.hashValue, msg.workerID);
	}
	/*
	Usage : print all the elements of the object 
	Input : 
		none 
	Output : 
		none 
	*/
	public void print(){
		System.out.println(String.format("%d %d",hashValue, workerID));
	}
}


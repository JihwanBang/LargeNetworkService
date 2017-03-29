package bin;

import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import bin.MessageProtocol;
import bin.Client;


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

	public static void handlerConnection(HDsync sync,int num){
		try{
			ServerSocket listener = new ServerSocket(10000+num);
			try{
				while(true){
					Socket socket = listener.accept();
					try{
						MessageProtocol messageRecv = new MessageProtocol();
						messageRecv.receive(socket);
						System.out.print(String.format("[Handler %d] ", num));
						messageRecv.print();
						
						int hashValue = joaat_hash(messageRecv.key.getBytes());
						System.out.println(String.format("hashValue remain %d", (hashValue%5 +5)%5));	


						if (messageRecv.command == 1){
							put(socket, hashValue, num, sync, messageRecv);
						}
						else if (messageRecv.command == 3){
							get(socket, hashValue, sync, messageRecv);
						}
						else if (messageRecv.command == 5){
							del(socket, hashValue, sync, messageRecv);
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

	private static void put(Socket socketLB, int hashValue, int num, HDsync sync, MessageProtocol msg) throws ClassNotFoundException, IOException, UnknownHostException{
		boolean success = true;
		for (int i=0; i<sync.syncList.size(); i++){
			if (sync.syncList.get(i).hashValue == hashValue){
				success = false;
			}
		}
		if (success){
			HDelement elem = new HDelement();
			elem.msgUpdate(hashValue, (hashValue%5 +5)%5);
			sync.append(elem);
			
			Socket socket1 = new Socket("127.0.0.1",5000+(num+1)%3);
			elem.send(socket1);
			Socket socket2 = new Socket("127.0.0.1", 5000+(num+2)%3);
			elem.send(socket2);
			

			handler2WK(1, hashValue, msg.key, msg.value);
	
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

	private static void get(Socket socketLB, int hashValue, HDsync sync, MessageProtocol msg) throws ClassNotFoundException, UnknownHostException, IOException{
		boolean success = false;
		for (int i=0; i<sync.syncList.size(); i++){
			if (sync.syncList.get(i).hashValue == hashValue){
				success = true;
				break;
			}
		}
		String value="";
		if (success){
			value = handler2WK(3, hashValue, msg.key, msg.value); 
		}
		
		//Socket socketLB = new Socket("127.0.0.1", 5131);
		MessageProtocol reply = new MessageProtocol();
		if (success){
			reply.messageUpdate(msg.clientID, msg.sequence, (short)4, (short) 1, msg.key, value);
		}
		else {
			reply.messageUpdate(msg.clientID, msg.sequence, (short)4, (short) 2, "", "");
		}
		reply.send(socketLB);
		
	}

	private static void del(Socket socketLB, int hashValue, HDsync sync, MessageProtocol msg) throws ClassNotFoundException, UnknownHostException, IOException{
		boolean success = false; 
		for (int i=0; i<sync.syncList.size(); i++){
			if (sync.syncList.get(i).hashValue == hashValue){
				success = true; 
				sync.syncList.remove(sync.syncList.get(i));
				break;
			}
		}
		
		if (success){
			handler2WK(5, hashValue, msg.key, msg.value);
		}

		//Socket socketLB = new Socket("127.0.0.1",5131);
		MessageProtocol reply = new MessageProtocol();
		if (success){
			reply.messageUpdate(msg.clientID, msg.sequence, (short)6, (short) 1, "", "");
		}
		else {
			reply.messageUpdate(msg.clientID, msg.sequence, (short)6, (short) 2, "", "");
		}
		reply.send(socketLB);
	}

	private static String handler2WK(int command, int hashValue, String key, String value) throws ClassNotFoundException{
		try{
			Socket socket = new Socket("127.0.0.1", 20000 + (hashValue%5+5)%5);
			MessageHD2WK msg = new MessageHD2WK();
			msg.messageUpdate(command, hashValue, key, value);
			msg.send(socket);
			MessageHD2WK ack = new MessageHD2WK();
			ack.receive(socket);
			return ack.value;
		}
		catch (IOException e){
			e.printStackTrace();
		}
		return "";
	}

}





class HDCLI extends Thread{
	int id ;
	public HDCLI(int id){
		this.id = id;
	}
	public void run(){
		while(true){
			Client c = new Client();
			String cmd = c.request(String.format("HD# %d>",id));
			String[] cmdSplit = cmd.split(" ");
			
			HDsync hd = new HDsync();

			if (cmdSplit[0].equals("list") && cmdSplit.length == 1){
				System.out.println("[worker id] / [hash values for keys stored in a worker");
				for (int i=0; i<5; i++){
					listPrint(hd, i);
				}
			}
			else if (cmdSplit[0].equals("show") && cmdSplit.length == 2){
				System.out.println("[hash values for keys stored in the worker]");
				listPrint(hd, Integer.parseInt(cmdSplit[1]));
				
			}
			else {
				System.out.println("usage : list \n\t show [worker ID]");
			}

		}
	}
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
						sync.append(elem); 
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

class HDsync{
	public static ArrayList<HDelement> syncList = new ArrayList<HDelement>();
	public void append(HDelement e){
		syncList.add(e);
	}
}

class HDelement implements Serializable{
	int hashValue;
	int workerID;
	public void msgUpdate(int hashValue, int workerID){
		this.hashValue = hashValue;
		this.workerID = workerID;
	}

	public void send(Socket socket) throws IOException{
		ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
		oos.writeObject(this);
	}

	public void receive(Socket socket) throws IOException, ClassNotFoundException{
		ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
		HDelement msg = (HDelement) ois.readObject();
		msgUpdate(msg.hashValue, msg.workerID);
	}
	public void print(){
		System.out.println(String.format("%d %d",hashValue, workerID));
	}
}


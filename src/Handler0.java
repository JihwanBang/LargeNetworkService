package bin;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import bin.MessageProtocol;
import bin.Client;

public class Handler0{
	public static void main(String[] args){
		int num = 0;
		Thread cli = new HDCLI(num);
		cli.start();
		handlerConnection(num);
			
	}

	public static void handlerConnection(int num){
		try{
			ServerSocket listener = new ServerSocket(10000+num);	
			while(true){
				Socket socket = listener.accept();
				try{
					MessageProtocol messageRecv = new MessageProtocol();
					messageRecv.receive(socket);
					System.out.print(String.format("[Handler %d] ", num));
					messageRecv.print();
					
					int hashValue = joaat_hash(messageRecv.key.getBytes());
					System.out.println(String.format("hashValue %d", hashValue));	


				}
				catch (ClassNotFoundException c){
					c.printStackTrace();
				}
				finally{
					socket.close();
				}
				
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
			if (cmdSplit[0].equals("list") && cmdSplit.length == 1){
				System.out.println("[worker id] / [hash values for keys stored in a worker");

			}
			else if (cmdSplit[0].equals("show") && cmdSplit.length == 2){
				System.out.println("[hash values for keys stored in the worker]");

			}
			else {
				System.out.println("usage : list \n\t show [worker ID]");
			}

		}
	}
}
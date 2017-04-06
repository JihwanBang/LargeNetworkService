/*
	Network System and Security Assignment #1 
	Due date : April 7th, 2017
	Author : Jihwan Bang
*/
package bin;

import java.net.Socket;
import java.net.ServerSocket;
import java.io.IOException;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import bin.MessageProtocol;
import bin.MessageHD2WK;

/*
This class is the worker0 class. Workers save the hash value, key, and value sending from handlers.
If handlers request "get" function to specific worker, then it should send the values corresponding
to the key to handlers. 
If handlers request "put" function, then it should save the key and value.
If handlers request "del" function, then it should delete the key and value in the save list. 
*/
class Worker0{
	public static void main(String[] args){
		int id = 0;
		Thread cli = new WKCLI(id);
		cli.start();
	
		workerServer(id);

	}
	/*
	Usage : receive all of the reqeusts from handlers. 
			handle the requests for each circumstance and reply the ack to the handler. 
	Input : 
		id 		worker id 
	Output 
		none 
	*/
	public static void workerServer(int id){
		try{
			File file = new File(String.format("Worker_%d.log", id));
			FileWriter fw = new FileWriter(file);

			ServerSocket listener = new ServerSocket(20000+id);	
			WKlist wk = new WKlist();	
			while(true){
				Socket socket = listener.accept();
				try{
					MessageHD2WK messageRecv = new MessageHD2WK();
					messageRecv.receive(socket);
					messageRecv.print(fw);
					if (messageRecv.command == 1){//put
						WKelem elem = new WKelem(messageRecv.hashValue, messageRecv.key, messageRecv.value);
						wk.append(elem);
						messageRecv.command = 2;
						messageRecv.send(socket);
					}
					else if (messageRecv.command == 3){//get
						String value = ""; 
						for (int i=0; i<wk.list.size(); i++){
							if (wk.list.get(i).hashValue == messageRecv.hashValue){
								value = wk.list.get(i).value;
								break;
							}
						}
						messageRecv.command = 4;
						messageRecv.value = value;
						messageRecv.send(socket);

					}	
					else if (messageRecv.command == 5){//del
						WKelem elem = new WKelem(messageRecv.hashValue, messageRecv.key, messageRecv.value);
						wk.remove(elem);
						messageRecv.command = 6;
						messageRecv.send(socket);

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
		catch (IOException e){
			e.printStackTrace();
		}			
	}
}

/*
Usage : When typing some commands in Worker CLI (such as "list", "show"), it can deal with the commands that 
we typed. 
*/
class WKCLI extends Thread{
	int id;
	public WKCLI(int id){
		this.id = id ;
	}
	public void run(){
		while(true){
			Client c = new Client();
			BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
			String cmd = c.request(String.format("WK# %d> ", id),bufferRead);
			String[] cmdList = cmd.split(" ");
			WKlist wkList = new WKlist();

			if(cmdList[0].equals("list") && cmdList.length == 1){
				System.out.println("[hash value of a key]\t[key]\t[key value]");
				for (int i=0; i<wkList.list.size(); i++){
					System.out.println(String.format("%d\t%s\t%s", wkList.list.get(i).hashValue,
						wkList.list.get(i).key, wkList.list.get(i).value));
				}
			}
			else if (cmdList[0].equals("show") && cmdList.length == 2){
				System.out.println("[hash value of a key]\t[key]\t[key value]");
				for (int i=0; i<wkList.list.size(); i++){
					if (wkList.list.get(i).key.equals(cmdList[1])){
						System.out.println(String.format("%d\t%s\t%s", wkList.list.get(i).hashValue,
							wkList.list.get(i).key, wkList.list.get(i).value));
					}
					else {
						System.out.println("there is no key!");
					}
				}
			}
			else {
				System.out.println("usage : list\n\tshow [key]");
			}
		}
	}
}
/*
	the class that save/delete the WKelem in the list.
*/
class WKlist{
	public static ArrayList<WKelem> list = new ArrayList<WKelem>();
	/*
	Usage : add the elem to the list 
	input 
		elem
	Output 
		none 
	*/
	public void append(WKelem elem){
		list.add(elem);
	} 
	/*
	Usage : remove the elem to the list 
	input 
		elem 
	Output 
		none 
	*/
	public void remove(WKelem elem){
		for (int i=0; i<list.size(); i++){
			if (list.get(i).hashValue == elem.hashValue){
				list.remove(list.get(i));
				break;
			}
		}
	}
}
/*
	the class that is an element of the list. 
*/
class WKelem{
	int hashValue;
	String key;
	String value;
	public WKelem(int hashValue, String key, String value){
		this.hashValue = hashValue;
		this.key = key;
		this.value = value;
	}
}


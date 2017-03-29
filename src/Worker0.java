package bin;

import java.net.Socket;
import java.net.ServerSocket;
import java.io.IOException;
import java.util.ArrayList;
import bin.MessageProtocol;
import bin.MessageHD2WK;


class Worker0{
	public static void main(String[] args){
		int id = 0;
		Thread cli = new WKCLI(id);
		cli.start();
	
		workerServer(id);

	}
	
	public static void workerServer(int id){
		try{
			ServerSocket listener = new ServerSocket(20000+id);	
			WKlist wk = new WKlist();	
			while(true){
				Socket socket = listener.accept();
				try{
					MessageHD2WK messageRecv = new MessageHD2WK();
					messageRecv.receive(socket);
					System.out.print(String.format("[Worker %d] ", id));
					messageRecv.print();
					if (messageRecv.command == 1){
						WKelem elem = new WKelem(messageRecv.hashValue, messageRecv.key, messageRecv.value);
						wk.append(elem);
						messageRecv.command = 2;
						messageRecv.send(socket);
					}
					else if (messageRecv.command == 3){
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
					else if (messageRecv.command == 5){
						for (int i=0; i<wk.list.size(); i++){
							if (wk.list.get(i).hashValue == messageRecv.hashValue){
								wk.list.remove(wk.list.get(i));
								break;
							}
						}
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

class WKCLI extends Thread{
	int id;
	public WKCLI(int id){
		this.id = id ;
	}
	public void run(){
		while(true){
			Client c = new Client();
			String cmd = c.request(String.format("WK# %d> ", id));
			String[] cmdList = cmd.split(" ");
			WKlist wkList = new WKlist();

			if(cmdList[0].equals("list") && cmdList.length == 1){
				//System.out.println("list");
				System.out.println("[hash value of a key]\t[key]\t[key value]");
				for (int i=0; i<wkList.list.size(); i++){
					System.out.println(String.format("%d\t%s\t%s", wkList.list.get(i).hashValue,
						wkList.list.get(i).key, wkList.list.get(i).value));
				}
			}
			else if (cmdList[0].equals("show") && cmdList.length == 2){
				//System.out.println("show");
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

class WKlist{
	public static ArrayList<WKelem> list = new ArrayList<WKelem>();
	public void append(WKelem elem){
		list.add(elem);
	} 
}

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


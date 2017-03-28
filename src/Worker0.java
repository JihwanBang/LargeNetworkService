package bin;

import java.net.Socket;
import java.net.ServerSocket;
import java.io.IOException;
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
			while(true){
				Socket socket = listener.accept();
				try{
					MessageHD2WK messageRecv = new MessageHD2WK();
					messageRecv.receive(socket);
					System.out.print(String.format("[Worker %d] ", id));
					messageRecv.print();
					

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

			if(cmdList[0].equals("list") && cmdList.length == 1){
				System.out.println("list");
				System.out.println("[hash value of a key] / [key] / [key value]");
			
			}
			else if (cmdList[0].equals("show") && cmdList.length == 2){
				System.out.println("show");
				System.out.println("[hash value of a key] / [key] / [key value]");
			}
			else {
				System.out.println("usage : list \n\t show [key]");
			}
		}
	}
}
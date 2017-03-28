package bin;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.net.UnknownHostException;
import bin.MessageProtocol;
import bin.Client;

public class LoadBalance 
{	
	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException
	{
		ServerSocket listener = new ServerSocket(5131);
		try
		{
			SharedArea sa = new SharedArea();

			Thread cli = new LBCLI(sa);
			cli.start();

			while(true)
			{
				Socket socket = listener.accept();
			 	/*
				System.out.println("accept!");
				Socket socket = listener.accept();
				System.out.println("message recv");
				
				MessageProtocol messageRecv = new MessageProtocol();
				messageRecv.receive(socket);
				messageRecv.print();
				*/
				System.out.println("start thread!");
				Thread multiClient = new MultiClient(socket,sa);
				multiClient.start();
				multiClient.join();
				System.out.println("loop");
			}
		
		}

		finally
		{
			listener.close();
		}
	}



}
class MultiClient extends Thread{
	Socket socket; 
	SharedArea sa;

	public MultiClient(Socket socket, SharedArea sa){
		this.socket = socket;
		//this.msg = msg;
		this.sa = sa;
	}

	public void run(){
		try{
			//System.out.println("start thread");
			
			//Socket socket = listener.accept();

			MessageProtocol msg = new MessageProtocol();
			msg.receive(socket);
			msg.print();
				

			Thread t = new Load2Handler(msg, sa);
			t.start();
			t.join();
			
			msg.replyAck(socket);
			socket.close();
			sa.rr = (sa.rr+1)%3;

		}
		catch (IOException e){
			e.printStackTrace();
		}
		catch (ClassNotFoundException c){
			c.printStackTrace();
		}
		catch (InterruptedException i){
			i.printStackTrace();
		}
	}
}

class LBCLI extends Thread{ 
	SharedArea sa_read;
	public LBCLI(SharedArea sa){
		this.sa_read = sa;
	}
	public void run(){
		while(true){
			Client c = new Client();
			String cmd = c.request("LB> "); 
			if (cmd.equals("list")){
				System.out.println("[Handler ID] / [Handler IP/port] / [# of requests]");
				System.out.println(String.format("0 / 127.0.0.1/10000 / %d", sa_read.request0));
				System.out.println(String.format("1 / 127.0.0.1/10001 / %d", sa_read.request1));
				System.out.println(String.format("2 / 127.0.0.1/10002 / %d", sa_read.request2));
			}
			else {
				System.out.println("usage : list");
			}

		}		
	}

}

class Load2Handler extends Thread {
	MessageProtocol msg; 
	SharedArea sa_save;
	public Load2Handler(MessageProtocol msg, SharedArea sa){
		this.msg = msg;
		this.sa_save = sa;
	}
	public void run() {
		try{
			Socket socket = new Socket("127.0.0.1", 10000+sa_save.rr);
			switch (sa_save.rr){
				case 0 : sa_save.request0 += 1; break;
				case 1 : sa_save.request1 += 1; break;
				case 2 : sa_save.request2 += 1; break;
				default : System.out.println("Wrong Port Number!");
			}
			msg.send(socket);	
		}
		catch(IOException e){
			e.printStackTrace();
		}
		
	}
}

class SharedArea{
	int request0 = 0;
	int request1 = 0;
	int request2 = 0;
	int rr =0;
}
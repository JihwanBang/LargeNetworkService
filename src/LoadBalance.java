/*
	Network System and Security Assignment #1 
	Due date : April 7th, 2017
	Author : Jihwan Bang
*/
package bin;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.net.UnknownHostException;
import bin.MessageProtocol;
import bin.Client;
/*
	This is class of LoadBalance. It works load balancing (send a message(work) to handlers using Round Robin).
	Also, it replys an ack to a client whether the client's order(command) is success or not. 
*/
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

			File file = new File("LoadBalance.log");
			FileWriter fw = new FileWriter(file);

			while(true)
			{
				Socket socket = listener.accept();

				Thread multiClient = new MultiClient(socket,sa,fw);
				multiClient.start();
				multiClient.join();

			}
		
		}

		finally
		{
			listener.close();
		}
	}
	



}
/*
	At first, it will receive the message from the clients. Then, it sends the message to speicific handler
	and receive the ack from that handler using load2Handler function. It contains the rule called 
	"Round Robin".
*/
class MultiClient extends Thread{
	Socket socket; 
	SharedArea sa;
	FileWriter fw;
	public MultiClient(Socket socket, SharedArea sa, FileWriter fw){
		this.socket = socket;
		this.sa = sa;
		this.fw = fw;
	}

	public void run(){
		try{


			MessageProtocol msg = new MessageProtocol();
			msg.receive(socket);
			msg.print(fw);
				
			msg = load2Handler(msg,sa);
			sa.rr = (sa.rr+1)%3; //Round Robin 
			msg.send(socket);

		}
		catch (IOException e){
			e.printStackTrace();
		}
		catch (ClassNotFoundException c){
			c.printStackTrace();
		}
		
	}
	/*
	Usage : LoadBalance sends the messgae to specfic handler according to some rules. After that, 
	it should receive the ack from that handler. 
	Input :
		msg 	message to send 
		sa_save	class of number of requests for each handler 

	Output 
		reply 	a received message from a handler 
	*/
	private MessageProtocol load2Handler(MessageProtocol msg, SharedArea sa_save) throws ClassNotFoundException,UnknownHostException, IOException{

		Socket socket = new Socket("127.0.0.1", 10000+sa_save.rr);
		switch (sa_save.rr){
			case 0 : sa_save.request0 += 1; break;
			case 1 : sa_save.request1 += 1; break;
			case 2 : sa_save.request2 += 1; break;
			default : System.out.println("Wrong Port Number!");
		}
		msg.send(socket);
		MessageProtocol reply = new MessageProtocol();
		reply.receive(socket);
		return reply;

	}
}
/*
Usage : When typing some commands in LoadBalance CLI (such as "list"), it can deal with the commands that 
we typed. 
*/
class LBCLI extends Thread{ 
	SharedArea sa_read;
	public LBCLI(SharedArea sa){
		this.sa_read = sa;
	}
	public void run(){
		while(true){
			Client c = new Client();
			BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
			String cmd = c.request("LB> ", bufferRead); 
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


//This is a class that stores the number of requests that send to handler0, handler1, and handler2.
class SharedArea{
	int request0 = 0;
	int request1 = 0;
	int request2 = 0;
	int rr =0;
}
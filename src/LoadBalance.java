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

public class LoadBalance 
{	
	public static void main(String[] args) throws IOException, ClassNotFoundException
	{
		ServerSocket listener = new ServerSocket(5131);
		try
		{
			while(true)
			{
				int num =0; //RoundRobin 
				Socket socket = listener.accept();
				try
				{
					MessageProtocol messageRecv = new MessageProtocol();
					messageRecv.receive(socket);
					messageRecv.print();

					Thread t = new load2Handler(messageRecv);
					t.start();
					
					
					messageRecv.replyAck(socket);
					
				}
				finally
				{
					socket.close();
				}
			}
		}
		finally
		{
			listener.close();
		}
	}



}


class load2Handler extends Thread{
	MessageProtocol msg; 
	public load2Handler(MessageProtocol msg){
		this.msg = msg;
	}
	public void run(){
		System.out.println("thread suceess!");
	}
}
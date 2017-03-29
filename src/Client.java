/*
	Network System and Security Assignment #1 
	Due date : April 7th, 2017
	Author : Jihwan Bang
*/
package bin;


import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import bin.MessageProtocol;

public class Client
{
	public static void main(String[] args) throws IOException, ClassNotFoundException
	{
		int sequence = 0;
		
		try{
			/*usage : connect [LB IP]*/
			String loadbalanceIP = "";
			while(true){
				String[] connectionList = request("client> ").split(" ");

				if (connectionList.length == 2 && connectionList[0].equals("connect")){
					loadbalanceIP = connectionList[1];
					break;
				}
				else {
					System.out.println("usage : connect [LB IP]");
				}
			}

			
			/*usage : set [clientNumber]*/
			int clientNumber = -1;
			while(true){
 
				String[] clientNumberList = request("client> ").split(" ");	
				if(clientNumberList.length == 2 && clientNumberList[0].equals("set")){
					clientNumber = Integer.parseInt(clientNumberList[1]);
					break;
				}
				else {
					System.out.println("usage : set [client number]");
				}
	
			}

			while(true){
				String command = request(String.format("client%s> ",clientNumber));			
				String[] commandList = command.split(" ");


				sequence = client2LB(loadbalanceIP, commandList, sequence, clientNumber);
			}
			
		}

		catch(IOException e)
		{
			e.printStackTrace();
		}
	}

	public static String request(String question){
		System.out.print(question);
		try{
			BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
			String buffer = bufferRead.readLine();
			return buffer;
		}
		catch(IOException e){
			return null;
		}
	}


	private static int client2LB(String loadbalanceIP, String[] commandList, int sequence, int clientNumber) throws IOException, ClassNotFoundException{
		String cmd = commandList[0];
		String key = commandList[1];
		if ((cmd.equals("put")) && (commandList.length==3))
		{
			String value = commandList[2];
			return sendAndRecv(loadbalanceIP, clientNumber, sequence, (short) 1, key, value);
				
		}
		else if ((cmd.equals("get"))  && (commandList.length==2))
		{
			return sendAndRecv(loadbalanceIP, clientNumber, sequence, (short) 3, key, "");
				
		}
		else if ((cmd.equals("del")) && (commandList.length==2))
		{
			return sendAndRecv(loadbalanceIP, clientNumber, sequence, (short) 5, key, "");

		}
		else{
			System.out.println("usage : put [key] [value] \n\t get [key] \n\t del [key]\n");
			return sequence;
		}		
	}

	private static int sendAndRecv(String loadbalanceIP, int clientNumber, int sequence, short num, String key, String value) throws IOException, ClassNotFoundException{
		Socket socket = new Socket(loadbalanceIP,5131);
		MessageProtocol messageSend = new MessageProtocol();
		messageSend.messageUpdate(clientNumber, sequence, num, (short) 0, key, value);
		messageSend.send(socket);
		
		MessageProtocol messageRecv = new MessageProtocol();
		messageRecv.receive(socket);
		messageRecv.print();
		messageRecv.ackProcess(socket);

		return sequence+1;

	}
}


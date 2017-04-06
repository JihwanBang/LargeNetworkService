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
import java.io.File;
import java.io.FileWriter;
import java.net.Socket;
import java.util.Scanner;
import bin.MessageProtocol;

public class Client
{
	/*
	this is the client's main function. It can get the LoadBalance IP address, 
	and client number. Also, it sends message, that we want, to LoadBalance using 
	message protocol. 
	*/
	public static void main(String[] args) throws IOException, ClassNotFoundException
	{
		int sequence = 0;
		BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
		try{
			/*usage : connect [LB IP]*/
			String loadbalanceIP = "";
			while(true){
				String[] connectionList = request("client> ",bufferRead).split(" ");

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
				String[] clientNumberList = request("client> ", bufferRead).split(" ");	
				if(clientNumberList.length == 2 && clientNumberList[0].equals("set")){
					clientNumber = Integer.parseInt(clientNumberList[1]);
					break;
				}
				else {
					System.out.println("usage : set [client number]");
				}
	
			}

			File file = new File(String.format("Client_%d.log",clientNumber));
			FileWriter fw = new FileWriter(file);

			while(true){
				String command = request(String.format("client%s> ",clientNumber), bufferRead);			
				String[] commandList = command.split(" ");


				sequence = client2LB(loadbalanceIP, commandList, sequence, clientNumber, fw);
			}

		}

		catch(IOException e)
		{

			e.printStackTrace();
		}
	}
	/*
	Usage : scan the data written in CLI. 
	Input 
		-question : String that we want to ask
		-bufferRead : BufferedReader 
	Output
		-buffer : data written in CLI  
	*/
	public static String request(String question, BufferedReader bufferRead){
		System.out.print(question);
		try{
			String buffer = bufferRead.readLine();
			return buffer;
		}
		catch(IOException e){
			return null;
		}
	}

	/*
	Usage : when a client sends a message to LoadBalance, we should send a message in terms of 
	message protocol. 
	Input 
		-loadbalanceIP	LoadBalance IP address 
		-commandList	CLI splitting string
		-sequence		sequence 
		-clientNumber 	client number 
	Output 
		-sequence 		Updated sequence (+1) 
	*/

	private static int client2LB(String loadbalanceIP, String[] commandList, int sequence, int clientNumber,
		FileWriter fw) throws IOException, ClassNotFoundException{

		String cmd = commandList[0];
		String key = commandList[1];
		if ((cmd.equals("put")) && (commandList.length==3))
		{
			String value = commandList[2];
			return sendAndRecv(loadbalanceIP, clientNumber, sequence, (short) 1, key, value, fw); // put : 1
				
		}
		else if ((cmd.equals("get"))  && (commandList.length==2))
		{
			return sendAndRecv(loadbalanceIP, clientNumber, sequence, (short) 3, key, "", fw); // get :3
				
		}
		else if ((cmd.equals("del")) && (commandList.length==2))
		{
			return sendAndRecv(loadbalanceIP, clientNumber, sequence, (short) 5, key, "", fw); // del : 5

		}
		else{
			System.out.println("usage : put [key] [value] \n\t get [key] \n\t del [key]\n");
			return sequence;
		}		
	}

	/*
	Usage : when client send message to loadbalance, client should receive the ack from the loadbalance. 
	Input 
		-loadbalanceIP	LoadBalance IP address 
		-clientNumber 	client number 
		-sequence 		sequence 
		-num 			command in Message Protocol that we want to send  
		-key 			key in Message Protocol that we want to send
		-value 			value in Message protocol that we want to send 
	Output 
		sequence 		Updated sequence (+1)
	*/
	private static int sendAndRecv(String loadbalanceIP, int clientNumber, int sequence, short num, 
		String key, String value, FileWriter fw) throws IOException, ClassNotFoundException{
		/*send message to LB*/
		Socket socket = new Socket(loadbalanceIP,5131);
		MessageProtocol messageSend = new MessageProtocol();
		messageSend.messageUpdate(clientNumber, sequence, num, (short) 0, key, value);
		messageSend.send(socket);
		
		/*receive ack from LB*/
		MessageProtocol messageRecv = new MessageProtocol();
		messageRecv.receive(socket);
		messageRecv.print(fw);
		messageRecv.ackProcess(socket);

		return sequence+1;

	}
}


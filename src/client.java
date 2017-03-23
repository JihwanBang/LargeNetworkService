package bin;

import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import bin.MessageProtocol;

public class client
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

			Socket socket = new Socket(loadbalanceIP,5131);			
			
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
			String command = request(String.format("client%s> ",clientNumber));			
			String[] commandList = command.split(" ");


			sequence = Client2LB(socket, commandList, sequence, clientNumber);
		}

		catch(IOException e)
		{
			e.printStackTrace();
		}
		//System.exit(0);
	}

	private static String request(String question){
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


	private static int Client2LB(Socket socket, String[] commandList, int sequence, int clientNumber) throws IOException, ClassNotFoundException{
		String cmd = commandList[0];
		String key = commandList[1];
		if ((cmd.equals("put")) && (commandList.length==3))
		{
			String value = commandList[2];
			return sendAndRecv(socket, clientNumber, sequence, (short) 1, key, value);
		}
		else if ((cmd.equals("get"))  && (commandList.length==2))
		{
			return sendAndRecv(socket, clientNumber, sequence, (short) 3, key, "");
		}
		else if ((cmd.equals("del")) && (commandList.length==2))
		{
			return sendAndRecv(socket, clientNumber, sequence, (short) 5, key, "");
		}
		else{
			System.out.println("usage : put [key] [value] \n\t get [key] \n\t del [key]\n");
			return sequence;
		}		
	}

	private static int sendAndRecv(Socket socket, int clientNumber, int sequence, short num, String key, String value) throws IOException, ClassNotFoundException{
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


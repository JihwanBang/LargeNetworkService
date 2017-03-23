package bin;

import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import bin.MessageProtocol;

public class client0
{

	public static void main(String[] args) throws IOException
	{
		int sequence = 0;
		
		try{
			String[] connectionList = request("client> ").split(" ");
			String loadbalanceIP = "";
			if (connectionList.length == 2){
				if (connectionList[0].equals("connect")){
					loadbalanceIP = connectionList[1];
				}
				
			}
			else {
				System.out.println("usage : connect [LB IP]");
			}

			String clientNumber = ""; 
			String[] clientNumberList = request("client> ").split(" ");
			if(clientNumberList.length == 2){
				if (clientNumberList[0].equals("set")){
					clientNumber = clientNumberList[1];
				}
			}
			else {
				System.out.println("usage : set [client number]");
			}

			String command = request(String.format("client%s> ",clientNumber));
	
			System.out.println(command);
			
			String[] commandList = command.split(" ");

			Socket socket = new Socket(loadbalanceIP,5131);
			
			PrintWriter send = new PrintWriter(socket.getOutputStream(), true);
			String cmd = commandList[0];
			String key = commandList[1];
			
			if ((cmd.equals("put")) && (commandList.length==3))
			{
				String value = commandList[2];
				send.println(new MessageProtocol(0, sequence, cmd, "none", key, value));
				sequence = sequence + 1;
			}
			else if (((cmd.equals("get")) || (cmd.equals("del"))) && (commandList.length==2))
			{
				send.println(new MessageProtocol(0, sequence, cmd, "none", key, ""));
				sequence = sequence + 1;
			}
			else{
				System.out.println("usage : put [key] [value] \n\t get [key] \n\t del [key]\n");
			}
		
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

	

}


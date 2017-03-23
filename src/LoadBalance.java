package bin;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class LoadBalance
{
	public static void main(String[] args) throws IOException
	{
		ServerSocket listener = new ServerSocket(5131);
		try
		{
			while(true)
			{
				Socket socket = listener.accept();
				try
				{
					BufferedReader bufferRead = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					String receive = bufferRead.readLine();
					System.out.println(receive);
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
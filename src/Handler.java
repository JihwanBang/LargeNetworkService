package bin;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import bin.MessageProtocol;


class Handler{
	public static void main(String[] args) throws IOException, ClassNotFoundException{
		ServerSocket listener = new ServerSocket(10000);
		try{
			while(true){
				Socket socket = listener.accept();
				try{
					MessageProtocol messageRecv = new MessageProtocol();
					messageRecv.receive(socket);
					messageRecv.print();
					

				}
				finally{
					socket.close();
				}
			}
		}
		finally{
			listener.close();
		}
	}


}

package bin;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import bin.MessageProtocol;
import bin.Client;
import bin.Handler0;

public class Handler2{
	public static void main(String[] args){
			int num = 2;
			Thread cli = new HDCLI(num);
			cli.start();
			HDsync sync = new HDsync();
			Thread syncServer = new Synchronization(sync, num);
			syncServer.start();
			Handler0 hd = new Handler0();
			hd.handlerConnection(sync, num);			
	}


}




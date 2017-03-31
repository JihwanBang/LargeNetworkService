/*
	Network System and Security Assignment #1 
	Due date : April 7th, 2017
	Author : Jihwan Bang
*/
package bin;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import bin.MessageProtocol;
import bin.Client;
import bin.Handler0;

/*
	This is for Handler1. we use 2 threads which are HDCLI, which is CLI handler, and Synchronization, which
	is a server for receiving updated message from other handlers. It uses the functions from Handler0.  
*/
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




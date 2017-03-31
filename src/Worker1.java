/*
	Network System and Security Assignment #1 
	Due date : April 7th, 2017
	Author : Jihwan Bang
*/
package bin;

import java.net.Socket;
import java.net.ServerSocket;
import java.io.IOException;
import bin.MessageProtocol;
import bin.WKCLI;
import bin.Worker0;

/*
	This class is the worker1 class. A functionality is as same as worker0.  
*/
class Worker1{
	public static void main(String[] args){
		int id = 1;
		Thread cli = new WKCLI(id);
		cli.start();
		Worker0 wk = new Worker0();
		wk.workerServer(id);

	}
}


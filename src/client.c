//this is client code 
#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <stdlib.h>

#define NumClient 2 // Number of client 
#define MAX_SIZE 1024 // in cli, max size 

void LOG(const char* ID, const char* msg)
{
	printf("[%s] %s", ID, msg);
	exit(0);
}

char* CLI_get_info(const char* str)
{
	char getinfo[MAX_SIZE];
	printf("%s>", str);
	fgets(getinfo, MAX_SIZE, stdin);
	char* y = (char*) &getinfo;
	return y;
}

void Client(int runProcess, char* LB_IP)
{
	printf("hello\n");
} 

int main()
{
	char info[MAX_SIZE];
	char*cmd, *LB_IP;
	pid_t client[NumClient];
	int runProcess = 0;
	int exitFlag = 0;
	int Act_ID =-1;

	while(runProcess < NumClient)
	{
		client[runProcess] = fork();
		if (client[runProcess] < 0)
		{
			printf("error! cannot make child process!\n");
			return -1;
		}
		if (client[runProcess] == 0)
		{
			if (runProcess == 0)
			{
				if (Act_ID == 0)
				{
					CLI_get_info("client0");
				}
			}
			if (runProcess == 1)
			{
				CLI_get_info("client1");
			}
			exit(0);
		}
		if (client[runProcess]>0)
		{

		}
		runProcess += 1;
	}

	printf("[parent] pid : %ld\n",(long)getpid());
	info = CLI_get_info("client");
	printf("info : %s\n", info);
	cmd = strtok(info," ");
	printf("cmd : %s\n",cmd);
	if (! strcmp("connect", cmd))
	{
		LB_IP = strtok(NULL, " ");
		if (LB_IP == NULL)
		{
			LOG("client","there is no IP");
		}
		printf("load balance : %s\n",LB_IP);

	}

	else 
	{
		printf("not connect!\n");
	}	
	
	return 1;
}
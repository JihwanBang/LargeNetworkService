//this is client code 
#include <stdio.h>
#include <string.h>
#include <unistd.h>


char* CLI_get_info(const char* str)
{
	char getinfo[100];
	printf("%s>", str);
	scanf("%s", getinfo);
	char* y = (char*) &getinfo;
	return y;
}

int main()
{
	char* info;

	pid_t client0, client1;

	client0 = fork();
	client1 = fork();

	printf("pid : %d, %d\n", client0, client1);
	printf(" %d ", strcmp("connect", CLI_get_info("client")));
	if (! strcmp("connect", CLI_get_info("client")))
	{
		printf("connect!\n");
	}
	else 
	{
		printf("not connect!\n");
	}
	return 1;
}
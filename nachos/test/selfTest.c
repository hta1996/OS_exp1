#include "syscall.h"
#include "stdio.h"
#include "stdlib.h"

#define BUFFERSIZE	256
#define MAXARGSIZE	16
#define MAXARGS		16



int main()
{
    //printf("%d arguments\n", argc);
    //printf("Begin My Test!!11\n");
    //printf("YES\n");
    char args[BUFFERSIZE], prog[BUFFERSIZE];
    char *argv[MAXARGS];
    //strcpy(prog),;
	//strcpy(prog, argv[0]);
	//strcat(prog, ".coff");
    for (int i = 1 ; i <= 10; i++)
        int pid = exec("halt.coff", 0, argv);
    
    //printf("Do not run Halt()!!");
    //halt();
    //exit(0);
    return 0;
}


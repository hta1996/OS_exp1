#include "syscall.h"
#include "stdio.h"
#include "stdlib.h"

#define BUFFERSIZE	256
#define MAXARGSIZE	16
#define MAXARGS		16

#include "syscall.h"
#include "stdio.h"
#include "stdlib.h"

#define BUFSIZE 1024

char buf[BUFSIZE];

int main()
{
  int fd, amount;

  /*if (argc!=2) {
    printf("Usage: cat <file>\n");
    return 1;
  }*/
   printf("C: start!\n");
  
  fd = open("1.in");
  if (fd == -1)
  {
    printf("Unable to open %s\n", "1.in");
    return 1;
  }
  else
  {
  	printf("C: Able to open %s\n", "1.in");
  }

  while ((amount = read(fd, buf, BUFSIZE))>0) {
    printf("C: Write to 1 from fd: amount %d\n", amount);
    write(1, buf, amount);
  }
    printf("M: herehere!\n");
//*
  unlink(fd);
  write(1, buf, amount);
//*/
  return 0;
}

/*int main()
{
    //printf("%d arguments\n", argc);
    printf("Begin My Test!!\n");
    //printf("YES\n");
    char args[BUFFERSIZE], prog[BUFFERSIZE];
    char *argv[MAXARGS];
    //strcpy(prog),;
	//strcpy(prog, argv[0]);
	//strcat(prog, ".coff");
	//int i;
    //for (i = 1 ; i <= 10; i++)
    //{
    //    int pid;pid = exec("halt.coff", 0, argv);
    //}
    //printf("Do not run Halt()!!");
    //halt();
    //exit(0);

  	int fd, amount;


  	fd = open(argv[1]);
  	if (fd==-1) {
    	printf("Unable to open %s\n", argv[1]);
    	return 1;
  	}

  	while ((amount = read(fd, buf, BUFSIZE))>0) {
    	write(1, buf, amount);
  	}

  close(fd);

  return 0;
}
    return 0;
}*/


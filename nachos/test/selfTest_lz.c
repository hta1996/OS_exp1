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
  int num = 1, i = 0;
  int pid[100];
  printf("Begin Test\n");
  char args[BUFFERSIZE], prog[BUFFERSIZE];
  char *argv[MAXARGS];
  for (i = 0; i < num; i++)
  {
    pid[i] = exec("selfTest_sub.coff", 0, argv);
    exit(0);
  }

  printf("End Test\n");
  return 0;
}

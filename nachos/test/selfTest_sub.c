#include "syscall.h"
#include "stdio.h"
#include "stdlib.h"

#define BUFFERSIZE	256
#define MAXARGSIZE	16
#define MAXARGS		16
#define BUFSIZE 1024

char buf[BUFSIZE];

int main()
{
  long i = 0;
  long s = 0;
  long fd;
  printf("YES\n");
  fd = open("1.in");
  //if (fd != -1) close(fd);
  //*
  for (i = 0; i < 10000; i++)
  {
    s += i % 3;
  }//*/
  //halt();
  //printf("%d\n", s);
  return 0;
}

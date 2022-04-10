#ifndef MULTITHREAD_IO_UTILS_H
#define MULTITHREAD_IO_UTILS_H

#define SIG_SAFE_IO_BLOCK(op, res) while((res = op) == -1 && (errno == EINTR));

#define SIG_SAFE_IO_BLOCK2(op)     while(op == -1 && (errno == EINTR));

#endif

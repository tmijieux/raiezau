#ifndef PROMPT_H
#define PROMPT_H

#include <pthread.h>

extern pthread_t prompt_thread;

void command_prompt(void);
void start_command_prompt_thread(void);
    
#endif //PROMPT_H

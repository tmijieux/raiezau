#include <sys/types.h>
#include <sys/stat.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <unistd.h>
#include <sys/stat.h>
#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <stdbool.h>
#include <pthread.h>
#include <ctype.h>

#include <readline/readline.h>
#include <readline/history.h>

#include "cutil/error.h"
#include "cutil/string2.h"
#include "config.h"

#define CHK(mesg, X_) do { if ((X_) < 0) {                      \
            rz_error("%s: %s\n", #mesg, strerror(errno));       \
            exit(EXIT_FAILURE);} } while(0)

static bool prompt_eval(const char *command);
static void read_eval_loop(void);
static char *my_generator(const char*,int);
static char **my_completion(const char*, int ,int);

static void prompt_command_client(int len, char **command);
static void prompt_command_file(int len, char **command);
static void prompt_command_help(int len, char **command);

static bool is_white(const char *str)
{
    for (int i = 0; str[i]; ++i)
        if (!isblank(str[i]))
            return false;
    return true;
}

void command_prompt(void)
{
    if ( option_daemonize() )
        return;

    rz_debug("Prompt thread started\n");
    rl_attempted_completion_function = my_completion;
    read_eval_loop();
    exit(EXIT_SUCCESS);
}

static void read_eval_loop(void)
{
    char *command = "";
    bool quit = false;

    while (!quit) {
        rl_bind_key('\t', rl_complete);
        command = readline("> ");
        if (NULL == command)
            break;

        if (strlen(command) > 0 && !is_white(command)) {
            add_history(command);
            quit = prompt_eval(command);
        }
    }
    printf("exiting ...\n");
}

static bool prompt_eval(const char *command__)
{
    bool ret_quit = false;
    int len;
    char **command;

    len = string_split(command__, " ", &command);

    if (!strcmp(command[0], "client")) {
        prompt_command_client(len, command);
    } else if (!strcmp(command[0], "file")) {
        prompt_command_file(len, command);
    } else if (!strcmp(command[0], "help")) {
        prompt_command_help(len, command);
    } else if (!strcmp(command[0], "quit") || !strcmp(command[0], "exit")) {
        ret_quit = true;
    } else {
        printf("unknown command '%s'.\n"
               "try the 'help' command to get started!\n", command[0]);
    }

    for (int i = 0; i < len; ++i)
        free(command[i]);
    free(command);

    return ret_quit;
}

static void prompt_command_client(int len, char **command)
{
    printf("nothing here\n");
}

static void prompt_command_file(int len, char **command)
{
    printf("nothing here\n");
}

static void prompt_command_help(int len, char **command)
{
    printf("nothing here ;'(\n");
}

static char **my_completion(const char *text, int start,  int end)
{
    char **matches;

    matches = (char **)NULL;
    if (start == 0)
        matches = rl_completion_matches((char*)text, &my_generator);
    return matches;
}

char *my_generator(const char* text, int state)
{
    static char *cmd [] = { "quit", "exit", "help" ,"client", "file", NULL };
    static int list_index, len;
    char *name;

    if (!state) {
        list_index = 0;
        len = strlen(text);
    }

    while ((name = cmd[list_index])) {
        list_index++;
        if (strncmp(name, text, len) == 0)
            return (strdup(name));
    }
    /* If no names matched, then return NULL. */
    return ((char *)NULL);
}

/* prompt.c -- code for the command line user interface */

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
#include <signal.h>
#include <setjmp.h>

#include <readline/readline.h>
#include <readline/history.h>

#include "client.h"
#include "file.h"
#include "cutil/error.h"
#include "cutil/string2.h"
#include "config.h"

#define CHK(mesg, X_) do { if ((X_) < 0) {                      \
            rz_error("%s: %s\n", #mesg, strerror(errno));       \
            exit(EXIT_FAILURE);} } while(0)

static bool eval(const char *command);
static void read_eval_loop(void);
static char *my_generator(const char*,int);
static char **my_completion(const char*, int ,int);

static void prompt_command_client(int len, char **command);
static void prompt_command_file(int len, char **command);
static void prompt_command_help(int len, char **command);
static void prompt_command_peers(int len, char **command);
static void prompt_command_ls(void);

static bool is_white(const char *str)
{
    for (int i = 0; str[i]; ++i)
        if (!isblank(str[i]))
            return false;
    return true;
}

static sigjmp_buf ctrlc_buf;
static bool back_from_signal = false;

static void ctrlc_handler(int signo)
{
    if (SIGINT == signo) {
        back_from_signal = true;
        siglongjmp(ctrlc_buf, 1);
    }
}

// this is the ENTRY POINT in the prompt thread
void command_prompt(void)
{
    if ( option_daemonize() )
        return;

    rz_debug(_("Prompt thread started\n"));
    printf(_("type 'help' to get started!\n"));

    struct sigaction sa;
    sa.sa_handler = &ctrlc_handler;
    sa.sa_flags = 0;
    sigemptyset(&sa.sa_mask);
    sigaction(SIGINT, &sa, NULL);

    read_eval_loop();
    exit(EXIT_SUCCESS);
}

static void setup_readline(void)
{
    rl_catch_signals = 0;
    rl_clear_signals();

    rl_attempted_completion_function = my_completion;
    rl_bind_key('\t', rl_complete);
}

static void read_eval_loop(void)
{
    char *command = "";
    bool quit = false;

    setup_readline();
    while (sigsetjmp(ctrlc_buf, 1) != 0);
    if (back_from_signal) {
        back_from_signal = false;
        rl_reset_terminal(NULL);
        printf("^C\n");
    }

    while (!quit) {
        command = readline("> ");
        if (NULL == command)
            break;
        if (strlen(command) > 0 && !is_white(command)) {
            add_history(command);
            quit = eval(command);
        }
    }
    printf(_("exiting ...\n"));
}

static bool eval(const char *command_str)
{
    bool ret_quit = false;
    int len;
    char **command;

    if ('!' == command_str[0]) {
        system(command_str+1);
    } else {
        len = string_split(command_str, " ", &command);

        #define EVAL_I(cmd, val, instr) if (!strcmp(cmd[0], val)) {   instr; }
        #define EVAL_EI(cmd, val, instr)        \
            else if (!strcmp(cmd[0], val))      \
            {   instr; }
        #define EVAL_E(instr) else {  instr; }

        EVAL_I(command, "client", prompt_command_client(len, command))
            EVAL_EI(command, "file", prompt_command_file(len, command))
            EVAL_EI(command, "help", prompt_command_help(len, command))
            EVAL_EI(command, "ls", prompt_command_ls())
            EVAL_EI(command, "peers", prompt_command_peers(len, command))
            #ifdef RUDE_LOLZ
            EVAL_EI(command, "tg", printf("T'es fier de toi?\n"))
            EVAL_EI(command, "TG", printf("T'es fier de toi?\n"))
            EVAL_EI(command, "Tg", printf("T'es fier de toi?\n"))
            EVAL_EI(command, "tG", printf("T'es fier de toi?\n"))
            EVAL_EI(command, "fuck", printf("TG\n"))
            EVAL_EI(command, "lol", printf("lel\n"))
            EVAL_EI(command, "lel", printf("loul\n"))
            EVAL_EI(command, "loul", printf("lol\n"))
            EVAL_EI(command, "lulz", printf("TG\n"))
            #endif
            EVAL_EI(command, "whoami", printf("tracker\n"))
            EVAL_EI(command, "quit", ret_quit = true)
            EVAL_EI(command, "exit", ret_quit = true)
            EVAL_E( printf(
                _("unknown command '%s'.\n"
                  "try the 'help' command to get started!\n"), command[0]));
        #undef EVAL_I
        #undef EVAL_EI
        #undef EVAL_E

        for (int i = 0; i < len; ++i)
            free(command[i]);
        free(command);
    }

    return ret_quit;
}

static const char *get_command_help(const char *cmd)
{
    const char *quit_str =
        _("\tNo help for this command. Sorry.\n"
          "\tBut this one should be rather obvious ;)");

    #define GET_I(cmd, val, str) if (!strcmp(cmd, val)) {   return str; }
    #define GET_EI(cmd, val, str) else if (!strcmp(cmd, val)) { return str; }
    #define GET_E(str) else {  return str; }

    GET_I(cmd, "client",  _("\tclient list: print a list of connected client"))
        GET_EI(cmd, "file",
               _("\tfile list: print a list of known files\n"
                 "\tfile peers filename: print peers for file 'filename'"))
        GET_EI(cmd, "help", _("\thelp cmd: display the help for 'cmd'"))
        GET_EI(cmd, "quit", quit_str)
        GET_EI(cmd, "exit", quit_str)
        GET_E( _("\tNo help for this command. Sorry."));
    #undef GET_I
    #undef GET_EI
    #undef GET_E
}

static void print_client_list(void)
{
    struct list *cl = client_list();
    unsigned l = list_size(cl);
    if (l == 0) {
        puts(_("No clients."));
        return;
    }

    for (unsigned i = 1; i <= l; ++i) {
        struct client *c = list_get(cl, i);
        printf(_("#%d: %s listening on port %hu\n"), i,
               ip_stringify(c->addr.sin_addr.s_addr),
               c->listening_port);
    }
}

static void print_file_list(void)
{
    struct list *cl = file_list();
    unsigned l = list_size(cl);
    if (l == 0) {
        puts(_("No files."));
        return;
    }

    for (unsigned i = 1; i <= l; ++i) {
        struct file *f = list_get(cl, i);
        printf(_("#%d: %s | %s | %lu B | %u p | ps: %u\n"),
               i, f->filename, f->md5_str, f->length,
               f->piece_count, f->piece_size);
    }
}

static void prompt_command_client(int len, char **command)
{
    if (len >= 2 && !strcmp(command[1], "list")) {
        print_client_list();
        return;
    }
    printf("%s\n", get_command_help("client"));
}

static void print_peer_list(char *file_id)
{
    struct list *l = file_get_by_name(file_id);
    if (l != NULL) {
        int size = list_size(l);
        for (int j = 1; j <= size; ++j) {
            struct file *f;
            f = list_get(l, j);
            file_print_peer_list(f);
        }
    } else {
        struct file *f = file_get_by_key(file_id);
        if (f != NULL)
            file_print_peer_list(f);
        else {
            printf(_("No such file: %s\n"), file_id);
        }
    }
}

static void prompt_command_peers(int len, char **command)
{
    if (len == 1) {
        print_client_list();
        return;
    }
    for (int i = 1; i < len; ++i)
        print_peer_list(command[i]);
}

static void prompt_command_file(int len, char **command)
{
    if (len >= 2 && !strcmp(command[1], "list")) {
        print_file_list();
    } else if (len >= 3 && !strcmp(command[1], "peers")) {
        prompt_command_peers(len-1, command+1);
    } else {
        printf("%s\n", get_command_help("file"));
    }
}

static void prompt_command_help(int len, char **command)
{
    if (len > 1) {
        for (int i =1; i < len; ++i) {
            printf("%s:\n%s\n", command[i], get_command_help(command[i]));
        }
    } else {
        printf(_("Press [tab][tab] (tabulation twice) to see"
                 " a list of available commands\n"
                 "Run 'help cmd' to get help on the given argument `cmd´\n"));
    }
}

static void prompt_command_ls(void)
{
    char *cmd[] = {"", "list"};
    printf("File list:\n");
    prompt_command_file(2, cmd);
    printf("\nPeer list:\n");
    prompt_command_client(2, cmd);
}

// the two following function are for readline customizable autocompletion
static char **my_completion(const char *text, int start,  int end)
{
    char **matches;

    matches = (char **)NULL;
    if (start == 0)
        matches = rl_completion_matches((char*)text, &my_generator);
    return matches;
}

static char *my_generator(const char* text, int state)
{
    static char *cmd [] = {
        "quit", "exit", "help" ,"client",
        "file", "peers", "ls", "whoami", NULL
    };
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

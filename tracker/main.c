#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <unistd.h>
#include <string.h>
#include <errno.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <locale.h>

#include "util.h"
#include "config.h"
#include "cutil/hash_table.h"
#include "network.h"
#include "cutil/error.h"

static void deamonize(void)
{
    pid_t p = fork();
    if (p == 0) {
        pid_t sid = setsid();
        if (sid < 0) {
            perror("setsid");
            exit(EXIT_FAILURE);
        }
        fputs(_("Daemonizing ...\n"), stderr);
        int fd = open("./tracker.log", O_CREAT | O_APPEND | O_RDWR, 0600);
        close(STDIN_FILENO);
        dup2(fd, STDOUT_FILENO);
        dup2(fd, STDERR_FILENO);
        close(fd);
    } else {
        exit(EXIT_SUCCESS);
    }
}

static void start_command_prompt_thread(void)
{
    extern void* command_prompt(void*);
    start_detached_thread(&command_prompt, NULL, _("command prompt"));
}

static void init_locale(void)
{
    setlocale(LC_ALL, "");
    bindtextdomain("rz_trackme", ".");
    textdomain("rz_trackme");
}

int main(int argc, char *argv[])
{
    uint16_t port;

    init_locale();
    parse_options(&argc, &argv);
    load_config_file();
    if ( option_daemonize() ) {
        deamonize();
    } else {
        start_command_prompt_thread();
    }

    port = option_get_port();
    server_run_bind_any_addr(port);
    return 0;
}

#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <unistd.h>
#include <string.h>
#include <errno.h>
#include <sys/stat.h>
#include <fcntl.h>

#include "config.h"
#include "cutil/hash_table.h"
#include "network.h"


static void deamonize(void)
{
    pid_t p = fork();
    if (p == 0) {
        pid_t sid = setsid();
        if (sid < 0) {
            perror("setsid");
            exit(EXIT_FAILURE);
        }
        fputs("Daemonizing ...\n", stderr);
        int fd = open("/dev/null", O_RDWR);
        dup2(fd, STDIN_FILENO);
        dup2(fd, STDOUT_FILENO);
        dup2(fd, STDERR_FILENO);
        close(fd);
    } else {
        exit(EXIT_SUCCESS);
    }
}

int main(int argc, char *argv[])
{
    uint16_t port;
    parse_options(&argc, &argv);
    load_config_file();
    if ( option_daemonize() )
        deamonize();
    
    port = option_get_port();
    server_run_bind_any(port);
    return 0;
}

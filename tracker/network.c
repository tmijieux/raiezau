/* network.c -- most of code that deals with the socket API */

#define _GNU_SOURCE
#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <stdbool.h>
#include <string.h>
#include <signal.h>
#include <pthread.h>
#include <errno.h>
#include <poll.h>

#include "util.h"
#include "protocol.h"
#include "client.h"
#include "cutil/error.h"

#define HEURISTIC_SIZE 3072
#define INITIAL_PEERFD_BUFSIZE 1000
#define ever true

pthread_t network_thread;
int nfds = 1;
uint32_t fds_buffer_size = INITIAL_PEERFD_BUFSIZE;
struct pollfd *fds = NULL;
static struct client **clients = NULL;

#define CHK(mesg, X_) do { if ((X_) < 0) { perror(#mesg);       \
            exit(EXIT_FAILURE);} } while(0)

char *sockaddr_stringify(const struct sockaddr_in *si)
{
    char *socket_str;
    char addr[20] = {0};
    inet_ntop(AF_INET, &si->sin_addr.s_addr, addr, 19);
    asprintf(&socket_str, "%s:%hu", addr, ntohs(si->sin_port));
    return socket_str;
}

char *ip_stringify(uint32_t ip_addr)
{
    char addr[20] = {0};
    inet_ntop(AF_INET, &ip_addr, addr, 19);
    return strdup(addr);
}

static int make_listener_socket(uint32_t addr, uint16_t port, int *sock)
{
    int s;
    struct sockaddr_in si = { 0 };
    int yes[1] = { 1 };

    CHK(socket, s = socket(AF_INET, SOCK_STREAM, 0));
    CHK(setsockopt, setsockopt(
        s, SOL_SOCKET, SO_REUSEADDR, yes, sizeof yes[0]));

    si.sin_family = AF_INET;
    si.sin_port = htons(port);
    si.sin_addr.s_addr = addr;

    CHK(bind, bind(s, (const struct sockaddr*) &si, sizeof si));
    CHK(listen, listen(s, SOMAXCONN));
    rz_debug("server listening on socket %s\n", sockaddr_stringify(&si));

    *sock = s;
    return 1;
}

static void start_server_reqhandler_thread(struct client *c)
{
    if (NULL != c) {
        client_inc_ref(c);
        start_detached_thread((void*(*)(void*))handle_request, c, "request");
    } else {
        rz_debug("invalid client\n");
    }
}

static int accept_connection(int listener)
{
    int accept_s;
    struct sockaddr_in accept_si = { 0 };
    socklen_t size = sizeof accept_si;

    accept_s = accept(listener, (struct sockaddr*)&accept_si, &size);
    if (accept_s < 0) {
        if (EINTR == errno)
            return -1;
        perror("accept");
        return -1;
    }

    return accept_s;
}

static void add_new_client_to_fds(struct client *c)
{
    if (nfds+1 > fds_buffer_size) {
        fds_buffer_size *= 2;
        fds = realloc(fds, sizeof*fds * fds_buffer_size);
        clients = realloc(clients, sizeof*clients * fds_buffer_size);
    }

    rz_debug("adding client to slot %d\n", nfds);

    client_inc_ref(c);
    clients[nfds] = c;
//    c->fds_slot = nfds;

    fds[nfds].fd = c->socket;
    fds[nfds].events = POLLIN | POLLRDHUP;
    fds[nfds].revents = 0;
    ++nfds;
}

static void handle_new_connection(int listener)
{
    int socket;
    struct client *c;

    socket = accept_connection(listener);
    c = client_get_or_create(socket);
    add_new_client_to_fds(c);
}

static void handle_incoming_data(void)
{
    for (int i = 1; i < nfds; ++i) {
        if (fds[i].fd < 0)
            continue;

        if (clients[i]->can_rehear) {
            clients[i]->can_rehear = 0;
            fds[i].events = POLLIN | POLLRDHUP;
        }

        if ((fds[i].revents & (POLLHUP|POLLRDHUP|POLLERR|POLLNVAL)) != 0) {
            struct client *c = clients[i];
            if (NULL != c) {
                client_dec_ref(clients[i]);
                clients[i] = NULL;

                fds[i].fd = -1;
                fds[i].events = 0;
                fds[i].revents = 0;
            }

        } else if ((fds[i].revents & POLLIN) != 0) {
            fds[i].events = POLLRDHUP;
            fds[i].revents = 0;
            rz_debug("start req handler thread\n");
            start_server_reqhandler_thread(clients[i]);
        }
    }
}

static void block_all_signals_but_usr1(void)
{
    sigset_t set;
    sigfillset(&set);
    sigdelset(&set, SIGUSR1);
    pthread_sigmask(SIG_SETMASK, &set, NULL);
}

static void usr1_handler(int sig)
{
    // nothing here: just useful to interrupt the poll syscall
}

static void install_network_signal_handler(void)
{
    struct sigaction sa;

    sa.sa_handler = &usr1_handler;
    sa.sa_flags = 0;
    sigemptyset(&sa.sa_mask);
    sigaction(SIGUSR1, &sa, NULL);
}

static void clean_fds(void)
{
    for (int i = 1; i < nfds; ++i) {
        while (fds[i].fd == -1) {
            if (i == nfds-1) {
                --nfds;
                fds[i].fd = -2; // make the condition false to break out the loop
            } else {
                fds[i] = fds[nfds-1];
                clients[i] = clients[nfds-1];
                --nfds;
            }
        }
    }
}

void server_run(uint32_t addr, uint16_t port)
{
    int listener, ret;
    network_thread = pthread_self();
    block_all_signals_but_usr1();
    install_network_signal_handler();
    // I think it is important that poll never get interrupted by signals!=USR1

    make_listener_socket(addr, port, &listener);
    fds = malloc(sizeof*fds * fds_buffer_size);
    clients = malloc(sizeof*clients * fds_buffer_size);

    // put the listener socket in the first slot of the pollfds
    fds[0].fd = listener;
    fds[0].events = POLLIN;

    for (;ever;) {
        // wait for an event
        ret = poll(fds, nfds, -1);
        if (ret < 0) {
            if (errno != EINTR) {
                rz_error("poll: %s\n", strerror(errno));
                exit(EXIT_FAILURE);
            }
            //rzerror(_("POLL INTERRUPTED: %s\n"), strerror(errno));
        }
        rz_debug("poll event !!\n");
        if ((fds[0].revents & POLLIN) != 0) {
            // if the event is on the first slot, there is a new connection
            rz_debug("handle new connection\n");
            handle_new_connection(listener);
        } else {
            rz_debug("no new connection\n");
        }
        handle_incoming_data();
        //handle_pending_clients();
        clean_fds();
        rz_debug("\n\n---------------------------------\n");
    }

    rz_debug("no new connection\n");
}

void server_run_bind_any_addr(uint16_t port)
{
    server_run(INADDR_ANY, port);
}

int socket_read_string(int sock, char **ret_str)
{
    int r, str_size= 0, str_bufsize = HEURISTIC_SIZE;
    char buf[1024] = {0};
    char *str = calloc(HEURISTIC_SIZE, 1);

    // loop that read and allocate sufficient memory
    // This end when a '\0' or '\n' is read at the very end of input
    do {
        r = read(sock, buf, 1000);
        if (r <= 0) {
            *ret_str = NULL;
            return -1;
        }
        buf[r] = 0;
        if (str_size + r > str_bufsize) {
            str = realloc(str, 2*str_bufsize);
            str_bufsize *= 2;
        }
        str_size += r;
        strcat(str, buf);
        rz_debug(_("terminating character is '#%d' : (%s)\n"), buf[r-1],
                 buf[r-1] == '\0' ? "nul"
                 : (buf[r-1] == '\n' ? _("newline") : _("unknown")));
    } while ( buf[r-1] != '\0' && buf[r-1] != '\n' );

    // remove '\n' at the end of the line
    while (str_size-1 >= 0 && str[str_size-1] == '\n') {
        str[str_size-1] = '\0';
        --str_size;
    }

    *ret_str = str;
    return str_size;
}

void socket_write_string(int sock, size_t len, const char *str)
{
    if (write(sock, str, len) != len)
        rz_error(_("bad write: %s"), strerror(errno));
}


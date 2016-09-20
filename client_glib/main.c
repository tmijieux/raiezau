#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <glib.h>

#include "./socket.h"

#define CHKP(error, X_) do { if ((X_ ) == NULL) {       \
            fprintf(stderr, "%s\n", error->message);    \
            exit(EXIT_FAILURE);} } while(0)

#define CHKI(error, X_) do { if ((X_ ) < 0) {           \
            fprintf(stderr, "%s\n", error->message);    \
            exit(EXIT_FAILURE);} } while(0)

static GSocket *
make_non_blocking_listener(char const *addr, uint16_t port)
{
    GError *error;
    GSocket *s;
    GSocketAddress *address;

    CHKP( error, s = g_socket_new(
        G_SOCKET_FAMILY_IPV4, G_SOCKET_TYPE_STREAM, 0, &error) );
    address = g_inet_socket_address_new_from_string(addr, port);
    CHKI( error, g_socket_bind(s, address, TRUE, &error) );
    CHKI( error, g_socket_listen(s, &error) );

    g_socket_set_blocking(s, FALSE);
    return s;
}

static void
attach_listener(GSocket *listener, GMainContext *context)
{
    GSource *source;

    source = g_socket_create_source(listener, G_IO_IN, NULL);
    g_source_set_callback(
        listener, (GSourceFunc) listener_accept_cb, context, NULL);
    g_source_attach(source, context);
}

int main(int argc, char * const argv[])
{
    GSocket *listener;
    GMainContext *context;
    GMainLoop *main_loop;

    listener = make_non_blocking_listener("0.0.0.0", 8080);
    context = g_main_context_new();
    attach_listener(listener, context);
    main_loop = g_main_loop_new(context, FALSE);

    g_main_loop_run(main_loop);

    return EXIT_SUCCESS;
}

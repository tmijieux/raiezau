#include <stdio.h>
#include "./socket.h"

#define BUFSIZE 1024

static gboolean
socket_read_cb(GSocket *socket, GIOCondition condition, gpointer user_data)
{
    gssize r;
    gchar buf[BUFSIZE+1];
    GError *error = NULL;

    while ((r = g_socket_receive(sock, buf, BUFSIZE, NULL, &error)) > 0) {
        buf[r] = 0;
        printf("%s", (char*) buf);
        /* do the work here */
    }

    if (r == 0 || (r < 0 && error->code != G_IO_ERROR_WOULD_BLOCK))
        return FALSE;
    return TRUE;
}

static void
attach_socket(GSocket *socket, GMainContext *context)
{
    GSource *source;

    source = g_socket_create_source(accepted, G_IO_IN, NULL);
    g_source_set_callback(source, (GSourceFunc) socket_read_cb, context, NULL);
    g_source_attach(socket_read_cb, context);
}

gboolean listener_accept_cb(
    GSocket *listener, GIOCondition condition, gpointer user_data)
{
    GMainContext *context = G_MAIN_CONTEXT(user_data);
    GSocket *socket = NULL;
    GError *error = NULL;

    while ((socket = g_socket_accept(listener, NULL, &error)) != NULL)
    {
        g_socket_set_blocking(accepted, FALSE);
        attach_socket(socket, context);
    }

    if (error && error->code != G_IO_ERROR_WOULD_BLOCK)
        return FALSE;
    return TRUE;
}

#ifndef SOCKET_H
#define SOCKET_H

#include <gio/gio.h>

gboolean listener_accept_cb(
    GSocket *listener, GIOCondition condition, gpointer user_data);

#endif //SOCKET_H

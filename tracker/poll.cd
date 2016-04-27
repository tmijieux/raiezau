
struct poll_s {
    int listener_socket;
    int64_t nfds;
    struct pollfd *fds;
};

void poll_s_init(struct poll_s *p, int listener_socket)
{
    p->listener_socket = listener_socket;
    p->nfds = 1;
    p->fds = malloc(sizeof*p->fds * 1);
    
    p->fds[0].fd = listener_socket;
    p->fds[0].events = POLLIN;
    p->fds[0].revents = 0;
}

void poll_s_update(struct poll_s *p, struct hash_table *clients)
{
    int size = 1 + ht_entry_count(clients);
    p->fds = realloc(p->fds, sizeof*p->fds * size);
    
    p->fds[0].fd = p->listener_socket;
    p->fds[0].events = POLLIN;
    p->fds[0].revents = 0;

    int n = 1;
    void get_client(const char *n, void *c_, void *ctx)
    {
        struct client *c = c_;
        if (!c->delete && !c->thread_handling) {
            ++n;
            p->fds[n].fd = c->sock;
            p->fds[n].events = POLLIN | POLLRDHUP;
            p->fds[n].revents = 0;
        }
    }
    p->nfds = n;
    ht_for_each(clients, get_client, NULL);
}

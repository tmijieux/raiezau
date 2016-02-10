#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <string.h>
#include <ctype.h>
#include <getopt.h>

#include "config.h"
#include "cutil/hash_table.h"

extern char *optarg;
extern int optind, opterr, optopt;
static struct hash_table *options;


__attribute__((constructor))
static void config_init(void)
{
    options = ht_create(0, NULL);
}

void load_config_file(void)
{
    
}

bool option_daemonize(void)
{
    uintptr_t daemon;
    if (ht_get_entry(options, "daemon", &daemon) < 0)
        return OPTION_DAEMONIZE_DEFAULT;
    return (bool) daemon;
}

uint16_t option_get_port(void)
{
    char *port;
    if (ht_get_entry(options, "port", &port) < 0)
        return OPTION_PORT_DEFAULT;
    return atoi(port);
}

static struct option option[] = {
    { "help", 0, NULL, 'h' },
    { "version", 0, NULL, 'v' },
    { "daemon", 0, NULL, 'd' },
    
    { "port", 1, NULL, 'p' },
    { "conf", 1, NULL, 'c' },

    { 0 }
};

void print_version(void)
{
    printf("Rz is v%d.%d.%d\n",
	   VERSION_MAJOR_NUMBER,
	   VERSION_MINOR_NUMBER,
	   VERSION_REVISION_NUMBER);
}

void print_help(void)
{
    puts("Rz help:\n"
	 "All options are optional! ;)\n\n"

	 "\t-p N, --port=N\n"
	 "\t\tset tracker listening port N\n"
	 "\t\tdefault is 8000\n\n"

	 "\t-c N, --conf=N\n"
	 "\t\tset the configuration file path N\n"
	 "\t\tdefault is working directory\n\n"
	 
	 "\t-v, --version\n"
	 "\t\tshow rz' version\n\n"

	 "\t-h, --help\n"
	 "\t\tshow this help\n"
    );
}

static int character_is_in_string(int c, char *str)
{
    int i;
    for (i = 0; str[i]; ++i)
	if (c == str[i])
	    return 1;
    return 0;
}

static char *rdstr[] = {
    "rand",
    "random",
    "randomize"
};

static int optarg_is_random(void)
{

    int l = sizeof(rdstr) / sizeof(rdstr[0]);
    for (int i = 0; i < l; i++) {
	if (!strcmp(rdstr[i], optarg))
	    return 1;
    }
    return 0;
}

static int string_is_positive_integer(const char *str)
{
    int i;
    for (i = 0; str[i]; ++i)
	if (!isdigit(str[i]))
	    return 0;
    return 1;
}

#define SET_OPT(field, value)                         \
    ht_add_entry(options, field, value)

void parse_options(int *argc, char ***argv)
{
    int c;
    
    while ((c = getopt_long(*argc, *argv, "+vhc:p:", option, NULL)) != -1) {
	switch (c) {
	case 'h':
	    print_help();
	    exit(EXIT_SUCCESS);
	    break;
	case 'v':
	    print_version();
	    exit(EXIT_SUCCESS);
	    break;
	case 'c':
	    SET_OPT("conf", optarg);
	    break;
	case 'p':
	    SET_OPT("port", optarg);
	    break;
	case '?':
	    exit(EXIT_FAILURE);
	    break;
	}
    }

    *argc -= optind - 1;
    *argv += optind - 1;
}

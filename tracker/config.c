#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <string.h>
#include <unistd.h>
#include <ctype.h>
#include <getopt.h>

#include "config.h"
#include "cutil/hash_table.h"
#include "cutil/yaml2.h"
#include "cutil/error.h"

extern char *optarg;
extern int optind, opterr, optopt;
static struct hash_table *options;


__attribute__((constructor))
static void config_init(void)
{
    options = ht_create(0, NULL);
}

const char *option_get_config_file_path(void)
{
    char *path;
    if (ht_get_entry(options, "conf", &path) < 0)
        return OPTION_CONF_PATH_DEFAULT;
    return path;
}


static void load_option_from_config(
    const char *key,
    void *value_,
    void *context_ __attribute__((unused))  )
{
    struct yaml_wrap *value = value_;
    
    if (!ht_has_entry(options, key)) {
        if (value->type == YAML_SCALAR) { 
            ht_add_entry(options, key, value->content.scalar);
            rz_debug(
                "option '%s' loaded from config with value '%s'\n",
                key, (char*) value->content.scalar);
        }
    }
}

void load_config_file(void)
{
    int ret;
    struct yaml_wrap *yw;
    const char *path = option_get_config_file_path();
    if (access(path, F_OK) != 0) {
        rz_debug("%s: %s\n", path, strerror(errno));
        return; // file doesn't exist; say nothing and skip
    }
    
    ret = yaml2_parse_file(&yw, path);
    if (ret < 0 || yw->type != YAML_MAPPING) {
        fprintf(stderr, "Invalid configuration file %s. Skipping...\n", path);
        return;
    }

    ht_for_each(yw->content.mapping, &load_option_from_config, NULL);
    rz_debug("Loaded config file %s\n", path);
}

bool option_daemonize(void)
{
    char *daemonize;
    if (ht_get_entry(options, "daemonize", &daemonize) < 0)
        return OPTION_DAEMONIZE_DEFAULT;
    return (bool) atoi(daemonize);
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
	 "\t\tset tracker listening port to N\n"
	 "\t\tdefault is 8000\n\n"

	 "\t-c N, --conf=N\n"
	 "\t\tset the configuration file path to N\n"
	 "\t\tdefault is `. (working directory)\n\n"
	 
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

#define SET_OPT(field, value)                   \
    ht_add_entry(options, field, value)

void parse_options(int *argc, char ***argv)
{
    int c;
    
    while ((c = getopt_long(*argc, *argv, "+vhdc:p:", option, NULL)) != -1) {
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
            if (access(optarg, F_OK) != 0) {
                perror(optarg);
                exit(EXIT_FAILURE);
            }
	    SET_OPT("conf", optarg);
	    break;
	case 'p':
	    SET_OPT("port", optarg);
	    break;
        case 'd':
	    SET_OPT("daemonize", "1");
	    break;
	case '?':
	    exit(EXIT_FAILURE);
	    break;
	}
    }

    *argc -= optind - 1;
    *argv += optind - 1;
}

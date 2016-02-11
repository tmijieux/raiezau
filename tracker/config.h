/* config.h -- functions in config.c and default option values */

#ifndef CONFIG_H
#define CONFIG_H

#include <stdbool.h>
#include <stdint.h>

#define OPTION_DAEMONIZE_DEFAULT ((bool) false)
#define OPTION_PORT_DEFAULT      ((uint16_t)8000)
#define OPTION_CONF_PATH_DEFAULT      ((const char *) "./config.yaml")

#define VERSION_MAJOR_NUMBER  0
#define VERSION_MINOR_NUMBER  1
#define VERSION_REVISION_NUMBER  1

void load_config_file(void);
void parse_options(int *argc, char ***argv);

bool option_daemonize(void);
uint16_t option_get_port(void);

#endif //CONFIG_H

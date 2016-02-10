#ifndef CONFIG_H
#define CONFIG_H

#include <stdbool.h>

void load_config_file(void);
void parse_options(int *argc, char ***argv);

bool option_daemonize(void);

#endif //CONFIG_H

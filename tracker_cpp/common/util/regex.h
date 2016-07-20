#include <regex.h>

int regex_exec(const char *regexp, const char *str,
               size_t nmatch, regmatch_t pmatch[]);

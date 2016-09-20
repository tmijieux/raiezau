
#include "./regex.h"

int regex_exec(const char *regexp, const char *str,
               size_t nmatch, regmatch_t pmatch[])
{
    int err = 0;
    regex_t reg;

    err = regcomp(&reg, regexp, REG_EXTENDED | REG_ICASE);
    if (err != 0) {
        char err_s[500] = {0};
        regerror(err, &reg, err_s, 500);
        rz_error(_("regexp compilation: %s\n"), err_s);
        err = -1;
    } else {
        if (regexec(&reg, str, nmatch, pmatch, 0) != 0) {
            rz_debug(_("regex doesn't match: '%s'\npattern was '%s'\n"),
                     str, regexp);
            err = -1;
        }
        regfree(&reg);
    }
    return err;
}

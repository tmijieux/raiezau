#ifndef COLOR_H
#define COLOR_H

#define RED_COLOR "31;1"
#define GREEN_COLOR "32"
#define RESET_COLOR "0"
#define COLOR(__c, __s) ESCAPE(__c) __s ESCAPE(RESET)
#define ESCAPE(__s) "\x1B[" __s##_COLOR "m"


#endif //COLOR_H

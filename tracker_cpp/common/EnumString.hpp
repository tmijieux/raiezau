#ifndef ENUM_STRING_CPP_H
#define ENUM_STRING_CPP_H

#include <unordered_map>
#include <sstream>

#define to_EnumString(table)      table insert_in_table
#define insert_in_table(id, value)    [value] = #id;
#define insert_in_enum(id, value)    id = value,

typedef std::unordered_map<uint32_t, std::string> EnumString;

#define make_enum(enum_name)                                            \
    enum enum_name {                                                    \
        enum_name(insert_in_enum)                                       \
    };                                                                  \
    extern EnumString enum_name##_string;                               \
    static inline std::string lookup_##enum_name##_name(uint32_t id)    \
    {                                                                   \
        auto itr = enum_name##_string.find(id) ;                        \
        if (itr == enum_name##_string.end()) {                          \
            std::stringstream ss;                                       \
            ss << "invalid '"#enum_name"' 0x" <<std::hex<<id;           \
            return ss.str();                                            \
        }                                                               \
        return itr->second;                                             \
    }                                                                   \

#define make_enum_string(enum_name)                     \
    static EnumString make_##enum_name(void)            \
    {                                                   \
        EnumString enum_##enum_name;                    \
        enum_name(to_EnumString(enum_##enum_name));     \
        return enum_##enum_name;                        \
    }                                                   \
    EnumString enum_name##_string = make_##enum_name()  \

#endif //ENUM_STRING_CPP_H

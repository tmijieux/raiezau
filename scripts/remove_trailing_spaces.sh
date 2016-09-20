#!/bin/bash

find . \( -name '*.h' -o -name '*.c' \) -print0 | xargs -i -r -0 sed -r -i 's/\s*$//' {}
find . \( -name '*.hpp' -o -name '*.cpp' \) -print0 | xargs -i -r -0 sed -r -i 's/\s*$//' {}
find . -name 'Makefile'  -print0 | xargs -i -r -0 sed -r -i 's/\s*$//' {}

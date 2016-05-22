#!/bin/bash

cd $(dirname $0)

yaml=yaml-0.1.5

install_yaml_dep() {
    prefix=$(pwd)/usr
    inc=$prefix/include
    lib=$prefix/lib
    
    tar xvf $yaml.tar.gz
    pushd $yaml > /dev/null
    ./configure --prefix $prefix
    make && make install

    cat <<EOF
Write this to your bashrc:
    export C_INCLUDE_PATH=\$C_INCLUDE_PATH:$inc
    export LIBRARY_PATH=\$LIBRARY_PATH:$lib
or run source ./yaml_path
EOF
    popd > /dev/null
    cat > ./yaml_path <<EOF
    export C_INCLUDE_PATH=\$C_INCLUDE_PATH:$inc
    export LIBRARY_PATH=\$LIBRARY_PATH:$lib
EOF
}


tmpfile=$(mktemp)
cat > $tmpfile <<EOF
#include "yaml.h"
int main(void) 
{
    return 0;
}
EOF

gcc -x c $tmpfile -o /dev/null && 
ok=$?

[[ "$ok" == "0" ]] && echo "yaml dep is ok"
[[ "$ok" != "0" ]] && (echo "yaml dep is NOT ok"; install_yaml_dep)

rm -rf $tmpfile
rm -rf $yaml


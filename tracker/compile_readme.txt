    
    ligne de compilation:
        make

    avec les messages de debug intéressant:
        export DEBUG=true
        make -B

    dependances de l'application:
        - libyaml
        - libreadline
        - libgc (pas encore réellement utilisé on peut 
                le desactiver assez facilement en renommant le fichier alloc.c 
                et en adaptant les CFLAGS  )
        - libcrypto (openssl)


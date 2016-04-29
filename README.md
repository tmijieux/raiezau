## RAIE - ZAU

# lancements des programmes:
compilation et lancement du tracker:
    au cas ou yaml manque:
    ./deps_install.sh

    then:
    cd tracker; make; ./trackme

# scripts
extract_i18n: 
    extrait les chaines du tracker qui sont candidates
    pour l'internationalization

embedder.py:
    script qui permet de convertir du binaire en c, afin
    d'integrer un fichier quelconque directement dans un 
    executable facilement

update_tags:
    creer un fichier de TAG au format d'emacs pour parcourir 
    le code plus rapidement dans emacs.

color.py:
    affiche et formatte les différentes couleurs supportés par le terminal
    avec le code pour les utiliser

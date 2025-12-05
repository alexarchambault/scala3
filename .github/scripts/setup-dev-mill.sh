#!/usr/bin/env bash
set -e

MILL_COMMIT="7be4725cfd"

mkdir thing
cd thing
git clone https://github.com/alexarchambault/mill.git -b scala3 mill-scala3
cd mill-scala3
git switch --detach "$MILL_COMMIT"
./mill -i dist.installLocal
cd ..
cd ..

ln -sf thing/mill-scala3/mill-assembly.jar mill

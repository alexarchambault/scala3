#!/usr/bin/env bash
set -e

MILL_COMMIT="3c5e636923ab"

git clone https://github.com/alexarchambault/mill.git -b scala3 mill-scala3
cd mill-scala3
git switch --detach "$MILL_COMMIT"
./mill -i dist.installLocal
cd ..

ln -sf mill-scala3/mill-assembly.jar mill

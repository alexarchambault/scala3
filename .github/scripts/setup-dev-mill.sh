#!/usr/bin/env bash
set -e

git clone https://github.com/alexarchambault/mill.git -b scala3 mill-scala3
cd mill-scala3
./mill -i dist.installLocal
cd ..

ln -sf mill-scala3/mill-assembly.jar mill

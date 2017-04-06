#!/usr/bin/sh
ant make_package -Dpackage=vfdr-1.0.0 -buildfile build_package.xml

git add * > /dev/null
git status
git commit -m "Built package"

git push origin :refs/tags/v1.0.0
git tag -fa v1.0.0
git push origin master --tags


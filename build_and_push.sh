#!/bin/bash
ant make_package -Dpackage=vfdr-1.0.0 -buildfile build_package.xml > /dev/null

git commit -aqm "$0"

git push origin :refs/tags/v1.0.0 -q
git tag -fa v1.0.0
git push origin master --tags

echo "You must now update the release on Github"


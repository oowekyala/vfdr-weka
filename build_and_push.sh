#!/usr/bin/sh
ant make_package -Dpackage=vfdr-1.0.0 -buildfile build_package.xml > /dev/null

git add * 
git commit -qm "$0"

git push origin :refs/tags/v1.0.0 -q
git tag -qfa v1.0.0
git push origin master --tags

echo "You must now update the release on Github"[D[D


# This script packages the algorithm to weka format
# Powershell version >= 5.0

# It requires the following files in the .\dist\$version directory to succeed: 
# --- Correct build_package.xml ant file and Description.props (downloadable from weka doc)
# --- a weka jar in lib subdirectory

$version="1.0.0"
$dist=".\dist\$version"
$top=Get-Location

Write-Output "Initialisation..."

If(Test-Path $dist\src) {Remove-Item  -Force -Recurse $dist\src}
New-Item  -ItemType "directory" $dist\src\main\java\weka\classifiers\rules -Force >$null
New-Item  -ItemType "directory" $dist\src\test\java\weka\classifiers\rules -Force >$null
Copy-Item .\src\weka $dist\src\main\java\ -Force -Recurse
Copy-Item .\src\test\VfdrTest.java $dist\src\test\java\weka\classifiers\rules -Force
If(-Not (Test-Path $dist\doc)) {New-Item  -ItemType "directory" $dist\doc >$null}
If(-Not (Test-Path $dist\lib)) {New-Item  -ItemType "directory" $dist\lib >$null}

Set-Location $dist
	Write-Output "Compilation..."
	ant -buildfile .\build_package.xml >$null

	Write-Output "jar creation..."
	jar cvf VFDR.jar build/classes/* >$null
Set-Location $top

Write-Output "Package archive creation..."
Compress-Archive -Force -Path $dist\* -DestinationPath "$dist\..\vfdr-$version.zip"

Write-Output "Vfdr package version $version successfully created in .\dist"
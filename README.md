# VFDR algorithm for Weka

Implementation of the VFDR algorithm for the **[Weka](http://www.cs.waikato.ac.nz/ml/weka/)** machine-learning platform. The algorithm is described in the following paper:

> João Gama and Petr Kosina. Learning decision rules from data streams. In *Proceedings of the Twenty-Second International Joint Conference on Artificial Intelligence - Volume Two*, IJCAI’11, pages 1255–1260. AAAI Press, 2011.

The algorithm performs **on-line classification** on numeric or nominal attributes. It learns **decision rules**,can perform multiclass classification, and needs **only one pass** on the data to build its model. However, this version **does not support concept drift**.  

## Installing the package
### Via the package manager's GUI

* Open Weka's package manager (in Weka's GUI chooser, `tools > Package manager`)
* Scroll to `vfdr` and click Install

### Via the package manager's CLI

* Assuming `weka.jar` is in the Java classpath, run
```
java weka.core.WekaPackageManager -install-package vfdr
```

### Via a local copy
* Build the package's zip archive or download it from /releases.
* Assuming `weka.jar` is in the Java classpath, run
```
java weka.core.WekaPackageManager -install-package path/to/zip
```


## Building the package

* Update Description.props
* From the top level directory, execute `ant make_package -Dpackage="vfdr-${version}" -buildfile ./build_package.xml`, where ${version} is the version number.
* The created zip will live under /dist

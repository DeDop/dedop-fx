# About

**DeDop FX** is an experimental (=fun) tool which *plays* satellite altimeter data. Currently it can transform the L1B data from the [SRAL](https://sentinel.esa.int/web/sentinel/missions/sentinel-3/instrument-payload/altimetry) 
sensor mounted on the [ESA Sentinel-3](http://www.esa.int/Our_Activities/Observing_the_Earth/Copernicus/Sentinel-3) satellite into 
audio samples. You can get SRAL netCDF sample files (`*.nc`) from the [Sentinel-3A Altimetry Test Data Set](https://sentinel.esa.int/web/sentinel/user-guides/sentinel-3-altimetry/test-data-set). 

![DeDop FX Screenshot](https://github.com/DeDop/dedop-fx/blob/master/screenshot.png)

# License

This software is distributed under the terms and conditions of the [MIT License (MIT)](https://opensource.org/licenses/MIT).


# Developer Guide

## Creating DeDop FX installers

Compile the Java code first. Expected output directories are 
* `out/production` for production Java code in `src`
* `out/production/dedopfx/resources` for resources in `src/dedopfx/resources`
* `out/test` for test Java code `test`

### Windows

Refer to [javapackager](http://docs.oracle.com/javase/8/docs/technotes/tools/unix/javapackager.html) documentation.

1. Install [Inno Setup Compiler](http://www.jrsoftware.org/isinfo.php).
2. Install [WiX tools](http://wixtoolset.org/) and add it (`C:\Program Files (x86)\WiX Toolset v3.10\bin`) to your `PATH`.
3. Open command prompt (`cmd.exe`), then
    ```
        > cd dedop-fx
        > mkwin
    ```
4. To create signed installers, set required environment variables 
    * `CRC_FILE` to a certificate file `*.p12`;
    * `CSC_KEY_PASSWORD` to the password for the certificate
    and run once more.

### Mac OS X

Just run

```
    > cd dedop-fx
    > bash ./mkmac.sh
```

### Linux
   
TODO



# Missing Features

* **Audio output level indicator** that also shows if we clip to lower/upper 16bit limits
* **High CPU load indicator** that warns if computer is too slow to stay above the sample rate of 44100 samples/sec
* **More visualisations**: show all input data as image, show currently played record within it


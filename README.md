# About

**DeDopFX** is an experimental (=fun) tool which converts satellite altimeter measurement data into *sound*. Currently it can transform the L1B data from the [SRAL](https://sentinel.esa.int/web/sentinel/missions/sentinel-3/instrument-payload/altimetry) 
sensor mounted on the [ESA Sentinel-3](http://www.esa.int/Our_Activities/Observing_the_Earth/Copernicus/Sentinel-3) satellite into 
audio samples. 

DeDopFX can play the L1B NetCDF output files (`*.nc`) from the [DeDop Processor](https://github.com/DeDop/dedop-core/releases) or the SRAL sample files from the [Sentinel-3A Altimetry Test Data Set](https://sentinel.esa.int/web/sentinel/user-guides/sentinel-3-altimetry/test-data-set). 

Here is a screenshot (Windows):

![DeDop FX Screenshot](https://github.com/DeDop/dedop-fx/blob/master/screenshot.png)


# Quick start

Make sure, you have sound enabled.

1. Start DeDopFX
2. In main menu select **File / Load Source File...**, then select a L1B (.nc) file from your file system
3. Press **Play**
4. In tab **Source Mapping**,
   - adjust the **Maximum source value** until you hear something, around `3000` is a good starting point
   - adjust other settings, they all influence the sound in terms of pitch, timbre, and harmonics
5. Select *File / Save* from main menu, if you believe, your settings should be saved 


# Missing Features

* **Audio output level indicator** that also shows if we clip to lower/upper 16bit limits
* **High CPU load indicator** that warns if computer is too slow to stay above the sample rate of 44100 samples/sec
* **More visualisations**: show all input data as image, show currently played record within it


# License

This software is distributed under the terms and conditions of the [MIT License (MIT)](https://opensource.org/licenses/MIT).


# For Developers

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




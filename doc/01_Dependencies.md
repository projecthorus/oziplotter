# 1 - Dependencies

## Operating System
* OziPlotter has currently only been tested under Windows 7. Windows 10 may work, but has not been tried. (If you get it running on Windows 10, let us know!)

## Software Dependencies
There are numerous dependencies required for OziPlotter to work. Make sure all of the following are satisfied before attempting to run OziPlotter.

### OziExplorer
OziExplorer is the mapping engine where all the payload position info is plotted. It's getting a bit long in the tooth now, but is quite reliable and supports reading many offline map formats (i.e. the ECW format which most Australian Topographic maps are supplied in). 

Usage instructions for OziExplorer are out of scope for this user guide. Read the manual.

* OziExplorer is available here: http://oziexplorer3.com/eng/oziexplorer.html

#### OziAPI
OziPlotter uses [OziAPI.dll](http://www.oziexplorer3.com/oziapi/oziapi.html) to allow plotting of data into OziExplorer. Download the DLL file from [here](http://www.oziexplorer3.com/oziapi/oziapi_dll.zip) and place it into `./bin/`. 

### Java JDK
OziPlotter is written in Java, and must be compiled before use. Because the OziAPI interface is 32-bit, we have to use a 32-bit version of the Java JDK. 

* Download a 32-bit version (Windows x86) of the JDK here: http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html
  * JDK 7 & 8 should be OK, JDK 9 hasn't been tested.

#### OziAPI-Java
The OziAPI-Java library is used to provide a wrapper around OziAPI.dll, and hence allow plotting of data into OziExplorer. 

* Download the OziAPI-Java release from here: https://sourceforge.net/projects/oziapi-java/files/latest/download
* Copy OziAPI.jar and OziAPIJava.dll into `./bin/`

### Python 2.7 (via Anaconda Python)
Python is used to [gather wind data](./bin/get_wind_data.py) from the NOAA NOMADS servers. Currently most of the Project Horus software ecosystem only works on Python 2.7 (sorry about that...)

We recommend the use of the [Anaconda Python Distrbution](https://www.anaconda.com/download/#windows) as a means of obtaining Python under Windows. The latest Python 2.7 release *should* work, though if you wish to use the Horus Ground Station utilities, I suggest using the older [2.3.0 version](https://repo.continuum.io/archive/Anaconda-2.3.0-Windows-x86.exe) until I can update everything to work with the latest.

#### Required Python Libraries
After installing Anaconda, open a command prompt and run the following commands to install various required libraries:
* `pip install pydap==3.1.1`
* `pip install crcmod`

### CUSF Standalone Predictor
The [Cambridge University Spaceflight Standalone Predictor](https://github.com/jonsowman/cusf-standalone-predictor) is used to run flight path predictions. Unfortunately, no Windows binary release is available.
Compiling under windows (using CygWin or MinGW) is difficult, but not impossible.

I've made a compiled version available here: http://rfhead.net/horus/cusf_standalone_predictor.zip
Unzip this and place all contents (pred.exe and other CygWin DLLs) into ./bin/

### Franson GPSGate (Optional)
Franson GPSGate allows splitting of a connected GPS unit (accessed via a serial port) between multiple virtual COM ports. In the context of running a balloon chase car, this allows GPS position data to be used by OziExplorer (to show your location on the map), and other software (i.e. [ChaseTracker](https://github.com/projecthorus/HorusGroundStation/blob/master/ChaseTracker.py)) simultaneously. If you don't intend to run ChaseTracker, then don't bother installing this.

* Download from: http://gpsgate.com/download/gpsgate_client
* Free version will split one GPS into two virtual com ports.
 * Paid version does fun stuff like GPS from a TCP socket.
* Setup GPSGate to split hardware GPS unit into two virtual COM ports (ideally COM10 and COM11)
 * One can be used for OziExplorer, one for ChaseTracker

## Final contents of ./bin/
Once you've done all of the above, you should have the following files within `./bin/`:
* OziAPI.dll
* OziAPI.jar
* OziAPIJava.dll
* cyggcc_s-1.dll
* cygglib-2.0-0.dll
* cygiconv-2.dll
* cygintl-8.dll
* cygpcre-0.dll
* cygwin1.dll
* pred.exe

## Compilation of OziPlotter
* Enter the `./src/` directory and run `make.bat`.
* If all went well, you will just see 'Press any key to continue....'



Next: [Configuration](./02_Configuration.md)

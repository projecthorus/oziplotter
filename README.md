# OziPlotter - Project Horus's Offline Balloon Tracking System
OziPlotter is an interface between [Project Horus's](www.areg.org.au/archives/category/activities/project-horus) ['Horus Ground Station'](https://github.com/projecthorus/HorusGroundStation) receiver utilities and the [OziExplorer](http://www.oziexplorer.com/au/) mapping software.
OziPlotter was designed as a means of plotting balloon payload position & flight path predictions on a map, without requiring an internet connection. This is quite important when chasing balloons in remote areas of Australia!

This software was originally written by Terry Baume, Project Horus's founder, and has been in various states of development since 2009. The core of OziPlotter is written in Java, but the Horus Ground Station utilities and other scripts are written in Python.

OziPlotter performs the following functions:
* Listens for payload telemetry data from various sources via simple UDP packets, and plots the payload position on the map.
* Runs balloon flight path predictions using saved wind model data, and plots these on the map.

OziPlotter has been one of the main reasons why Project Horus has had such a good track record of payload recovery, and we figured it's now about time we shared this software with the wider High-Altitude Ballooning community. We hope it will prove useful to other high-altitude ballooning projects. 

## Setup & Operation
Setup & use of OziPlotter is somewhat involved, and requires a number of specific dependencies. OziPlotter currently can only be run under Microsoft Windows 7 (Win 10 may work, but hasn't been tested).

Detailed setup & operation instructions are available within the 'doc' directory. Please read through and follow the instructions in these files.

NOTE: The documentation for OziPlotter is under development, and may not be complete!

1. [Dependencies](./doc/01_Dependencies.md)
2. [Configuration](./doc/02_Configuration.md)
3. [Inputs](./doc/03_Inputs.md)


THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
(In short, if you lose your balloon payload while using this software, don't blame us! Always have backup methods of tracking, like radio direction finding.)
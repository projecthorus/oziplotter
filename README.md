# OziPlotter - Project Horus's Offline Balloon Tracking System
OziPlotter is an interface between [Project Horus's](http://www.areg.org.au/archives/category/activities/project-horus) [Ground Station & Chase-Car Utilities](https://github.com/projecthorus/horus_utils) and the [OziExplorer](http://www.oziexplorer.com/au/) mapping software.
OziPlotter was designed as a means of plotting balloon payload position & flight path predictions on a map, without requiring an internet connection. This is quite important when chasing balloons in remote areas of Australia!

![Chasing a radiosonde..](https://raw.githubusercontent.com/projecthorus/oziplotter/master/doc/images/sonde_chase.jpg)

This software was originally written by Terry Baume, Project Horus's founder, and has been in various states of development since 2009. The core of OziPlotter is written in Java, but the Horus Ground Station utilities and other scripts are written in Python. 

OziPlotter performs the following functions:
* Listens for payload telemetry data from various sources via simple UDP packets, and plots the payload position on the map.
* Runs balloon flight path predictions using saved wind model data, and plots these on the map.

Predictions are run for both the expected flight parameters (i.e. predicted burst rate), but also for an 'abort' case (what would happen if the balloon burst *now*). These are both visible in the above screenshot: The orange trace is the expected flight path, and the red trace shows the abort flight path.

OziPlotter has been one of the main reasons why Project Horus has had such a good track record of payload recovery, and we figured it's now about time we shared this software with the wider High-Altitude Ballooning community. We hope it will prove useful to other high-altitude ballooning projects. 

## Setup & Operation
Setup & use of OziPlotter is somewhat involved, and requires a number of specific dependencies. OziPlotter currently can only be run under Microsoft Windows 7 (Win 10 may work, but hasn't been tested).

Detailed setup & operation instructions are available within the 'doc' directory. Please read through and follow the instructions in these files.

NOTE: The documentation for OziPlotter is under development, and may not be complete!

1. [Dependencies](https://github.com/projecthorus/oziplotter/wiki/1---Dependencies)
2. [Configuration](https://github.com/projecthorus/oziplotter/wiki/2---Configuration)
3. [Data Sources](https://github.com/projecthorus/oziplotter/wiki/3---Data-Sources)
4. [The Chase!](https://github.com/projecthorus/oziplotter/wiki/4---The-Chase!)


THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
(In short, if you lose your balloon payload while using this software, don't blame us! Always have backup methods of tracking, like radio direction finding.)
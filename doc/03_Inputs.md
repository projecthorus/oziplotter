# 3 - OziPlotter Data Inputs

Oziplotter accepts telemetry (payload position) and waypoint data as LF terminated lines of CSV on UDP port 8942 (configurable in OziPlotter.conf). 
The following formats are accepted:
* Payload Positions:  `TELEMETRY,HH:MM:SS,latitude,longitude,altitude\n` 
  * Example: `TELEMETRY,01:05:20,-34.12345,138.12345,10123\n`
  * Payload positions are added to a track within OziExplorer.

* Waypoint data: `WAYPOINT,name,latitude,longitude,comment\n`
  * Example: `WAYPOINT,VK5QI-9,-34.12345,138.1234,QSY 439.900\n`
  * Waypoints are plotted in OziExplorer, and are referenced by name. Hence, they can be updated by sending a new Waypoint data sentence with the same waypoint name.

The following software has been designed to produce data in this format, and is suitable for gathering payload telemetry from various sources:

## OziMux
[OziMux](https://github.com/projecthorus/HorusGroundStation/blob/master/OziMux.py), part of the HorusGroundStation repository, was developed as a means to switch between different data sources during a flight. For example, if you have a LoRa and a RTTY payload, you can switch between the two payloads depending on which is producing the most consistent/reliable data.

OziMux listens for telemetry data (in the same format as above) on multiple UDP ports (configured in ozimux.cfg), displays the received data, and allows the user to select which data source should be passed onto OziPlotter.

![OziMux Screenshot](https://raw.githubusercontent.com/projecthorus/oziplotter/master/doc/images/ozimux.jpg)

## FldigiBridge
[FldigiBridge](https://github.com/projecthorus/HorusGroundStation/blob/master/FldigiBridge.py), part of the HorusGroundStation repository, connects to ['dl-fldigi'](https://ukhas.org.uk/projects:dl-fldigi), and listens for telemetry in the [UKHAS standard format](https://ukhas.org.uk/communication:protocol). The telemetry string must use a CRC16 checksum. 

FldigiBridge cares not what modulation scheme (RTTY, or otherwise) your payload is using - it just reads the decoded data out of dl-fldigi via a TCP connection. 

By default, FldigiBridge outputs data to UDP port 55683, which is a default setting within OziMux.

![Fldigi Bridge Screenshot](https://raw.githubusercontent.com/projecthorus/oziplotter/master/doc/images/fldigibridge.jpg)

## Horus Ground Station
[HorusGroundStation](https://github.com/projecthorus/HorusGroundStation/blob/master/HorusGroundStation.py) is the GUI interface for Project Horus's 'mission control' [telemetry payload](https://github.com/projecthorus/FlexTrack-Horus), which communicates via the LoRa modulation scheme. Refer to the [HorusGroundStation](https://github.com/projecthorus/HorusGroundStation) repository for further information on this system. 

The port which HorusGroundStation sends data to is configured within [defaults.cfg](https://github.com/projecthorus/HorusGroundStation/blob/master/defaults.cfg.example#L29), and can be set to either communicate directly with OziPlotter, or via OziMux.

![Horus Ground Station Screenshot](https://raw.githubusercontent.com/projecthorus/oziplotter/master/doc/images/horusgroundstation.jpg)

## Radiosonde Auto RX
Along with launching our own High Altitude balloons, we also go hunting radiosondes launched by the local Bureau of Meteorology. The ['auto_rx' radiosonde decoding software](https://github.com/darksidelemm/RS/tree/master/auto_rx) uses a RTLSDR to automatically search for radiosonde transmissions, decodes them, and plots data to APRS-IS and the [Habitat online tracker](http://tracker.habhub.org). It can also output data in OziPlotter compatible format!

The OziPlotter output must be enabled in the [auto_rx configuration file](https://github.com/darksidelemm/RS/blob/master/auto_rx/station.cfg.example#L85). As with the above applications, you can either push data directly to OziPlotter, or via OziMux. As auto_rx is intended to run on a standalone Raspberry Pi, you will need to specify both the destination IP address, and the destination UDP port (either 8942, or 55681, if sending via OziMux).




Next: [Chasing a balloon!](./04_The_Chase.md)



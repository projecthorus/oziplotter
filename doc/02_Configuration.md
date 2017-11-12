# 2 - Configuration

A number of files must be edited prior to using OziPlotter. The following sections details the necessary changes.

## WindGrabber.bat
['WindGrabber.bat'](../WindGrabber.bat) is used to collect portions of NOAA's Global Forecast System wind model, for use in running balloon flight path predictions.

Line 7 of this file must be edited, setting the appropriate latitude and longitude:
`python src/get_wind_data.py --lat=-33 --lon=139 --latdelta=10 --londelta=10 -v -f 24 -r 0p50`

Refer to the [documentation for get_wind_data.py](https://github.com/projecthorus/oziplotter/blob/master/src/get_wind_data.py#L21) for more information on the command line options used.

## OziPlotter.conf
['OziPlotter.conf'](../OziPlotter.conf) contains parameters relating to the balloon flight itself. For accurate predictions, these parameters must be changed based on your flight profile:

* PREDASC - The planned ascent rate for your flight, in m/s. Once the payload has launched, the measured ascent rate based on telemetry will be used.
* PREDDESC - The planned descent rate for your flight (at sea level), in m/s. The [Random Engineering descent rate calculator](http://www.randomengineering.co.uk/Random_Aerospace/Parachutes.html) can help in estimating this value. Once the payload has burst the measured descent rate (extrapolated to sea level) will be used.
* PREDBURST - The predicted burst altitude for your flight.
* LAUNCHLAT / LAUNCHLON - The lat/long of your launch site. These values are only used to plot a waypoint on the map.

Refer to ['OziPlotter.conf'](../OziPlotter.conf) for other customizable parameters.


Next: [Data Inputs](./03_Inputs.md)


@echo off
TITLE Downloading wind...
CLS
echo ------------------------------------------------------------
echo DON'T FORGET TO DELETE OLD WIND DATA FROM THE GFS DIRECTORY!
echo ------------------------------------------------------------
python src/get_wind_data.py --lat=-33 --lon=139 --latdelta=10 --londelta=10 -v -f 24 -r 0p50
echo DON'T FORGET TO DELETE OLD WIND DATA FROM THE GFS DIRECTORY!
pause
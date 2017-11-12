:START
@echo off
TITLE OziPlotter
cd bin
CLS
java -cp OziAPI.jar;. OziPlotterUDP
TITLE OziPlotter [Error!!]
pause
GOTO START
@echo off
set /p filename1="New Program Name:  "
set filename1=%filename1: =_%
md %filename1% 

::cd ..
::findstr /C:Motor Wiring.csv > "arduino\%filename1%\%filename1%.ino"
::findstr /C:Motor Wiring.csv > "arduino\%filename1%\%filename1%.ino"
::cd arduino

echo void setup(){ >> ".\%filename1%\%filename1%.ino"
echo. >> ".\%filename1%\%filename1%.ino"
echo } >> ".\%filename1%\%filename1%.ino"
echo. >> ".\%filename1%\%filename1%.ino"
echo void loop(){ >> ".\%filename1%\%filename1%.ino"
echo. >> ".\%filename1%\%filename1%.ino"
echo } >> ".\%filename1%\%filename1%.ino"

echo.
echo Created new program called:       %filename1%.ino
echo.

PAUSE
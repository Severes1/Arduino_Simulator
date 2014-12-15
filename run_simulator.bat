@echo off

set PATH="C:\Program Files (x86)\Java\jdk1.8.0_25\bin";%PATH%

cd arduino

if exist "libraries\" rd /q /s "libraries\"
for /f %%A in ('dir /s /b *.ino ^| find /v /c ""') do set cnt=%%A
::echo File count = %cnt%

echo %cnt% arduino files found:

@dir /b /s *.ino | more

for %%x in (*.ino) do if not defined firstFile set "firstFile=%%x"

set filename1 = %firstFile%

echo.
echo.
echo.

if %cnt% gtr 1 set /p filename1="Press Tab to pick a program and then press enter     "

cd..

echo %filename1% > bin/mostRecentProgram.txt

echo loading %filename1%...
:: First, convert the start_file to java
javac -cp ./bin/core.jar;. -d ./bin ./java/File_Converter.java
java -cp ./bin/core.jar;./bin File_Converter arduino/%filename1%/%filename1%.ino

echo running %filename1%...
echo.
echo.------------------
echo Program Output:
echo.
:: Then compile the java files including the new one
javac -cp ./bin/core.jar;. -d ./bin ./java/Arduino_Simulator.java ./java/Arduino_Program.java
java -cp ./bin/core.jar;./bin Arduino_Simulator
:end
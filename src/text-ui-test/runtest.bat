@echo off
REM Navigate to the Java source folder and compile
cd ..\src\main\java
javac *.java

REM Run Yoda with input.txt as stdin, capture output to ACTUAL.txt
java Yoda < ..\..\..\text-ui-test\input.txt > ..\..\..\text-ui-test\ACTUAL.txt

REM Compare ACTUAL.txt to EXPECTED.txt and report differences
fc ..\..\..\text-ui-test\ACTUAL.txt ..\..\..\text-ui-test\EXPECTED.txt
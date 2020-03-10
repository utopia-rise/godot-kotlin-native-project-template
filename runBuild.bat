@echo off
REM Currently having trouble passing arguments to the second command
REM in Godot, thus this file acts as a way to pass arguments to gradle
REM from Godot.
REM If the argument passing problem can be solved in Godot, then these
REM scripts can be removed.
call gradlew.bat build
@echo off
setlocal
pushd "%~dp0" || exit /b 1

if not exist "out\" mkdir "out"
javac --release 17 -encoding UTF-8 -d "out" -sourcepath "src" "src\dynamaze\DynaMazeApp.java"
if errorlevel 1 (
    popd
    exit /b 1
)

java -cp "out" dynamaze.DynaMazeApp %*
set "app_exit=%errorlevel%"
popd
exit /b %app_exit%

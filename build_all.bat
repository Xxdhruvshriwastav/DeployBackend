@echo off
setlocal enabledelayedexpansion

set "JAVA_HOME=C:\Program Files\Java\jdk-21"
set "PATH=%JAVA_HOME%\bin;%PATH%"

set ROOT_DIR=c:\Users\rudra\Desktop\Deploy\hireconnect
cd /d %ROOT_DIR%

for /d %%D in (*) do (
    if exist "%%D\pom.xml" (
        echo Building %%D...
        cd "%%D"
        call mvnw.cmd clean package -DskipTests -B
        cd ..
    )
)

echo All builds finished!

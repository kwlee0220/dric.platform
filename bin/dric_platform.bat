@echo off

java -cp %DRIC_HOME%/bin/dric_platform.jar ^
dric.DrICPlatformMain ^
--config %DRIC_HOME%/dric_platform.yaml %*
@echo off
@ECHO OFF
REM $Id$
REM $URL$

SET LOG_FILE=%0.out

CLS
CD /d %~dp0
ECHO Running Time Manager Multi-Agent System with output in %LOG_FILE% and initial settings:>CON
ECHO.>CON
DEL %LOG_FILE%
MORE eve.yaml >CON
CALL "java" -Dlog4j.configurationFile=file:/%~dp0/log4j2.yaml^
  -Dlog4j2.disable.jmx=true^
  -Djava.util.logging.manager=org.apache.logging.log4j.jul.LogManager^
  -jar ./tc-master-eve.jar ./eve.yaml^
  1>%LOG_FILE%^
  2>&1

IF NOT ["%ERRORLEVEL%"]==["0"] PAUSE
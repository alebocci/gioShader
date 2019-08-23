@echo off
set EXEC_DIR=%CD%

mvn package && ^
WinSCP.com ^
  /ini=nul ^
  /command ^
    "open scp://pi:pi@192.168.0.17/ -hostkey=""ssh-ed25519 256 mPI8G4G6lsL/7xARP+c0EzUx63OdxBpEhhmaiuBUH5M=""" ^
    "put .\target\gioshader-0.0.1-SNAPSHOT.jar /home/pi/Workspace/gio/shutterDriver/" ^
    "exit"

pause && ^

set WINSCP_RESULT=%ERRORLEVEL%
if %WINSCP_RESULT% equ 0 (
  echo Success
) else (
  echo Error
)

exit /b %WINSCP_RESULT%

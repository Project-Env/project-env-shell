@echo off

set SCRIPTPATH=%~dp0

set SHELL=C:\cygwin64\bin\bash

set PROJECT_ENV_CONFIG=project-env.yml
if not exist %PROJECT_ENV_CONFIG% (
    %SHELL%
) else (
    set EXECUTABLE_VERSION=v@project.version@
    set EXECUTABLE_BASE=%SCRIPTPATH%project-env-shell
    set EXECUTABLE=%EXECUTABLE_BASE%-%EXECUTABLE_VERSION%.exe

    if not exist %EXECUTABLE% (
        powershell -Command "(New-Object Net.WebClient).DownloadFile('https://github.com/Project-Env/project-env-shell/releases/download/v%EXECUTABLE_VERSION%/project-env-shell-windows-amd64.exe', '%EXECUTABLE%')"
    )

    %EXECUTABLE% refresh --config-file=%PROJECT_ENV_CONFIG% --output-template=cygwin --output-file=.project-env

    %SHELL% --init-file .project-env
)
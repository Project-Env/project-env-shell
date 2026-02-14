# Project-Env Shell

![Build](https://github.com/Project-Env/project-env-shell/workflows/Build/badge.svg)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Project-Env_project-env-shell&metric=alert_status)](https://sonarcloud.io/dashboard?id=Project-Env_project-env-shell)

**Note that this project has been archived, as the shell integration has been integrated into [project-env-cli](https://github.com/Project-Env/project-env-cli).**

The Project-Env Shell integration application allows to use tools setup by the Project-Env CLI in a shell environment. See [Project-Env](https://project-env.github.io/) for more details.

## Installation

### Homebrew
`brew install --cask project-env/tap/project-env-shell`

### Winget

`winget install ProjectEnv.ProjectEnvShell`

## Shell integration examples

### Unix shell

To use Project-Env managed tools in your unix shell, create the following script:

```sh
#!/bin/sh
if [[ ! -f "project-env.toml" ]]; then
    sh
else
  project-env-shell --config-file="project-env.toml" --output-template=sh --output-file=.project-env
  source .project-env
  sh
fi
```

To use the tools, you now only need to call the script to setup the shell. 

For example, if you want to start your IntelliJ Terminal with Project-Env tools setup, configure the following command in the Terminal settings:

```sh
/bin/sh --login -c <path to script>
```

### Cygwin shell (started through Windows CMD)

To use Project-Env managed tools in your Cygwin shell, create the following script:

```batch
@echo off
if not exist project-env.toml (
    bash.exe
) else (
    project-env-shell.exe --config-file=project-env.toml --output-template=cygwin --output-file=.project-env
    dos2unix .project-env
    bash.exe --init-file .project-env
)
```

To use the tools, you now only need to call the script to setup the shell.

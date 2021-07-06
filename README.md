# Project-Env Shell

![Build](https://github.com/Project-Env/project-env-shell/workflows/Build/badge.svg)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Project-Env_project-env-shell&metric=alert_status)](https://sonarcloud.io/dashboard?id=Project-Env_project-env-shell)

The Project-Env Shell integration application allows to use tools setup by the Project-Env CLI in a shell environment. See https://project-env.github.io for more details.

## Installation

### Homebrew
`brew install --cask project-env/tap/project-env-shell`

## Shell integration examples

### ZSH

```shell
#!/bin/zsh
if [[ ! -f "project-env.toml" ]]; then
    zsh -i
else
  project-env-shell --config-file="project-env.toml" --output-template=zsh --output-file=.project-env
  source .project-env
  zsh -i
fi
```

### Cygwin

```shell
@echo off
if not exist project-env.toml (
    bash.exe
) else (
    project-env-cli.exe --config-file=project-env.toml --output-template=cygwin --output-file=.project-env
    bash.exe --init-file .project-env
)
```
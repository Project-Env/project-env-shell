#!/bin/zsh

SCRIPTPATH="$( cd "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"

SHELL="zsh"

PROJECT_ENV_CONFIG="project-env.yml"
if [[ ! -f "$PROJECT_ENV_CONFIG" ]]; then
    $SHELL -i
else
  EXECUTABLE_VERSION="v@project.version@"
  EXECUTABLE_BASE="$SCRIPTPATH/project-env-shell"
  EXECUTABLE="$EXECUTABLE_BASE-$EXECUTABLE_VERSION"

  if [[ ! -f "$EXECUTABLE" ]]; then
    case "$(uname -s)" in
      Darwin)
        curl -L "https://github.com/Project-Env/project-env-shell/releases/download/$EXECUTABLE_VERSION/project-env-shell-macos-amd64" -o "$EXECUTABLE"
        ;;

      Linux)
        curl -L "https://github.com/Project-Env/project-env-shell/releases/download/$EXECUTABLE_VERSION/project-env-shell-linux-amd64" -o "$EXECUTABLE"
        ;;
      *)
        exit 1
        ;;
    esac
    chmod +x "$EXECUTABLE"
  fi

  "$EXECUTABLE" refresh --config-file="$PROJECT_ENV_CONFIG" --output-template=zsh --output-file=.project-env

  source .project-env
  $SHELL -i
fi
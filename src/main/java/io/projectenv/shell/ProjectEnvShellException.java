package io.projectenv.shell;

import java.io.IOException;

public class ProjectEnvShellException extends IOException {

    public ProjectEnvShellException(String message) {
        super(message);
    }

    public ProjectEnvShellException(String message, Throwable cause) {
        super(message, cause);
    }

}

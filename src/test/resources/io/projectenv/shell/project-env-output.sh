#!/bin/zsh

export PATH="BASE_PATH/.tools/maven-SHA256/apache-maven-3.6.3/bin:$PATH"
alias mvn="mvn -s BASE_PATH/settings-user.xml"

export PATH="BASE_PATH/.tools/gradle-SHA256/gradle-6.7.1/bin:$PATH"

export JAVA_HOME="BASE_PATH/.tools/jdk-SHA256/graalvm-ce-java11-20.3.0JDK_HOME"
export PATH="BASE_PATH/.tools/jdk-SHA256/graalvm-ce-java11-20.3.0JDK_HOME/bin:$PATH"

export JAXB_HOME="BASE_PATH/.tools/generic-SHA256/jaxb-ri"
export PATH="BASE_PATH/.tools/generic-SHA256/jaxb-ri/bin:$PATH"

export PATH="BASE_PATH/.tools/nodejs-SHA256/node-v14.15.3-NODE_PLATFORM-x64NODE_BIN_PATH:$PATH"
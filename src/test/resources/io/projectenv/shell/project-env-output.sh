#!/bin/zsh

export JAVA_HOME="BASE_PATH/.tools/graalvm-ce-java11-20.3.0JDK_HOME"
export PATH="BASE_PATH/.tools/graalvm-ce-java11-20.3.0JDK_HOME/bin:$PATH"

export PATH="BASE_PATH/.tools/apache-maven-3.6.3/bin:$PATH"
export MVN_GS_PARAM="BASE_PATH/settings.xml"
export MVN_S_PARAM="BASE_PATH/settings-user.xml"
alias mvn="mvn $MVN_GS_PARAM $MVN_S_PARAM"

export PATH="BASE_PATH/.tools/gradle-6.7.1/bin:$PATH"

export PATH="BASE_PATH/.tools/node-v14.15.3-NODE_PLATFORM-x64NODE_BIN_PATH:$PATH"


export JAXB_HOME="BASE_PATH/.tools/jaxb-ri"
export PATH="BASE_PATH/.tools/jaxb-ri/bin:$PATH"

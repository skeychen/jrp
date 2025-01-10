#!/bin/sh
JAVA_PATH=""
if [ -d "/WorkServer/Java/jdk" ]; then
	JAVA_PATH="/WorkServer/Java/jdk/bin"
fi
if [ -d "/WorkServer/Java/jre" ]; then
	JAVA_PATH="/WorkServer/Java/jre/bin"
fi
if [ "${JAVA_PATH}" = "" ]; then
	echo "not find JAVA_HOME or JRE_HOME"
	exit 1
fi
PATH=${JAVA_PATH}/bin:$PATH
CLASSPATH=.:${JAVA_PATH}/lib
export JAVA_PATH PATH CLASSPATH
cd "/WorkServer/jrp-server"
${JAVA_PATH}/java -jar server.jar server.json
exit 0
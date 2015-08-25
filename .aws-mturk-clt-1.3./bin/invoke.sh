#!/usr/bin/env sh
#
# Copyright 2012 Amazon Technologies, Inc.
# 
# Licensed under the Amazon Software License (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at:
# 
# http://aws.amazon.com/asl
# 
# This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
# OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and
# limitations under the License.
 
 
# This bin "concentrates" all of our Java invocations into a single location
# for maintainability.

# Adds jar files to CP variable
setCP() {
    if [ -d "$1" ]; then
        if [ "$cygwin" = "true" ]; then
            CP="${CP};$(find "$1" -name "*.jar" -exec cygpath -w -a "{}" \; | tr '\n' ';')"
        else
            CP="${CP}:$(find "$1" -name "*.jar" | tr '\n' ':')"
        fi
    fi
}

# 'Globals'
MTURK_CMD_HOME=${MTURK_CMD_HOME:-$(dirname "$0")/..}
MTURK_SDK_HOME=${MTURK_SDK_HOME:-$MTURK_CMD_HOME}
SDK_LIB_DIR="$MTURK_SDK_HOME"/lib
CMD_LIB_DIR="$MTURK_CMD_HOME"/lib

# Check our Java env
if [ -z "${JAVA_HOME}" ]; then
    echo "You must set JAVA_HOME (i.e. export JAVA_HOME=/usr)"
    echo "If you do not have JAVA on your machine, you can download it from http://java.sun.com/"
    exit -1
fi

# ---- Start of Cygwin test ----
cygwin=false
cygprop=""
ps=":"
case "`uname`" in
    CYGWIN*) cygwin=true ;;
esac 

# And add our own libraries too
if [ "$cygwin" = "true" ]; then
    ps=";" 
    cygprop="-Dec2.cygwin=true"
    # Make sure that when using Cygwin we use Unix 
    # Semantics for MTURK_SDK_HOME
    if [ -n "$MTURK_SDK_HOME" ]; then
        if echo $MTURK_SDK_HOME | egrep -q '[[:alpha:]]:\\'; then
            echo
            echo " *INFO* Your MTURK_SDK_HOME variable needs to specified as a Unix path under Cygwin"
            echo
        fi
    fi
fi
# ---- End of Cygwin Tests ----

CP="${CLASSPATH}${ps}${MTURK_CMD_HOME}/etc"
setCP "$MTURK_CMD_HOME/build/lib"
setCP "$SDK_LIB_DIR"
setCP "$CMD_LIB_DIR"

case $1 in
    *.*) CMD=$1 ;;
    *) CMD=com.amazonaws.mturk.cmd.$1 ;;
esac

shift
exec "$JAVA_HOME/bin/java" $MTURK_JVM_ARGS $cygprop -classpath "$CP" $CMD "$@"

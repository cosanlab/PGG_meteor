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
 

MTURK_CMD_HOME=${MTURK_CMD_HOME:-$(dirname "$0")/..}
export MTURK_CMD_HOME
exec "$MTURK_CMD_HOME"/bin/invoke.sh GenerateResultsSummary "$@"

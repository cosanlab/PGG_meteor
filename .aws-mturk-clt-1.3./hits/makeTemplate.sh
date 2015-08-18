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
cd ..
cd bin
./makeTemplate.sh $@ -os Unix -type Hit -templateRootDir ../samples -targetRootDir ../hits -scriptTemplateDir ../etc/templates/hits
cd ..
cd hits

# Set the scripts executable
seen_target=0
for param in $@
do
  if [ $param = "-target" ]; then
    seen_target=1
    continue
  fi
  if [ $seen_target -eq 1 ]; then
    chmod a+x $param/*.sh
    seen_target=0
  fi
done

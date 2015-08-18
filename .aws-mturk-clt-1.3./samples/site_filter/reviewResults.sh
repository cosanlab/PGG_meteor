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
 

#!/usr/bin/env bash
cd ../..
cd bin
./reviewResults.sh $1 $2 $3 $4 $5 $6 $7 $8 $9 -resultsfile ../samples/site_filter/site_filter.results
cd ..
cd samples/site_filter

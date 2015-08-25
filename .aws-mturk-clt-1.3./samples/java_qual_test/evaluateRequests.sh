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
 

cd ../..
cd bin
./evaluateQualificationRequests.sh $1 $2 $3 $4 $5 $6 $7 $8 $9 -sandbox -answers ../samples/java_qual_test/qualification.answerkey -input ../samples/java_qual_test/qualification.properties.success 
cd ..
cd samples/java_qual_test

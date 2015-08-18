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


# You must supply at least one parameter
if [ -z $1 ]; then
	echo "Usage: makeTemplate [template_name]"
	exit

fi

# Check to see if the directory already exists.
if [ -d "$1" ]; then
	echo "A template named $1 already exists."
	exit
fi

mkdir $1
cp ../etc/templates/qualifications/template.question $1/$1.question
cp ../etc/templates/qualifications/template.answer $1/$1.answer
cp ../etc/templates/qualifications/template.properties $1/$1.properties


# create the createQualification.sh file for Unix
echo \#!/usr/bin/env bash >> $1/createQualification.sh
echo cd ../.. >> $1/createQualification.sh
echo cd bin >> $1/createQualification.sh
echo ./createQualificationType.sh \$@ -question ../qualifications/$1/$1.question -properties ../qualifications/$1/$1.properties -answer ../qualifications/$1/$1.answer >> $1/createQualification.sh
echo cd .. >> $1/createQualification.sh
echo cd qualifications/$1 >> $1/createQualification.sh
chmod +xxx $1/createQualification.sh

# create the updateQualification.sh file for Unix
echo \#!/usr/bin/env bash >> $1/updateQualification.sh
echo cd ../.. >> $1/updateQualification.sh
echo cd bin >> $1/updateQualification.sh
echo ./updateQualificationType.sh \$@ -qualtypeid [put your qualification id here] -question ../qualifications/$1/$1.question -properties ../qualifications/$1/$1.properties -answer ../qualifications/$1/$1.answer >> $1/updateQualification.sh
echo cd .. >> $1/updateQualification.sh
echo cd qualifications/$1 >> $1/updateQualification.sh
chmod +xxx $1/updateQualification.sh

# create the deactivateQualification.sh file for Unix
echo \#!/usr/bin/env bash >> $1/deactivateQualification.sh
echo cd ../.. >> $1/deactivateQualification.sh
echo cd bin >> $1/deactivateQualification.sh
echo ./updateQualificationType.sh \$@ -qualtypeid [put your qualification id here] -status Inactive >> $1/deactivateQualification.sh
echo cd .. >> $1/deactivateQualification.sh
echo cd qualifications/$1 >> $1/deactivateQualification.sh
chmod +xxx $1/deactivateQualification.sh

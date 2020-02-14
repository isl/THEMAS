#!/bin/sh
# Copyright 2015 Institute of Computer Science,
#                Foundation for Research and Technology - Hellas.
#
# Licensed under the EUPL, Version 1.1 or - as soon they will be approved
# by the European Commission - subsequent versions of the EUPL (the "Licence");
# You may not use this work except in compliance with the Licence.
# You may obtain a copy of the Licence at:
#
#      http://ec.europa.eu/idabc/eupl
#
# Unless required by applicable law or agreed to in writing, software distributed
# under the Licence is distributed on an "AS IS" basis,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the Licence for the specific language governing permissions and limitations
# under the Licence.
# 
# =============================================================================
# Contact: 
# =============================================================================
# Address: N. Plastira 100 Vassilika Vouton, GR-700 13 Heraklion, Crete, Greece
#     Tel: +30-2810-391632
#     Fax: +30-2810-391638
#  E-mail: isl@ics.forth.gr
# WebSite: https://www.ics.forth.gr/isl/centre-cultural-informatics
# 
# =============================================================================
# Authors: 
# =============================================================================
# Elias Tzortzakakis <tzortzak@ics.forth.gr>
# 
# This file is part of the THEMAS system.
currentPath=$( cd $(dirname $0) ; pwd -P )
cd  $currentPath

source ./0_MainSetup.sh

Mode=FixDB

SystemOutFilePath="$BaseInputOutpoutPath/OfflineToolsClassOutput.txt"

cd "$BaseTHEMASwebappPath/WEB-INF/classes"

"$JAVA_BIN_PATH/java"  -Xmx2048M -cp ".:$libFolder/*:"  -Dfile.encoding=UTF8 Admin_Thesaurus.OfflineToolsClass $Mode "$BaseTHEMASwebappPath" 1> $SystemOutFilePath 2>&1


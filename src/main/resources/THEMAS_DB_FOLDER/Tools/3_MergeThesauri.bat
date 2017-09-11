REM Copyright 2015 Institute of Computer Science,
REM                Foundation for Research and Technology - Hellas.
REM
REM Licensed under the EUPL, Version 1.1 or - as soon they will be approved
REM by the European Commission - subsequent versions of the EUPL (the "Licence");
REM You may not use this work except in compliance with the Licence.
REM You may obtain a copy of the Licence at:
REM
REM      http://ec.europa.eu/idabc/eupl
REM
REM Unless required by applicable law or agreed to in writing, software distributed
REM under the Licence is distributed on an "AS IS" basis,
REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
REM See the Licence for the specific language governing permissions and limitations
REM under the Licence.
REM 
REM =============================================================================
REM Contact: 
REM =============================================================================
REM Address: N. Plastira 100 Vassilika Vouton, GR-700 13 Heraklion, Crete, Greece
REM     Tel: +30-2810-391632
REM     Fax: +30-2810-391638
REM  E-mail: isl@ics.forth.gr
REM WebSite: http://www.ics.forth.gr/isl/cci.html
REM 
REM =============================================================================
REM Authors: 
REM =============================================================================
REM Elias Tzortzakakis <tzortzak@ics.forth.gr>
REM 
REM This file is part of the THEMAS system.
REM
SET currentPath=%~dp0
cd  "%currentPath%"
call 0_MainSetup.bat

SET Mode=MergeThesauri

SET ThesaurusName1=EMPTY
SET ThesaurusName2=TEST
SET MERGEDThesaurusName=MERGED
SET LogFilePath="%BaseInputOutpoutPath%\MergeLog.xml"

SET SystemOutFilePath="%BaseInputOutpoutPath%\OfflineToolsClassOutput.txt"


cd "%BaseTHEMASwebappPath%\WEB-INF\classes"
"%JAVA_BIN_PATH%\java.exe"  -Xmx2048M -cp ".;%libFolder%\*;" -Dfile.encoding=UTF8 Admin_Thesaurus.OfflineToolsClass %Mode% "%BaseTHEMASwebappPath%" %ThesaurusName1% %ThesaurusName2% %MERGEDThesaurusName% %LogFilePath% 1> %SystemOutFilePath% 2>&1

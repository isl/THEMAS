Copyright 2015 Institute of Computer Science,
               Foundation for Research and Technology - Hellas.

Licensed under the EUPL, Version 1.1 or - as soon they will be approved
by the European Commission - subsequent versions of the EUPL (the "Licence");
You may not use this work except in compliance with the Licence.
You may obtain a copy of the Licence at:

     http://ec.europa.eu/idabc/eupl

Unless required by applicable law or agreed to in writing, software distributed
under the Licence is distributed on an "AS IS" basis,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the Licence for the specific language governing permissions and limitations
under the Licence.

=============================================================================
Contact: 
=============================================================================
Address: N. Plastira 100 Vassilika Vouton, GR-700 13 Heraklion, Crete, Greece
    Tel: +30-2810-391632
    Fax: +30-2810-391638
 E-mail: isl@ics.forth.gr
WebSite: http://www.ics.forth.gr/isl/cci.html

=============================================================================
Authors: 
=============================================================================
Elias Tzortzakakis <tzortzak@ics.forth.gr>

This file is part of the THEMAS system.


=============================================================================
THEMAS_DB_FOLDER structure
=============================================================================


- Backups folder will be used in order to store all backups of 
  the database (zip fils,xml, automatic backups etc.). Contains 
  subfolders("db_backups", "xml_backups", "XMLandTSVexports"). 
  
  This Path must be in accordance with file:
  /Sources/WebApplication/web/DBadmin/tms_db_admin_config_files/config.xml

- Database folder will contain the current Neo4j graph database 
  of the application. 
  
  This Path must be in accordance with file:
  /Sources/WebApplication/web/DBadmin/tms_db_admin_config_files/config.xml
  
- Tools folder contains some bat files that can be used in order to perform 
  some mostly administrative operations without requiring the web application 
  to be online. In order to be used the correct paths must be set to file:
  /Tools/0_MainSetup.bat
  Contains subfolder "input-output" which is just a temporary folder that 
  may be used in order to set/get the bat files' input/ouput files. 
  This subfolder may be ignored if redirected to another valid folder

- TSVs folder will be used for the creation of temporary Tab seperated values files (.tsv)
  that are needed when a thesaurus is created or some export to TSV file operation is performed.
  It contains a subfolder "System" that contains 3 tab seperated files that will be used 
  for the creation of generic and specific model.
  
  This Path and file names must be in accordance with file:
  /Sources/WebApplication/web/DBadmin/tms_db_admin_config_files/config.xml
  
  
Date: 2015-09-22
Author: Elias Tzortzakakis <tzortzak@ics.forth.gr>
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

Contact:  POBox 1385, Heraklion Crete, GR-700 13 GREECE
Tel:+30-2810-391632
Fax: +30-2810-391638
E-mail: isl@ics.forth.gr
https://www.ics.forth.gr/isl

Authors: Elias Tzortzakakis <tzortzak@ics.forth.gr>

This file is part of the THEMAS system.
 
# THEMAS

The THEMAS system (Thesaurus Management System) is a Web 
based system for creating and managing multi-
faceted multilingual thesauri with Greek or English language 
serving as the dominant language and a configurable number 
of reference translation languages. [Neo4j open source graph database] (http://neo4j.com/ "Neo4j") 
is used in the backend for data storage, [Neo4j-sisapi 1.4] (https://github.com/isl/Neo4j-sisapi/tree/1.4 "Neo4j-sisapi") 
for the interaction with the database and TELOS 
representation language structures are used as reference for 
the thesauri structure.

The possibilities offered by THEMAS in a thesaurus management 
level, cover a wide range of functions ranging from conservation 
and expansion of thesauri (functions of addition, modification 
and deletion of terms, hierarchies and facets, mass import / 
export of terms, creation of thesauri, saving and restoring backups
etc.) to integration of multiple presentation methods (alphabetical, 
systematic graphical, hierarchical presentation) and access (alternative 
forms of navigation, support of complex search criteria, 
scalability search etc.). 

The purpose of the system and the underlying modeling is to satisfy 
all the needs that derive during the administration of thesauri by 
accelerating and facilitating the procedures necessary for their 
conservation (maintenance) according to the consistency checks 
specified by the relevant ISO and ELOT standards. 

## Build
Instructions on how to compile the THEMAS system are included in file: **How to run and compile.txt**

## Dependencies
The THEMAS dependencies and licenses used are described in file: **THEMAS-Dependencies-LicensesUsed.txt**


# Functionalities
A list of the basic system functionalities is the following:

-	Terms

   --- Create new term 
   
   --- Rename term 
   
   --- Edit / Display  term 
   
   --- Move term to hierarchy (3 subfunctions) 
   
   --- Graphical presentation of term 
   
   --- Delete term 
   
   --- Display all the preferred terms
   
   --- Search terms 
   
   --- Alphabetical presentation 
   
   --- Systematic presentation 
   
   --- Hierarchical presentation 


-	Hierarchies

   --- Create new hierarchy 
   
   --- Rename hierarchy
   
   --- Edit / Display hierarchy
   
   --- Graphical presentation of hierarchy 
   
   --- Delete hierarchy 
   
   --- Display all hierarchies 
   
   --- Search hierarchies

-	Facets

   --- Create new facet
   
   --- Rename facet

   --- Edit / Display facet

   --- Graphical presentation of facet

   --- Delete facet

   --- Show all facets 

   --- Search facets 

-	Thesauri

   --- Change Current Thesaurus

   --- Thesauri Management 

   --- Data Correction of Thesaurus

   --- Create / Copy Thesaurus

   --- Merge Thesauri


- Database 

   --- Database Management
   
   --- Create / restore / delete backup
   
   --- DataBase Correction

-	Users

   --- New user 
   
   --- Show all users 
   
   --- Edit user 

   --- Delete user 

   --- Manage properties of thesaurus users


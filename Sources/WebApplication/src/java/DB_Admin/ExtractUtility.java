/* 
 * Copyright 2015 Institute of Computer Science,
 *                Foundation for Research and Technology - Hellas.
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 * 
 * =============================================================================
 * Contact: 
 * =============================================================================
 * Address: N. Plastira 100 Vassilika Vouton, GR-700 13 Heraklion, Crete, Greece
 *     Tel: +30-2810-391632
 *     Fax: +30-2810-391638
 *  E-mail: isl@ics.forth.gr
 * WebSite: http://www.ics.forth.gr/isl/cci.html
 * 
 * =============================================================================
 * Authors: 
 * =============================================================================
 * Elias Tzortzakakis <tzortzak@ics.forth.gr>
 * 
 * This file is part of the THEMAS system.
 */
package DB_Admin;



import Utils.Parameters;
import java.io.*;
import java.util.Collections;
import java.util.Vector;
import java.util.zip.*;

/*-----------------------------------------------------
                  class ExtractUtility
-------------------------------------------------------*/
class  ExtractUtility 
{
    /*----------------------------------------------------------------------
                          ExtractUtility()
    ------------------------------------------------------------------------*/    
    public ExtractUtility(String file_to_be_extracted, String extractPath) {
        try
        {
            ExtractFile(file_to_be_extracted,extractPath);
        }
        catch (Exception e)
        {
            Utils.StaticClass.handleException(e);
        }        
    }        

    public void ExtractFile(String filename, String extractPath)
    {
        try
        {
            
            //first find all folders to be created
            Vector<String> directories = new Vector<String>();
            ZipInputStream zipinputstream = new ZipInputStream(new FileInputStream(filename));
            ZipEntry zipentry = zipinputstream.getNextEntry();
            while (zipentry != null){ 
                //Utils.StaticClass.webAppSystemOutPrintln(zipentry.getName());
                if(zipentry.getName().endsWith("\\")){
                    directories.add(zipentry.getName().replace("\\","/"));
                }
                zipinputstream.closeEntry();
                zipentry = zipinputstream.getNextEntry();

            }//while

            zipinputstream.close();
            
            Collections.sort(directories);            
            
            for(String dirPath : directories){
                File f = new File(extractPath.replace("\\","/")+"/"+dirPath);
                f.mkdir();
            }
            
            //String destinationname = "C:\\Documents and Settings\\karam\\Desktop\\xxx\\ZIP\\";
            String destinationname = "";
            // ATTENTION: set blank because the zipped files created with "Create Backup" 
            // contain already the path information
            byte[] buf = new byte[1024];
            
            zipinputstream = new ZipInputStream(new FileInputStream(filename));
            zipentry = zipinputstream.getNextEntry();
            while (zipentry != null) { 
                //for each entry to be extracted
                String entryName = zipentry.getName();
                if(entryName.endsWith("\\")){
                    zipinputstream.closeEntry();
                    zipentry = zipinputstream.getNextEntry();
                    continue;
                }
                
                //Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"entryname "+entryName);
                int n;
                FileOutputStream fileoutputstream;
                File newFile = new File(extractPath.replace("\\","/")+"/"+entryName.replace("\\","/"));
                String directory = newFile.getParent();
                
                if(directory == null)
                {
                    if(newFile.isDirectory())
                        break;
                }
                
                //fileoutputstream = new FileOutputStream(destinationname+entryName);             
                fileoutputstream = new FileOutputStream(extractPath.replace("\\","/")+"/"+entryName.replace("\\","/"));

                while ((n = zipinputstream.read(buf, 0, 1024)) > -1)
                    fileoutputstream.write(buf, 0, n);

                fileoutputstream.close(); 
                zipinputstream.closeEntry();
                zipentry = zipinputstream.getNextEntry();

            }//while

            zipinputstream.close();
        }
        catch (Exception e)
        {
            Utils.StaticClass.handleException(e);
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"ExtractFile() FAILED!!!!!!!!!!!!!!!!!!!!!!!! for: " + filename);
            Utils.StaticClass.webAppSystemOutPrintln(Parameters.LogFilePrefix+"REASON: " + e.getMessage());
        }
    }
    
    /*----------------------------------------------------------------------
                         AppendFileContents()
    ------------------------------------------------------------------------
    INPUT : - fileName: the full path of a file
            - contents: a string to be appended in the file
    FUNCTION : appends the given file with the given String contents
    ------------------------------------------------------------------------*/
    public void AppendFileContents(String fileName, String contents) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(fileName, true));
            out.write(contents);
            out.close();
        } catch (IOException e) {
            Utils.StaticClass.handleException(e);
        }
    }                
} 
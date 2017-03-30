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

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Vector;

import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.parboiled.common.FileUtils;


/*-----------------------------------------------------
 class ZipUtilityDBadmin
 -------------------------------------------------------*/
public class ZipUtilityDBadmin {

    private ZipOutputStream cpZipOutputStream = null;
    private String strSource = "";
    private String strTarget = "";
    private static long size = 0;
    private static int numOfFiles = 0;

    /*----------------------------------------------------------------------
     ZipUtility()
     ------------------------------------------------------------------------*/
    public ZipUtilityDBadmin(String directory_or_file_to_be_zipped, String directory_of_zip_file_to_be_created) {
        strSource = directory_or_file_to_be_zipped;
        if(strSource.contains("\\")){
            strSource= strSource.replace("\\", "/");
        }
        if(strSource.endsWith("/")==false){
            strSource+="/";
        }
        strTarget = directory_of_zip_file_to_be_created;
        zip();
    }

    public Vector<Path> fileList(String directory) {
        Vector<Path> fileNames = new Vector<Path>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(directory))) {
            for (Path path : directoryStream) {
                fileNames.add(path);
                if (java.nio.file.Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
                    fileNames.addAll(fileList(path.toString()));
                }
            }
        } catch (IOException ex) {
            Utils.StaticClass.webAppSystemOutPrintln(ex.getMessage());
            Utils.StaticClass.handleException(ex);
        }
        return fileNames;
    }
    /*----------------------------------------------------------------------
     zip()
     ------------------------------------------------------------------------*/

    public void zip() {

        Vector<Path> fileNamesVec = fileList(strSource);
        Vector<String> directories = new Vector<String>();
        Vector<String> files = new Vector<String>();
        //int counter = 1;
        for (Path fpath : fileNamesVec) {
            //Utils.StaticClass.webAppSystemOutPrintln((counter++) + ".\t" + fpath.toString() + "\t" + java.nio.file.Files.isDirectory(fpath, LinkOption.NOFOLLOW_LINKS));
            if (java.nio.file.Files.isDirectory(fpath, LinkOption.NOFOLLOW_LINKS)) {
                directories.add(fpath.toString().replace("\\","/").replace(strSource, ""));
            } else {
                files.add(fpath.toString().replace("\\","/").replace(strSource, ""));
            }
        }

        Collections.sort(directories);
        /*String[] filenames = new File(strSource).list();
        
         for (int i=0; i<filenames.length; i++) {
         filenames[i] = strSource + File.separator + filenames[i];
         }
         */

        // Create a buffer for reading the files
        byte[] buf = new byte[1024];

        try {

            // Create the ZIP file
            String outFilename = strTarget;
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outFilename));

            for (int i = 0; i < directories.size(); i++) {
                out.putNextEntry(new ZipEntry(directories.get(i) + File.separator));
                out.closeEntry();
            }

            // Compress the files
            for (int i = 0; i < files.size(); i++) {
            //Utils.StaticClass.webAppSystemOutPrintln(filenames[i]);

                FileInputStream in = new FileInputStream(strSource+files.get(i));

                // Add ZIP entry to output stream.
                out.putNextEntry(new ZipEntry(files.get(i)));

                // Transfer bytes from the file to the ZIP file
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }

                // Complete the entry
                out.closeEntry();
                in.close();
            }

            // Complete the ZIP file
            out.close();
        } catch (IOException e) {
            Utils.StaticClass.handleException(e);
        }

    }

}

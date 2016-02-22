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
package Utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import neo4j_sisapi.IntegerObject;
import neo4j_sisapi.QClass;
import neo4j_sisapi.StringObject;
import neo4j_sisapi.tmsapi.TMSAPIClass;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Elias Tzortzakakis <tzortzak@ics.forth.gr>
 */
public class StaticClass {

    private static GraphDatabaseService graphDb = null;

    //HLIAS Machine Configurations
    private static String graphdbPath = "";
    
    public static String getGraphDbFolderPath() {
        return graphdbPath;
    }
    //Pavlos Machine Configurations
    //private static final String graphdbPath = "C:\\Development\\New DB Implementations\\Implementation1";
    
    private static boolean isClosing = false;

    public synchronized static void closeDb() {
        Utils.StaticClass.webAppSystemOutPrintln("closeDb CALLED");
        if (graphDb != null) {
            isClosing = true;
            graphDb.shutdown();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Utils.StaticClass.webAppSystemOutPrintln("Thread exception");
                Utils.StaticClass.handleException(e);
                // handle the exception...        
                // For example consider calling Thread.currentThread().interrupt(); here.
            }
            graphDb = null;
            isClosing = false;
        }
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Utils.StaticClass.webAppSystemOutPrintln("Thread exception");
            Utils.StaticClass.handleException(e);
            // handle the exception...        
            // For example consider calling Thread.currentThread().interrupt(); here.
        }
        Utils.StaticClass.webAppSystemOutPrintln("closeDb FINISHED");
        
        return;
    }

    public static boolean isDbready() {
        if (isClosing) {
            return false;
        }
        if (graphDb == null) {
            return false;
        }
        if (graphDb.isAvailable(500) == false) {
            return false;
        }
        return true;
    }

    
    
    
    public static void initializeDatabasePath(String path){
        
        if(graphdbPath.equals("")){
            
            graphdbPath = path;
            
            //redirectSystemOutIfNeeded();
        
        }
    }
    public static void handleException(Exception ex){
        
        Logger.getLogger(StaticClass.class.getName()).log(Level.SEVERE, null, ex);        
        
    }
    /*
    public static PrintStream getWebAppPtrnStream(){
        if(JustInSimpleSystemOut || prtStream==null){
            return System.out;
        }
        else{
            return prtStream;
        }
        
    }*/
    
    static boolean JustInSimpleSystemOut = false;
    public static synchronized void setJustSimpleSystemOut(boolean value){
        JustInSimpleSystemOut = value;
    }
    
    
    public static synchronized void webAppSystemOutPrintln(String str){
        if(JustInSimpleSystemOut /*|| prtStream==null*/){
            //System.out.println(str);
            System.out.println(str);            
        }
        else{
            Logger.getLogger(StaticClass.class.getName()).log(Level.INFO, str);
        }
        
    }
    
    public static synchronized void webAppSystemOutPrint(String str){
        if(JustInSimpleSystemOut /*|| prtStream==null*/){
            System.out.print(str);
        }
        else{
            Logger.getLogger(StaticClass.class.getName()).log(Level.INFO, str);
            //prtStream.print(str);
        }
        
    }
    
    
    /*
    static PrintStream prtStream = null;
    public static synchronized void redirectSystemOutIfNeeded(){

        //SystemOuts
        Calendar rightNow = Calendar.getInstance();
        int current_year = rightNow.get(Calendar.YEAR);
        int current_month = rightNow.get(Calendar.MONTH) + 1;
        int current_day = rightNow.get(Calendar.DAY_OF_MONTH);
        int current_hour = rightNow.get(Calendar.HOUR_OF_DAY);
        int current_minute = rightNow.get(Calendar.MINUTE);
        int current_sec = rightNow.get(Calendar.SECOND);
        
        if(Parameters.ApplicationName.length()==0){
            Parameters.initParams(Parameters.BaseRealPath);
        }
        String todaysFileName = Parameters.BaseRealPath+"/WEB-INF/SystemLogs/"+Parameters.ApplicationName+"_"+current_year+"-"+(current_month<10?"0":"")+current_month +"-"+(current_day<10?"0":"")+current_day 
                +"_"+(current_hour<10?"0":"")+current_hour+"_"+(current_minute<10?"0":"")+current_minute+"_"+(current_hour<10?"0":"")+current_hour+"_"+(current_sec<10?"0":"")+current_sec +".log";
        
        try {
            
            File f = new File(todaysFileName);

            boolean createdNewFile = false;
            if(f.exists()==false){
                f.createNewFile();
                createdNewFile = true;
            }

            
            if(prtStream!=null){
                prtStream.close();
                
            }
            
            OutputStream fout = new FileOutputStream(todaysFileName);
            OutputStream bout = new BufferedOutputStream(fout);
            prtStream = new PrintStream(bout, true, "UTF-8");                
                
            if(createdNewFile){
                prtStream.println("Created New LogFile: " + todaysFileName+"\r\n");
            }
            
        
        } catch (FileNotFoundException ex) {
            Logger.getLogger(StaticClass.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(StaticClass.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(StaticClass.class.getName()).log(Level.SEVERE, null, ex);
        }        
        
    }
    */
    
    public static GraphDatabaseService getDBService() {
        if (graphDb == null) {
            if(graphdbPath.length()==0){
                return null;
            }
            try {
                
                

                //graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(graphdbPath);
                graphDb = new GraphDatabaseFactory()
                        .newEmbeddedDatabaseBuilder(graphdbPath)
                        //.setConfig(GraphDatabaseSettings.allow_store_upgrade, "true")
                        //.setConfig(GraphDatabaseSettings.keep_logical_logs, "2000k txs")
                        .setConfig(GraphDatabaseSettings.keep_logical_logs, "false")
                        //.setConfig(GraphDatabaseSettings.pagecache_memory, "512000000")                        
                        //.setConfig(GraphDatabaseSettings.cache_type, "none")
                        .newGraphDatabase();
                while (graphDb.isAvailable(500) == false) {
                    Utils.StaticClass.webAppSystemOutPrintln("Waiting for 0.5 sec");
                    Thread.sleep(500);
                }
                graphDb.shutdown();
                Thread.sleep(500);
                graphDb = new GraphDatabaseFactory()
                        .newEmbeddedDatabaseBuilder(graphdbPath)
                        //.setConfig(GraphDatabaseSettings.allow_store_upgrade, "true")
                        //.setConfig(GraphDatabaseSettings.keep_logical_logs, "2000k txs")
                        .setConfig(GraphDatabaseSettings.keep_logical_logs, "false")
                        //.setConfig(GraphDatabaseSettings.pagecache_memory, "512000000")                                               
                        //.setConfig(GraphDatabaseSettings.cache_type, "none")
                        .newGraphDatabase();
                while (graphDb.isAvailable(500) == false) {
                    Utils.StaticClass.webAppSystemOutPrintln("Waiting for 0.5 sec");
                    Thread.sleep(500);
                }
                registerShutdownHook();
                while (graphDb.isAvailable(500) == false) {
                    Utils.StaticClass.webAppSystemOutPrintln("Waiting for 0.5 sec");
                    Thread.sleep(500);
                }

            } catch (Exception ex) {
                Utils.StaticClass.webAppSystemOutPrintln("Exception Caught while trying to create newEmbeddedDatabase in path " + graphdbPath);
                Utils.StaticClass.webAppSystemOutPrintln(ex.getMessage());
                Utils.StaticClass.handleException(ex);
            }
        }
        else{
            try{
                while (graphDb.isAvailable(500) == false) {
                    Utils.StaticClass.webAppSystemOutPrintln("Waiting for 0.5 sec");
                    Thread.sleep(500);
                }
            } catch (Exception ex) {
                Utils.StaticClass.webAppSystemOutPrintln("Exception Caught while checking if graph db is available to create newEmbeddedDatabase in path " + graphdbPath);
                Utils.StaticClass.webAppSystemOutPrintln(ex.getMessage());
                Utils.StaticClass.handleException(ex);
            }
        }
        return graphDb;

    }

    private static void registerShutdownHook() {

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (graphDb != null) {
                    //DB_Admin.DBAdminUtilities.setLockVariable(true);
                    graphDb.shutdown();
                    graphDb = null;
                }
            }
        });
    }
    /*
    
     public static int openConnectionAndStartQueryOrTransaction(QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session,
     String targetThesaurus, boolean startQueryInsteadOfTransaction){

     Utils.StaticClass.webAppSystemOutPrintln("openConnectionAndStartQueryOrTransaction");
     //create_SIS_CS_Session
     if(Q.create_SIS_CS_Session() == QClass.APIFail){
     Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ create_SIS_CS_Session");
     return QClass.APIFail;
     }

     //open_connection
     if(Q.open_connection() == QClass.APIFail){
     Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ open_connection");
     Q.release_SIS_Session();
     return QClass.APIFail;
     }

     if(startQueryInsteadOfTransaction){//if query
     //begin_query
     if(Q.begin_query()==QClass.APIFail){
     Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ begin_query");
     CloseDBConnection(Q, null, sis_session,null, false);
     return QClass.APIFail;
     }
     }
     else{
     //create_TMS_API_Session
     if (TA.create_TMS_API_Session() == TMSAPIClass.TMS_APIFail) {
     Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ create_TMS_API_Session");
     CloseDBConnection(Q, null, sis_session,null, false);
     return QClass.APIFail;
     }
            
     //SetThesaurusName
     if(targetThesaurus!=null){
     Utils.StaticClass.webAppSystemOutPrintln("setting thesaurus Name to: " + targetThesaurus);
     if(TA.SetThesaurusName(tms_session.getValue(), new StringObject(targetThesaurus))==TMSAPIClass.TMS_APIFail){
     Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ SetThesaurusName");
     CloseDBConnection(Q, TA, sis_session, tms_session, true);
     return QClass.APIFail;
     }
     }

     //begin_transaction
     if(Q.begin_transaction()==QClass.APIFail){
     Utils.StaticClass.webAppSystemOutPrintln("OPEN CONNECTION ERROR @ begin_transaction");
     CloseDBConnection(Q, TA, sis_session, tms_session, true);
     return QClass.APIFail;
     }
     }

     return QClass.APISucc;
     }
    
     public static int CloseDBConnection(QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session,boolean TMSSessionRelease){
     int ret =QClass.APISucc;

     Utils.StaticClass.webAppSystemOutPrintln("CloseDBConnection");
        
     if(TMSSessionRelease && TA!=null){
     ret = TA.release_TMS_API_Session(tms_session.getValue());
     if(ret==TMSAPIClass.TMS_APIFail){
     Utils.StaticClass.webAppSystemOutPrintln("CLOSE CONNECTION ERROR @ release_TMS_API_Session");
     Q.close_connection();
     Q.release_SIS_Session();
     return ret;
     }
     }

     ret=Q.close_connection();
     if(ret==QClass.APIFail){
     Utils.StaticClass.webAppSystemOutPrintln("CLOSE CONNECTION ERROR @ close_connection");
     Q.release_SIS_Session();
     return ret;
     }

     ret=Q.release_SIS_Session();
     if(ret==QClass.APIFail){
     Utils.StaticClass.webAppSystemOutPrintln("CLOSE CONNECTION ERROR @ release_SIS_Session");
     }
     return ret;
     }
    
     */
}

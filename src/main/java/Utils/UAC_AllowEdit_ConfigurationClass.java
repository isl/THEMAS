/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utils;

import java.util.ArrayList;

/**
 *
 * @author Elias
 */
public class UAC_AllowEdit_ConfigurationClass {
    String targetStatus;
    String targetRole;
    boolean requiresCreatedBySameUser = false;
    boolean allowEdit = false;
    
    UAC_AllowEdit_ConfigurationClass(){
        targetRole ="";
        targetStatus ="";        
        requiresCreatedBySameUser = false;
        allowEdit = false;
    }
    
    UAC_AllowEdit_ConfigurationClass(String statusVal, String roleVal, boolean reqCreatedByVal, boolean allowEditVal){
        targetStatus =statusVal;
        targetRole =roleVal;
        requiresCreatedBySameUser = reqCreatedByVal;
        allowEdit = allowEditVal;
    }
}

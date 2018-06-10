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
class UAC_StatusChanges_ConfigurationClass {
    
    String targetStatus;
    String targetRole;
    boolean requiresCreatedBySameUser = false;
    ArrayList<String> availableStatusChoices;
    
    UAC_StatusChanges_ConfigurationClass(){
        targetStatus ="";
        targetRole ="";
        requiresCreatedBySameUser = false;
        availableStatusChoices = new ArrayList<>();
    }
    
    UAC_StatusChanges_ConfigurationClass(String statusVal, String roleVal, boolean reqCreatedByVal, ArrayList<String> statusChanges){
        targetStatus =statusVal;
        targetRole =roleVal;
        requiresCreatedBySameUser = reqCreatedByVal;
        availableStatusChoices = new ArrayList<>();
        for(String str : statusChanges ){
            availableStatusChoices.add(str);
        }
        
    }
}

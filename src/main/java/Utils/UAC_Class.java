/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utils;

import java.util.ArrayList;
import java.util.Optional;

/**
 * UAC_Class stands for User Access Control class and will be used in order to
 * apply the rules of the thesaurus management work-flow.
 *
 * @author Elias
 */
public class UAC_Class {

    /**
     * Thesaurus name is passed as a parameter as the configuration of roles may
     * later be applied to the thesaurus access specification not in the
     * config.xml file where it affects the whole web-application (multiple
     * thesauri).
     *
     * @param roleName
     * @param thesaurusName
     * @param currentTermStatus
     * @return
     */
    public ArrayList<String> getAvailable_Term_StatusChanges(String userName, String roleName, String thesaurusName, String currentTermStatus, ArrayList<String> termCreatorNames) {
        ArrayList<String> retVals = new ArrayList<>();

        Optional<UAC_StatusChanges_ConfigurationClass> uacClass
                = Parameters.Override_RoleConfigurations.stream().filter(conf
                        -> conf.targetRole.equals(roleName) && conf.targetStatus.equals(currentTermStatus)).findFirst();

        if (roleName != null) {
            switch (roleName) {
                //admin no override
                case ConstantParameters.Group_Administrator: {
                    //no override available ( Admins can perform any status transition)
                    retVals.add(Parameters.Status_For_Insertion);
                    retVals.add(Parameters.Status_Under_Construction);
                    retVals.add(Parameters.Status_For_Approval);
                    retVals.add(Parameters.Status_Approved);
                    break;
                }
                
                case ConstantParameters.Group_ThesaurusCommittee: {

                    //case that this is overriden by config.xml
                    if (uacClass != null && uacClass.isPresent()) {
                        
                        if ( !uacClass.get().requiresCreatedBySameUser || 
                             (uacClass.get().requiresCreatedBySameUser && termCreatorNames.contains(userName)) 
                            ) {
                            retVals.addAll(uacClass.get().availableStatusChoices);
                            
                        } 
                    } else {
                        
                        //default case for committe check xslt code
                        if (currentTermStatus.equalsIgnoreCase(Parameters.Status_For_Insertion)) {
                            retVals.add(currentTermStatus);
                            retVals.add(Parameters.Status_Under_Construction);
                            
                        } else if (currentTermStatus.equalsIgnoreCase(Parameters.Status_Under_Construction)) {
                            retVals.add(currentTermStatus);
                            retVals.add(Parameters.Status_For_Approval);
                            retVals.add(Parameters.Status_Approved);
                            
                        } else if (currentTermStatus.equalsIgnoreCase(Parameters.Status_For_Approval)) {
                            retVals.add(currentTermStatus);
                            retVals.add(Parameters.Status_Under_Construction);
                            retVals.add(Parameters.Status_Approved);
                            
                        } else if (currentTermStatus.equalsIgnoreCase(Parameters.Status_Approved)) {
                            retVals.add(currentTermStatus);
                            retVals.add(Parameters.Status_Under_Construction);
                        }
                    }
                    break;
                }
                case ConstantParameters.Group_ThesaurusTeam: {
                    
                    if (uacClass != null && uacClass.isPresent()) {
                        
                        if ( !uacClass.get().requiresCreatedBySameUser || 
                             (uacClass.get().requiresCreatedBySameUser && termCreatorNames.contains(userName)) 
                            ) {
                            retVals.addAll(uacClass.get().availableStatusChoices);
                        }                        
                    } else {
                        
                        //Parameters.ThesTeamEditOnlyCreatedByTerms;                    
                        if (currentTermStatus.equalsIgnoreCase(Parameters.Status_For_Insertion)) {
                            retVals.add(currentTermStatus);
                            retVals.add(Parameters.Status_Under_Construction);
                        } else if (currentTermStatus.equalsIgnoreCase(Parameters.Status_Under_Construction)) {
                            retVals.add(currentTermStatus);
                            retVals.add(Parameters.Status_For_Approval);
                        } else if (currentTermStatus.equalsIgnoreCase(Parameters.Status_For_Approval)) {
                            retVals.add(currentTermStatus);
                            retVals.add(Parameters.Status_Under_Construction);
                        } else if (currentTermStatus.equalsIgnoreCase(Parameters.Status_Approved)) {
                            retVals.add(currentTermStatus);
                        }
                    }
                    break;
                }
                case ConstantParameters.Group_Library: {
                    //case that this is overriden by config.xml
                    if (uacClass != null && uacClass.isPresent()) {
                        
                        if (!uacClass.get().requiresCreatedBySameUser ||
                            (uacClass.get().requiresCreatedBySameUser && termCreatorNames.contains(userName)) ) {                            
                                retVals.addAll(uacClass.get().availableStatusChoices);
                        } 
                    } else {
                        
                        //default behaviour
                        //no option at all                        
                    }
                    //in general library users cannot change status -- 
                    //perhaps this is wrong trhough as they do not have a way 
                    //to inform higher role users that they have finished their 
                    //proposal.

                    //Parameters.Override_Library_Users_StatusChanges;
                    break;
                }
                /* 
                 In general these readers should not be able to reach the edit card at all
                case ConstantParameters.Group_External_Reader:{
                    retVals.add(currentTermStatus);
                    break;
                }
                case ConstantParameters.Group_Reader:{
                    retVals.add(currentTermStatus);
                    break;
                }*/
                default: {
                    retVals.add(currentTermStatus);
                    break;
                }
            }
        }
        return retVals;
    }

    public static boolean allowTermEdit(String userName, String roleName, String thesaurusName, String currentTermStatus, ArrayList<String> termCreatorNames) {

        Optional<UAC_AllowEdit_ConfigurationClass> uacClass
                = Parameters.Override_AllowEditConfigurations.stream().filter(conf
                        -> conf.targetRole.equals(roleName) && conf.targetStatus.equals(currentTermStatus)).findFirst();

        if (roleName != null) {
            switch (roleName) {
                case ConstantParameters.Group_Administrator: {
                    return true;
                }
                case ConstantParameters.Group_Reader: {
                    return false;
                }
                case ConstantParameters.Group_External_Reader: {
                    return false;
                }
                case ConstantParameters.Group_ThesaurusCommittee: {
                    if (uacClass != null && uacClass.isPresent()) {
                        if ( (uacClass.get().requiresCreatedBySameUser && termCreatorNames.contains(userName)) || 
                             !uacClass.get().requiresCreatedBySameUser) {
                            return true;
                        }
                    }
                    else{
                        //default behaviour for thesaurus committe
                        return true;
                    }
                    break;
                }
                case ConstantParameters.Group_ThesaurusTeam: {
                    if (uacClass != null && uacClass.isPresent()) {
                        if ( (uacClass.get().requiresCreatedBySameUser && termCreatorNames.contains(userName)) || 
                             !uacClass.get().requiresCreatedBySameUser) {
                            return true;
                        }
                    }
                    else{
                        //default behaviour for thesaurus team
                        if (!currentTermStatus.equalsIgnoreCase(Parameters.Status_Approved)) {
                            return true;
                        }
                    }
                    break;
                }
                case ConstantParameters.Group_Library: {
                    
                    if (uacClass != null && uacClass.isPresent()) {
                        if ( (uacClass.get().requiresCreatedBySameUser && termCreatorNames.contains(userName)) || 
                             !uacClass.get().requiresCreatedBySameUser) {
                            return true;
                        }
                    }
                    else{
                        //default behaviour for library user
                        if (currentTermStatus.equalsIgnoreCase(Parameters.Status_For_Insertion) && termCreatorNames.contains(userName)) {
                            return true;
                        }
                    }
                    break;                    
                }
                default: {
                    return true;
                }
            }
        }
        return false;
    }
}

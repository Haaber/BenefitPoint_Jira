package com.vp.jira.project.calc;


import com.atlassian.jira.issue.Issue;
import com.vp.jira.project.data.BenefitPoint;
import com.vp.jira.project.data.Contribution;
import com.vp.jira.project.data.Objective;
import com.vp.jira.project.data.Return;
import com.vp.jira.project.web.ValuePanel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



public class BenefitCalculation {


    // Returns the total contribution for each objective
    public static double contribution(List<Contribution> conlist, List<Return> retlist, int objectiveID){

        double total = 0;
        for (Contribution contribution: conlist) {
            if (contribution.getObjectiveID() == objectiveID){
                for (Return return1: retlist) {
                    if (contribution.getReturnID() == return1.getID() ){ // This check is actually obsolette xD
                        total += (contribution.getValue() * return1.getContribution()) / 100;
                    }
                }
            }
        }
        return total;
    }

    // Returns the total contribution for all objective
    public static double contributionTotal(List<Double> totalResList){

        return totalResList.stream().mapToDouble(Double::doubleValue).sum();
    }

    // Returns the ProjectWeight
    public  static double projectWeight (double totalResList, double contributionSum){

        if (contributionSum == 0){
            return 0.0;
        } else{
            return totalResList/contributionSum;
        }

    }

    // totalObjEpic returns the total amount of benefitpoints for each individually epic
    public static int totalObjEpic(List<BenefitPoint> benlist, List<Objective> objlist, long id){

        int total = 0;
        for (BenefitPoint benefitpoint : benlist) {
            if(benefitpoint.getEpicID() == id){
                for (Objective objective : objlist) {
                    if(benefitpoint.getObjectiveID() == objective.getID())
                        total += benefitpoint.getValue();
                }
            }
        }
        return total;
    }

    // totalEpicObj returns the total benefitpoints for all epics
    public static int totalEpicObj(List<BenefitPoint> benlist, List<Issue> issueList, long id){

        int total = 0;
        for (BenefitPoint benefitpoint : benlist) {
            if(benefitpoint.getObjectiveID() == id){
                for (Issue issue : issueList) {
                    if(benefitpoint.getEpicID() == issue.getId())
                        total += benefitpoint.getValue();
                }
            }
        }
        return total;
    }
    // balanceBenefitPoint returns the calculated balance benefitpoint
    public static double balanceBenefitPoint (double projectWeight, int totalBP, int bp, int totalobjbp){
        return bp * projectWeight * totalBP / totalobjbp;
    }

    // totalReturnCont returns the total contribution for a return
     public static double totalReturnCont(List<Contribution> contributionList, List<Objective> objectiveList, long returnID){
        double total = 0;

        for (Contribution contribution: contributionList) {
            if (contribution.getReturnID() == returnID){
                for (Objective objective: objectiveList) {
                    if (contribution.getObjectiveID() == objective.getID()){
                        total += contribution.getValue();
                    }
                }
            }
        }

        return total;
    }
}

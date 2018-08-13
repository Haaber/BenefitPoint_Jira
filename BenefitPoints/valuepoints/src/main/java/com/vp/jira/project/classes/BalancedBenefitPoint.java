package com.vp.jira.project.classes;

public class BalancedBenefitPoint {

    long epicID;
    long objectiveID;
    double balancedBP;

    public BalancedBenefitPoint(long epicID, long objectiveID, double balancedBP){
        this.epicID = epicID;
        this.objectiveID = objectiveID;
        this.balancedBP = balancedBP;
    }

    public long getEpicID() {
        return epicID;
    }

    public long getObjectiveID() {
        return objectiveID;
    }

    public double getBalancedBP() {
        return balancedBP;
    }
}

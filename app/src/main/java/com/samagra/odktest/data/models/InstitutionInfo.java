package com.samagra.odktest.data.models;

/**
 * A POJO for representing the a InstitutionInfo.
 */
public class InstitutionInfo {

    public String District;
    public String Block;
    public String SchoolName;

    public String getStringForSearch() {
        return this.District + " "
                + this.Block + " "
                + this.SchoolName;
    }

    public InstitutionInfo(String district, String block, String institutionName) {
        this.SchoolName = institutionName;
        this.Block = block;
        this.District = district;
    }

}

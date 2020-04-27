package com.samagra.odktest.data.models;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.PriorityQueue;

public class Submission {

    String udise;
    String url;
    String submissionDate;
    String formID;
    String formName;
    String fileName;

    public String getFormName() {
        return fileName;
    }

    public String getSubmissionDate() {
        return submissionDate;
    }

    public String getURL() {
        return url;
    }

    public Date getSubmissionDateISO(){
        DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");
        // DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        try {
            return df1.parse(this.submissionDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}

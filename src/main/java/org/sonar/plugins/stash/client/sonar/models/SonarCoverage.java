package org.sonar.plugins.stash.client.sonar.models;

import java.util.Date;
import java.util.List;
/**
 * Created by dmytro.khaynas on 3/29/17.
 */
public class SonarCoverage {

    private long id;
    private String uuid;
    private String key;
    private String name;
    private String lname;
    private String version;
    private String scope;
    private String qualifier;
    private Date creationDate;
    private Date date;

    private List<Msr> msr;

    public static class Msr {

        private String key;
        private double val;
        private String frmt_val;
    }

    public double getCoverage() {
        return msr.size() > 0 ? msr.get(0).val : 0;
    }
}

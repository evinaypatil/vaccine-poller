package com.vaccine.poller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.Ints;

import java.awt.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Polls for vaccine availability.
 */
public class Poll {

    /**
     * Object mapper instance.
     */
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /**
     * Poll time.
     */
    private static final int POLL_TIME_SECONDS = 60;

    /**
     * Check if vaccine is available.
     *
     * @param date          Date to check for (7 days from this date).
     * @param districtId    District ID.
     * @param min_age_limit Minimum age limit.
     * @param feeType       Fee type.
     * @return true, if available.
     */
    private static boolean check(String date, int districtId, int min_age_limit, String feeType) {

        var available = false;

        HttpURLConnection connection = null;
        try {
            // Create a neat value object to hold the URL
            var url = new URL("https://cdn-api.co-vin.in/api/v2/appointment/sessions/public/calendarByDistrict?district_id=" + districtId + "&date=" + date);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("accept", "application/json");
            var responseStream = connection.getInputStream();

            // Parse the response and check for availability.
            var centres = MAPPER.readValue(responseStream, Centres.class).getCenters();
            for (var centre : centres) {
                for (var session : centre.getSessions()) {
                    if (session.getMinAgeLimit() == min_age_limit
                            && session.getAvailableCapacity() > 0
                            && (feeType == null || feeType.equalsIgnoreCase(centre.getFeeType().toLowerCase()))) {

                        System.out.println("===================================================");
                        System.out.println("Date: " + session.getDate());
                        System.out.println("Centre: " + centre.getName());
                        System.out.println("Address: " + centre.getAddress());
                        System.out.println("Pin: " + centre.getPinCode());

                        System.out.println("Fee type: " + centre.getFeeType());
                        System.out.println("Age limit: " + session.getMinAgeLimit());
                        System.out.println("Vaccine: " + session.getVaccine());
                        System.out.println("Capacity: " + session.getAvailableCapacity());
                        System.out.println("===================================================");

                        // Set available to true.
                        available = true;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error - " + e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        // If unavailable, print message.
        if (!available) {
            System.out.println("No vaccine available week starting at (Date = " + date + ", age limit = " + min_age_limit + " and fee type = " + (feeType == null ? "free/paid" : feeType) + ")");
        } else {
            IntStream.range(0, 10000).forEach(n ->
                    Toolkit.getDefaultToolkit().beep()
            );
        }

        return available;
    }

    /**
     * Main method.
     *
     * @param args Arguments.
     */
    public static void main(String[] args) {

        // Check arguments
        var vaccineArgs = new ArrayList<VaccineArg>();
        if (args.length == 0) {
            System.out.println("Provide arguments in the format - date,district_id,age-limit,fee-type (Date string format 'mm-dd-yyyy', age-limit is either 18/45,  fee-type is optional (if provided, should be paid or free)))");
            System.exit(1);
        } else {
            // Extract vaccine arguments.
            for (String arg : args) {

                var values = arg.split(",");
                if (values.length < 3) {
                    System.out.println("Each argument should be comma separated list - date,district_id,age-limit,fee-type (Date string format 'mm-dd-yyyy', age-limit is either 18/45,  fee-type is optional (if provided, should be paid or free))");
                    System.exit(1);
                }

                vaccineArgs.add(new VaccineArg()
                        .setDate(values[0])
                        .setDistrictId(Ints.tryParse(values[1]))
                        .setAgeLimit(Ints.tryParse(values[2]))
                        .setFeeType(values.length == 4 ? values[3] : null)
                );
            }
        }

        // Loop and check.
        while (true) {
            System.out.println("**********************" + new Date() + "*************************");
            for (VaccineArg arg : vaccineArgs) {
                check(arg.getDate(), arg.getDistrictId(), arg.getAgeLimit(), arg.getFeeType());
            }
            System.out.println("***************************************************************************");
            System.out.println("");

            // Sleep 30 seconds.
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                System.out.println("Error - " + e.getMessage());
            }
        }
    }
}

/**
 * Centres class.
 */
class Centres {
    private List<Centre> centers;

    public List<Centre> getCenters() {
        return centers;
    }

    public void setCenters(List<Centre> centers) {
        this.centers = centers;
    }
}

/**
 * Centre class.
 */
class Centre {

    private String name;

    private String address;

    @JsonProperty("pincode")
    private long pinCode;

    @JsonProperty("fee_type")
    private String feeType;

    private List<Session> sessions;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public long getPinCode() {
        return pinCode;
    }

    public void setPinCode(long pinCode) {
        this.pinCode = pinCode;
    }

    public String getFeeType() {
        return feeType;
    }

    public void setFeeType(String feeType) {
        this.feeType = feeType;
    }

    public List<Session> getSessions() {
        return sessions;
    }

    public void setSessions(List<Session> sessions) {
        this.sessions = sessions;
    }
}

/**
 * Vaccine session.
 */
class Session {

    private String date;

    @JsonProperty("min_age_limit")
    private Integer minAgeLimit;

    private String vaccine;

    @JsonProperty("available_capacity")
    private Integer availableCapacity;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Integer getMinAgeLimit() {
        return minAgeLimit;
    }

    public void setMinAgeLimit(Integer minAgeLimit) {
        this.minAgeLimit = minAgeLimit;
    }

    public String getVaccine() {
        return vaccine;
    }

    public void setVaccine(String vaccine) {
        this.vaccine = vaccine;
    }

    public Integer getAvailableCapacity() {
        return availableCapacity;
    }

    public void setAvailableCapacity(Integer availableCapacity) {
        this.availableCapacity = availableCapacity;
    }
}

/**
 * vaccine arguments.
 */
class VaccineArg {

    private String date;

    private Integer districtId;

    private Integer ageLimit;

    private String feeType;

    public Integer getDistrictId() {
        return districtId;
    }

    public VaccineArg setDistrictId(Integer districtId) {
        this.districtId = districtId;
        return this;
    }

    public String getDate() {
        return date;
    }

    public VaccineArg setDate(String date) {
        this.date = date;
        return this;
    }

    public Integer getAgeLimit() {
        return ageLimit;
    }

    public VaccineArg setAgeLimit(Integer ageLimit) {
        this.ageLimit = ageLimit;
        return this;
    }

    public String getFeeType() {
        return feeType;
    }

    public VaccineArg setFeeType(String feeType) {
        this.feeType = feeType;
        return this;
    }
}

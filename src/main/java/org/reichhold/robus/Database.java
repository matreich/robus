/**
 * Created with IntelliJ IDEA.
 * User: matthias
 * Date: 21.07.12
 * Time: 12:39
 * To change this template use File | Settings | File Templates.
 */
package org.reichhold.robus;

import org.reichhold.robus.hbm.JobAd;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database
{
    Connection conn = null;
    String url = "jdbc:mysql://localhost:3306/robus";

    public Database()
    {
        try {
            // The newInstance() call is a work around for some
            // broken Java implementations

            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (Exception ex) {
            System.out.println("SQLException: " + ex.getMessage());
        }
    }

    public boolean Open()
    {
        try
        {
            conn = DriverManager.getConnection(url, "root", "");
            return true;
        }
        catch (SQLException ex)
        {
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
            return false;
        }
    }

    public boolean Close()
    {
        try
        {
            if(conn != null)
            {
                conn.close();
            }
            return true;
        }
        catch (SQLException ex)
        {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
            return false;
        }
    }

    public void InsertJobAds(List<JobAd> jobs)
    {
        int rowCount = 0;
        boolean isInserted;

        if(conn == null)
        {
            Open();
        }

        for (JobAd job:jobs)
        {
            if(job == null)
            {
                isInserted = false;
                break;
            }
            isInserted = InsertOrUpdateJobAd(job);

            if (isInserted)
            {
                rowCount++;
            }
        }
        System.out.println("Successfully inserted " + rowCount + " jobs into jobAds table");
    }

    public boolean InsertOrUpdateJobAd(JobAd job)
    {
        int jobid = Integer.parseInt(job.getJobId());
        JobAd jobAd = GetJobAdById(jobid);

        if(jobAd != null)
        {
               DeleteJobAdById(jobid);
        }

        try {
            PreparedStatement pstmt = null;

            String insertQuery = "INSERT INTO jobAd (JobID, CompanyName, CompanyID, Title, Description, Skills)"
                    +"VALUES"
                    +"(?, ?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(insertQuery);
            pstmt.setString(1, job.getJobId());
            pstmt.setString(2, job.getCompanyName());
            pstmt.setString(3, job.getCompanyId());
            pstmt.setString(4, job.getTitle());
            pstmt.setString(5, job.getDescription());
            pstmt.setString(6, job.getSkills());
            pstmt.executeUpdate();

            //System.out.println("Inserted details for job " + job.jobId);

            return true;

        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());

            return false;
        }
    }

    public JobAd GetJobAdById(int id)
    {
        Statement stmt = null;
        ResultSet rs = null;
        JobAd job = null;

        try {
            stmt = conn.createStatement();

            rs = stmt.executeQuery("SELECT * FROM JobAd where jobID =" + id);

            while(rs.next())
            {
                job = new JobAd();
                job.setJobId(rs.getString("JobID"));
                job.setCompanyName(rs.getString("CompanyName"));
                job.setCompanyId(rs.getString("CompanyID"));
                job.setTitle(rs.getString("Title"));
                job.setDescription(rs.getString("Description"));
                job.setSkills(rs.getString("Skills"));
            }

            return job;
        }
        catch (SQLException ex){
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());

            return null;
        }
        finally
        {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sqlEx) { } // ignore

                rs = null;
            }

            if (stmt != null)
            {
                try
                {
                    stmt.close();
                } catch (SQLException sqlEx) { } // ignore

                stmt = null;
            }
        }
    }

    public List<String> GetEmptyJobIds(int max)
    {
        Statement stmt = null;
        ResultSet rs = null;
        List<String> ids = new ArrayList<String>();

        if (conn == null)
        {
            Open();
        }
        try {
            stmt = conn.createStatement();

            rs = stmt.executeQuery("SELECT * FROM JobAd where title = '' limit " + max);

            while(rs.next())
            {
                ids.add(rs.getString("JobID"));
            }
        }
        catch (SQLException ex){
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
        finally
        {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sqlEx) { } // ignore

                rs = null;
            }

            if (stmt != null)
            {
                try
                {
                    stmt.close();
                } catch (SQLException sqlEx) { } // ignore

                stmt = null;
            }
        }

        return ids;
    }

    public void DeleteJobAdById(int id)
    {
        if (conn == null)
        {
            Open();
        }
        try {
            PreparedStatement pstmt = null;

            String delQuery = "DELETE FROM JobAd where jobID = " + id;
            pstmt = conn.prepareStatement(delQuery);
            pstmt.executeUpdate();
        }
        catch (SQLException ex){
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
    }
}

/**
 * 
 */
package com.amazonaws.samples;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.CreateTagsResult;

/**
 * @author nirmoho-Mac
 *
 */
public class AWSInstanceClass {
    
    private String AMI_ID;
    private String INSTANCE_TYPE;
    private String KEY_NAME ; 
    private String SECURITY_GROUP;
    private String PUBLIC_DNS;
    private String INSTANCE_ID;
    
    public AWSInstanceClass(String aMI_ID, String iNSTANCE_TYPE) {
        super();
        this.AMI_ID = aMI_ID;
        this.INSTANCE_TYPE = iNSTANCE_TYPE;
        this.KEY_NAME = "Ubuntu_t2_micro";
        this.SECURITY_GROUP = "launch-wizard-3";
    }

    public String getAMI_ID() {
        return this.AMI_ID;
    }

    public void setAMI_ID(String aMI_ID) {
        this.AMI_ID = aMI_ID;
    }

    public String getINSTANCE_TYPE() {
        return this.INSTANCE_TYPE;
    }

    public void setINSTANCE_TYPE(String iNSTANCE_TYPE) {
        this.INSTANCE_TYPE = iNSTANCE_TYPE;
    }

    public String getKEY_NAME() {
        return this.KEY_NAME;
    }

    public void setKEY_NAME(String kEY_NAME) {
        this.KEY_NAME = kEY_NAME;
    }

    public String getSECURITY_GROUP() {
        return this.SECURITY_GROUP;
    }

    public void setSECURITY_GROUP(String sECURITY_GROUP) {
        this.SECURITY_GROUP = sECURITY_GROUP;
    }

    public String getPUBLIC_DNS() {
        AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();
        boolean done = false;

        while(!done) {
            
            DescribeInstancesRequest request = new DescribeInstancesRequest();
            DescribeInstancesResult response = ec2.describeInstances(request);
            
            for(Reservation reservation : response.getReservations()) {
                for(Instance instance : reservation.getInstances()) {
                    if(instance.getInstanceId().equals(this.INSTANCE_ID))   {
                        this.PUBLIC_DNS = instance.getPublicDnsName();
                    }   
                }
            }

            request.setNextToken(response.getNextToken());

            if(this.PUBLIC_DNS.length() > 0) {
                done = true;
            }
       
        }
        ec2.shutdown();
        return this.PUBLIC_DNS;
    }

    public String createInstance()  {
        AWSCredentialsProvider credentialsProvider = new DefaultAWSCredentialsProviderChain();
        
     // Create an Amazon EC2 Client
        AmazonEC2 ec2 = AmazonEC2ClientBuilder
                .standard()
                .withCredentials(credentialsProvider)
                .withRegion(Regions.US_EAST_1)
                .build();
        // Create a Run Instance Request
        
        RunInstancesRequest runInstancesRequest = new RunInstancesRequest()
              .withImageId(AMI_ID)
              .withInstanceType(INSTANCE_TYPE)
              .withMinCount(1)
              .withMaxCount(1)
              .withKeyName(KEY_NAME)
              .withSecurityGroups(SECURITY_GROUP);
        
        RunInstancesResult runResponse = ec2.runInstances(runInstancesRequest);
        String reservationId = runResponse.getReservation().getReservationId();
        Instance instance = runResponse.getReservation().getInstances().get(0);
        this.INSTANCE_ID = instance.getInstanceId();
        
        Tag tag = new Tag()
                .withKey("Project")
                .withValue("2.1");

        CreateTagsRequest tag_request = new CreateTagsRequest().withTags(tag).withResources(this.INSTANCE_ID);
        CreateTagsResult tag_response = ec2.createTags(tag_request);
        return this.INSTANCE_ID;
    }
}

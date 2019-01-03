package org.deep.rogs.model;


public class S3PointerMessage {
    private String s3BucketName;
    private String s3Key;

    public S3PointerMessage() {
    }

    public S3PointerMessage(String s3BucketName, String s3Key) {
        this.s3BucketName = s3BucketName;
        this.s3Key = s3Key;
    }

    public String getS3BucketName() {
        return s3BucketName;
    }

    public void setS3BucketName(String s3BucketName) {
        this.s3BucketName = s3BucketName;
    }

    public String getS3Key() {
        return s3Key;
    }

    public void setS3Key(String s3Key) {
        this.s3Key = s3Key;
    }


}

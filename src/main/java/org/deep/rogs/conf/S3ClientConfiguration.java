package org.deep.rogs.conf;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "deep.s3")
public class S3ClientConfiguration {

    private String s3BucketName;
    private String clientRegion;
    private String stringObjectKeyName;
    private String fileObjectKeyName;
    private String fileName;

    public String getS3BucketName() {
        return s3BucketName;
    }

    public void setS3BucketName(String s3BucketName) {
        this.s3BucketName = s3BucketName;
    }

    public String getClientRegion() {
        return clientRegion;
    }

    public void setClientRegion(String clientRegion) {
        this.clientRegion = clientRegion;
    }

    public String getStringObjectKeyName() {
        return stringObjectKeyName;
    }

    public void setStringObjectKeyName(String stringObjectKeyName) {
        this.stringObjectKeyName = stringObjectKeyName;
    }

    public String getFileObjectKeyName() {
        return fileObjectKeyName;
    }

    public void setFileObjectKeyName(String fileObjectKeyName) {
        this.fileObjectKeyName = fileObjectKeyName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}

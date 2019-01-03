package org.deep.rogs.conf;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "deep.sqs")
public class SQSClientConfiguration {

    private String ACCESS_KEY_ID;
    private String SECRET_ACCESS_KEY;
    private int port;
    private String PROXY_HOST;
    private boolean largePayloadSupport;
    private boolean alwaysThroughS3;
    private String region;

    public String getACCESS_KEY_ID() {
        return ACCESS_KEY_ID;
    }

    public void setACCESS_KEY_ID(String ACCESS_KEY_ID) {
        this.ACCESS_KEY_ID = ACCESS_KEY_ID;
    }

    public String getSECRET_ACCESS_KEY() {
        return SECRET_ACCESS_KEY;
    }

    public void setSECRET_ACCESS_KEY(String SECRET_ACCESS_KEY) {
        this.SECRET_ACCESS_KEY = SECRET_ACCESS_KEY;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPROXY_HOST() {
        return PROXY_HOST;
    }

    public void setPROXY_HOST(String PROXY_HOST) {
        this.PROXY_HOST = PROXY_HOST;
    }

    public boolean isLargePayloadSupport() {
        return largePayloadSupport;
    }

    public void setLargePayloadSupport(boolean largePayloadSupport) {
        this.largePayloadSupport = largePayloadSupport;
    }

    public boolean isAlwaysThroughS3() {
        return alwaysThroughS3;
    }

    public void setAlwaysThroughS3(boolean alwaysThroughS3) {
        this.alwaysThroughS3 = alwaysThroughS3;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }
}

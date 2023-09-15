package org.xiaoxingqi.shengxi.model;

public class OssToken {

    private String AccessKeySecret;
    private String AccessKeyId;
    private String Expiration;
    private String SecurityToken;

    public String getAccessKeySecret() {
        return AccessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        AccessKeySecret = accessKeySecret;
    }

    public String getAccessKeyId() {
        return AccessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        AccessKeyId = accessKeyId;
    }

    public String getExpiration() {
        return Expiration;
    }

    public void setExpiration(String expiration) {
        Expiration = expiration;
    }

    public String getSecurityToken() {
        return SecurityToken;
    }

    public void setSecurityToken(String securityToken) {
        SecurityToken = securityToken;
    }

    @Override
    public String toString() {
        return "OssToken{" +
                "AccessKeySecret='" + AccessKeySecret + '\'' +
                ", AccessKeyId='" + AccessKeyId + '\'' +
                ", Expiration='" + Expiration + '\'' +
                ", SecurityToken='" + SecurityToken + '\'' +
                '}';
    }
}

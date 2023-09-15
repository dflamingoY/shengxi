package org.xiaoxingqi.shengxi.impl;

import org.xiaoxingqi.shengxi.model.BaseRepData;

public class IpQuestData extends BaseRepData {
    private IpDataBean data;

    public IpDataBean getData() {
        return data;
    }

    public void setData(IpDataBean data) {
        this.data = data;
    }

    public static class IpDataBean {
        private String area;
        private String city;
        private String city_id;
        private String country;
        private String country_id;//CN
        private String ip;
        private String isp;
        private String long_ip;
        private String region;
        private String region_id;

        public String getArea() {
            return area;
        }

        public void setArea(String area) {
            this.area = area;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getCity_id() {
            return city_id;
        }

        public void setCity_id(String city_id) {
            this.city_id = city_id;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getCountry_id() {
            return country_id;
        }

        public void setCountry_id(String country_id) {
            this.country_id = country_id;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public String getIsp() {
            return isp;
        }

        public void setIsp(String isp) {
            this.isp = isp;
        }

        public String getLong_ip() {
            return long_ip;
        }

        public void setLong_ip(String long_ip) {
            this.long_ip = long_ip;
        }

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        public String getRegion_id() {
            return region_id;
        }

        public void setRegion_id(String region_id) {
            this.region_id = region_id;
        }
    }
}

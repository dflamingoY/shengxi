package org.xiaoxingqi.shengxi.model;

public class CustomerDeviceData extends BaseRepData {

    private DeviceBean data;

    public DeviceBean getData() {
        return data;
    }

    public void setData(DeviceBean data) {
        this.data = data;
    }

    public static class DeviceBean {
        private String platform_id;
        private String app_version;
        private String device_info;
        private String personality_no;
        private String gender;//性别，1:男，2:女，0表示没有进行性格测试

        public String getPlatform_id() {
            return platform_id;
        }

        public void setPlatform_id(String platform_id) {
            this.platform_id = platform_id;
        }

        public String getApp_version() {
            return app_version;
        }

        public void setApp_version(String app_version) {
            this.app_version = app_version;
        }

        public String getDevice_info() {
            return device_info;
        }

        public void setDevice_info(String device_info) {
            this.device_info = device_info;
        }

        public String getPersonality_no() {
            return personality_no;
        }

        public void setPersonality_no(String personality_no) {
            this.personality_no = personality_no;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }
    }
}

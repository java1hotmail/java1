package com.att.cso.reports.data.models;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * The persistent class for the Device database table.
 *
 */
@Entity
public class Device implements Serializable {

    @Override
    public String toString() {

        return "Device [deviceId=" + deviceId + ", badgeCount=" + badgeCount
            + ", created=" + created + ", deviceName="
            + deviceName + ", token={..}" + ", Uuid={..}" + ", manufacturer="
            + manufacturer + ", model=" + model + ", modified=" + modified
            + ", os=" + os + ", osVersion=" + osVersion
            + ", appVersion=" + appVersion + ", user=" + user + ", deviceType="
            + deviceType + "]";
    }

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "device_id")
    private Long deviceId;

    @Column(name = "badge_count")
    private Integer badgeCount;

    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Column(name = "device_name")
    private String deviceName;

    @Lob
    @Column(name = "device_token")
    private String deviceToken;

    @Column(name = "device_uuid")
    private String deviceUuid;

    private String manufacturer;

    private String model;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false)
    private Date modified;

    private String os;

    private String osVersion;

    // bi-directional many-to-one association to App_Version
    @ManyToOne
    @JoinColumn(name = "app_version_id")
    private AppVersion appVersion;

    // bi-directional many-to-one association to User
    @ManyToOne
    @JoinColumn(name = "user")
    private User user;

    // uni-directional many-to-one association to Device_Type
    @ManyToOne
    @JoinColumn(name = "device_type")
    private DeviceType deviceType;

    public Device() {

    }

    public Long getDeviceId() {

        return this.deviceId;
    }

    public void setDeviceId(Long deviceId) {

        this.deviceId = deviceId;
    }

    public Integer getBadgeCount() {

        return this.badgeCount;
    }

    public void setBadgeCount(Integer badgeCount) {

        this.badgeCount = badgeCount;
    }

    public Date getCreated() {

        return this.created;
    }

    public void setCreated(Date created) {

        this.created = created;
    }

    public String getDeviceName() {

        return this.deviceName;
    }

    public void setDeviceName(String deviceName) {

        this.deviceName = deviceName;
    }

    public String getDeviceToken() {

        return this.deviceToken;
    }

    public void setDeviceToken(String deviceToken) {

        this.deviceToken = deviceToken;
    }

    public String getDeviceUuid() {

        return this.deviceUuid;
    }

    public void setDeviceUuid(String deviceUuid) {

        this.deviceUuid = deviceUuid;
    }

    public String getManufacturer() {

        return this.manufacturer;
    }

    public void setManufacturer(String manufacturer) {

        this.manufacturer = manufacturer;
    }

    public String getModel() {

        return this.model;
    }

    public void setModel(String model) {

        this.model = model;
    }

    public Date getModified() {

        return this.modified;
    }

    public void setModified(Date modfied) {

        this.modified = modfied;
    }

    public String getOs() {

        return this.os;
    }

    public void setOs(String os) {

        this.os = os;
    }

    public String getOsVersion() {

        return this.osVersion;
    }

    public void setOsVersion(String osVersion) {

        this.osVersion = osVersion;
    }

    public AppVersion getAppVersion() {

        return this.appVersion;
    }

    public void setAppVersion(AppVersion appVersion) {

        this.appVersion = appVersion;
    }

    public User getUser() {

        return this.user;
    }

    public void setUser(User user) {

        this.user = user;
    }

    public DeviceType getDeviceType() {

        return this.deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {

        this.deviceType = deviceType;
    }

}

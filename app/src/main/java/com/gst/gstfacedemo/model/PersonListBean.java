package com.gst.gstfacedemo.model;

/**
 * Created by zhenghangxia on 17-4-21.
 */

public class PersonListBean {

    private String personId;
    private String name;
    private String userData;
    //private String persistedFaceIds;


    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserData() {
        return userData;
    }

    public void setUserData(String userData) {
        this.userData = userData;
    }
}

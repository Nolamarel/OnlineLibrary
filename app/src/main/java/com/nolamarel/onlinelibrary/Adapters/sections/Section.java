package com.nolamarel.onlinelibrary.Adapters.sections;

public class Section {
    public String sectionName, sectionId, sectionIv;

    public Section(String sectionName, String sectionIv, String sectionId) {
        this.sectionId = sectionId;
        this.sectionName = sectionName;
        this.sectionIv = sectionIv;
    }

    public void setSectionIv(String sectionIv) {
        this.sectionIv = sectionIv;
    }

    public String getSectionId() {
        return sectionId;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }
}

package com.api.Autonova.models.site;


import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;

@Entity
@Table(name = "pages_description")
public class PageDescription {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @JsonProperty("page_id")
    @Column(name = "page_id")
    private int pageId;

    @Column(name = "language")
    private String language;

    @Column(name = "meta_title")
    private String meta_title;

    @Column(name = "meta_keyword")
    private String meta_keyword;

    @Column(name = "meta_description")
    private String meta_description;

    @Column(name = "title")
    private String title;

    @Column(name = "title_banner")
    private String title_banner;

    @Column(name = "description")
    private String description;

    @Column(name = "block_1_title")
    private String block_1_title;

    @Column(name = "block_1_description")
    private String block_1_description;

    @Column(name = "block_2_title")
    private String block_2_title;

    @Column(name = "block_2_1_title")
    private String block_2_1_title;

    @Column(name = "block_2_1_description")
    private String block_2_1_description;

    @Column(name = "block_2_2_title")
    private String block_2_2_title;

    @Column(name = "block_2_2_description")
    private String block_2_2_description;

    @Column(name = "block_2_3_title")
    private String block_2_3_title;

    @Column(name = "block_2_3_description")
    private String block_2_3_description;

    @Column(name = "block_3_title")
    private String block_3_title;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPageId() {
        return pageId;
    }

    public void setPageId(int pageId) {
        this.pageId = pageId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getMeta_title() {
        return meta_title;
    }

    public void setMeta_title(String meta_title) {
        this.meta_title = meta_title;
    }

    public String getMeta_keyword() {
        return meta_keyword;
    }

    public void setMeta_keyword(String meta_keyword) {
        this.meta_keyword = meta_keyword;
    }

    public String getMeta_description() {
        return meta_description;
    }

    public void setMeta_description(String meta_description) {
        this.meta_description = meta_description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBlock_2_1_title() {
        return block_2_1_title;
    }

    public void setBlock_2_1_title(String block_2_1_title) {
        this.block_2_1_title = block_2_1_title;
    }

    public String getBlock_2_1_description() {
        return block_2_1_description;
    }

    public void setBlock_2_1_description(String block_2_1_description) {
        this.block_2_1_description = block_2_1_description;
    }

    public String getBlock_2_2_title() {
        return block_2_2_title;
    }

    public void setBlock_2_2_title(String block_2_2_title) {
        this.block_2_2_title = block_2_2_title;
    }

    public String getBlock_2_2_description() {
        return block_2_2_description;
    }

    public void setBlock_2_2_description(String block_2_2_description) {
        this.block_2_2_description = block_2_2_description;
    }

    public String getBlock_2_3_title() {
        return block_2_3_title;
    }

    public void setBlock_2_3_title(String block_2_3_title) {
        this.block_2_3_title = block_2_3_title;
    }

    public String getBlock_2_3_description() {
        return block_2_3_description;
    }

    public void setBlock_2_3_description(String block_2_3_description) {
        this.block_2_3_description = block_2_3_description;
    }

    public String getBlock_3_title() {
        return block_3_title;
    }

    public void setBlock_3_title(String block_3_title) {
        this.block_3_title = block_3_title;
    }

    public String getBlock_1_title() {
        return block_1_title;
    }

    public void setBlock_1_title(String block_1_title) {
        this.block_1_title = block_1_title;
    }

    public String getBlock_1_description() {
        return block_1_description;
    }

    public void setBlock_1_description(String block_1_description) {
        this.block_1_description = block_1_description;
    }

    public String getBlock_2_title() {
        return block_2_title;
    }

    public void setBlock_2_title(String block_2_title) {
        this.block_2_title = block_2_title;
    }

    public String getTitle_banner() {
        return title_banner;
    }

    public void setTitle_banner(String title_banner) {
        this.title_banner = title_banner;
    }
}

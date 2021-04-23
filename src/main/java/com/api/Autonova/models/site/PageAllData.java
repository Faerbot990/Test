package com.api.Autonova.models.site;

import java.util.List;


public class PageAllData {


    private int id;
    private String seo_url;
    private boolean show_in_header;
    private String template;
    private String image;
    private boolean status;
    private String ico_block_2_1;
    private String ico_block_2_2;
    private String ico_block_2_3;

    private String meta_title;
    private String meta_keyword;
    private String meta_description;
    private String title;
    private String title_banner;
    private String description;
    private String block_1_title;
    private String block_1_description;
    private String block_2_title;
    private String block_2_1_title;
    private String block_2_1_description;
    private String block_2_2_title;
    private String block_2_2_description;
    private String block_2_3_title;
    private String block_2_3_description;
    private String block_3_title;

    private List<PageImage> images = null;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSeo_url() {
        return seo_url;
    }

    public void setSeo_url(String seo_url) {
        this.seo_url = seo_url;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getIco_block_2_1() {
        return ico_block_2_1;
    }

    public void setIco_block_2_1(String ico_block_2_1) {
        this.ico_block_2_1 = ico_block_2_1;
    }

    public String getIco_block_2_2() {
        return ico_block_2_2;
    }

    public void setIco_block_2_2(String ico_block_2_2) {
        this.ico_block_2_2 = ico_block_2_2;
    }

    public String getIco_block_2_3() {
        return ico_block_2_3;
    }

    public void setIco_block_2_3(String ico_block_2_3) {
        this.ico_block_2_3 = ico_block_2_3;
    }

    public List<PageImage> getImages() {
        return images;
    }

    public void setImages(List<PageImage> images) {
        this.images = images;
    }


    public boolean isShow_in_header() {
        return show_in_header;
    }

    public void setShow_in_header(boolean show_in_header) {
        this.show_in_header = show_in_header;
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

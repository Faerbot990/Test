package com.api.Autonova.models.site;


import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "pages")
public class Page {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(name = "seo_url")
    private String seo_url;

    @JsonProperty("show_in_header")
    @Column(name = "show_in_header")
    private boolean showInHeader;

    @Column(name = "template")
    private String template;

    @Column(name = "image")
    private String image;

    @Column(name = "status")
    private boolean status;

    @Column(name = "ico_block_2_1")
    private String ico_block_2_1;

    @Column(name = "ico_block_2_2")
    private String ico_block_2_2;

    @Column(name = "ico_block_2_3")
    private String ico_block_2_3;

    @Transient
    private List<PageImage> images = null;

    @Transient
    private Descriptions descriptions = null;


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

    public Descriptions getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(Descriptions descriptions) {
        this.descriptions = descriptions;
    }

    public boolean isShowInHeader() {
        return showInHeader;
    }

    public void setShowInHeader(boolean showInHeader) {
        this.showInHeader = showInHeader;
    }

    public static class Descriptions {

        private PageDescription ua = null;

        private PageDescription ru = null;

        public PageDescription getUa() {
            return ua;
        }

        public void setUa(PageDescription ua) {
            this.ua = ua;
        }

        public PageDescription getRu() {
            return ru;
        }

        public void setRu(PageDescription ru) {
            this.ru = ru;
        }
    }

}

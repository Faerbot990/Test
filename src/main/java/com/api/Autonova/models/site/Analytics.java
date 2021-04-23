package com.api.Autonova.models.site;


import javax.persistence.*;

@Entity
@Table(name = "analytics")
public class Analytics {

    public Analytics(int id, String domain, int partner_id, String product_link, String date_added, long total){
        this.id = id;
        this.domain = domain;
        this.partner_id = partner_id;
        this.product_link = product_link;
        this.date_added = date_added;
        this.total = total;
    }

    public Analytics(){}


    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(name = "domain")
    private String domain;

    @Column(name = "partner_id")
    private int partner_id;

    @Column(name = "product_link")
    private String product_link;

    @Column(name = "date_added")
    private String date_added;

    private long total;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public int getPartner_id() {
        return partner_id;
    }

    public void setPartner_id(int partner_id) {
        this.partner_id = partner_id;
    }

    public String getProduct_link() {
        return product_link;
    }

    public void setProduct_link(String product_link) {
        this.product_link = product_link;
    }

    public String getDate_added() {
        return date_added;
    }

    public void setDate_added(String date_added) {
        this.date_added = date_added;
    }


    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}

package com.api.Autonova.repository;


import com.api.Autonova.models.site.Analytics;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnalyticsRepository extends CrudRepository<Analytics, Integer> {

    @Query("select "
            + " new com.api.Autonova.models.site.Analytics(p.id, p.domain, p.partner_id, p.product_link, p.date_added, COUNT(p)) "
            + "from Analytics p "
            + "group by p.product_link, p.partner_id, p.date_added")
    List<Analytics> getAnalytics();

}



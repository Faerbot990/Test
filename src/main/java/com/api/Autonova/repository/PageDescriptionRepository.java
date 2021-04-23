package com.api.Autonova.repository;

import com.api.Autonova.models.site.PageDescription;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface PageDescriptionRepository extends CrudRepository<PageDescription, Integer> {

    //PageDescription findPageDescriptionByPageIdAndLanguage(int page_id, String language);
    PageDescription findFirstByPageIdAndLanguage(int pageId, String language);

    @Transactional
    void deleteAllByPageId(int pageId);
}

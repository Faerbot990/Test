package com.api.Autonova.repository;

import com.api.Autonova.models.site.PageImage;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface PageImageRepository extends CrudRepository<PageImage, Integer> {

    List<PageImage> findAllByPageId(int id);

    @Transactional
    void deleteAllByPageId(int pageId);
}

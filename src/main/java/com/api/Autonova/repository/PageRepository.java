package com.api.Autonova.repository;

import com.api.Autonova.models.site.Page;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface PageRepository extends CrudRepository<Page, Integer> {

    Page findPageById(int id);


    Page findPageByIdAndStatus(int id, boolean status);

    List<Page> findAllByShowInHeaderAndStatus(boolean showInHeader, boolean status);

    @Transactional
    void deleteById(int pageId);
}

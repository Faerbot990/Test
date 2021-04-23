package com.api.Autonova.repository;

import com.api.Autonova.models.FilterLink;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


@Repository
public interface FilterLinkRepository extends CrudRepository<FilterLink, Integer> {


    FilterLink findById(int id);
    FilterLink findByLink(String link);

    @Transactional
    void deleteById(int id);
}

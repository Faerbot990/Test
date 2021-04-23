package com.api.Autonova.repository;


import com.api.Autonova.models.AttributesPattern;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface AttributesPatternsRepository extends CrudRepository<AttributesPattern, Integer> {

    AttributesPattern findAttributesPatternByName(String name);


    List<AttributesPattern> findAllByFilter(boolean filter);

    List<AttributesPattern> findByOrderByPosition();

    AttributesPattern findByName(String name);

    @Transactional
    void deleteById(int id);



}

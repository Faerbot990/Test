package com.api.Autonova.repository;


import com.api.Autonova.models.ExternalAccess;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccessRepository extends CrudRepository<ExternalAccess, Integer> {

    ExternalAccess findByName(String name);

}

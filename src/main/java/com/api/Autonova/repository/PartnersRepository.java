package com.api.Autonova.repository;


import com.api.Autonova.models.site.Partner;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface PartnersRepository extends CrudRepository<Partner, Integer> {

    Partner findById(int id);

    Partner findFirstByPartnerToken(String token);

    Partner findPartnerByDomain(String domain);

    @Transactional
    void deleteById(int id);
}

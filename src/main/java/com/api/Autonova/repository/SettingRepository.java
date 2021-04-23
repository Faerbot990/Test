package com.api.Autonova.repository;


import com.api.Autonova.models.site.SettingModel;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface SettingRepository extends CrudRepository<SettingModel, Integer> {

    SettingModel findSettingModelByName(String s);

    @Transactional
    void deleteAll();
}

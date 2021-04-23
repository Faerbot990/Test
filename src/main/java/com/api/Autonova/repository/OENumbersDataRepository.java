package com.api.Autonova.repository;


import com.api.Autonova.models.site.OENumberData;
import com.api.Autonova.models.site.SettingModel;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface OENumbersDataRepository extends CrudRepository<OENumberData, Integer> {

    List<OENumberData> findAllByParentId(int parentId);

    @Transactional
    void deleteById(int id);

    @Transactional
    void deleteAllByParentCode(String parent);
}

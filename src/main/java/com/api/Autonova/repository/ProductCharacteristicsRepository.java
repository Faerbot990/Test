package com.api.Autonova.repository;


import com.api.Autonova.models.Product;
import com.api.Autonova.models.ProductCharacteristic;
import com.api.Autonova.models.site.ProductAttributeFilterGetting;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ProductCharacteristicsRepository extends CrudRepository<ProductCharacteristic, Integer> {

    List<ProductCharacteristic> findAllByProductCode(String code);

    ProductCharacteristic findByProductCodeAndId(String code, int id);


    @Query("select DISTINCT "
            + " charact.attrShortName "
            + "from ProductCharacteristic charact ")
    List<String> getUniqueCharacteristicsByAttrShortName();

    @Query("select "
            + " charact.attrShortNameUa "
            + "from ProductCharacteristic charact "
            + "group by charact.attrShortNameUa")
    List<String> getUniqueCharacteristicsByAttrShortNameUA();

    @Query("select "
            + " new com.api.Autonova.models.ProductCharacteristic(charact.localid, charact.id, charact.attrShortName, charact.attrShortNameUa, charact.attrName, charact.attrNameUa, charact.attrUnit) "
            + "from ProductCharacteristic charact "
            + "group by charact.attrShortName "
            + "order by charact.attrShortName ")
    List<ProductCharacteristic> getUniqueCharacteristicsModelByAttrShortName(Pageable pageable);

/*
    @Query("select "
            + " new com.api.Autonova.models.ProductCharacteristic(charact.localid, charact.id, charact.attrShortName, charact.attrShortNameUa, charact.attrName, charact.attrNameUa, charact.attrUnit) "
            + "from ProductCharacteristic charact "
            + "group by charact.attrShortNameUa")
    List<ProductCharacteristic> getUniqueCharacteristicsByAttrShortNameUA();*/

    @Query("SELECT DISTINCT charact.attrValue  FROM ProductCharacteristic charact WHERE (charact.attrShortName = :name) AND charact.productCode in :codes ORDER BY charact.attrValue")
    List<String> findDistinctCharactValues(@Param("name") String name, @Param("codes") Iterable<String> codes);

    @Query("SELECT DISTINCT charact.attrValue  FROM ProductCharacteristic charact WHERE (charact.attrShortNameUa = :name) AND charact.productCode in :codes ORDER BY charact.attrValue")
    List<String> findDistinctCharactValuesUa(@Param("name") String name, @Param("codes") Iterable<String> codes);

    @Query("SELECT DISTINCT new com.api.Autonova.models.site.ProductAttributeFilterGetting(charact.attrShortName, charact.attrValue) " +
            "FROM ProductCharacteristic charact INNER JOIN charact.product prod " +
            "WHERE prod.id in :ids" )
    List<ProductAttributeFilterGetting> getCharacteristicVariantsInIds(@Param("ids") Iterable<Integer> ids);

    @Query("SELECT DISTINCT new com.api.Autonova.models.site.ProductAttributeFilterGetting(charact.attrShortNameUa, charact.attrValueUa) " +
            "FROM ProductCharacteristic charact INNER JOIN charact.product prod " +
            "WHERE prod.id in :ids" )
    List<ProductAttributeFilterGetting> getCharacteristicVariantsInIdsUa(@Param("ids") Iterable<Integer> ids);

    @Transactional
    void deleteByProductCodeAndId(String code, int id);

    @Transactional
    void deleteAllByProductCode(String code);

    @Transactional
    void deleteAllByProduct(Product product);

}

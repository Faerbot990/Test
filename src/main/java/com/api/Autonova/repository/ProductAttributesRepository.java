package com.api.Autonova.repository;


import com.api.Autonova.models.Product;
import com.api.Autonova.models.ProductAttribute;
import com.api.Autonova.models.site.ProductAttributeFilterGetting;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ProductAttributesRepository extends CrudRepository<ProductAttribute, Integer> {

    ProductAttribute findProductAttributeByNameAndProductCode(String name, String code);
    List<ProductAttribute> findAllByProductCode(String code);
    List<ProductAttribute> findAllByProductCodeOrderById(String code);

    List<ProductAttribute>  findAllByName(String name);

    ProductAttribute  findFirstByProductCodeAndName(String code, String name);

    ProductAttribute findFirstByNameAndValue(String name, String value);

    @Query("SELECT DISTINCT attr.value  FROM ProductAttribute attr WHERE (attr.name = :name) AND attr.productCode in :codes ORDER BY attr.value")
    List<String> findDistinctAttrValues(@Param("name") String name, @Param("codes") Iterable<String> codes);

    @Query("SELECT DISTINCT attr.value  FROM ProductAttribute attr INNER JOIN attr.product prod WHERE (attr.name = :name) AND prod.id in :ids ORDER BY attr.value")
    List<String> findDistinctAttrValuesByIds(@Param("name") String name, @Param("ids") Iterable<Integer> ids);

    @Query("SELECT DISTINCT attr.value  FROM ProductAttribute attr WHERE (attr.name = :name) ORDER BY attr.value ASC ")
    List<String> findDistinctAttrValuesInAll(@Param("name") String name);


    @Query("SELECT DISTINCT new com.api.Autonova.models.site.ProductAttributeFilterGetting(attr.name, attr.value) " +
            "FROM ProductAttribute attr INNER JOIN attr.product prod " +
            "WHERE  attr.name in :names AND prod.id in :ids" )
    List<ProductAttributeFilterGetting> getAttrsVariantsByNameAndIds(@Param("names") Iterable<String> names, @Param("ids") Iterable<Integer> ids);

    @Query("SELECT attr " +
            "FROM ProductAttribute attr " +
            "WHERE  attr.productCode in :codes AND (attr.name = :nameParam1 OR attr.name = :nameParam2)" )
    List<ProductAttribute>  findAttrsForAnalog(@Param("codes") Iterable<String> codes, @Param("nameParam1") String nameParam1,
                                               @Param("nameParam2") String nameParam2);


    @Transactional
    void deleteAllByProductCode(String code);

    @Transactional
    void deleteAllByProduct(Product product);

    //@Query("SELECT attr FROM ProductAttribute attr WHERE (attr.name = :name) AND attr.productCode in :codes")
    //List<ProductAttribute> findAttrsByNameInCodes(@Param("name") String name, @Param("codes") Iterable<String> codes);

    @Query("SELECT attr FROM ProductAttribute attr INNER JOIN attr.product prod WHERE prod.id in :ids")
    List<ProductAttribute> findAllInIds(@Param("ids") Iterable<Integer> ids);

    @Query("SELECT attr FROM ProductAttribute attr INNER JOIN attr.product prod WHERE attr.name in :names AND prod.id in :ids ")
    List<ProductAttribute> getAttrsInIdsAndNames(@Param("ids") Iterable<Integer> ids, @Param("names") Iterable<String> names);

    @Query("SELECT attr FROM ProductAttribute attr INNER JOIN attr.product prod WHERE attr.name = :name AND prod.id in :ids ")
    List<ProductAttribute> getAttrsInIdsAndName(@Param("ids") Iterable<Integer> ids, @Param("name") String name);


    @Query("SELECT count(attr) FROM ProductAttribute attr  WHERE attr.name = :name ")
    Long getPriceCount(@Param("name") String name);
}

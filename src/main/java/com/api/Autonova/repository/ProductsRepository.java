package com.api.Autonova.repository;


import com.api.Autonova.models.Car;
import com.api.Autonova.models.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ProductsRepository extends CrudRepository<Product, Integer> {

    @Query("SELECT product FROM Product product WHERE product.code in :codes")
    List<Product> findAllInCodes(@Param("codes") Iterable<String> codes);

    Product findProductByCode(String code);

    @Query("select p from Product p INNER JOIN p.attributes attr where (attr.name = :param1 AND attr.value LIKE :value) OR (attr.name = :param2 AND attr.value LIKE :value)")
    public List<Product> findProductsByNameOrCode(@Param("param1") String param1, @Param("param2") String param2, @Param("value") String value, Pageable pageable);

    @Transactional
    void deleteByCode(String code);


    @Query("SELECT car FROM Product p LEFT JOIN p.cars car WHERE p.id = :id AND car.manuName LIKE %:manu%")
    List<Car> findAllCarsByProductAndManu(@Param("id") int id, @Param("manu") String manu);

    @Query("SELECT p.id FROM Product p WHERE p.code in :codes")
    List<Integer> findAllProductsIdByCodes(@Param("codes") Iterable<String> codes);

    @Query("SELECT COUNT(p) FROM Product p ")
    long getAllProductsCount();

    @Query("SELECT new Product(p.id, p.code) FROM Product p")
    List<Product> getAllIdsAndCodes(Pageable pageable);


}

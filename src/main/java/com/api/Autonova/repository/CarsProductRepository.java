package com.api.Autonova.repository;


import com.api.Autonova.models.CarProduct;
import com.api.Autonova.models.Product;
import com.api.Autonova.models.site.ProductAttributeFilterGetting;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Repository
public interface CarsProductRepository extends CrudRepository<CarProduct, Integer> {

    @Query("SELECT DISTINCT car.carId " +
            "FROM CarProduct car " +
            "WHERE  car.productId in :ids" )
    List<Integer> getCarsByIds(@Param("ids") Iterable<Integer> ids);


    List<CarProduct> findAllByProductId(int id);


    @Transactional
    void deleteAllByProductId(int product);

    @Transactional
    void deleteById(int id);
}

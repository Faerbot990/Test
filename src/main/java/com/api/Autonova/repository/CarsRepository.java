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
public interface CarsRepository extends CrudRepository<Car, Integer> {

    List<Car> findAllByCarIdLike(int carId, Pageable pageable);
    List<Car> findAllByModelNameLike(String model, Pageable pageable);
    List<Car> findAllByManuNameLike(String manu, Pageable pageable);


    @Query("SELECT car FROM Car car WHERE car.carId in :ids")
    List<Car> findAllForProduct(@Param("ids") Iterable<Integer> ids, Pageable pageable);

    @Query("SELECT new com.api.Autonova.models.Car(car.manuName, car.modelName, car.yearOfConstrFrom, car.yearOfConstrTo, car.typeName) FROM Car car WHERE car.id in :ids ")
    List<Car> findCarsDataByIds(@Param("ids") Iterable<Integer> ids);


    @Query("SELECT DISTINCT car.manuName FROM Car car WHERE car.carId in :ids ORDER BY car.manuName")
    List<String> findDistinctManuNamesByIds(@Param("ids") Iterable<Integer> ids);

    @Query("SELECT DISTINCT car.modelName FROM Car car WHERE car.carId in :ids ORDER BY car.modelName")
    List<String> findDistinctModelNamesByIds(@Param("ids") Iterable<Integer> ids);

    @Query("SELECT DISTINCT car.typeName FROM Car car WHERE car.carId in :ids ORDER BY car.typeName")
    List<String> findDistinctTypeNamesByIds(@Param("ids") Iterable<Integer> ids);



    //it is 1C Api id
    Car findCarByCarId(int carId);



    @Query("SELECT DISTINCT car.manuName FROM Car car ORDER BY car.manuName")
    List<String> findDistinctManuNames();



    @Query("SELECT car.id FROM Car car WHERE car.carId in :ids ")
    List<Integer> findInnerCarIdsBy1CIds(@Param("ids") Iterable<Integer> ids);


    @Query("SELECT DISTINCT car.manuName from Car car JOIN car.products p WHERE p.code = :code ORDER BY car.manuName")
    List<String> getDistinctManuNamesByProduct(@Param("code") String code);
}

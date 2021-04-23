package com.api.Autonova.components.cache;


import com.api.Autonova.models.site.ProductAllData;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Component;

@Component
public class CacheUpdateComponent {

    /*@CachePut(value = "product", key = "#code")
    public void updateProductByCode(String code){
        //return getProductData("ua", code);
    }*/


    @Caching(evict = {
            @CacheEvict(value="products", allEntries=true),
            @CacheEvict(value="attributes", allEntries=true),
            @CacheEvict(value="characteristics", allEntries=true) })
    public void clearProductsDataCache(){}

    @CacheEvict(value="cars", allEntries=true)
    public void clearCarsDataCache(){}


}

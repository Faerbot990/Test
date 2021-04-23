package com.api.Autonova.dao;

import com.api.Autonova.models.Car;
import com.api.Autonova.models.Product;
import com.api.Autonova.models.ProductAttribute;
import com.api.Autonova.models.ProductCharacteristic;
import com.api.Autonova.models.site.ProductAttributeFilter;
import com.api.Autonova.models.site.ProductFilterModel;
import com.api.Autonova.utils.Constants;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ProductsAdminDao {

    @PersistenceContext
    private EntityManager em;



    public List<Product> findProductsByFilter(String language,
            String code, String name, String category, String manufacturer, String amount, String priceFrom, String priceTo,
                                              String sortVector, String sortAttrName,
                                              int offset, int limit) {

        List<Predicate> predicatesAllAttributes = new ArrayList<>();

        //EntityManager em = entityManagerFactory.createEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Product> cq = cb.createQuery(Product.class);
        Root<Product> product = cq.from(Product.class);
        cq.select(product);


        Predicate predicateResultAttr = makePredicateByFilter(cb, product,
                            predicatesAllAttributes,
                            language, code, name, category, manufacturer, amount, priceFrom, priceTo);

        //set filters count - количество, которое выражает все варианты продуктов которые мы находим, подходих вариантов продуктов должно быть равное произведению всех фильтров
        int filterCount = Math.max(predicatesAllAttributes.size() - 1, 0); //- 1 для условия "больше равно"


        boolean useFilters = true;
        if (predicatesAllAttributes.size() > 0 && predicateResultAttr != null) {
            useFilters = true;
        } else {
            useFilters = false;
        }


        //sort
        //get sort attr
        Join<Product, ProductAttribute> attributeSort = product.join("attributes", JoinType.LEFT);
        attributeSort.on(
                cb.equal(attributeSort.get("name"), sortAttrName)
        );
        //set value type
        Expression sortType;
        if (sortAttrName.equals(Constants.PRODUCT_ATTR_PRICE_FROM) || sortAttrName.equals(Constants.PRODUCT_ATTR_PRICE_TO)
                || sortAttrName.equals(Constants.PRODUCT_ATTR_AMOUNT)) {
            sortType = attributeSort.get("value").as(Integer.class);
        } else {
            sortType = attributeSort.get("value").as(String.class);
        }
        //set vector
        Order orderSort;
        if (sortVector.equals(Constants.SORT_VECTOR_DESC)) {
            orderSort = cb.desc(sortType);
        } else {
            orderSort = cb.asc(sortType);
        }


        if (useFilters) {
            cq.where(predicateResultAttr)
                    .groupBy(product.get("id"))
                    .having(cb.gt(cb.count(product.get("id")), filterCount))
                    .orderBy(orderSort);
            // .orderBy(cb.asc(product.get("id")));
        } else {
            cq
                    .groupBy(product.get("id"))
                    .orderBy(orderSort);
        }

        TypedQuery<Product> query = em.createQuery(cq).setFirstResult(offset).setMaxResults(limit);
        return query.getResultList();
    }


    public List<Product> findProductsIdsByFilter(String language,
                                                   String code, String name, String category, String manufacturer, String amount, String priceFrom, String priceTo) {

        List<Predicate> predicatesAllAttributes = new ArrayList<>();

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Product> cq = cb.createQuery(Product.class);
        Root<Product> product = cq.from(Product.class);
        cq.select(product.get("id"));

        Predicate predicateResultAttr = makePredicateByFilter(cb, product,
                predicatesAllAttributes,
                language, code, name, category, manufacturer, amount, priceFrom, priceTo);

        //set filters count - количество, которое выражает все варианты продуктов которые мы находим, подходих вариантов продуктов должно быть равное произведению всех фильтров
        int filterCount = Math.max(predicatesAllAttributes.size() - 1, 0); //- 1 для условия "больше равно"

        boolean useFilters = true;
        if (predicatesAllAttributes.size() > 0 && predicateResultAttr != null) {
            useFilters = true;
        } else {
            useFilters = false;
        }

        if (useFilters) {
            cq.where(predicateResultAttr)
                    .groupBy(product.get("id"))
                    .having(cb.gt(cb.count(product.get("id")), filterCount));
        } else {
            cq
                    .groupBy(product.get("id"));
        }

        TypedQuery<Product> query = em.createQuery(cq);
        return query.getResultList();
    }


    public List<String> findProductsCodesByFilter(String language,
                                                      String code, String name, String category, String manufacturer, String amount, String priceFrom, String priceTo) {

        List<Predicate> predicatesAllAttributes = new ArrayList<>();

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<String> cq = cb.createQuery(String.class);
        Root<Product> product = cq.from(Product.class);
        cq.select(product.get("code"));

        Predicate predicateResultAttr = makePredicateByFilter(cb, product,
                predicatesAllAttributes,
                language, code, name, category, manufacturer, amount, priceFrom, priceTo);

        //set filters count - количество, которое выражает все варианты продуктов которые мы находим, подходих вариантов продуктов должно быть равное произведению всех фильтров
        int filterCount = Math.max(predicatesAllAttributes.size() - 1, 0); //- 1 для условия "больше равно"


        boolean useFilters = true;
        if (predicatesAllAttributes.size() > 0 && predicateResultAttr != null) {
            useFilters = true;
        } else {
            useFilters = false;
        }

        if (useFilters) {
            cq.where(predicateResultAttr)
                    .groupBy(product.get("id"))
                    .having(cb.gt(cb.count(product.get("id")), filterCount));
        } else {
            cq
                    .groupBy(product.get("id"));
        }

        TypedQuery<String> query = em.createQuery(cq);
        return query.getResultList();
    }



    //private methods
    private Predicate makePredicateByFilter(CriteriaBuilder cb, Root<Product> product,
                                      List<Predicate> predicatesAllAttributes,
                                      String language, String code, String name, String category, String manufacturer, String amount, String priceFrom, String priceTo){

        //set JOINS
        Join<Product, ProductAttribute> attributes = null;
        if(code != null && code.trim().trim().length() > 0 || name != null && name.trim().trim().length() > 0 ||
                category != null && category.trim().trim().length() > 0 || manufacturer != null && manufacturer.trim().trim().length() > 0 ||
                amount != null && amount.trim().trim().length() > 0 || priceFrom != null && priceFrom.trim().trim().length() > 0 ||
                priceTo != null && priceTo.trim().trim().length() > 0){
            attributes = product.join("attributes", JoinType.LEFT);
        }


        //for price
        Double filterPriceFrom = null;
        Double filterPriceTo = null;

        //filters
        if(code != null && code.trim().length() > 0){
            predicatesAllAttributes.add(
                    cb.and(
                            cb.equal(attributes.get("name"), Constants.PRODUCT_ATTR_CODE),
                            cb.equal(attributes.get("value"), code.trim())
                    )
            );
        }
        if(name != null && name.trim().length() > 0){
            String attrName = Constants.PRODUCT_ATTR_NAME_UA;
            if(language.equals(Constants.LANGUAGE_RU)){
                attrName = Constants.PRODUCT_ATTR_NAME;
            }
            predicatesAllAttributes.add(
                    cb.and(
                            cb.equal(attributes.get("name"), attrName),
                            cb.like(attributes.get("value"), "%" + name.trim() + "%")
                    )
            );
        }
        if(category != null && category.trim().length() > 0){
            String attrName = Constants.PRODUCT_ATTR_CATEGORY_NAME_UA;
            if(language.equals(Constants.LANGUAGE_RU)){
                attrName = Constants.PRODUCT_ATTR_CATEGORY_NAME;
            }
            predicatesAllAttributes.add(
                    cb.and(
                            cb.equal(attributes.get("name"), attrName),
                            cb.like(attributes.get("value"), "%" + category.trim() + "%")
                    )
            );
        }
        if(manufacturer != null && manufacturer.trim().length() > 0){
            predicatesAllAttributes.add(
                    cb.and(
                            cb.equal(attributes.get("name"), Constants.PRODUCT_ATTR_MANUFACTURER_NAME),
                            cb.like(attributes.get("value"), "%" + manufacturer.trim() + "%")
                    )
            );
        }
        if(amount != null && amount.trim().length() > 0){
            if(amount.equals("true") || amount.equals("1")){
                predicatesAllAttributes.add(
                        cb.and(
                                cb.equal(attributes.get("name"), Constants.PRODUCT_ATTR_AMOUNT),
                                cb.greaterThan(attributes.get("value"), 0)
                        )
                );
            }else {
                predicatesAllAttributes.add(
                        cb.and(
                                cb.equal(attributes.get("name"), Constants.PRODUCT_ATTR_AMOUNT),
                                cb.equal(attributes.get("value"), "0")
                        )
                );
            }

        }
        if(priceFrom != null && priceFrom.trim().length() > 0){
            try {
                filterPriceFrom =  Double.parseDouble(priceFrom);

            }catch (NumberFormatException | NullPointerException e){}
        }
        if(priceTo != null && priceTo.trim().length() > 0){
            try {
                filterPriceTo =  Double.parseDouble(priceTo);
            }catch (NumberFormatException | NullPointerException e){}
        }

        //эта часть запускается ПОСЛЕ прохождения обоих атрибутов цены, на моменте когда отработали второй
        //для работы должны передаться оба фильтра от и до + цены товаров должны быть рабочие
        //НО если передать один из фильтров условия будут строиться по другому
        //PRICE FILTER
        if (filterPriceFrom != null && filterPriceTo != null) {
            predicatesAllAttributes.add(
                    cb.or(
                            cb.and(
                                    cb.equal(attributes.get("name"), Constants.PRODUCT_ATTR_PRICE_FROM),
                                    cb.between(attributes.get("value"), filterPriceFrom, filterPriceTo)
                            ),
                            cb.and(
                                    cb.equal(attributes.get("name"), Constants.PRODUCT_ATTR_PRICE_FROM),
                                    cb.lessThan(attributes.get("value"), filterPriceFrom)
                            )
                    )
            );
            predicatesAllAttributes.add(
                    cb.or(
                            cb.and(
                                    cb.equal(attributes.get("name"), Constants.PRODUCT_ATTR_PRICE_TO),
                                    cb.between(attributes.get("value"), filterPriceFrom, filterPriceTo)
                            ),
                            cb.and(
                                    cb.equal(attributes.get("name"), Constants.PRODUCT_ATTR_PRICE_TO),
                                    cb.greaterThan(attributes.get("value"), filterPriceTo)
                            )
                    )
            );
        }else if(filterPriceFrom != null){
            predicatesAllAttributes.add(
                    cb.and(
                            cb.equal(attributes.get("name"), Constants.PRODUCT_ATTR_PRICE_FROM),
                            cb.greaterThanOrEqualTo(attributes.get("value"), filterPriceFrom)
                    )
            );
        }else if(filterPriceTo != null){
            predicatesAllAttributes.add(
                    cb.and(
                            cb.equal(attributes.get("name"), Constants.PRODUCT_ATTR_PRICE_TO),
                            cb.lessThanOrEqualTo(attributes.get("value"), filterPriceTo)
                    )
            );
        }

       return cb.or(predicatesAllAttributes.toArray(new Predicate[0]));
    }

}

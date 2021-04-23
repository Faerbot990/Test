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
public class ProductsSiteDao {

    @PersistenceContext
    private EntityManager em;



    public List<Product> findProductsByFilter(String language, @NotNull ProductFilterModel productFilterModel, String queryNameCode,
                                               String sortVector, String sortAttrName,
                                               int offset, int limit) {

        List<Predicate> predicatesAllAttributes = new ArrayList<>();
        List<Predicate> predicatesAllCharacteristics = new ArrayList<>();
        List<Predicate> predicatesAllCars = new ArrayList<>();

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery cq = cb.createQuery();
        Root<Product> product = cq.from(Product.class);
        cq.select(product);

        Predicate predicateResult = makePredicateByFilter(cb, cq, product,
                            predicatesAllAttributes, predicatesAllCharacteristics, predicatesAllCars,
                            language, productFilterModel, queryNameCode, false);

        //списки предикатов формируются в методе makePredicateByFilter
        //set filters count - количество, которое выражает все варианты продуктов которые мы находим, подходих вариантов продуктов должно быть равное произведению всех фильтров
        int filterCount = Math.max(
                (Math.max(predicatesAllAttributes.size(), 1) * Math.max(predicatesAllCharacteristics.size(), 1)) - 1, 0); //- 1 для условия "больше равно"

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

        if(predicateResult != null){
            cq.where(predicateResult)
                    .groupBy(product.get("id"))
                    .having(cb.gt(cb.count(product.get("id")), filterCount))
                    .orderBy(orderSort);
        }else {
            cq
                    .groupBy(product.get("id"))
                    .orderBy(orderSort);
        }

        TypedQuery<Product> query = em.createQuery(cq).setFirstResult(offset).setMaxResults(limit);
        return query.getResultList();
    }


    public List<Integer> findAllIdsByFilter(String language, @NotNull ProductFilterModel productFilterModel, String queryNameCode, boolean priceIgnore) {

        List<Predicate> predicatesAllAttributes = new ArrayList<>();
        List<Predicate> predicatesAllCharacteristics = new ArrayList<>();
        List<Predicate> predicatesAllCars = new ArrayList<>();

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery cq = cb.createQuery();
        Root<Product> product = cq.from(Product.class);
        cq.select(product.get("id"));

        Predicate predicateResult = makePredicateByFilter(cb, cq, product,
                predicatesAllAttributes, predicatesAllCharacteristics, predicatesAllCars,
                language, productFilterModel, queryNameCode, priceIgnore);

        //set filters count - количество, которое выражает все варианты продуктов которые мы находим, подходих вариантов продуктов должно быть равное произведению всех фильтров
        int filterCount = Math.max(
                (Math.max(predicatesAllAttributes.size(), 1) * Math.max(predicatesAllCharacteristics.size(), 1)) - 1, 0); //- 1 для условия "больше равно"

        if (predicateResult != null) {
            cq.where(predicateResult)
                    .groupBy(product.get("id"))
                    .having(cb.gt(cb.count(product.get("id")), filterCount));
        } else {
            cq
                    .groupBy(product.get("id"));
        }

        TypedQuery<Integer> query = em.createQuery(cq);
        return query.getResultList();
    }



    public List<Product> findProductForSearch(String language, String attrName, String queryData, int offset, int limit){

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery cq = cb.createQuery();
        Root<Product> product = cq.from(Product.class);
        cq.select(product);

        Join<Product, ProductAttribute> attributes = product.join("attributes", JoinType.LEFT);


        Predicate predicateResult = cb.and(
                cb.equal(attributes.get("name"), attrName),
                cb.like(attributes.get("value"), queryData+"%")
        );



        Join<Product, ProductAttribute> attributeSort = product.join("attributes", JoinType.LEFT);
        if(language.equals(Constants.LANGUAGE_RU)){
            attributeSort.on(
                    cb.equal(attributeSort.get("name"), Constants.PRODUCT_ATTR_NAME)
            );
        }else {
            attributeSort.on(
                    cb.equal(attributeSort.get("name"), Constants.PRODUCT_ATTR_NAME_UA)
            );
        }
        Order orderSort = cb.asc(attributeSort.get("value").as(String.class));

        cq.where(predicateResult).orderBy(orderSort);

        TypedQuery<Product> query = em.createQuery(cq).setFirstResult(offset).setMaxResults(limit);
        return query.getResultList();
    }


    //private methods
    private Predicate makePredicateByFilter(CriteriaBuilder cb, CriteriaQuery cq, Root<Product> product,
                                      List<Predicate> predicatesAllAttributes, List<Predicate> predicatesAllCharacteristics, List<Predicate> predicatesAllCars,
                                      String language, @NotNull ProductFilterModel productFilterModel, String queryNameCode,
                                            boolean priceIgnore){

        //проверка фильтров - какие таблицы нужно подвязать
        //если изменить структуру и вынески фильтры авто отдельно эту проверку можно избежать
        boolean filterCars = false;
        boolean filterAttrs = false;
        if (productFilterModel != null && productFilterModel.getAttributes() != null && productFilterModel.getAttributes().size() > 0) {
            for (ProductAttributeFilter filterItem : productFilterModel.getAttributes()) {
                if (filterItem.getName().equals("model_name") || filterItem.getName().equals("manu_name") || filterItem.getName().equals("type_name") || filterItem.getName().equals("year")) {
                    filterCars = true;
                } else {
                    filterAttrs = true;
                }
            }
        }
        if(queryNameCode != null && queryNameCode.trim().length() > 0){
            filterAttrs = true;
        }
        //set JOINS
        Join<Product, ProductAttribute> attributes = null;
        if (filterAttrs) {
            attributes = product.join("attributes", JoinType.LEFT);
        }
        Subquery subqueryCarsCount = null;
        Root subCar = null;
        Join subProductsInCars = null;
        if (filterCars) {
            subqueryCarsCount = cq.subquery(Long.class);
            subCar = subqueryCarsCount.from(Car.class);
            subProductsInCars = subCar.join("products", JoinType.INNER);
            subqueryCarsCount.select(cb.count(subCar.get("id")));
        }

        //get criteries
        if (productFilterModel != null && productFilterModel.getAttributes() != null && productFilterModel.getAttributes().size() > 0) {
            //for price
            Double filterPriceFrom = null;
            Double filterPriceTo = null;
            for (ProductAttributeFilter filterItem : productFilterModel.getAttributes()) {
                //cars
                if (filterItem.getName().equals("model_name") || filterItem.getName().equals("manu_name") || filterItem.getName().equals("type_name") || filterItem.getName().equals("year")) {
                    //Join<Product, Car> cars = product.join("cars", JoinType.LEFT);
                    switch (filterItem.getName()) {
                        case "manu_name":
                            predicatesAllCars.add(subCar.get("manuName").in(filterItem.getValues()));
                            break;
                        case "model_name":
                            predicatesAllCars.add(subCar.get("modelName").in(filterItem.getValues()));
                            break;
                        case "type_name":
                            predicatesAllCars.add(subCar.get("typeName").in(filterItem.getValues()));
                            break;
                        case "year":
                            if (filterItem.getValues() != null && filterItem.getValues().size() > 0) {
                                //String yearFilter = filterItem.getValues().get(0).trim() + "-00-00";
                                try {
                                    int yearFilter = Integer.parseInt(filterItem.getValues().get(0).trim());
                                    Expression<Integer> yearOfConstrFrom = cb.function("DATE_FORMAT", Integer.class, subCar.get("yearOfConstrFrom"), cb.literal("%Y"));
                                    Expression<Integer> yearOfConstrTo = cb.function("DATE_FORMAT", Integer.class, subCar.get("yearOfConstrTo"), cb.literal("%Y"));
                                    predicatesAllCars.add(
                                            cb.and(
                                                    cb.lessThanOrEqualTo(yearOfConstrFrom, yearFilter),
                                                    cb.greaterThanOrEqualTo(yearOfConstrTo, yearFilter)
                                            )
                                    );
                                } catch (NumberFormatException | NullPointerException e) {}
                            }
                            break;
                    }
                } else {
                    //attrs
                    switch (filterItem.getName()) {
                        case Constants.PRODUCT_ATTR_AMOUNT:
                            if (filterItem.getValues() != null && filterItem.getValues().size() > 0) {
                                boolean amountTrue = false;
                                boolean amountFalse = false;
                                for (String filterValueItem : filterItem.getValues()) {
                                    if (filterValueItem.equals("true")) {
                                        amountTrue = true;
                                    } else if (filterValueItem.equals("false")) {
                                        amountFalse = true;
                                    }
                                }
                                //add predicate
                                Predicate predicateAmount;
                                if (amountTrue && amountFalse) {
                                    predicateAmount = cb.or(
                                            cb.and(
                                                    cb.equal(attributes.get("name"), filterItem.getName()),
                                                    cb.greaterThan(attributes.get("value"), 0)
                                            ),
                                            cb.and(
                                                    cb.equal(attributes.get("name"), filterItem.getName()),
                                                    cb.equal(attributes.get("value"), "0")
                                            )
                                    );
                                } else {
                                    if (amountTrue) {
                                        predicateAmount = cb.and(
                                                cb.equal(attributes.get("name"), filterItem.getName()),
                                                cb.greaterThan(attributes.get("value"), 0)
                                        );
                                    } else {
                                        predicateAmount = cb.and(
                                                cb.equal(attributes.get("name"), filterItem.getName()),
                                                cb.equal(attributes.get("value"), "0")
                                        );
                                    }
                                }
                                predicatesAllAttributes.add(predicateAmount);
                            }
                            break;

                        case Constants.PRODUCT_ATTR_PRICE_FROM:
                        case Constants.PRODUCT_ATTR_PRICE_TO:
                            if(!priceIgnore){
                                if (filterItem.getValues() != null) {
                                    if (filterItem.getName().equals(Constants.PRODUCT_ATTR_PRICE_FROM)) {
                                        for (String filterValueItem : filterItem.getValues()) {
                                            try {
                                                filterPriceFrom = Double.parseDouble(filterValueItem);
                                                break;
                                            } catch (NumberFormatException | NullPointerException e) {}
                                        }
                                    } else {  //PRODUCT_ATTR_PRICE_TO
                                        for (String filterValueItem : filterItem.getValues()) {
                                            try {
                                                filterPriceTo = Double.parseDouble(filterValueItem);
                                                break;
                                            } catch (NumberFormatException | NullPointerException e) {
                                            }
                                        }
                                    }
                                }
                                //эта часть запускается ПОСЛЕ прохождения обоих атрибутов цены, на моменте когда отработали второй
                                //для работы должны передаться оба фильтра от и до + цены товаров должны быть рабочие
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
                                }
                            }
                            break;

                        default:
                            predicatesAllAttributes.add(
                                    cb.and(
                                            cb.equal(attributes.get("name"), filterItem.getName()),
                                            attributes.get("value").in(filterItem.getValues())
                                    )
                            );
                            break;
                    }
                }
            }
        }

        //characteristics
        if (productFilterModel != null && productFilterModel.getCharacteristics() != null && productFilterModel.getCharacteristics().size() > 0) {
            Join<ProductCharacteristic, Product> characteristics = product.join("characteristics", JoinType.LEFT);
            for (ProductAttributeFilter filterItem : productFilterModel.getCharacteristics()) {
                if (language.equals(Constants.LANGUAGE_RU)) {
                    predicatesAllCharacteristics.add(cb.and(cb.equal(characteristics.get("attrShortName"), filterItem.getName()), characteristics.get("attrValue").in(filterItem.getValues())));
                } else {
                    predicatesAllCharacteristics.add(cb.and(cb.equal(characteristics.get("attrShortNameUa"), filterItem.getName()), characteristics.get("attrValueUa").in(filterItem.getValues())));
                }
            }
        }

        //query name OR article
        if(queryNameCode != null && queryNameCode.trim().length() > 0){
            Predicate predicateSearchName = null;
            if(language.equals(Constants.LANGUAGE_RU)){
                predicateSearchName = cb.and(
                        cb.equal(attributes.get("name"), Constants.PRODUCT_ATTR_NAME),
                        cb.like(attributes.get("value"), "%" + queryNameCode.trim() + "%")
                );
            }else {
                predicateSearchName = cb.and(
                        cb.equal(attributes.get("name"), Constants.PRODUCT_ATTR_NAME_UA),
                        cb.like(attributes.get("value"), "%" + queryNameCode.trim() + "%")
                );
            }
            predicatesAllAttributes.add(
                    cb.or(
                            cb.and(
                                    cb.equal(attributes.get("name"), Constants.PRODUCT_ATTR_ARTICLE),
                                    cb.like(attributes.get("value"), "%" + queryNameCode.trim() + "%")
                            ),
                            predicateSearchName
                    )
            );
        }


        Predicate predicateResultAttr = cb.or(predicatesAllAttributes.toArray(new Predicate[0]));
        Predicate predicateResultChar = cb.or(predicatesAllCharacteristics.toArray(new Predicate[0]));

        Predicate predicateResultCars = null;
        if(filterCars) {
            subqueryCarsCount.where(
                    cb.and(
                            cb.equal(product.get("id"), subProductsInCars.get("id")),
                            cb.and(predicatesAllCars.toArray(new Predicate[0]))

                    )
            );
            predicateResultCars = cb.greaterThanOrEqualTo(subqueryCarsCount, 1L);
        }

        Predicate predicateResult = null;
        if (predicatesAllAttributes.size() > 0 && predicatesAllCharacteristics.size() > 0 && predicatesAllCars.size() > 0) {
            predicateResult = cb.and(predicateResultAttr, predicateResultChar, predicateResultCars);
        } else if (predicatesAllAttributes.size() > 0 && predicatesAllCharacteristics.size() > 0) {
            predicateResult = cb.and(predicateResultAttr, predicateResultChar);
        } else if (predicatesAllCharacteristics.size() > 0 && predicatesAllCars.size() > 0) {
            predicateResult = cb.and(predicateResultChar, predicateResultCars);
        } else if (predicatesAllAttributes.size() > 0 && predicatesAllCars.size() > 0) {
            predicateResult = cb.and(predicateResultAttr, predicateResultCars);
        } else if (predicatesAllAttributes.size() > 0) {
            predicateResult = predicateResultAttr;
        } else if (predicatesAllCharacteristics.size() > 0) {
            predicateResult = predicateResultChar;
        } else if (predicatesAllCars.size() > 0) {
            predicateResult = predicateResultCars;
        }
        return predicateResult;
    }

}

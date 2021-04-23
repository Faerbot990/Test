package com.api.Autonova.components.handlers;

import com.api.Autonova.exceptions.ServerException;
import com.api.Autonova.models.Car;
import com.api.Autonova.models.CarProduct;
import com.api.Autonova.models.ProductAttribute;
import com.api.Autonova.repository.CarsProductRepository;
import com.api.Autonova.repository.CarsRepository;
import com.api.Autonova.repository.ProductAttributesRepository;
import com.api.Autonova.services.external_api.OneCApiService;
import com.api.Autonova.utils.AttributeParseUtil;
import com.api.Autonova.utils.Constants;
import com.sun.istack.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AttrCarListHandler   {

    @Autowired
    OneCApiService oneCApiService;

    @Autowired
    CarsRepository carsRepository;

    @Autowired
    ProductAttributesRepository productAttributesRepository;

    @Autowired
    CarsProductRepository carsProductRepository;

    private Logger logger = LoggerFactory.getLogger(AttrCarListHandler.class);

    public List<Car> findNewCarsFromTecDoc(List<Car> listFrom1C, List<Car> listFromTecDoc) {
        List<Car> listNew = new ArrayList<>();
        if(listFromTecDoc != null && listFrom1C != null){
            for(Car itemTecDoc : listFromTecDoc){
                boolean isNew = true;
                if(itemTecDoc != null){
                    for(Car item1C : listFrom1C){
                        if(item1C != null && item1C.getCarId() == itemTecDoc.getCarId()){
                            isNew = false;
                            break;
                        }
                    }
                }else {
                    isNew = false;
                }
                if(isNew){
                    listNew.add(itemTecDoc);
                }
            }
        }
        return listNew;
    }

    public void addNewCarsTo1C(List<Car> carsNew, String productCode){
        //add new cars from tec doc to 1C
        for(Car itemNew : carsNew){
            if(itemNew != null && itemNew.getCarId() != 0){
//ИЗ ТЕК ДОКА НЕТУ ДРУГОГО ЯЗЫКА УКР ЗНАЧЕНИЯ ЗАПОЛНЯЕМ РУССКИМИ
                itemNew.setFuelTypeUa(itemNew.getFuelType());
                itemNew.setConstructionTypeUa(itemNew.getConstructionType());
                saveCarTo1C(itemNew);
            }else {
                logger.error(Constants.MAIN_SERVICE_ERROR_SAVE_CAR  + "null" + " "
                        + Constants.MAIN_SERVICE_ERROR_PART_PRODUCT + productCode);
            }
        }
    }

    //Save car to 1С
    private void saveCarTo1C(@NotNull Car findCar){
        try{
            Car savedCar = oneCApiService.addCar(findCar);
            if(savedCar != null){
                //если мы записали авто в 1С запрашиваем его записывая в БД
                saveCarToDB(savedCar);
            }
        }catch (ServerException e){
            logger.error(e.getMessage());
            logger.error(Constants.ONEC_API_ERROR_SAVE_1C_API + "car: " + "'" +findCar.getCarId() + "'");
        }
    }
    public void saveCarToDB(@NotNull Car saveCar){
        Car carFromDB = carsRepository.findCarByCarId(saveCar.getCarId());
        //контроль ошибки, когда значение объекта превышает допустимое количество символов
        try {
            if(carFromDB == null){
                //save new
//ВРЕМЕННАЯ функция для дублирования в укр. значение
                if(saveCar.getConstructionTypeUa() == null || saveCar.getConstructionTypeUa().trim().length() == 0){
                    saveCar.setConstructionTypeUa(saveCar.getConstructionType());
                }
                if(saveCar.getFuelTypeUa() == null || saveCar.getFuelTypeUa().trim().length() == 0){
                    saveCar.setFuelTypeUa(saveCar.getFuelType());
                }
//
                carsRepository.save(saveCar);
            }else {
                //update old
                saveCar.setId(carFromDB.getId());
//ВРЕМЕННАЯ функция для дублирования в укр. значение
                if(saveCar.getConstructionTypeUa() == null || saveCar.getConstructionTypeUa().trim().length() == 0){
                    saveCar.setConstructionTypeUa(saveCar.getConstructionType());
                }
                if(saveCar.getFuelTypeUa() == null || saveCar.getFuelTypeUa().trim().length() == 0){
                    saveCar.setFuelTypeUa(saveCar.getFuelType());
                }
//
                carsRepository.save(saveCar);
            }
        }catch (DataIntegrityViolationException e){
            logger.error(e.getMessage());
            logger.error(Constants.MYSQL_ITEM_VALUE_EXCEEDED + "'saveCarToDB'" + " from " + "'" + saveCar.getCarId() + "'");
        }
    }

    //собираем список carId в строку для записи строки в БД
    public String convertCarsToString(@NotNull List<Car> list){
        String carsStr = "";
        for(Car item : list){
            if(item != null && item.getCarId() != 0){
                if(carsStr.trim().length() > 0){
                    carsStr += ",";
                }
                carsStr += item.getCarId();
            }
        }
        return carsStr;
    }
    //собираем список carId в строку (для передачи в метод изменения атрибута в 1С)
    public String convertCarsToJSONArray(@NotNull List<Car> list){
        String cars = "";
        for(Car item : list){
            if(item != null && item.getCarId() != 0){
                if(cars.trim().length() > 0){
                    cars += ",";
                }else {
                    cars += "[";
                }
                cars += "{\"" + Constants.PRODUCT_ATTR_CAR_LIST_ITEM + "\"" + ":"+ item.getCarId() + "}";
            }
        }
        if(cars.trim().length() > 0){
            cars += "]";
        }
        return cars;
    }

    //создаём или обновляем все связи продукта с автомобилями для дальнейшего взаимодействия Many to Many
    public void createCarsToProductsLink(){
        ArrayList<ProductAttribute> productAttributes = (ArrayList<ProductAttribute>) productAttributesRepository.findAllByName(Constants.PRODUCT_ATTR_CAR_LIST);
        //int step = productAttributes.size();
        //System.out.println("ALL - " + step);
        for(ProductAttribute attributeItem : productAttributes){
            makeCarsToProductLink(attributeItem);
            //step -= 1;
            //System.out.println("step - " + step);
        }
    }
    public void makeCarsToProductLink(ProductAttribute attributeItem){
        int productId = attributeItem.getProduct().getId();

        List<CarProduct> carsToProductLinksNew = new ArrayList<>();
        //cars inner ids (inner - in this DB)
        List<Integer> carList = AttributeParseUtil.parseListAttrFromDBToListInt(attributeItem.getValue());
        List<Integer> innerIds = carsRepository.findInnerCarIdsBy1CIds(carList.size() > 0 ? carList : null);
        //get all OLD links
        List<CarProduct> oldLinks = carsProductRepository.findAllByProductId(attributeItem.getProduct().getId());
        List<CarProduct> dellNeedLinks = new ArrayList<>();

        //generate new link list
        if(innerIds != null){
            for(Integer carInnerId : innerIds){
                CarProduct carProductLinkItem = new CarProduct();
                carProductLinkItem.setCarId(carInnerId);
                carProductLinkItem.setProductId(productId);
                carsToProductLinksNew.add(carProductLinkItem);
            }
        }

        //find dell links  and  set ID new for update old
        if(oldLinks != null){
            for(CarProduct linkOldItem : oldLinks){
                boolean notFound = true;
                for (CarProduct linkNewItem : carsToProductLinksNew){
                    if(linkOldItem.getProductId() == linkNewItem.getProductId() && linkOldItem.getCarId() == linkNewItem.getCarId()){
                        notFound = false;
                        linkNewItem.setId(linkOldItem.getId());
                    }
                }
                if(notFound){
                    dellNeedLinks.add(linkOldItem);
                }
            }
        }

        //delete old
        for(CarProduct dellLink : dellNeedLinks){
            carsProductRepository.deleteById(dellLink.getId());
        }
        //save or update new
        carsProductRepository.save(carsToProductLinksNew);
    }
}

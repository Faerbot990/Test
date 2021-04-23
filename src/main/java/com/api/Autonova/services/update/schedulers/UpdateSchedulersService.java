package com.api.Autonova.services.update.schedulers;

import com.api.Autonova.services.update.UpdateAmountService;
import com.api.Autonova.services.update.UpdateMainService;
import com.api.Autonova.services.update.UpdatePricesService;
import com.api.Autonova.utils.Constants;
import com.api.Autonova.utils.ExceptionsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class UpdateSchedulersService {

    @Autowired
    UpdateMainService updateMainService;

    @Autowired
    UpdatePricesService updatePricesService;

    @Autowired
    UpdateAmountService updateAmountService;

    private Logger logger = LoggerFactory.getLogger(UpdateSchedulersService.class);


    //Каждые 3 часа - обновляем цены
    @Scheduled(cron = "0 0 0/3 * * *")
    public void pricesUpdate() {
        try{
            updatePricesService.update();
        }catch (Exception e){
            logger.error(Constants.ERROR_GLOBAL);
            logger.error(e.getClass().getName());
            logger.error(e.getMessage());
            logger.error(ExceptionsUtil.getStackTrace(e.getStackTrace()));
        }
    }

    //Каждый час - обновляем наличие
    @Scheduled(cron = "0 0 0/1 * * *")
    public void amountUpdate() {
        try{
            updateAmountService.update();
        }catch (Exception e){
            logger.error(Constants.ERROR_GLOBAL);
            logger.error(e.getClass().getName());
            logger.error(e.getMessage());
            logger.error(ExceptionsUtil.getStackTrace(e.getStackTrace()));
        }
    }

    //Раз в неделю - полное обновление
    @Scheduled(cron = "0 0 1 * * FRI")
    public void mainUpdate() {
        try{
            updateMainService.updateMain();
        }catch (Exception e){
            logger.error(Constants.ERROR_GLOBAL);
            logger.error(e.getClass().getName());
            logger.error(e.getMessage());
            logger.error(ExceptionsUtil.getStackTrace(e.getStackTrace()));
        }
    }
}
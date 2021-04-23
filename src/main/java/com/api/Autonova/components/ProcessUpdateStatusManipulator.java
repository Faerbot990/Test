package com.api.Autonova.components;

import com.api.Autonova.models.site.SettingModel;
import com.api.Autonova.repository.SettingRepository;
import com.api.Autonova.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProcessUpdateStatusManipulator {

    @Autowired
    SettingRepository settingRepository;

    public boolean checkWorkingStatus(String process){
        SettingModel setting = null;
        switch (process){
            case Constants.SETTING_SYSTEM_UPDATE_STATUS:
                setting = settingRepository.findSettingModelByName(Constants.SETTING_SYSTEM_UPDATE_STATUS);
                break;
            case Constants.SETTING_SYSTEM_UPDATE_PRICES_STATUS:
                setting = settingRepository.findSettingModelByName(Constants.SETTING_SYSTEM_UPDATE_PRICES_STATUS);
                break;
            case Constants.SETTING_SYSTEM_UPDATE_AMOUNT_STATUS:
                setting = settingRepository.findSettingModelByName(Constants.SETTING_SYSTEM_UPDATE_AMOUNT_STATUS);
                break;
            case Constants.SETTING_SYSTEM_GET_EXCEL_STATUS:
                setting = settingRepository.findSettingModelByName(Constants.SETTING_SYSTEM_GET_EXCEL_STATUS);
                break;
            case Constants.SETTING_SYSTEM_GET_XML_STATUS:
                setting = settingRepository.findSettingModelByName(Constants.SETTING_SYSTEM_GET_XML_STATUS);
                break;
            case Constants.SETTING_SYSTEM_UPDATE_BY_CODES_STATUS:
                setting = settingRepository.findSettingModelByName(Constants.SETTING_SYSTEM_UPDATE_BY_CODES_STATUS);
                break;
        }
        if(setting == null){
            return false;
        }else {
            if(setting.getValue() != null && setting.getValue().equals("1") || setting.getValue() != null && setting.getValue().equals("true")){
                return true;
            }else {
                return false;
            }
        }

    }
    public void updateWorkingStatus(String process, boolean status){
        SettingModel settingOld = null;
        switch (process){
            case Constants.SETTING_SYSTEM_UPDATE_STATUS:
                settingOld = settingRepository.findSettingModelByName(Constants.SETTING_SYSTEM_UPDATE_STATUS);
                break;
            case Constants.SETTING_SYSTEM_UPDATE_PRICES_STATUS:
                settingOld = settingRepository.findSettingModelByName(Constants.SETTING_SYSTEM_UPDATE_PRICES_STATUS);
                break;
            case Constants.SETTING_SYSTEM_UPDATE_AMOUNT_STATUS:
                settingOld = settingRepository.findSettingModelByName(Constants.SETTING_SYSTEM_UPDATE_AMOUNT_STATUS);
                break;
            case Constants.SETTING_SYSTEM_GET_EXCEL_STATUS:
                settingOld = settingRepository.findSettingModelByName(Constants.SETTING_SYSTEM_GET_EXCEL_STATUS);
                break;
            case Constants.SETTING_SYSTEM_GET_XML_STATUS:
                settingOld = settingRepository.findSettingModelByName(Constants.SETTING_SYSTEM_GET_XML_STATUS);
                break;
            case Constants.SETTING_SYSTEM_UPDATE_BY_CODES_STATUS:
                settingOld = settingRepository.findSettingModelByName(Constants.SETTING_SYSTEM_UPDATE_BY_CODES_STATUS);
                break;
        }
        if(settingOld == null){
            settingOld = new SettingModel();
            settingOld.setName(process);
        }
        if(status){
            settingOld.setValue("1");
        }else {
            settingOld.setValue("0");
        }
        settingRepository.save(settingOld);
    }

}

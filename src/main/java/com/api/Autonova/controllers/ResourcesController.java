package com.api.Autonova.controllers;

import com.api.Autonova.exceptions.AccessException;
import com.api.Autonova.exceptions.NotFoundException;
import com.api.Autonova.exceptions.ServerException;
import com.api.Autonova.models.site.SettingModel;
import com.api.Autonova.repository.SettingRepository;
import com.api.Autonova.utils.Constants;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.activation.FileTypeMap;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;

@CrossOrigin
@RestController
@RequestMapping(value = "/")
public class ResourcesController {

    @Autowired
    SettingRepository settingRepository;

    //getFile
    @RequestMapping(value = "/resources/{name:.+}", method = RequestMethod.GET)
    public ResponseEntity<byte[]> getFile(@PathVariable(value = "name") String name) throws IOException {
        SettingModel baseFolder = settingRepository.findSettingModelByName(Constants.SETTING_BASE_RESOURCES_FOLDER);
        if(baseFolder != null && baseFolder.getValue() != null && baseFolder.getValue().trim().length() > 0){
            try{
                File file = new File(new FileSystemResource("").getFile().getAbsolutePath() + baseFolder.getValue().trim() + "/" + name);
                return ResponseEntity.ok().contentType(MediaType.valueOf(FileTypeMap.getDefaultFileTypeMap().getContentType(file))).body(Files.readAllBytes(file.toPath()));
            }catch (NoSuchFileException e){
                throw new NotFoundException(Constants.ERROR_DATA_NOT_FOUND);
            }
        }else {
            throw new ServerException(Constants.ERROR_API_SETTING_NOT_FOUND + Constants.SETTING_BASE_RESOURCES_FOLDER);
        }
    }
}


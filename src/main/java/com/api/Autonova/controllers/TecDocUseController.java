package com.api.Autonova.controllers;

import com.api.Autonova.exceptions.AccessException;
import com.api.Autonova.repository.UserRepository;
import com.api.Autonova.services.external_api.TecDocApiService;
import com.api.Autonova.utils.Constants;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping(value = "/api")
public class TecDocUseController {

    @Autowired
    TecDocApiService tecDocApiService;

    @Autowired
    UserRepository userRepository;

    //метод для проверки аутентификации
    private boolean checkPermission(String token){
        if(token != null && token.trim().length() > 0 && userRepository.findUserByToken(token) != null &&
                userRepository.findUserByToken(token).isStatus()){
            return true;
        }else {
            throw new AccessException(Constants.ADMIN_TOKEN_EXCEPTIONS);
        }
    }

    @RequestMapping(value = "/tecdoc/categories", method = RequestMethod.GET)
    public JsonNode getCategories(@RequestHeader("Authorization") String authToken,
                                  @RequestParam(defaultValue = "true") boolean assemblyGroupFacetOptionsEnabled, @RequestParam(defaultValue = "") String linkingTargetType,
                                  @RequestParam(defaultValue = "true") boolean includeCompleteTree, @RequestParam(defaultValue = "0") int perPage,
                                  @RequestParam(defaultValue = "1") int page) {
        checkPermission(authToken);
        tecDocApiService.updateAccess();
        return tecDocApiService.getCategories(assemblyGroupFacetOptionsEnabled, linkingTargetType, includeCompleteTree, perPage, page);
    }

    @RequestMapping(value = "/tecdoc/categories/getByArticle", method = RequestMethod.GET)
    public JsonNode getCategoriesByArticle(@RequestHeader("Authorization") String authToken,
                                   @RequestParam String searchQuery, @RequestParam(defaultValue = "0") int searchType, @RequestParam int dataSupplierIds,
                                   @RequestParam(defaultValue = "") String assemblyGroupType, @RequestParam(defaultValue = "true") boolean assemblyGroupFacetOptionsEnabled,
                                   @RequestParam(defaultValue = "true") boolean includeCompleteTree, @RequestParam(defaultValue = "100") int perPage,
                                   @RequestParam(defaultValue = "1") int page) {
        checkPermission(authToken);
        tecDocApiService.updateAccess();
        return tecDocApiService.getCategoriesByArticle(searchQuery, searchType, dataSupplierIds, assemblyGroupFacetOptionsEnabled, assemblyGroupType,
                includeCompleteTree, perPage, page);
    }
}


package com.api.Autonova.components.handlers;

import com.api.Autonova.models.ProductImage;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.List;

@Component
public class AttrImageListHandler {

    public String convertImagesToString(@NotNull List<ProductImage> list){
        String images = "";
        for(ProductImage item : list){
            if(item != null && item.getImage() != null){
                if(images.trim().length() > 0){
                    images += "|";
                }
                images += item.getImage().trim();
            }
        }
        return images;
    }

}

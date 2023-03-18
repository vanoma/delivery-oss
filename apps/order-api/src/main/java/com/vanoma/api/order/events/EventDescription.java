package com.vanoma.api.order.events;

import com.vanoma.api.utils.exceptions.InvalidParameterException;

public class EventDescription {
    public static String getTemplateEN(EventName eventName) {
        switch (eventName) {
            case ORDER_PLACED:
                return "Your delivery request was placed successfully";
            case DRIVER_ASSIGNED:
                return "A driver is assigned to your package";
            case DRIVER_CONFIRMED:
                return "Our driver has confirmed your delivery";
            case DRIVER_DEPARTING_PICK_UP:
                return "Our driver is heading to the pick-up place";
            case DRIVER_ARRIVED_PICK_UP:
                return "Our driver just arrived at the pick-up place";
            case PACKAGE_PICKED_UP:
                return "The package is now picked up";
            case DRIVER_DEPARTING_DROP_OFF:
                return "Our driver is heading to the drop-off place";
            case DRIVER_ARRIVED_DROP_OFF:
                return "Our driver just arrived at the drop-off place";
            case PACKAGE_DELIVERED:
                return "The package is now delivered";
            case PACKAGE_CANCELLED:
                return "The delivery is now cancelled";
            default:
                throw new InvalidParameterException("crud.packageEvent.eventNotSupported");
        }
    }

    public static String getTemplateFR(EventName eventName) {
        switch (eventName) {
            case ORDER_PLACED:
                return "Votre demande de livraison a été effectuée avec succès";
            case DRIVER_ASSIGNED:
                return "Un chauffeur est affecté à votre livraison";
            case DRIVER_CONFIRMED:
                return "Notre chauffeur a confirmé votre livraison";
            case DRIVER_DEPARTING_PICK_UP:
                return "Notre chauffeur se dirige vers le lieu de ramassage";
            case DRIVER_ARRIVED_PICK_UP:
                return "Notre chauffeur vient d'arriver au lieu de ramassage";
            case PACKAGE_PICKED_UP:
                return "Le colis est maintenant récupéré";
            case DRIVER_DEPARTING_DROP_OFF:
                return "Notre chauffeur se dirige vers le lieu de destination";
            case DRIVER_ARRIVED_DROP_OFF:
                return "Le chauffeur vient d'arriver à la destination";
            case PACKAGE_DELIVERED:
                return "Le colis est maintenant livré";
            case PACKAGE_CANCELLED:
                return "La livraison est maintenant annulé";
            default:
                throw new InvalidParameterException("crud.packageEvent.eventNotSupported");
        }
    }

    public static String getTemplateRW(EventName eventName) {
        switch (eventName) {
            case ORDER_PLACED:
                return "Gusaba utware ubutumwa bigenze neza";
            case DRIVER_ASSIGNED:
                return "Umushoferi agiye guhaguruka ajye gufata ubutumwa";
            case DRIVER_CONFIRMED:
                return "Umushoferi yemeje deliveri";
            case DRIVER_DEPARTING_PICK_UP:
                return "Umushoferi ari kwerekeza aho ubutumwa buri";
            case DRIVER_ARRIVED_PICK_UP:
                return "Umushoferi wacu ageze aho afata ubutumwa";
            case PACKAGE_PICKED_UP:
                return "Umushoferi amaze gushyikira ubutumwa";
            case DRIVER_DEPARTING_DROP_OFF:
                return "Umushoferi ari kwerekeza aho ubutumwa burashyirwa";
            case DRIVER_ARRIVED_DROP_OFF:
                return "Umushoferi wacu ageze aho atanga ubutumwa";
            case PACKAGE_DELIVERED:
                return "Ubutumwa bumaze gushyikirizwa nyirabwo";
            case PACKAGE_CANCELLED:
                return "Ubutumwa burahagaritswe";
            default:
                throw new InvalidParameterException("crud.packageEvent.eventNotSupported");
        }
    }
}

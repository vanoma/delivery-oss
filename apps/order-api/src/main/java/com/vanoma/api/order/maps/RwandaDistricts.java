package com.vanoma.api.order.maps;

public enum RwandaDistricts {
    GASABO,
    KICUKIRO,
    NYARUGENGE,
    BURERA,
    GAKENKE,
    GICUMBI,
    MUSANZE,
    RULINDO,
    GISAGARA,
    HUYE,
    KAMONYI,
    MUHANGA,
    NYAMAGABE,
    NYANZA,
    NYARUGURU,
    RUHANGO,
    BUGESERA,
    GATSIBO,
    KAYONZA,
    KIREHE,
    NGOMA,
    NYAGATARE,
    RWAMAGANA,
    KARONGI,
    NGORORERO,
    NYABIHU,
    NYAMASHEKE,
    RUBAVU,
    RUSIZI,
    RUTSIRO;

    public static boolean isValid(String district) {
        if (district == null) return false;
        for (RwandaDistricts p : RwandaDistricts.values()) {
            if (district.toUpperCase().equals(p.name())) return true;
        }
        return false;
    }
}

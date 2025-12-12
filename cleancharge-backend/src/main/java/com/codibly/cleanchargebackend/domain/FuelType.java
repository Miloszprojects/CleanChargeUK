package com.codibly.cleanchargebackend.domain;

public enum FuelType {
    GAS,
    COAL,
    BIOMASS,
    NUCLEAR,
    HYDRO,
    IMPORTS,
    OTHER,
    WIND,
    SOLAR;

    public static FuelType fromApi(String fuel) {
        return FuelType.valueOf(fuel.toUpperCase());
    }

    public boolean isClean() {
        return switch (this) {
            case BIOMASS, NUCLEAR, HYDRO, WIND, SOLAR -> true;
            default -> false;
        };
    }
}

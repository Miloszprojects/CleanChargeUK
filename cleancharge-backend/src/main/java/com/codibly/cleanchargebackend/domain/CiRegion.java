package com.codibly.cleanchargebackend.domain;


public enum CiRegion {
    NORTH_SCOTLAND(1, "North Scotland"),
    SOUTH_SCOTLAND(2, "South Scotland"),
    NORTH_WEST_ENGLAND(3, "North West England"),
    NORTH_EAST_ENGLAND(4, "North East England"),
    SOUTH_YORKSHIRE(5, "South Yorkshire"),
    NORTH_WALES(6, "North Wales, Merseyside and Cheshire"),
    SOUTH_WALES(7, "South Wales"),
    WEST_MIDLANDS(8, "West Midlands"),
    EAST_MIDLANDS(9, "East Midlands"),
    EAST_ENGLAND(10, "East England"),
    SOUTH_WEST_ENGLAND(11, "South West England"),
    SOUTH_ENGLAND(12, "South England"),
    LONDON(13, "London"),
    SOUTH_EAST_ENGLAND(14, "South East England"),
    ENGLAND(15, "England"),
    SCOTLAND(16, "Scotland"),
    WALES(17, "Wales");

    private final int id;
    private final String shortName;

    CiRegion(int id, String shortName) {
        this.id = id;
        this.shortName = shortName;
    }

    public int getId() { return id; }
    public String getShortName() { return shortName; }

    public static CiRegion fromId(int id) {
        for (var r : values()) {
            if (r.id == id) return r;
        }
        throw new IllegalArgumentException("Unknown region id: " + id);
    }
}

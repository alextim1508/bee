package com.alextim.bee.client.dto;

public record GeoData(float lat, float lon) {

    @Override
    public String toString() {
        return "Геоданные: " + lat + ", " + lon;
    }
}

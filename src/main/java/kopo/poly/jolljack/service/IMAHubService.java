package kopo.poly.jolljack.service;

import kopo.poly.jolljack.dto.RegionDTO;
import kopo.poly.jolljack.dto.WeatherSumDTO;

public interface IMAHubService {

    WeatherSumDTO getDailyWeather(RegionDTO pDTO) throws Exception;

}
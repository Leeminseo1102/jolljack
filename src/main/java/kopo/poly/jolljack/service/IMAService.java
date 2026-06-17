package kopo.poly.jolljack.service;

import kopo.poly.jolljack.dto.RegionDTO;
import kopo.poly.jolljack.dto.WeatherSumDTO;

public interface IMAService {

    WeatherSumDTO getMonthWeather(RegionDTO regionDTO) throws Exception;

}

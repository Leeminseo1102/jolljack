package kopo.poly.jolljack.service;


import kopo.poly.jolljack.dto.RegionWeatherDTO;

public interface IRegionWeatherService {

    RegionWeatherDTO getRegionWeatherProc(Long userId) throws Exception;

}
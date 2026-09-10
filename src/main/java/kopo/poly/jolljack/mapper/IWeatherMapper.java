package kopo.poly.jolljack.mapper;

import kopo.poly.jolljack.dto.RegionGridDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IWeatherMapper {

    Long getRegionId(RegionGridDTO pDTO) throws Exception;

    RegionGridDTO getRegionGrid(RegionGridDTO pDTO) throws Exception;
}
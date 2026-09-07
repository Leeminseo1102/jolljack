package kopo.poly.jolljack.mapper;

import kopo.poly.jolljack.dto.RegionGridDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IWeatherMapper {

    Long getRegionId(Long userId);

    RegionGridDTO getRegionGrid(Long regionId);
}

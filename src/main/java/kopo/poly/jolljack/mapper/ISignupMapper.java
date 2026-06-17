package kopo.poly.jolljack.mapper;

import kopo.poly.jolljack.dto.CropDTO;
import kopo.poly.jolljack.dto.RegionDTO;
import kopo.poly.jolljack.dto.UserDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ISignupMapper {

    int checkEmail(UserDTO pDTO) throws Exception;

    int checkLoginId(UserDTO pDTO) throws Exception;

    int insertUser(UserDTO pDTO) throws Exception;

    List<RegionDTO> getSidoList() throws Exception;

    List<RegionDTO> getSigunguList(RegionDTO pDTO) throws Exception;

    RegionDTO getRegionId(RegionDTO pDTO) throws Exception;

    List<CropDTO> getCropList() throws Exception;

    RegionDTO getRegionByRegionId(RegionDTO pDTO) throws Exception;

    CropDTO getCropByCropName(CropDTO pDTO) throws Exception;
}
package kopo.poly.jolljack.mapper;

import kopo.poly.jolljack.dto.UserDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IFindMapper {

    UserDTO getUserByNameAndEmail(UserDTO pDTO) throws Exception;

    UserDTO getUserByLoginIdAndNameAndEmail(UserDTO pDTO) throws Exception;

    int updatePasswordHash(UserDTO pDTO) throws Exception;

    UserDTO getUserByUserId(UserDTO pDTO) throws Exception;
}
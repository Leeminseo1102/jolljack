package kopo.poly.jolljack.mapper;

import kopo.poly.jolljack.dto.UserDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ILoginMapper {


    UserDTO getLoginUser(UserDTO pDTO) throws Exception;


    int updateLastLoginAt(UserDTO pDTO) throws Exception;

}

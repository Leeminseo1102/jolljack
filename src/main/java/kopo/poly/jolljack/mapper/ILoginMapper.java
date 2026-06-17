package kopo.poly.jolljack.mapper;

import kopo.poly.jolljack.dto.UserDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ILoginMapper {


    //UserDTO getLoginUser(UserDTO pDTO) throws Exception;

    int updateLastLoginAt(UserDTO pDTO) throws Exception;

    UserDTO getLoginUserIncludeDeleted(UserDTO pDTO) throws Exception;

    UserDTO getDeletedUserWithin15Days(UserDTO pDTO) throws Exception;

    int restoreUser(UserDTO pDTO) throws Exception;

}

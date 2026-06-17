package kopo.poly.jolljack.mapper;

import kopo.poly.jolljack.dto.*;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IMyPageMapper {

    MyPageDTO getUserInfo(UserDTO pDTO) throws Exception;

    int updateFavoriteCrop(UserDTO pDTO) throws Exception;

    int updateRegion(UserDTO pDTO) throws Exception;

    int withdrawUser(UserDTO pDTO) throws Exception;

    List<CropAnalysisDTO> getAnalysisList(UserDTO pDTO) throws Exception;

    List<DiseaseDiagnosisDTO> getDiagnosisList(UserDTO pDTO) throws Exception;

    int deleteExpiredDiagnosis() throws Exception;

    int deleteExpiredAnalysis() throws Exception;

    int deleteExpiredUsers() throws Exception;

}

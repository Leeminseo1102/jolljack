package kopo.poly.jolljack.mapper;

import kopo.poly.jolljack.dto.DiseaseDiagnosisDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IDiseaseDiagnosisMapper {

    int insertDiseaseDiagnosis(DiseaseDiagnosisDTO pDTO) throws Exception;

    DiseaseDiagnosisDTO getDiseaseDiagnosisResult(DiseaseDiagnosisDTO pDTO) throws Exception;

}
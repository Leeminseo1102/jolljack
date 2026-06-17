package kopo.poly.jolljack.mapper;

import kopo.poly.jolljack.dto.CropAnalysisDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ICropAnalysisMapper {

    int insertCropAnalysis(CropAnalysisDTO pDTO) throws Exception;

    CropAnalysisDTO getCropAnalysisResult(CropAnalysisDTO pDTO) throws Exception;
}
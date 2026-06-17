package kopo.poly.jolljack.scheduler;

import kopo.poly.jolljack.mapper.IMyPageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserDeleteScheduler {

    private final IMyPageMapper myPageMapper;

    /**
     * 매일 새벽 3시에 탈퇴 후 15일 지난 회원 삭제
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void deleteExpiredUsers() {

        log.info("탈퇴 후 15일 지난 회원 삭제 스케줄러 시작");

        try {
            int diagnosisCnt = myPageMapper.deleteExpiredDiagnosis();
            int analysisCnt = myPageMapper.deleteExpiredAnalysis();
            int userCnt = myPageMapper.deleteExpiredUsers();

            log.info("삭제된 진단 데이터 수 : {}", diagnosisCnt);
            log.info("삭제된 분석 데이터 수 : {}", analysisCnt);
            log.info("삭제된 회원 수 : {}", userCnt);

        } catch (Exception e) {
            log.error("탈퇴 회원 삭제 스케줄러 실패", e);
        }

        log.info("탈퇴 후 15일 지난 회원 삭제 스케줄러 종료");
    }
}
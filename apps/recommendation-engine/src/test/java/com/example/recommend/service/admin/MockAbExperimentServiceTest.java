package com.example.recommend.service.admin;

import com.example.recommend.common.BusinessException;
import com.example.recommend.common.ErrorCode;
import com.example.recommend.dto.admin.AbExperimentDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 修复Bug-010：MockAbExperimentService 单元测试。
 * <p>
 * 覆盖实验状态机（修复Bug-013 running 状态不可修改）、实验 CRUD、分流配置等核心逻辑。
 *
 * @author recommend-team
 * @since 1.0.0
 */
class MockAbExperimentServiceTest {

    private MockAbExperimentService experimentService;

    @BeforeEach
    void setUp() {
        experimentService = new MockAbExperimentService();
    }

    @Test
    @DisplayName("修复Bug-013：running 状态的实验不允许修改核心配置")
    void updateExperiment_withRunningStatus_shouldThrowException() {
        // 获取一个 running 状态的实验（Mock 数据中 exp_001 为 running）
        List<AbExperimentDTO> runningExperiments = experimentService.listExperiments("running");
        assertFalse(runningExperiments.isEmpty(), "应存在 running 状态的实验");
        String runningExpId = runningExperiments.get(0).getExperimentId();

        // 尝试修改 running 状态实验的 trafficPercent
        AbExperimentDTO update = AbExperimentDTO.builder()
                .trafficPercent(80)
                .build();

        BusinessException exception = assertThrows(BusinessException.class, () ->
                experimentService.updateExperiment(runningExpId, update));

        assertEquals(ErrorCode.EXPERIMENT_STATUS_INVALID.getCode(), exception.getErrorCode().getCode(),
                "running 状态实验修改应抛出 EXPERIMENT_STATUS_INVALID");
        assertTrue(exception.getMessage().contains("running"),
                "异常信息应说明 running 状态不允许修改");
    }

    @Test
    @DisplayName("修复Bug-013：draft 状态的实验允许修改")
    void updateExperiment_withDraftStatus_shouldSucceed() {
        // 获取一个 draft 状态的实验（Mock 数据中 exp_004 为 draft）
        List<AbExperimentDTO> draftExperiments = experimentService.listExperiments("draft");
        assertFalse(draftExperiments.isEmpty(), "应存在 draft 状态的实验");
        String draftExpId = draftExperiments.get(0).getExperimentId();
        int originalVersion = draftExperiments.get(0).getConfigVersion();

        // 修改 draft 状态实验
        AbExperimentDTO update = AbExperimentDTO.builder()
                .name("修改后的实验名称")
                .trafficPercent(30)
                .build();

        AbExperimentDTO result = experimentService.updateExperiment(draftExpId, update);

        assertEquals("修改后的实验名称", result.getName());
        assertEquals(30, result.getTrafficPercent());
        assertEquals(originalVersion + 1, result.getConfigVersion(), "configVersion 应自增");
    }

    @Test
    @DisplayName("修复Bug-013：paused 状态的实验允许修改")
    void updateExperiment_withPausedStatus_shouldSucceed() {
        // 获取一个 paused 状态的实验（Mock 数据中 exp_003 为 paused）
        List<AbExperimentDTO> pausedExperiments = experimentService.listExperiments("paused");
        assertFalse(pausedExperiments.isEmpty(), "应存在 paused 状态的实验");
        String pausedExpId = pausedExperiments.get(0).getExperimentId();

        AbExperimentDTO update = AbExperimentDTO.builder()
                .trafficPercent(25)
                .build();

        AbExperimentDTO result = experimentService.updateExperiment(pausedExpId, update);

        assertEquals(25, result.getTrafficPercent());
    }

    @Test
    @DisplayName("创建实验初始状态为 draft，configVersion=1")
    void createExperiment_shouldInitializeStatusAndVersion() {
        AbExperimentDTO experiment = AbExperimentDTO.builder()
                .name("新测试实验")
                .trafficPercent(50)
                .build();

        AbExperimentDTO result = experimentService.createExperiment(experiment);

        assertNotNull(result.getExperimentId());
        assertEquals("draft", result.getStatus(), "新建实验状态应为 draft");
        assertEquals(1, result.getConfigVersion(), "新建实验 configVersion 应为 1");
    }

    @Test
    @DisplayName("启动实验：draft → running 成功")
    void startExperiment_fromDraft_shouldSucceed() {
        AbExperimentDTO draft = experimentService.createExperiment(
                AbExperimentDTO.builder().name("待启动实验").build());

        AbExperimentDTO result = experimentService.startExperiment(draft.getExperimentId());

        assertEquals("running", result.getStatus());
        assertNotNull(result.getStartTime());
    }

    @Test
    @DisplayName("启动实验：running 状态再次启动应抛异常")
    void startExperiment_fromRunning_shouldThrowException() {
        List<AbExperimentDTO> runningExperiments = experimentService.listExperiments("running");
        String runningExpId = runningExperiments.get(0).getExperimentId();

        assertThrows(BusinessException.class, () ->
                experimentService.startExperiment(runningExpId));
    }

    @Test
    @DisplayName("暂停实验：running → paused 成功")
    void pauseExperiment_fromRunning_shouldSucceed() {
        List<AbExperimentDTO> runningExperiments = experimentService.listExperiments("running");
        String runningExpId = runningExperiments.get(0).getExperimentId();

        AbExperimentDTO result = experimentService.pauseExperiment(runningExpId);

        assertEquals("paused", result.getStatus());
    }

    @Test
    @DisplayName("按状态过滤实验列表")
    void listExperiments_withStatusFilter_shouldReturnFilteredResults() {
        List<AbExperimentDTO> running = experimentService.listExperiments("running");
        List<AbExperimentDTO> draft = experimentService.listExperiments("draft");
        List<AbExperimentDTO> all = experimentService.listExperiments(null);

        assertTrue(running.stream().allMatch(e -> "running".equals(e.getStatus())));
        assertTrue(draft.stream().allMatch(e -> "draft".equals(e.getStatus())));
        assertTrue(all.size() > running.size());
    }
}

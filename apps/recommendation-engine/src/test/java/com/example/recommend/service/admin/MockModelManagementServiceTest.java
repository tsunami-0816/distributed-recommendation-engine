package com.example.recommend.service.admin;

import com.example.recommend.common.BusinessException;
import com.example.recommend.common.ErrorCode;
import com.example.recommend.dto.admin.ModelVersionDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 修复Bug-010：MockModelManagementService 单元测试。
 * <p>
 * 覆盖模型激活/回滚、特征一致性门禁、活跃模型查询（修复Bug-002 模型激活后推荐链路读取活跃模型）等核心逻辑。
 *
 * @author recommend-team
 * @since 1.0.0
 */
class MockModelManagementServiceTest {

    private MockModelManagementService modelService;

    @BeforeEach
    void setUp() {
        modelService = new MockModelManagementService();
    }

    @Test
    @DisplayName("修复Bug-002：激活模型后，该模型状态为 active，同类型其他模型为 inactive")
    void activateModel_shouldSetTargetActiveAndOthersInactive() {
        // Mock 数据中 deepfm:v2 初始为 active，deepfm:v1 为 inactive
        // 激活 deepfm:v1
        ModelVersionDTO result = modelService.activateModel("deepfm", "v1");

        assertEquals("active", result.getStatus());
        assertEquals("v1", result.getVersion());

        // 验证同类型其他模型变为 inactive
        List<ModelVersionDTO> deepfmModels = modelService.listModels("deepfm");
        long activeCount = deepfmModels.stream()
                .filter(m -> "active".equalsIgnoreCase(m.getStatus()))
                .count();
        assertEquals(1, activeCount, "同一类型应只有一个 active 模型");
    }

    @Test
    @DisplayName("修复Bug-002：激活模型后可通过 listModels 查询到 active 状态模型")
    void activateModel_shouldBeQueryableAsActive() {
        modelService.activateModel("deepfm", "v1");

        List<ModelVersionDTO> activeModels = modelService.listModels("deepfm").stream()
                .filter(m -> "active".equalsIgnoreCase(m.getStatus()))
                .toList();

        assertEquals(1, activeModels.size());
        assertEquals("v1", activeModels.get(0).getVersion());
    }

    @Test
    @DisplayName("激活未通过特征一致性校验的模型应抛异常")
    void activateModel_withFeatureConsistencyFailed_shouldThrowException() {
        // Mock 数据中 deepfm:v3 的 featureConsistencyPassed=false，status=training
        BusinessException exception = assertThrows(BusinessException.class, () ->
                modelService.activateModel("deepfm", "v3"));

        assertEquals(ErrorCode.FEATURE_CONSISTENCY_FAILED.getCode(), exception.getErrorCode().getCode(),
                "未通过特征一致性校验的模型无法激活");
    }

    @Test
    @DisplayName("激活不存在的模型应抛异常")
    void activateModel_nonExistentModel_shouldThrowException() {
        assertThrows(BusinessException.class, () ->
                modelService.activateModel("deepfm", "v999"));
    }

    @Test
    @DisplayName("回滚模型等价于激活指定版本")
    void rollbackModel_shouldActivateSpecifiedVersion() {
        // 先激活 v1，再回滚到 v2
        modelService.activateModel("deepfm", "v1");
        ModelVersionDTO result = modelService.rollbackModel("deepfm", "v2");

        assertEquals("active", result.getStatus());
        assertEquals("v2", result.getVersion());
    }

    @Test
    @DisplayName("查询模型列表按类型过滤")
    void listModels_withTypeFilter_shouldReturnFilteredResults() {
        List<ModelVersionDTO> deepfmModels = modelService.listModels("deepfm");
        List<ModelVersionDTO> xgboostModels = modelService.listModels("xgboost");
        List<ModelVersionDTO> allModels = modelService.listModels(null);

        assertTrue(deepfmModels.stream().allMatch(m -> "deepfm".equals(m.getModelType())));
        assertTrue(xgboostModels.stream().allMatch(m -> "xgboost".equals(m.getModelType())));
        assertTrue(allModels.size() > deepfmModels.size());
    }

    @Test
    @DisplayName("查询模型指标返回模型信息（含 AUC、LogLoss 等）")
    void getModelMetrics_shouldReturnModelInfo() {
        ModelVersionDTO result = modelService.getModelMetrics("deepfm", "v2");

        assertEquals("deepfm", result.getModelType());
        assertEquals("v2", result.getVersion());
        assertNotNull(result.getAuc());
        assertNotNull(result.getLogLoss());
    }

    @Test
    @DisplayName("查询不存在的模型指标应抛异常")
    void getModelMetrics_nonExistentModel_shouldThrowException() {
        assertThrows(BusinessException.class, () ->
                modelService.getModelMetrics("deepfm", "v999"));
    }

    @Test
    @DisplayName("模型列表按 modelType 和 version 降序排列")
    void listModels_shouldBeSortedByTypeAndVersionDesc() {
        List<ModelVersionDTO> deepfmModels = modelService.listModels("deepfm");

        // deepfm 模型应按 version 降序：v3, v2, v1
        assertEquals("v3", deepfmModels.get(0).getVersion());
        assertEquals("v2", deepfmModels.get(1).getVersion());
        assertEquals("v1", deepfmModels.get(2).getVersion());
    }
}

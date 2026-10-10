package com.example.recommend.service.admin;

import com.example.recommend.dto.admin.FeatureConsistencyDTO;

/**
 * 特征一致性校验服务接口（M11-T05）。
 * <p>
 * 提供校验结果查询和手动触发校验。
 * 校验离线训练特征与在线推理特征的一致性，是模型发布的前置门禁。
 *
 * @author recommend-team
 * @since 1.0.0
 */
public interface FeatureConsistencyService {

    /**
     * 查询指定日期的特征一致性校验结果。
     *
     * @param date 日期（YYYY-MM-DD）
     * @return 校验结果
     */
    FeatureConsistencyDTO getConsistencyResult(String date);

    /**
     * 手动触发特征一致性校验。
     *
     * @return 触发后的校验结果（异步校验时返回初始状态）
     */
    FeatureConsistencyDTO triggerConsistencyCheck();
}

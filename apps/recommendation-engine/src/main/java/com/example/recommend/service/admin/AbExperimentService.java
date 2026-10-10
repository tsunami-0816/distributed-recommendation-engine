package com.example.recommend.service.admin;

import com.example.recommend.dto.admin.AbExperimentDTO;

import java.util.List;

/**
 * A/B 实验管理服务接口（M11-T01）。
 * <p>
 * 提供实验 CRUD、状态机流转（draft→running→paused→ended）及实验报告查询。
 *
 * @author recommend-team
 * @since 1.0.0
 */
public interface AbExperimentService {

    /**
     * 获取实验列表。
     *
     * @param status 可选状态过滤（draft/running/paused/ended）
     * @return 实验列表
     */
    List<AbExperimentDTO> listExperiments(String status);

    /**
     * 创建实验（初始状态 draft，configVersion=1）。
     *
     * @param experiment 实验信息
     * @return 创建后的实验（含生成的 experimentId）
     */
    AbExperimentDTO createExperiment(AbExperimentDTO experiment);

    /**
     * 更新实验（configVersion 自增，失效旧分流缓存）。
     *
     * @param experimentId 实验 ID
     * @param experiment   更新内容
     * @return 更新后的实验
     */
    AbExperimentDTO updateExperiment(String experimentId, AbExperimentDTO experiment);

    /**
     * 启动实验（draft/paused → running）。
     *
     * @param experimentId 实验 ID
     * @return 更新后的实验
     */
    AbExperimentDTO startExperiment(String experimentId);

    /**
     * 暂停实验（running → paused）。
     *
     * @param experimentId 实验 ID
     * @return 更新后的实验
     */
    AbExperimentDTO pauseExperiment(String experimentId);

    /**
     * 结束实验（running/paused → ended）。
     *
     * @param experimentId 实验 ID
     * @return 更新后的实验
     */
    AbExperimentDTO endExperiment(String experimentId);

    /**
     * 获取实验报告（含各指标的对照组/实验组数据、提升度、p 值）。
     *
     * @param experimentId 实验 ID
     * @return 实验报告（含 metrics 字段）
     */
    AbExperimentDTO getExperimentReport(String experimentId);
}

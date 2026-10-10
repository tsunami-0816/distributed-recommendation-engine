package com.example.recommend.service.recall;

import com.example.recommend.dto.RecallChannel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * 召回候选物品。
 * <p>
 * 多路召回合并后的候选物品结构，携带召回通道标记和召回分数。
 *
 * @author recommend-team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateItem {

    /** 物品 ID */
    private String itemId;

    /** 召回分数（该通道内的相似度/热度分，0~1） */
    private double recallScore;

    /** 召回通道集合（一个物品可能被多路召回命中） */
    private Set<RecallChannel> recallChannels;

    /** 物品详情（召回阶段填充，避免后续 N+1 查询） */
    private com.example.recommend.dto.ItemInfoDTO itemInfo;
}

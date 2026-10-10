package com.example.recommend.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 缓存刷新请求 DTO（M11-T04）。
 * <p>
 * 对应前端 CacheManage.vue 及 POST /admin/cache/refresh 接口。
 * 支持按 Key 前缀批量失效，或指定具体 Key 删除。
 *
 * @author recommend-team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CacheRefreshRequestDTO {

    /** Key 前缀（如 rec:result / user:profile:long），与 key 二选一 */
    private String prefix;

    /** 具体 Key，与 prefix 二选一 */
    private String key;
}

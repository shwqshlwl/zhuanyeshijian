package com.example.citp.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 批量导入结果 VO
 */
@Data
@Schema(description = "批量导入结果")
public class BatchImportResultVO {

    @Schema(description = "成功数量")
    private Integer successCount;

    @Schema(description = "失败数量")
    private Integer failCount;

    @Schema(description = "失败详情")
    private String failDetails;

    @Schema(description = "失败原因列表")
    private List<String> failReasons;

    public BatchImportResultVO() {
        this.successCount = 0;
        this.failCount = 0;
        this.failDetails = "";
        this.failReasons = new ArrayList<>();
    }

    public BatchImportResultVO(Integer successCount, Integer failCount, String failDetails) {
        this.successCount = successCount;
        this.failCount = failCount;
        this.failDetails = failDetails;
        this.failReasons = new ArrayList<>();
    }

    public BatchImportResultVO(Integer successCount, Integer failCount, List<String> failReasons) {
        this.successCount = successCount;
        this.failCount = failCount;
        this.failReasons = failReasons;
        this.failDetails = failReasons != null ? String.join("; ", failReasons) : "";
    }
}

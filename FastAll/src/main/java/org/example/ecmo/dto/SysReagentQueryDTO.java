package org.example.ecmo.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 试剂查询参数对象
 */
@Data
public class SysReagentQueryDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer pageNum = 1;

    private Integer pageSize = 10;

    private String name;

    private String casNumber;

    private String safeLevel;

    private String batchNumber;

    private String purity;

    private String storageCondition;

    /**
     * 库存状态：1-低库存(<10), 2-正常(>=10)
     */
    private Integer stockStatus;

    /**
     * 有效期状态：1-已过期, 2-临期(30天), 3-正常
     */
    private Integer expiryStatus;

    private String expiryStartDate;

    private String expiryEndDate;
}

package org.example.ecmo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

@Data
@TableName("sys_file_info")
public class SysFileInfo {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String fileName;
    private String filePath;
    private Long fileSize;
    private String fileExt;
    private String fileMd5;
    private Long uploadUserId;

    private String bizType;
    private Long bizId;
    private Integer sortOrder;
    private String fileType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}

package org.example.ecmo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

@Data
@TableName("sys_file_chunk")
public class SysFileChunk {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String identifier;    // 文件唯一标识 (MD5)
    private Integer chunkNumber;  // 分片序号
    private Integer totalChunks;  // 总分片数
    private Long chunkSize;       // 分片大小
    private String relativePath;  // 分片存储路径
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}

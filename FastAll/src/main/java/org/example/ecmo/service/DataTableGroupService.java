package org.example.ecmo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.ecmo.entity.DataTableGroup;
import java.util.List;
import java.util.Map;

public interface DataTableGroupService extends IService<DataTableGroup> {
    
    List<DataTableGroup> findAllActive();
    
    List<Map<String, Object>> getGroupTree();
    
    DataTableGroup findByCode(String groupCode);
}

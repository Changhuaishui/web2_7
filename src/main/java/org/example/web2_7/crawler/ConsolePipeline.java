package org.example.web2_7.crawler;
/*
 * 输出控制台
 */

import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

public class ConsolePipeline implements Pipeline {
    @Override
    public void process(ResultItems resultItems, Task task) {
        // 遍历结果数据并输出到控制台
        for (String key : resultItems.getAll().keySet()) {
            System.out.println(key + ":\n" + resultItems.get(key));
        }
    }
}

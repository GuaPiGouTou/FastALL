package org.example.ecmo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.local.path:./uploads/}")
    private String localPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        try {
            File dir = new File(localPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String absolutePath = dir.getAbsolutePath().replace("\\", "/");
            if (!absolutePath.endsWith("/")) {
                absolutePath += "/";
            }
            String pathLocation = "file:" + absolutePath;
            registry.addResourceHandler("/files/**")
                    .addResourceLocations(pathLocation);
        } catch (Exception e) {
            System.err.println("❌ 资源映射配置异常: " + e.getMessage());
        }
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/{spring:[^\\.]*}")
                .setViewName("forward:/index.html");
        registry.addViewController("/**/{spring:[^\\.]*}")
                .setViewName("forward:/index.html");
        registry.addViewController("/**/{spring:[^\\.]*}/**{spring:[^\\.]*}")
                .setViewName("forward:/index.html");
    }
}

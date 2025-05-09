package org.example.web2_7;

import org.example.web2_7.crawler.DatabasePipeline;
import org.example.web2_7.crawler.WeChatArticleSpider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import us.codecraft.webmagic.Spider;

import java.util.Scanner;

// 主程序，启动spring boot自动装配，启动爬虫
@SpringBootApplication
public class Main implements CommandLineRunner {

    @Autowired
    private DatabasePipeline databasePipeline;

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        //System.out.println("运行程序后，\t控制台会提示你输入要爬取的微信公众号文章网址。输入网址后，程序将开始爬取该文章的相关信息。");
       // System.out.println("能下载图片到同目录下的image文件夹");
        System.out.println("启动微信公众号爬虫...");
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入要爬取的微信公众号文章网址：");
        String url = scanner.nextLine();
        scanner.close();

        Spider.create(new WeChatArticleSpider())
                .addUrl(url)
                .thread(1)
                .addPipeline(databasePipeline)
                .run();
    }
}
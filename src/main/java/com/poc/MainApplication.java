package com.poc;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@SpringBootApplication
@EnableAsync
public class MainApplication implements ApplicationRunner {

    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }

    public static CompletableFuture<Integer> downloadWebPage(Integer pageLink, ExecutorService executor) {
        return CompletableFuture.supplyAsync(() -> {
            // Code to download and return the web page's content
            try {
                Thread.sleep(pageLink.longValue());
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            System.out.println("----- Inside Phase 1 : " + pageLink);
            return pageLink + 5000;
        }, executor);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("############################   RUN   ############################");

        ExecutorService executor = Executors.newFixedThreadPool(20);

        List<Integer> webPageLinks = Arrays.asList(1000, 2000, 1000);

        // ######  Phase 1 BEGIN FIRST API CALLS IN PARALLEL ####################################"
        // LIST OF FUTURE TO EXCUTE IN PARALLEL FOR LIST INPUT PARAM
        List<CompletableFuture<Integer>> pageContentFutures = webPageLinks.stream()
                .map(webPageLink -> MainApplication.downloadWebPage(webPageLink, executor)).toList();

        // RUN ALL FUTURES , Create a combined Future using allOf()
        CompletableFuture<Void> allFutures = CompletableFuture
                .allOf(pageContentFutures.toArray(new CompletableFuture[pageContentFutures.size()]));

        // When all the Futures are completed, call `future.join()` to get their results
        // and collect the results in a list -
        CompletableFuture<List<Integer>> allPageContentsFuture = allFutures.thenApply(v -> {

            return pageContentFutures.stream()
                    .map(pageContentFuture -> {
                        //	System.out.println("isDone :" + pageContentFuture.isDone());
                        return pageContentFuture.join();

                    })
                    .collect(Collectors.toList());
        });

        // GET ONLY  List<Integer>
        List<Integer> finalResult = allPageContentsFuture.join();

        finalResult.stream().forEach(in -> System.out.println("----- Valeur : " + in));
    }

}

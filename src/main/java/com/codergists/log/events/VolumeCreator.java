package com.codergists.log.events;

import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

@Slf4j
public class VolumeCreator {

    private String name;

    private Integer age;

    private Instant time = Instant.now();

   public VolumeCreator(){

    }
    VolumeCreator(String name, Integer age) {
        this.name = name;
        this.age = age;
        this.time = Instant.now();
    }

    public void run() throws Exception {
        for (int i = 1; i < 200; i++) {
            log.info("INSIDE_CREATION {} {} {} ", i, System.getenv("suid"), new VolumeCreator("Thi",44+i));
            // Thread.sleep(1);
        }
        // log.info("VOLUME_CREATION_SUCCESSFUL");
    }

    public static void main(String[] args) throws Exception {
        new VolumeCreator().run();
        //System.out.println("running" + System.getenv().get("suid"));
        // log.info("VOLUME_CREATION_SUCCESSFUL_MAIN");
    }

    @Override
    public String toString() {
        return "VolumeCreator{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", time=" + time +
                '}';
    }
}

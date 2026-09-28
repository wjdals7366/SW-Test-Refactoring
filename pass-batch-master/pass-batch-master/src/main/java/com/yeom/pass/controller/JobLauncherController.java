package com.yeom.pass.controller;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.integration.launch.JobLaunchRequest;
import org.springframework.web.bind.annotation.*;
import java.util.Properties;

@RestController
@RequestMapping("job")
public class JobLauncherController {

    private final JobLauncher jobLauncher;

    private final JobRegistry jobRegistry;

    public JobLauncherController(JobLauncher jobLauncher, JobRegistry jobRegistry){
        this.jobLauncher = jobLauncher;
        this.jobRegistry = jobRegistry;
    }

    @PostMapping("launcher")
    public ExitStatus launchJob(@RequestBody JobLauncherRequest request) throws Exception{
        Job job = jobRegistry.getJob(request.getName());

        System.out.println(request.getJobParameters().toString());

        return this.jobLauncher.run(job, request.getJobParameters()).getExitStatus();   // job 이름을 통해 이동
    }

//    @PostMapping("launcher_usePass")
//    public ExitStatus launcher_usePass(@RequestBody JobLauncherRequest request) throws Exception{
//        Job job = jobRegistry.getJob(request.getName());
//
//        return this.jobLauncher.run(job, request.getJobParameters()).getExitStatus();
//    }


}

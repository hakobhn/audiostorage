package com.epam.training.microservices.audio.resources.bdd;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/test/resources/features", plugin = {
        "html:/output/report.html" }, monochrome = true)
public class CucumberRunnerTest {

}

/*
 * (C) Copyright 2017 Boni Garcia (http://bonigarcia.github.io/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package io.github.bonigarcia.handler;

import static io.github.bonigarcia.SeleniumJupiter.ARGS;
import static io.github.bonigarcia.SeleniumJupiter.BINARY;
import static io.github.bonigarcia.SeleniumJupiter.EXTENSIONS;
import static io.github.bonigarcia.SeleniumJupiter.EXTENSION_FILES;
import static java.lang.invoke.MethodHandles.lookup;
import static org.openqa.selenium.chrome.ChromeOptions.CAPABILITY;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.lang.reflect.Parameter;
import java.util.Optional;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;

import io.github.bonigarcia.AnnotationsReader;
import io.github.bonigarcia.DriverOptions;
import io.github.bonigarcia.Option;

/**
 * Resolver for ChromeDriver.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.2.0
 */
public class ChromeDriverHandler {

    final Logger log = getLogger(lookup().lookupClass());

    static ChromeDriverHandler instance;

    public static synchronized ChromeDriverHandler getInstance() {
        if (instance == null) {
            instance = new ChromeDriverHandler();
        }
        return instance;
    }

    public WebDriver resolve(Parameter parameter,
            Optional<Object> testInstance) {
        WebDriver webDriver = null;
        Optional<Capabilities> capabilities = AnnotationsReader.getInstance()
                .getCapabilities(parameter, testInstance);

        ChromeOptions chromeOptions = getChromeOptions(parameter, testInstance);
        if (capabilities.isPresent()) {
            ((DesiredCapabilities) capabilities.get()).setCapability(CAPABILITY,
                    chromeOptions);
            webDriver = new ChromeDriver(capabilities.get());
        } else {
            webDriver = new ChromeDriver(chromeOptions);
        }
        return webDriver;
    }

    private ChromeOptions getChromeOptions(Parameter parameter,
            Optional<Object> testInstance) {
        ChromeOptions chromeOptions = new ChromeOptions();
        DriverOptions driverOptions = parameter
                .getAnnotation(DriverOptions.class);

        // Search first DriverOptions annotation in parameter
        if (driverOptions != null) {
            for (Option option : driverOptions.options()) {
                String name = option.name();
                String value = option.value();
                switch (name) {
                case ARGS:
                    chromeOptions.addArguments(value);
                    break;
                case BINARY:
                    chromeOptions.setBinary(value);
                    break;
                case EXTENSIONS:
                    chromeOptions.addEncodedExtensions(value);
                    break;
                case EXTENSION_FILES:
                    chromeOptions.addExtensions(new File(value));
                    break;
                default:
                    chromeOptions.setExperimentalOption(name, value);
                }
            }
        } else {
            // If not, search DriverOptions in any field
            Object optionsFromAnnotatedField = AnnotationsReader.getInstance()
                    .getOptionsFromAnnotatedField(testInstance,
                            DriverOptions.class);
            if (optionsFromAnnotatedField != null) {
                chromeOptions = (ChromeOptions) optionsFromAnnotatedField;
            }
        }

        return chromeOptions;
    }

}

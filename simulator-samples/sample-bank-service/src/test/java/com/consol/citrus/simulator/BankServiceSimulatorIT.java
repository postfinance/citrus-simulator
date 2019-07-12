/*
 * Copyright 2006-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.simulator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.samePropertyValuesAs;

import com.consol.citrus.simulator.config.SSLRestTemplateConfiguration;
import com.consol.citrus.simulator.sample.BankServiceSimulator;
import com.consol.citrus.simulator.sample.model.BankAccount;
import com.consol.citrus.simulator.sample.model.CalculateIbanResponse;
import com.consol.citrus.simulator.sample.model.ValidateIbanResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.testng.annotations.Test;

@Import({ SSLRestTemplateConfiguration.class })
@SpringBootTest(classes = { BankServiceSimulator.class }, webEnvironment = WebEnvironment.RANDOM_PORT)
public class BankServiceSimulatorIT extends AbstractTestNGSpringContextTests {

    @LocalServerPort
    private int localServerPort;

    @Autowired
    private RestTemplate sslRestTemplate;

    @Test
    public void testCalculate() throws IOException {
        String uri = UriComponentsBuilder
                .fromUriString(String.format("https://localhost:%s/services/rest/bank", localServerPort))
                .queryParam("sortCode", "12345670").queryParam("accountNumber", "0006219653").toUriString();

        CalculateIbanResponse actual = sslRestTemplate.getForObject(uri, CalculateIbanResponse.class);

        CalculateIbanResponse expected = CalculateIbanResponse.builder()
                .bankAccount(BankAccount.builder().iban("DE92123456700006219653").bic("ABCDEFG5670")
                        .bank("The Wealthy ABC bank").sortCode("12345670").accountNumber("0006219653").build())
                .build();

        assertThat(actual, samePropertyValuesAs(expected));
    }

    public void testValidate() {
        String uri = UriComponentsBuilder
                .fromUriString(String.format("https://localhost:%s/services/rest/bank", localServerPort))
                .queryParam("iban", "DE92123456700006219653").toUriString();

        ValidateIbanResponse actual = sslRestTemplate.getForObject(uri, ValidateIbanResponse.class);

        ValidateIbanResponse expected = ValidateIbanResponse.builder()
                .bankAccount(BankAccount.builder().iban("DE92123456700006219653").bic("ABCDEFG5670")
                        .bank("The Wealthy ABC bank").sortCode("12345670").accountNumber("0006219653").build())
                .build();

        assertThat(actual, samePropertyValuesAs(expected));
    }
}

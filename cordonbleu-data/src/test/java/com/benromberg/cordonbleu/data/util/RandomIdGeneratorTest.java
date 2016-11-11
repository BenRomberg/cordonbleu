package com.benromberg.cordonbleu.data.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.benromberg.cordonbleu.data.util.RandomIdGenerator;

public class RandomIdGeneratorTest {
    @Test
    public void randomId_Has22Characters() throws Exception {
        String randomId = RandomIdGenerator.generate();
        assertThat(randomId).hasSize(22);
    }
}

package com.jobheroes.rest.integration;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.hamcrest.Matcher;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.ResourceUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jobheroes.repository.CandidateRepository;
import com.jobheroes.repository.OccupationRepository;
import com.jobheroes.repository.Repository;
import com.jobheroes.repository.mongo.TestEnvironment;
import com.jobheroes.rest.Dependencies;
import com.jobheroes.rest.gateway.CandidateGateway;
import com.jobheroes.rest.gateway.ObjectMapperContextResolver;
import com.jobheroes.util.jackson.JsonEqualsMatcher;
import com.jobheroes.util.jackson.ObjectMapperFactory;

public abstract class GatewayIntegrationTest implements Repository {
	private static final String FILE_ROOT = "integration/";

	@ClassRule
	public static TestEnvironment testEnvironment = new TestEnvironment();

	@Rule
	public JerseyTestRule jerseyTestRule = new JerseyTestRule() //
			.withResourceAndProviderClasses(CandidateGateway.class, ObjectMapperContextResolver.class);

	@BeforeClass
	public static void setupClass() {
		Dependencies.setDependencies(new Dependencies(testEnvironment.getRepository()));
	}

	private String getContent(String requestFilename, Object... parameters) throws IOException {
		File requestFile = ResourceUtils.getFile("classpath:" + getFilePath(requestFilename));
		String fileContent = FileCopyUtils.copyToString(new FileReader(requestFile));
		Map<String, Object> parameterMap = new HashMap<>();
		for (int i = 0; i < parameters.length; i++) {
			parameterMap.put(Integer.toString(i), convertParameterToJson(parameters[i]));
		}
		return StrSubstitutor.replace(fileContent, parameterMap);
	}

	private String convertParameterToJson(Object parameter) throws JsonProcessingException {
		return ObjectMapperFactory.getObjectMapper().writeValueAsString(parameter);
	}

	protected Matcher<String> getContentMatcher(String requestFilename, Object... parameters) throws IOException {
		return JsonEqualsMatcher.jsonEquals(getContent(requestFilename, parameters));
	}

	protected ResultResponse post(String url) throws Exception {
		return postWithContent(url, "");
	}

	protected ResultResponse postWithContent(String url, String request) throws Exception {
		return new ResultResponse(jerseyTestRule.target(url).request()
				.post(Entity.<String> entity(request, MediaType.APPLICATION_JSON)));
	}

	protected String getFilePath(String filePath) {
		return FILE_ROOT + filePath;
	}

	@Override
	public CandidateRepository getCandidateRepository() {
		return testEnvironment.getRepository().getCandidateRepository();
	}

	@Override
	public OccupationRepository getOccupationRepository() {
		return testEnvironment.getRepository().getOccupationRepository();
	}

}

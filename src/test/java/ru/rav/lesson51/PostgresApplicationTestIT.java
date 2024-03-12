package ru.rav.lesson51;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.ResourceUtils;
import ru.rav.lesson51.account.create.AccountCreate;
import ru.rav.lesson51.instance.create.*;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest( classes = { PostgresApplication.class })
@AutoConfigureMockMvc
@TestPropertySource("/application-test.properties")
@Sql(value = "/process-before.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
public class PostgresApplicationTestIT {
	public static String getRequestBodyFromFile(String fileLocation) throws IOException {
		File file = ResourceUtils.getFile( String.format("classpath:%s", fileLocation));
		return new String(Files.readAllBytes(file.toPath()));
	}

	@Mock
	ClientMdm clientMdm;

	@InjectMocks
	InstanceService instanceService;

	@Autowired
	MockMvc mvc;

	@Test
	@Order(2)
	void testIt() throws Exception {
		// Создание объекта ObjectMapper
		ObjectMapper mapper = new ObjectMapper();
		InstanceCreate instanceCreate;
		AccountCreate accountCreate;

		final String jsonString = getRequestBodyFromFile("instance-create-ok.json");

		Mockito.when(clientMdm.getClientIdForMdm( anyString())).thenReturn( new BigInteger( "1") );

		instanceCreate = mapper.readValue( jsonString, InstanceCreate.class);

		//последовательности <restart with> в process-before.sql
		//проверяем, что при создании возвращаются эти же id
		mvc.perform( MockMvcRequestBuilders.post("/corporate-settlement-instance/create")
						.contentType(MediaType.APPLICATION_JSON)
						.content( mapper.writeValueAsString( instanceCreate)))
						.andDo(print())
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.data.instanceId").value( 1))
						.andExpect(jsonPath("$.data.registerId.[0]").value( 5))
						.andExpect(jsonPath("$.data.supplementaryAgreementId.[0]").value( 10));

		//второй договор с тем же номером
		mvc.perform( MockMvcRequestBuilders.post("/corporate-settlement-instance/create")
						.contentType(MediaType.APPLICATION_JSON)
						.content( mapper.writeValueAsString( instanceCreate)))
						.andDo(print())
						.andExpect(status().isBadRequest())
						.andExpect(jsonPath("$.message", containsString("ContractNumber")));

		instanceCreate = mapper.readValue( jsonString, InstanceCreate.class);

		instanceCreate.setContractNumber( "newNumb");

		//второй договор с другим номером, но таким же номером ДС
		mvc.perform( MockMvcRequestBuilders.post("/corporate-settlement-instance/create")
						.contentType(MediaType.APPLICATION_JSON)
						.content( mapper.writeValueAsString( instanceCreate)))
						.andDo(print())
						.andExpect(status().isBadRequest())
						.andExpect(jsonPath("$.message", containsString("№ Дополнительного соглашения")));


		instanceCreate = mapper.readValue( jsonString, InstanceCreate.class);

		instanceCreate.setContractNumber( "newNumb");
		for ( Arrangements ar : instanceCreate.getInstanceArrangement())
			ar.setNumber("newDs");
		instanceCreate.setProductCode( "008.07.009");

		//второй договор с другим номером, но таким же номером ДС
		mvc.perform( MockMvcRequestBuilders.post("/corporate-settlement-instance/create")
						.contentType(MediaType.APPLICATION_JSON)
						.content( mapper.writeValueAsString( instanceCreate)))
						.andDo(print())
						.andExpect(status().isBadRequest())
						.andExpect(jsonPath("$.message", containsString("типа продукта")));

		String jsonRegTypeNull = getRequestBodyFromFile("instance-reg-type-null.json");

		//registerType - обязательное поле
		mvc.perform( MockMvcRequestBuilders.post("/corporate-settlement-instance/create")
						.contentType(MediaType.APPLICATION_JSON)
						.content( jsonRegTypeNull))
						.andDo(print())
						.andExpect(status().isBadRequest())
						.andExpect(jsonPath("$.message", containsString("registerType")));

		accountCreate = new AccountCreate();
		accountCreate.setInstanceId( new BigInteger( "1"));
		accountCreate.setBranchCode( "0021");
		accountCreate.setAccountType("Клиентский");
		accountCreate.setPriorityCode("00");
		accountCreate.setRegistryTypeCode( "02.001.005_45343_CoDowFF");
		accountCreate.setCurrencyCode( "500");
		accountCreate.setMdmCode( "13");

		mvc.perform( MockMvcRequestBuilders.post("/corporate-settlement-account/create")
						.contentType(MediaType.APPLICATION_JSON)
						.content( mapper.writeValueAsString( accountCreate)))
						.andDo(print())
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.data.accountId.[0]").value( 6));

		//проверка на задвоение
		mvc.perform( MockMvcRequestBuilders.post("/corporate-settlement-account/create")
						.contentType(MediaType.APPLICATION_JSON)
						.content( mapper.writeValueAsString( accountCreate)))
						.andDo(print())
						.andExpect(status().isBadRequest())
						.andExpect(jsonPath("$.message", containsString("registryTypeCode")));

		//нет договора
		accountCreate.setInstanceId( new BigInteger( "2"));

		mvc.perform( MockMvcRequestBuilders.post("/corporate-settlement-account/create")
						.contentType(MediaType.APPLICATION_JSON)
						.content( mapper.writeValueAsString( accountCreate)))
						.andDo(print())
						.andExpect(status().isBadRequest())
						.andExpect(jsonPath("$.message", containsString("Отсутствует договор")));

		//нет регистра
		accountCreate.setInstanceId( new BigInteger( "1"));
		accountCreate.setRegistryTypeCode("02.001.005_45343_CoDowFF_");

		mvc.perform( MockMvcRequestBuilders.post("/corporate-settlement-account/create")
						.contentType(MediaType.APPLICATION_JSON)
						.content( mapper.writeValueAsString( accountCreate)))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message", containsString("из пула счетов")));
	}

	@Test
	@Order(1)
	void testNullClient() {
		InstanceCreate instanceCreate = new InstanceCreate();
		InstanceResult instanceResult = new InstanceResult();
		//InstanceService instanceService = new InstanceService();
		int iError;
		BigInteger res = new BigInteger( "-1");
		instanceCreate.setMdmCode("1024");

		Mockito.when(clientMdm.getClientIdForMdm( "1024")).thenReturn( res );

		iError = instanceService.setProduct( instanceCreate, instanceResult);
		Assertions.assertEquals( iError, 400);
		String mess = instanceService.getMessage();
		if (mess.indexOf("МДМ") <= 0) {
			Assertions.fail( mess);
		}
	}
}

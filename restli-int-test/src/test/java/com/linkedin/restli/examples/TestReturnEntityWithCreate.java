/*
   Copyright (c) 2015 LinkedIn Corp.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package com.linkedin.restli.examples;

import com.linkedin.r2.RemoteInvocationException;
import com.linkedin.restli.client.BatchCreateIdEntityRequest;
import com.linkedin.restli.client.BatchCreateIdRequest;
import com.linkedin.restli.client.CreateIdEntityRequest;
import com.linkedin.restli.client.CreateIdRequest;
import com.linkedin.restli.client.Response;
import com.linkedin.restli.client.RestClient;
import com.linkedin.restli.common.BatchCreateIdEntityResponse;
import com.linkedin.restli.common.BatchCreateIdResponse;
import com.linkedin.restli.common.CreateIdEntityStatus;
import com.linkedin.restli.common.CreateIdStatus;
import com.linkedin.restli.common.HttpStatus;
import com.linkedin.restli.common.IdEntityResponse;
import com.linkedin.restli.common.IdResponse;
import com.linkedin.restli.common.RestConstants;
import com.linkedin.restli.examples.greetings.api.Greeting;
import com.linkedin.restli.examples.greetings.api.Tone;
import com.linkedin.restli.examples.greetings.client.CreateGreetingRequestBuilders;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Test createEntity request set.
 *
 * @author Boyang Chen
 */
public class TestReturnEntityWithCreate extends RestLiIntegrationTest
{
  @BeforeClass
  public void initClass() throws Exception
  {
    super.init();
  }

  @AfterClass
  public void shutDown() throws Exception
  {
    super.shutdown();
  }

  @Test(dataProvider = com.linkedin.restli.internal.common.TestConstants.RESTLI_PROTOCOL_1_2_PREFIX + "newBuildersClientDataDataProvider")
  public void testCreateIdEntity(RestClient restClient, String expectedContentType, CreateGreetingRequestBuilders builders) throws RemoteInvocationException
  {
    Greeting greeting = new Greeting();
    greeting.setMessage("second time!");
    greeting.setTone(Tone.FRIENDLY);

    CreateIdEntityRequest<Long, Greeting> createIdEntityRequest = builders.createAndGet().input(greeting).build();
    Response<IdEntityResponse<Long, Greeting>> response = restClient.sendRequest(createIdEntityRequest).getResponse();

    long id = response.getEntity().getId();
    @SuppressWarnings("deprecation")
    String stringId = response.getId();
    Assert.assertEquals(response.getHeader(RestConstants.HEADER_CONTENT_TYPE), expectedContentType);
    Assert.assertEquals(id, Long.parseLong(stringId));
    Assert.assertEquals("second time!", ((Greeting) response.getEntity().getEntity()).getMessage());
  }

  @Test(dataProvider = com.linkedin.restli.internal.common.TestConstants.RESTLI_PROTOCOL_1_2_PREFIX + "newBuildersClientDataDataProvider")
  public void testEntityWithProjection(RestClient restClient, String expectedContentType, CreateGreetingRequestBuilders builders) throws RemoteInvocationException
  {
    Greeting greeting = new Greeting();
    greeting.setMessage("second time!");
    greeting.setTone(Tone.FRIENDLY);

    CreateIdEntityRequest<Long, Greeting> createIdEntityRequest = builders.createAndGet().fields(Greeting.fields().tone(),
                                                                                                 Greeting.fields().id()).input(greeting).build();
    Response<IdEntityResponse<Long, Greeting>> response = restClient.sendRequest(createIdEntityRequest).getResponse();

    long id = response.getEntity().getId();
    @SuppressWarnings("deprecation")
    String stringId = response.getId();
    Assert.assertEquals(response.getHeader(RestConstants.HEADER_CONTENT_TYPE), expectedContentType);
    Assert.assertEquals(id, Long.parseLong(stringId));
    Assert.assertEquals(false, ((Greeting) response.getEntity().getEntity()).hasMessage());
    Assert.assertEquals(Tone.FRIENDLY, ((Greeting) response.getEntity().getEntity()).getTone());
  }

  @Test (dataProvider = com.linkedin.restli.internal.common.TestConstants.RESTLI_PROTOCOL_1_2_PREFIX + "newBuildersClientDataDataProvider")
  public void testBatchCreateWithEntity(RestClient restClient, String expectedContentType, CreateGreetingRequestBuilders builders) throws RemoteInvocationException
  {
    Greeting greeting = new Greeting();
    greeting.setMessage("second time!");
    greeting.setTone(Tone.FRIENDLY);

    Greeting greeting2 = new Greeting();
    greeting2.setMessage("first time!");
    greeting2.setTone(Tone.FRIENDLY);
    List<Greeting> greetings = new ArrayList<Greeting>();
    greetings.add(greeting);
    greetings.add(greeting2);

    BatchCreateIdEntityRequest<Long, Greeting> batchCreateIdEntityRequest = builders.batchCreateAndGet().inputs(greetings).build();
    Response<BatchCreateIdEntityResponse<Long, Greeting>> response = restClient.sendRequest(batchCreateIdEntityRequest).getResponse();
    BatchCreateIdEntityResponse<Long, Greeting> entityResponses = response.getEntity();
    Assert.assertEquals(response.getHeader(RestConstants.HEADER_CONTENT_TYPE), expectedContentType);

    for (CreateIdEntityStatus<?, ?> singleResponse : entityResponses.getElements())
    {
      @SuppressWarnings("deprecation")
      String id = singleResponse.getId();
      Assert.assertNotNull(id);
      Greeting entity = (Greeting)singleResponse.getEntity();
      Assert.assertEquals(Tone.FRIENDLY, entity.getTone());
      Assert.assertEquals(singleResponse.getStatus().intValue(), HttpStatus.S_201_CREATED.getCode());
    }
  }

  @Test(dataProvider = com.linkedin.restli.internal.common.TestConstants.RESTLI_PROTOCOL_1_2_PREFIX + "newBuildersClientDataDataProvider")
  public void testBatchCreateEntityWithProjection(RestClient restClient, String expectedContentType, CreateGreetingRequestBuilders builders) throws RemoteInvocationException
  {
    Greeting greeting = new Greeting();
    greeting.setMessage("projection time!");
    greeting.setTone(Tone.FRIENDLY);

    BatchCreateIdEntityRequest<Long, Greeting> batchCreateIdEntityRequest = builders.batchCreateAndGet().fields(Greeting.fields().tone(),
                                                                                                                Greeting.fields().id()).inputs(
                                                                                                                Arrays.asList(greeting, greeting)).build();
    Response<BatchCreateIdEntityResponse<Long, Greeting>> response = restClient.sendRequest(batchCreateIdEntityRequest).getResponse();
    BatchCreateIdEntityResponse<Long, Greeting> entityResponses = response.getEntity();

    Assert.assertEquals(response.getHeader(RestConstants.HEADER_CONTENT_TYPE), expectedContentType);
    for (CreateIdEntityStatus<?, ?> singleResponse : entityResponses.getElements())
    {
      @SuppressWarnings("deprecation")
      String id = singleResponse.getId();
      Assert.assertNotNull(id);
      Greeting entity = (Greeting)singleResponse.getEntity();
      Assert.assertEquals(false, entity.hasMessage());
      Assert.assertEquals(true, entity.hasId());
      Assert.assertEquals(Tone.FRIENDLY, entity.getTone());
    }
  }

  @Test (dataProvider = com.linkedin.restli.internal.common.TestConstants.RESTLI_PROTOCOL_1_2_PREFIX + "newBuildersClientDataDataProvider")
  public void testBatchCreateWithEntityWithError(RestClient restClient, String expectedContentType, CreateGreetingRequestBuilders builders) throws RemoteInvocationException
  {
    Greeting greeting = new Greeting();
    greeting.setMessage("good!");
    greeting.setTone(Tone.FRIENDLY);

    Greeting greeting2 = new Greeting();
    greeting2.setMessage("too much!");
    greeting2.setTone(Tone.FRIENDLY);
    List<Greeting> greetings = new ArrayList<Greeting>(Arrays.asList(greeting, greeting, greeting, greeting2));


    BatchCreateIdEntityRequest<Long, Greeting> batchCreateIdEntityRequest = builders.batchCreateAndGet().inputs(
        greetings).build();
    Response<BatchCreateIdEntityResponse<Long, Greeting>> response = restClient.sendRequest(batchCreateIdEntityRequest).getResponse();
    BatchCreateIdEntityResponse<Long, Greeting> entityResponses = response.getEntity();
    Assert.assertEquals(response.getHeader(RestConstants.HEADER_CONTENT_TYPE), expectedContentType);

    int numOfElem = 0;
    for (CreateIdEntityStatus<?, ?> singleResponse : entityResponses.getElements())
    {
      if (numOfElem > 2)
      {
        Assert.assertTrue(singleResponse.hasError());
        Assert.assertEquals(singleResponse.getStatus().intValue(), HttpStatus.S_400_BAD_REQUEST.getCode());
        Assert.assertEquals(singleResponse.getError().getMessage(), "exceed quota"); // More than 3 elements were sent, should trigger exception.
      }
      else
      {
        @SuppressWarnings("deprecation")
        String id = singleResponse.getId();
        Assert.assertNotNull(id);
        Greeting entity = (Greeting)singleResponse.getEntity();
        Assert.assertEquals(Tone.FRIENDLY, entity.getTone());
        Assert.assertEquals(singleResponse.getStatus().intValue(), HttpStatus.S_201_CREATED.getCode());
      }
      numOfElem++;
    }
  }


  /**
   * Test for backward compatibility of create id request.
   * @param restClient
   * @param expectedContentType
   * @param builders
   * @throws RemoteInvocationException
   */
  @Test(dataProvider = com.linkedin.restli.internal.common.TestConstants.RESTLI_PROTOCOL_1_2_PREFIX + "newBuildersClientDataDataProvider")
  public void testCreateId(RestClient restClient, String expectedContentType, CreateGreetingRequestBuilders builders) throws RemoteInvocationException
  {
    Greeting greeting = new Greeting();
    greeting.setMessage("first time!");
    greeting.setTone(Tone.FRIENDLY);

    CreateIdRequest<Long, Greeting> createIdRequest = builders.create().input(greeting).build();
    Response<IdResponse<Long>> response = restClient.sendRequest(createIdRequest).getResponse();

    Assert.assertEquals(response.getHeader(RestConstants.HEADER_CONTENT_TYPE), expectedContentType);
    long id = response.getEntity().getId();
    @SuppressWarnings("deprecation")
    String stringId = response.getId();
    Assert.assertEquals(id, Long.parseLong(stringId));
  }

  /**
   * Test for backward compatibility of batch create id request.
   * @param restClient
   * @param expectedContentType
   * @param builders
   * @throws RemoteInvocationException
   */
  @Test(dataProvider = com.linkedin.restli.internal.common.TestConstants.RESTLI_PROTOCOL_1_2_PREFIX + "newBuildersClientDataDataProvider")
  public void testBatchCreateId(RestClient restClient, String expectedContentType, CreateGreetingRequestBuilders builders) throws RemoteInvocationException
  {
    Greeting greeting = new Greeting();
    greeting.setMessage("first time!");
    greeting.setTone(Tone.FRIENDLY);

    BatchCreateIdRequest<Long, Greeting> batchCreateIdRequest = builders.batchCreate().inputs(Arrays.asList(greeting,
                                                                                                            greeting)).build();
    Response<BatchCreateIdResponse<Long>> response = restClient.sendRequest(batchCreateIdRequest).getResponse();

    Assert.assertEquals(response.getHeader(RestConstants.HEADER_CONTENT_TYPE), expectedContentType);
    List<CreateIdStatus<Long>> elems = response.getEntity().getElements();
    Assert.assertEquals(elems.get(0).getStatus().intValue(), HttpStatus.S_201_CREATED.getCode());
    Assert.assertEquals(elems.get(1).getStatus().intValue(), HttpStatus.S_201_CREATED.getCode());
  }

  @SuppressWarnings("deprecation")
  @DataProvider(name = com.linkedin.restli.internal.common.TestConstants.RESTLI_PROTOCOL_1_2_PREFIX + "newBuildersClientDataDataProvider")
  public Object[][] newBuildersClientDataDataProvider()
  {
    return new Object[][]
    {
      {new RestClient(getDefaultTransportClient(),
                      URI_PREFIX), "application/json", new CreateGreetingRequestBuilders()}, // default client
      {new RestClient(getDefaultTransportClient(), URI_PREFIX), "application/json", new CreateGreetingRequestBuilders(
          TestConstants.FORCE_USE_NEXT_OPTIONS)}, // default client
      {
          new RestClient(getDefaultTransportClient(),
                         URI_PREFIX,
                         Collections.singletonList(RestClient.AcceptType.PSON)),
          "application/x-pson",
          new CreateGreetingRequestBuilders()
      },
      {
          new RestClient(getDefaultTransportClient(),
                         URI_PREFIX,
                         Collections.singletonList(RestClient.AcceptType.PSON)),
          "application/x-pson",
          new CreateGreetingRequestBuilders(TestConstants.FORCE_USE_NEXT_OPTIONS)
      },
      {
          new RestClient(getDefaultTransportClient(),
                         URI_PREFIX,
                         Collections.singletonList(RestClient.AcceptType.PSON)),
          "application/x-pson",
          new CreateGreetingRequestBuilders(TestConstants.FORCE_USE_NEXT_OPTIONS)
      },
      {
          new RestClient(getDefaultTransportClient(),
                         URI_PREFIX,
                         Collections.singletonList(RestClient.AcceptType.JSON)),
          "application/json",
          new CreateGreetingRequestBuilders()
      },
      {
          new RestClient(getDefaultTransportClient(),
                         URI_PREFIX,
                         Collections.singletonList(RestClient.AcceptType.JSON)),
          "application/json",
          new CreateGreetingRequestBuilders(TestConstants.FORCE_USE_NEXT_OPTIONS)
      },
      {
          new RestClient(getDefaultTransportClient(), URI_PREFIX,
                         Collections.singletonList(
                             RestClient.AcceptType.ANY)),
          "application/json",
          new CreateGreetingRequestBuilders()
      },
      {
          new RestClient(getDefaultTransportClient(), URI_PREFIX,
                         Collections.singletonList(
                             RestClient.AcceptType.ANY)),
          "application/json",
          new CreateGreetingRequestBuilders(TestConstants.FORCE_USE_NEXT_OPTIONS)
      },
      {
          new RestClient(getDefaultTransportClient(),
                         URI_PREFIX,
                         Arrays.asList(RestClient.AcceptType.PSON, RestClient.AcceptType.JSON)),
          "application/x-pson",
          new CreateGreetingRequestBuilders()
      },
      {
          new RestClient(getDefaultTransportClient(),
                         URI_PREFIX,
                         Arrays.asList(RestClient.AcceptType.PSON, RestClient.AcceptType.JSON)),
          "application/x-pson",
          new CreateGreetingRequestBuilders(TestConstants.FORCE_USE_NEXT_OPTIONS)
      },
      {
          new RestClient(getDefaultTransportClient(),
                         URI_PREFIX,
                         Arrays.asList(RestClient.AcceptType.JSON, RestClient.AcceptType.PSON)),
          "application/json",
          new CreateGreetingRequestBuilders()
      },
      {
          new RestClient(getDefaultTransportClient(),
                         URI_PREFIX,
                         Arrays.asList(RestClient.AcceptType.JSON, RestClient.AcceptType.PSON)),
          "application/json",
          new CreateGreetingRequestBuilders(TestConstants.FORCE_USE_NEXT_OPTIONS)
      },
      {
          new RestClient(getDefaultTransportClient(),
                         URI_PREFIX,
                         Arrays.asList(RestClient.AcceptType.PSON, RestClient.AcceptType.ANY)),
          "application/x-pson",
          new CreateGreetingRequestBuilders()
      },
      {
          new RestClient(getDefaultTransportClient(),
                         URI_PREFIX,
                         Arrays.asList(RestClient.AcceptType.PSON, RestClient.AcceptType.ANY)),
          "application/x-pson",
          new CreateGreetingRequestBuilders(TestConstants.FORCE_USE_NEXT_OPTIONS)
      },
      {
          new RestClient(getDefaultTransportClient(),
                         URI_PREFIX,
                         Arrays.asList(RestClient.AcceptType.JSON, RestClient.AcceptType.ANY)),
          "application/json",
          new CreateGreetingRequestBuilders()
      },
      {
          new RestClient(getDefaultTransportClient(),
                         URI_PREFIX,
                         Arrays.asList(RestClient.AcceptType.JSON, RestClient.AcceptType.ANY)),
          "application/json",
          new CreateGreetingRequestBuilders(TestConstants.FORCE_USE_NEXT_OPTIONS)
      },
      {
          new RestClient(getDefaultTransportClient(),
                         URI_PREFIX,
                         Arrays.asList(RestClient.AcceptType.ANY, RestClient.AcceptType.PSON)),
          "application/x-pson",
          new CreateGreetingRequestBuilders()
      },
      {
          new RestClient(getDefaultTransportClient(),
                         URI_PREFIX,
                         Arrays.asList(RestClient.AcceptType.ANY, RestClient.AcceptType.PSON)),
          "application/x-pson",
          new CreateGreetingRequestBuilders(TestConstants.FORCE_USE_NEXT_OPTIONS)
      },
      {
          new RestClient(getDefaultTransportClient(),
                         URI_PREFIX,
                         Arrays.asList(RestClient.AcceptType.ANY, RestClient.AcceptType.JSON)),
          "application/json",
          new CreateGreetingRequestBuilders()
      },
      {
          new RestClient(getDefaultTransportClient(),
                         URI_PREFIX,
                         Arrays.asList(RestClient.AcceptType.ANY, RestClient.AcceptType.JSON)),
          "application/json",
          new CreateGreetingRequestBuilders(TestConstants.FORCE_USE_NEXT_OPTIONS)
      },
      // accept types and content types
      {
          new RestClient(getDefaultTransportClient(),
                         URI_PREFIX,
                         RestClient.ContentType.JSON, Collections.<RestClient.AcceptType>emptyList()),
          "application/json",
          new CreateGreetingRequestBuilders()
      },
      {
          new RestClient(getDefaultTransportClient(),
                         URI_PREFIX,
                         RestClient.ContentType.JSON, Collections.<RestClient.AcceptType>emptyList()),
          "application/json",
          new CreateGreetingRequestBuilders(TestConstants.FORCE_USE_NEXT_OPTIONS)
      },
      {
          new RestClient(getDefaultTransportClient(),
                         URI_PREFIX,
                         RestClient.ContentType.JSON, Collections.singletonList(RestClient.AcceptType.JSON)),
          "application/json",
          new CreateGreetingRequestBuilders()
      },
      {
          new RestClient(getDefaultTransportClient(),
                         URI_PREFIX,
                         RestClient.ContentType.JSON, Collections.singletonList(RestClient.AcceptType.JSON)),
          "application/json",
          new CreateGreetingRequestBuilders(TestConstants.FORCE_USE_NEXT_OPTIONS)
      },
      {
          new RestClient(getDefaultTransportClient(),
                         URI_PREFIX,
                         RestClient.ContentType.JSON, Collections.singletonList(RestClient.AcceptType.PSON)),
          "application/x-pson",
          new CreateGreetingRequestBuilders()
      },
      {
          new RestClient(getDefaultTransportClient(),
                         URI_PREFIX,
                         RestClient.ContentType.JSON, Collections.singletonList(RestClient.AcceptType.PSON)),
          "application/x-pson",
          new CreateGreetingRequestBuilders(TestConstants.FORCE_USE_NEXT_OPTIONS)
      },
      {
          new RestClient(getDefaultTransportClient(),
                         URI_PREFIX,
                         RestClient.ContentType.PSON, Collections.<RestClient.AcceptType>emptyList()),
          "application/json",
          new CreateGreetingRequestBuilders()
      },
      {
          new RestClient(getDefaultTransportClient(),
                         URI_PREFIX,
                         RestClient.ContentType.PSON, Collections.<RestClient.AcceptType>emptyList()),
          "application/json",
          new CreateGreetingRequestBuilders(TestConstants.FORCE_USE_NEXT_OPTIONS)
      },
      {
          new RestClient(getDefaultTransportClient(),
                         URI_PREFIX,
                         RestClient.ContentType.PSON, Collections.singletonList(RestClient.AcceptType.JSON)),
          "application/json",
          new CreateGreetingRequestBuilders()
      },
      {
          new RestClient(getDefaultTransportClient(),
                         URI_PREFIX,
                         RestClient.ContentType.PSON, Collections.singletonList(RestClient.AcceptType.JSON)),
          "application/json",
          new CreateGreetingRequestBuilders(TestConstants.FORCE_USE_NEXT_OPTIONS)
      },
      {
          new RestClient(getDefaultTransportClient(),
                         URI_PREFIX,
                         RestClient.ContentType.PSON, Collections.singletonList(RestClient.AcceptType.PSON)),
          "application/x-pson",
          new CreateGreetingRequestBuilders()
      },
      {
          new RestClient(getDefaultTransportClient(),
                         URI_PREFIX,
                         RestClient.ContentType.PSON, Collections.singletonList(RestClient.AcceptType.PSON)),
          "application/x-pson",
          new CreateGreetingRequestBuilders(TestConstants.FORCE_USE_NEXT_OPTIONS)
      }
    };
  }
}

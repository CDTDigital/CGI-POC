package com.cgi.poc.dw.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.cgi.poc.dw.api.service.MapsApiService;
import com.cgi.poc.dw.api.service.data.GeoCoordinates;
import com.cgi.poc.dw.auth.data.Role;
import com.cgi.poc.dw.auth.service.PasswordHash;
import com.cgi.poc.dw.dao.UserDao;
import com.cgi.poc.dw.dao.model.User;
import com.cgi.poc.dw.factory.AddressBuilder;
import com.cgi.poc.dw.factory.AddressBuilderImpl;
import com.cgi.poc.dw.rest.dto.FcmTokenDto;
import com.cgi.poc.dw.validator.ValidationErrors;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import org.hibernate.HibernateException;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceUnitTest {

  @InjectMocks
  UserServiceImpl underTest;

  @Mock
  private UserDao userDao;

  @Mock
  private PasswordHash passwordHash;

  @Spy
  private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Mock
  private Client client;

  @Mock
  private MapsApiService mapsApiService;

  @Spy
  private AddressBuilder addressBuilder = new AddressBuilderImpl();

  private User user;

  @SuppressWarnings("unchecked")
	@Before
  public void createUser() throws IOException {
    user = new User();
    user.setEmail("success@gmail.com");
    user.setPassword("test123");
    user.setFirstName("john");
    user.setLastName("smith");
    user.setRole(Role.RESIDENT.name());
    user.setPhone("1234567890");
    user.setZipCode("95814");
    user.setCity("Sacramento");
    user.setState("CA");
    user.setAddress1("621 Capitol Mall");
    user.setAddress2(null);
    user.setEmailNotification(false);
    user.setSmsNotification(false);
    user.setPushNotification(false);
    user.setLatitude(0.0);
    user.setLongitude(0.0);
    user.setSmsNotification(true);
    
    GeoCoordinates geoCoordinates = new GeoCoordinates();
    geoCoordinates.setLatitude(10.00);
    geoCoordinates.setLongitude(20.00);
    when(mapsApiService.getGeoCoordinatesByAddress(anyString())).thenReturn(geoCoordinates);

    JsonNode jsonRespone = new ObjectMapper()
        .readTree(getClass().getResource("/google_maps_api/success_geocode_response.json"));

    //mocking the Jersey Client
    WebTarget mockWebTarget = mock(WebTarget.class);
    when(client.target(anyString())).thenReturn(mockWebTarget);
    when(mockWebTarget.queryParam(anyString(), anyString())).thenReturn(mockWebTarget);
    Invocation.Builder mockBuilder = mock(Invocation.Builder.class);
    when(mockWebTarget.request(anyString())).thenReturn(mockBuilder);
    when(mockBuilder.get(String.class)).thenReturn(jsonRespone.toString());
  }

  @Test
  public void registerUser_RegisterUserWithValidInput() throws Exception {

    String saltedHash = "518bd5283161f69a6278981ad00f4b09a2603085f145426ba8800c:"
        + "8bd85a69ed2cb94f4b9694d67e3009909467769c56094fc0fce5af";
    when(passwordHash.createHash(user.getPassword())).thenReturn(saltedHash);
    Response actual = underTest.registerUser(user);

    assertEquals(200, actual.getStatus());
  }


  @Test
  public void passwordValidationFails() throws Exception {

    user.setPassword("a"); //one character password

    try {
      underTest.registerUser(user);
      fail("Expected an exception to be thrown");
    } catch (ConstraintViolationException exception) {
      Set<ConstraintViolation<?>> constraintViolations = exception.getConstraintViolations();
      for (ConstraintViolation<?> violation : constraintViolations) {
        String tmp = ((PathImpl) violation.getPropertyPath()).getLeafNode().getName();
        String annotation = violation.getConstraintDescriptor().getAnnotation().annotationType()
            .getCanonicalName();

        if (tmp.equals("password") && annotation.equals("javax.validation.constraints.Size")) {
          assertThat(violation.getMessageTemplate())
              .isEqualTo("must be at least 2 characters in length.");
        } else if (tmp.equals("password") && annotation
            .equals("com.cgi.poc.dw.validator.PasswordType")) {
          assertThat(violation.getMessage())
              .isEqualTo(ValidationErrors.INVALID_PASSWORD);
        } else {
          fail("not an expected constraint violation");
        }
      }
    }
  }

  @Test
  public void registerUser_EmailValidationFails() throws Exception {

    user.setPassword("aaa"); //one character password
    user.setEmail("aaa"); //one character password

    try {
      underTest.registerUser(user);
      fail("Expected an exception to be thrown");
    } catch (ConstraintViolationException exception) {
      Set<ConstraintViolation<?>> constraintViolations = exception.getConstraintViolations();
      for (ConstraintViolation<?> violation : constraintViolations) {
        String tmp = ((PathImpl) violation.getPropertyPath()).getLeafNode().getName();
        String annotation = violation.getConstraintDescriptor().getAnnotation().annotationType()
            .getCanonicalName();

        if (tmp.equals("email") && annotation.equals("javax.validation.constraints.Pattern")) {
          assertThat(violation.getMessageTemplate())
              .isEqualTo(ValidationErrors.INVALID_EMAIL);
        } else {
          fail("not an expected constraint violation");
        }
      }
    }
  }
  @Test
  public void registerUser_EmailValidationFailsWhiteSpace() throws Exception {

    user.setPassword("aaa"); //one character password
    user.setEmail("aaa @i23.com"); //one character password

    try {
      underTest.registerUser(user);
      fail("Expected an exception to be thrown");
    } catch (ConstraintViolationException exception) {
      Set<ConstraintViolation<?>> constraintViolations = exception.getConstraintViolations();
      for (ConstraintViolation<?> violation : constraintViolations) {
        String tmp = ((PathImpl) violation.getPropertyPath()).getLeafNode().getName();
        String annotation = violation.getConstraintDescriptor().getAnnotation().annotationType()
            .getCanonicalName();

        if (tmp.equals("email") && annotation.equals("javax.validation.constraints.Pattern")) {
          assertThat(violation.getMessageTemplate())
              .isEqualTo(ValidationErrors.INVALID_EMAIL);
        } else {
          fail("not an expected constraint violation");
        }
      }
    }
  }
   @Test
  public void registerUser_EmailValidationFailsInvalidDomain() throws Exception {

    user.setPassword("aaa"); //one character password
    user.setEmail("aaa@ggg"); //(looks for patter @XX.YYY

    try {
      underTest.registerUser(user);
      fail("Expected an exception to be thrown");
    } catch (ConstraintViolationException exception) {
      Set<ConstraintViolation<?>> constraintViolations = exception.getConstraintViolations();
      for (ConstraintViolation<?> violation : constraintViolations) {
        String tmp = ((PathImpl) violation.getPropertyPath()).getLeafNode().getName();
        String annotation = violation.getConstraintDescriptor().getAnnotation().annotationType()
            .getCanonicalName();

        if (tmp.equals("email") && annotation.equals("javax.validation.constraints.Pattern")) {
          assertThat(violation.getMessageTemplate())
              .isEqualTo(ValidationErrors.INVALID_EMAIL);
        } else {
          fail("not an expected constraint violation");
        }
      }
    }
  }
    @Test
  public void registerUserEmailValidationFailsNoDomeain() throws Exception {

    user.setPassword("aaa"); //one character password
    user.setEmail("aaa@"); //(looks for patter @XX.YYY

    try {
      underTest.registerUser(user);
      fail("Expected an exception to be thrown");
    } catch (ConstraintViolationException exception) {
      Set<ConstraintViolation<?>> constraintViolations = exception.getConstraintViolations();
      for (ConstraintViolation<?> violation : constraintViolations) {
        String tmp = ((PathImpl) violation.getPropertyPath()).getLeafNode().getName();
        String annotation = violation.getConstraintDescriptor().getAnnotation().annotationType()
            .getCanonicalName();

        if (tmp.equals("email") && annotation.equals("javax.validation.constraints.Pattern")) {
          assertThat(violation.getMessageTemplate())
              .isEqualTo(ValidationErrors.INVALID_EMAIL);
        } else {
          fail("not an expected constraint violation");
        }
      }
    }
  }
  @Test
  public void registerUser_UserAlreadyExistsError() throws Exception {

    String saltedHash = "518bd5283161f69a6278981ad00f4b09a2603085f145426ba8800c:"
        + "8bd85a69ed2cb94f4b9694d67e3009909467769c56094fc0fce5af";
    when(passwordHash.createHash(user.getPassword())).thenReturn(saltedHash);
    when(userDao.findUserByEmail(user.getEmail())).thenReturn(user);

    try {
      Response registerUser = underTest.registerUser(user);
      fail("Expected an exception to be thrown");
    } catch (BadRequestException exception) {
      assertEquals(exception.getMessage(), ValidationErrors.DUPLICATE_USER);
    }
  }

  @Test
  public void registerUser_CreateUserFails() throws Exception {
    String saltedHash = "518bd5283161f69a6278981ad00f4b09a2603085f145426ba8800c:"
        + "8bd85a69ed2cb94f4b9694d67e3009909467769c56094fc0fce5af";
    when(passwordHash.createHash(user.getPassword())).thenReturn(saltedHash);

    doThrow(new HibernateException("Something went wrong.")).when(userDao).save(any(User.class));
    try {
      Response registerUser = underTest.registerUser(user);
      fail("Expected an exception to be thrown");
    } catch (HibernateException exception) {
      assertEquals(exception.getMessage(), "Something went wrong.");
    }
  }

  @Test
  public void passwordHashingFails() throws Exception {

    doThrow(new InternalServerErrorException("Something went wrong.")).when(passwordHash).createHash(user.getPassword());
    try {
      Response response = underTest.registerUser(user);
      fail("Expected an exception to be thrown");
    }
    catch (InternalServerErrorException exception) {
      assertEquals("Something went wrong.", exception.getMessage());
    }
  }

  @Test
  public void mapsAPICommunicationFails() {

    String saltedHash = "518bd5283161f69a6278981ad00f4b09a2603085f145426ba8800c:"
        + "8bd85a69ed2cb94f4b9694d67e3009909467769c56094fc0fce5af";
    when(passwordHash.createHash(user.getPassword())).thenReturn(saltedHash);
    doThrow(new InternalServerErrorException("Processing failed.")).when(mapsApiService).getGeoCoordinatesByAddress(anyString());
    
    try {
      Response response = underTest.registerUser(user);
      fail("Expected an exception to be thrown");
    } 
    catch (InternalServerErrorException exception) {
      assertEquals("Processing failed.", exception.getMessage());
    }
  }

  @Test
  public void returnsExpectedGeoCoordinates() throws Exception {

    String saltedHash = "518bd5283161f69a6278981ad00f4b09a2603085f145426ba8800c:"
        + "8bd85a69ed2cb94f4b9694d67e3009909467769c56094fc0fce5af";
    when(passwordHash.createHash(user.getPassword())).thenReturn(saltedHash);
    Response actual = underTest.registerUser(user);

    User actualUser = (User) actual.getEntity();

    assertEquals(200, actual.getStatus());
    assertEquals(new Double(10.0), actualUser.getLatitude());
    assertEquals(new Double(20.0), actualUser.getLongitude());
  }

  @Test
	public void updateUser_UpdateUserWithValidInput() throws Exception {
    String saltedHash = "9e5f3dd72fbd5f309131364baf42b446f570629f4a809390be533f:"
				+ "1db93c4885d4bf980e92286d74da720dc298fdc1a29c89cf9c67ce";

		when(passwordHash.createHash(user.getPassword())).thenReturn(saltedHash);
		when(userDao.findUserByEmail(user.getEmail())).thenReturn(user);
		Response actual = underTest.updateUser(user, user);

		assertEquals(200, actual.getStatus());
	}

  @Test
  public void updateUserToken() {
    String fcmtoken = "afbjk324bn2k34bklj43_2njn421_22-f23";

    FcmTokenDto fcmTokenDto = new FcmTokenDto();
    fcmTokenDto.setFcmtoken(fcmtoken);

    Response actual = underTest.updateFcmToken(user, fcmTokenDto);

    assertEquals(200, actual.getStatus());
  }

}

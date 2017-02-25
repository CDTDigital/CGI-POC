package com.cgi.poc.dw.service;

import com.cgi.poc.dw.api.service.MapsApiService;
import com.cgi.poc.dw.api.service.data.GeoCoordinates;
import com.cgi.poc.dw.auth.service.PasswordHash;
import com.cgi.poc.dw.dao.UserDao;
import com.cgi.poc.dw.dao.model.User;
import com.cgi.poc.dw.util.ErrorInfo;
import com.cgi.poc.dw.util.GeneralErrors;
import com.cgi.poc.dw.util.LoginValidationGroup;
import com.cgi.poc.dw.util.PersistValidationGroup;
import com.cgi.poc.dw.util.RestValidationGroup;
import com.cgi.poc.dw.util.ValidationErrors;
import com.google.inject.Inject;
import java.util.Arrays;
import javax.validation.Validator;
import javax.validation.groups.Default;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserServiceImpl extends BaseServiceImpl implements UserService {

	private final static Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);

	private final UserDao userDao;

	private final PasswordHash passwordHash;
	
	private MapsApiService mapsApiService;

	private final EmailService emailService;

	private final TextMessageService textMessageService;

	// The name of the query param for the
	private static final String ADDRESS = "address";

	@Inject
	public UserServiceImpl(MapsApiService mapsApiService, UserDao userDao, PasswordHash passwordHash,
			Validator validator, Client client, EmailService emailService, TextMessageService textMessageService) {
		super(validator);
		this.userDao = userDao;
		this.passwordHash = passwordHash;
		this.mapsApiService = mapsApiService;
		this.emailService = emailService;
		this.textMessageService = textMessageService;
	}

	public Response registerUser(User user) {
		// Defaulting the user to RESIDENT
		user.setRole("RESIDENT");

		validate(user, "rest", RestValidationGroup.class, Default.class);
		// check if the email already exists.
		User exitingUser = userDao.findUserByEmail(user.getEmail());
		if (exitingUser != null) {
			throw new BadRequestException(ValidationErrors.DUPLICATE_USER);
		}

		return processForSave(user, false, false);
	}

	private void saveUser(User user, boolean registered) {
		validate(user, "save", Default.class, PersistValidationGroup.class);

		userDao.save(user);
		if (!registered) {
			// Future TODO enhancement: make the subject and email body configurable
			emailService.send(null, Arrays.asList(user.getEmail()), "Registration confirmation",
					"Hello there, thank you for registering.");
			textMessageService.send(user.getPhone(), "MyCAlerts: Thank you for registering.");
		}
	}
	
	private ErrorInfo getInternalErrorInfo(Exception exception, GeneralErrors generalErrors) {
		ErrorInfo errRet = new ErrorInfo();
		String message = generalErrors.getMessage();
		String exMsg = "";
		if (exception.getMessage() != null) {
			exMsg = exception.getMessage();
		}
		String errorString = message.replace("REPLACE1", exception.getClass().getCanonicalName()).replace("REPLACE2",
				exMsg);
		errRet.addError(generalErrors.getCode(), errorString);
		return errRet;
	}

	public Response updateUser(User user, User modifiedUser) {
		//If user password is empty keep same password.
		boolean keepPassword = false;
		if(StringUtils.isBlank(modifiedUser.getPassword())){
			modifiedUser.setPassword(user.getPassword());
			keepPassword = true;
		}
		else {
			validate(modifiedUser, "update", LoginValidationGroup.class);
		}
		modifiedUser.setId(user.getId());
		modifiedUser.setRole(user.getRole());
		return processForSave(modifiedUser, true, keepPassword);
	}

	private Response processForSave(User user, boolean registered, boolean keepPassword) {
		Response response = null;
		if (!keepPassword) {
			String hash = passwordHash.createHash(user.getPassword());
			user.setPassword(hash);
		}

		GeoCoordinates geoCoordinates = mapsApiService.getGeoCoordinatesByZipCode(user.getZipCode());
		user.setLatitude(geoCoordinates.getLatitude());
		user.setLongitude(geoCoordinates.getLongitude());

		saveUser(user, registered);
		response = Response.ok().entity(user).build();

		return response;
	}

}

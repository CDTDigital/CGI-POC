package com.cgi.poc.dw.rest.resource;

import com.cgi.poc.dw.dao.model.User;
import com.cgi.poc.dw.rest.model.LocalizationDto;
import com.cgi.poc.dw.service.UserService;
import com.cgi.poc.dw.service.UserServiceImpl;
import com.codahale.metrics.annotation.Timed;
import com.google.inject.Inject;

import io.dropwizard.auth.Auth;
import io.dropwizard.hibernate.UnitOfWork;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.annotation.security.RolesAllowed;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api(value = "/user", basePath = "/")
public class UserResource {

  private final static Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);

  @Inject
  UserService userService;
  
  @Path("/register")
  @POST
  @UnitOfWork
  @ApiOperation(value = "User profile registration",
      notes = "Allows a user to register.")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Success"),
      @ApiResponse(code = 500, message = "System Error")
  })
  @Timed(name = "User.save")
  public Response signup(@NotNull User user) {
      Response response = userService.registerUser(user);
      if (!response.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
                throw new WebApplicationException(response);
      }
    return response;
  }
  
  @RolesAllowed("RESIDENT")
  @Path("/localizer")
  @POST
  @UnitOfWork
  @ApiOperation(value = "User localization",
      notes = "stores the users current geo location")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Success"),
      @ApiResponse(code = 500, message = "System Error")
  })
  @ApiImplicitParams({
      @ApiImplicitParam(name = "Authorization", required = true, dataType = "string", paramType = "header")
  })
  @Timed(name = "User.save")
  public Response setLocalization(@Auth User user, LocalizationDto localizationDto) {
	  
	  user.setGeoLocLatitude(localizationDto.getGeoLocLatitude());
	  user.setGeoLocLongitude(localizationDto.getGeoLocLongitude());
	  
      Response response = userService.setLocalization(user);
      if (!response.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
                throw new WebApplicationException(response);
      }
    return response;
  }
}
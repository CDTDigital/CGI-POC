package com.cgi.poc.dw.rest.resource;

import com.cgi.poc.dw.dao.model.User;
import com.cgi.poc.dw.rest.dto.EventNotificationDto;
import com.codahale.metrics.annotation.Timed;
import com.google.inject.Inject;
import io.dropwizard.auth.Auth;
import io.dropwizard.hibernate.UnitOfWork;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.cgi.poc.dw.service.EventNotificationService;
import javax.ws.rs.GET;

@Path("/notification")
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "/notification", basePath = "/")
public class EventNotificationResource {
  
  @Inject
  EventNotificationService notificationService;

  @POST
  @RolesAllowed("ADMIN")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Publish an event notification",
      notes = "Allows the admin to publish event notifications.")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Success"),
      @ApiResponse(code = 401, message = "Authentication failed."),
      @ApiResponse(code = 500, message = "System Error")
  })
  @ApiImplicitParams({
      @ApiImplicitParam(name = "Authorization", required = true, dataType = "string", paramType = "header")
  })
  @UnitOfWork
  @Timed(name = "EventNotification.publishNotification")
  public Response publishNotification(@ApiParam(hidden = true) @Auth User principal, @NotNull @Valid EventNotificationDto eventNotificationDto) {
    
    
    Response response =  notificationService.publishNotification(principal, eventNotificationDto);
    if (!response.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
      throw new WebApplicationException(response);
    }
    return response;
  }

  
  @GET
  @Path("/admin")
  @RolesAllowed("ADMIN")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Retrieve event notifications",
      notes = "Allows retrieval of event notifications.")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Success"),
      @ApiResponse(code = 401, message = "Authentication failed."),
      @ApiResponse(code = 500, message = "System Error")
  })
  @ApiImplicitParams({
      @ApiImplicitParam(name = "Authorization", required = true, dataType = "string", paramType = "header")
  })
  @UnitOfWork
  @Timed(name = "EventNotification.getNotifications")
  public Response getNotifications(@ApiParam(hidden = true) @Auth User principal) {
      Response response = notificationService.retrieveAllNotifications(principal);
      if (!response.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
          throw new WebApplicationException(response);
      }
      return response;
 }
  @GET
  @RolesAllowed("RESIDENT")
  @Path("/user")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Retrieve event notifications",
      notes = "Allows retrieval of event notifications.")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Success"),
      @ApiResponse(code = 401, message = "Authentication failed."),
      @ApiResponse(code = 500, message = "System Error")
  })
  @ApiImplicitParams({
      @ApiImplicitParam(name = "Authorization", required = true, dataType = "string", paramType = "header")
  })
  @UnitOfWork
  @Timed(name = "EventNotification.getNotifications")
  public Response getNotificationsForUser(@ApiParam(hidden = true) @Auth User principal) {
      Response response = notificationService.retrieveNotificationsForUser(principal);
      if (!response.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
          throw new WebApplicationException(response);
      }
      return response;
 }
}

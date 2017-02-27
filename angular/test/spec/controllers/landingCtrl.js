'use strict';

describe('landingController', function() {
  var landingController;
  var $scope;
  var notificationService;
  var $state;
  var $q;
  var deferred;

  beforeEach(module('cgi-web-app'));
    beforeEach(inject(function(_$rootScope_, _$controller_, _EventNotificationService_, _$state_, _$q_) {

    $q = _$q_;
    $scope = _$rootScope_.$new();
    notificationService = _EventNotificationService_;
    $state = _$state_;

    deferred = _$q_.defer();
    spyOn(notificationService, 'allNotifications').and.returnValue(deferred.promise);
    spyOn(notificationService, 'userNotifications').and.returnValue(deferred.promise);


    landingController = _$controller_('landingController', {
      $scope: $scope,
      ProfileService: notificationService
    });
  }));
  it('initializes the apiErrors', function() {
    expect($scope.apiErrors.length).toBe(0);
  });
  it('should filter by type and date', function() {
    spyOn($scope, 'initLoad');
       var todaysDate = new Date();
       var day30Date = new Date();
       var day60Date = new Date();
       var day90Date = new Date();
       day30Date.setDate(todaysDate.getDate() - 31);
       day60Date.setDate(todaysDate.getDate() - 61);
       day90Date.setDate(todaysDate.getDate() - 91);

      $scope.model.notifications.push({type: 'Weather', generationDate: todaysDate, zipcodes: [], description: 'Pick up essentials and leave', citizensAffected: 111});
      $scope.model.notifications.push({type: 'Flood', generationDate: todaysDate, zipcodes: ['99999','94545-444'], description: 'Urgent message', citizensAffected: 111});
      $scope.model.notifications.push({type: 'Fire', generationDate: day30Date, zipcodes: ['99999','94545-444'], description: 'Urgent message', citizensAffected: 111});
      $scope.model.notifications.push({type: 'Flood', generationDate: day30Date, zipcodes: ['99999','94545-444'], description: 'Urgent message', citizensAffected: 111});
      $scope.model.notifications.push({type: 'Flood', generationDate: day60Date, zipcodes: ['99999','94545-444'], description: 'Urgent message', citizensAffected: 111});
      $scope.model.notifications.push({type: 'Flood', generationDate: day60Date, zipcodes: ['99999','94545-444'], description: 'Urgent message', citizensAffected: 111});
      $scope.model.notifications.push({type: 'Flood', generationDate: day60Date, zipcodes: ['99999','94545-444'], description: 'Urgent message', citizensAffected: 111});
      $scope.model.notifications.push({type: 'Fire', generationDate: day90Date, zipcodes: ['99999','94545-444'], description: 'Urgent message', citizensAffected: 111});
      $scope.model.notifications.push({type: 'Weather', generationDate: day90Date, zipcodes: ['99999','94545-444'], description: 'Urgent message', citizensAffected: 111});
      $scope.model.notifications.push({type: 'Flood', generationDate: day90Date, zipcodes: ['99999','94545-444'], description: 'Urgent message', citizensAffected: 111});


    $scope.changeFilters();

    expect($scope.model.notifications.length).toBe(10);
    expect($scope.model.filteredNotifications.length).toBe(10);

    $scope.eventTypeFilter='Weather';
    $scope.changeFilters();
    expect($scope.model.filteredNotifications.length).toBe(2);

    $scope.eventTypeFilter='Flood';
    $scope.changeFilters();
    expect($scope.model.filteredNotifications.length).toBe(6);


  });
  it('set the current select event and the showMapOrDetails', function() {
    var todaysDate = new Date();
    var selectedEvent =  {type: 'Weather', generationDate: todaysDate, zipcodes: [], description: 'Pick up essentials and leave', citizensAffected: 111};
    $scope.loadEventDetails(selectedEvent);

      expect($scope.currentSelectedEvent).toBe(selectedEvent);
      expect($scope.showMapOrDetails).toBe('DETAILS');
  });

 it('set the current select event and the showMapOrDetails', function() {
    var todaysDate = new Date();
    var selectedEvent =  {type: 'Weather', generationDate: todaysDate, zipcodes: [], description: 'Pick up essentials and leave', citizensAffected: 111};
    $scope.loadMap(selectedEvent);

      expect($scope.currentSelectedEvent).toBe(selectedEvent);
      expect($scope.showMapOrDetails).toBe('MAP');
 });

 it('converts inbound data strings to objects', function() {
    var apiData = [
  {
    description: 'Severe Thunderstorm Warning',
    type: 'Weather',
    generationDate: '2017-02-19T20:36:37.000+0000',
    geometry: '{\"x\":-81.36500307953563,\"y\":27.776949558089495}',
    url1: 'http://forecast.weather.gov/product.php?site=EWX&issuedby=EWX&product=SVR',
    url2: 'www.cnn.com',
    citizensAffected: 500,
    userId: {
      firstName: 'Dawna',
      lastName: 'Floyd',
      email: 'me@me.com',
      password: '0d78e8a655575f3aa49dab465349cd96e5773c507020ebe92ab05f:e3352e3a7cf37fcb885088a67979bd63792ed089bd830629d5acc9',
      phone: '2183309196',
      zipCode: '56401',
      emailNotification: false,
      pushNotification: false,
      smsNotification: false
    },
    eventNotificationZipcodes: []
  }];
     var expectDate = Date.parse('2017-02-19T20:36:37.000+0000');
     var geometryObj = {
         x:-81.36500307953563,
         y: 27.776949558089495
     };
     $scope.convertApiData(apiData);

      expect($scope.model.notifications.length).toBe(1);
      expect($scope.model.notifications[0].geometry).toEqual(geometryObj);
      expect($scope.model.notifications[0].generationDate).toEqual(expectDate);
 });

 it('set the default display based on mobile or not', function() {
    var todaysDate = new Date();
    var selectedEvent =  {type: 'Weather', generationDate: todaysDate, zipcodes: [], description: 'Pick up essentials and leave', citizensAffected: 111};
    $scope.isMobile = true;
    $scope.backToDefault(selectedEvent);
      expect($scope.showMapOrDetails).toBe('LIST');

    $scope.isMobile = false;
    $scope.backToDefault(selectedEvent);
      expect($scope.showMapOrDetails).toBe('MAP');
 
 });
});
'use strict';

describe('ProfileController', function() {
  var profileController;
  var $scope;
  var profileService;
  var $q;
  var deferred;

  beforeEach(module('cgi-web-app'));

  beforeEach(inject(function(_$rootScope_, _$controller_, _ProfileService_, _$q_) {
    $q = _$q_;
    $scope = _$rootScope_.$new();
    profileService = _ProfileService_;

    deferred = _$q_.defer();
    spyOn(profileService, 'register').and.returnValue(deferred.promise);

    profileController = _$controller_('ProfileController', {
      $scope: $scope,
      ProfileService: profileService
    });
  }));

  it('initializes a profile object', function() {
    expect($scope.profile.firstName).toBe('');
    expect($scope.profile.lastName).toBe('');
    expect($scope.profile.email).toBe('');
    expect($scope.profile.confirmEmail).toBe('');
    expect($scope.profile.password).toBe('');
    expect($scope.profile.confirmPassword).toBe('');
    expect($scope.profile.phone).toBe('');
    expect($scope.profile.zipCode).toBe('');
    expect($scope.profile.notificationType.length).toBe(0);
    expect($scope.profile.emailNotification).toBe(false);
    expect($scope.profile.pushNotification).toBe(false);
    expect($scope.profile.smsNotification).toBe(false);
  });

  it('initializes these pattern validations', function() {
    expect('12345').toMatch($scope.regexZip);
    expect('123-456-7890').toMatch($scope.regexPhone);
    expect('abcABC123').toMatch($scope.regexPassword);
  });

  describe('register', function() {
    it('should call the ProfileService.register', function() {
      $scope.profile = {};
      $scope.registerProfile($scope.profile);
      deferred.resolve({ status: 200, data: {} });
      $scope.$apply();
      expect(profileService.register).toHaveBeenCalledWith($scope.profile);
    });
  });
});

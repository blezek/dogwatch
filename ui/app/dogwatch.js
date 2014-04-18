

// Configuration for require.js
// foundation, xtk and dat.gui are loaded by default
require.config({
  baseURL: 'js',
  // Some packages do not provide require info, so we 'shim' them here
  shim: {
    'angular': { exports: 'angular'},
    'angular-route': ['angular'],
    'angular-ui-router' : ['angular'],
    'ui-ace' : ['angular'],
    // The angularAMD and ngload let us load a page and add angular apps later
    'angularAMD':['angular'],
    'ngload':['angularAMD'],
    'ui-bootstrap-tpls':['angular']
  }
})

// To work, the model, angular and angularAMD packages are required
require(['angular', 'angularAMD', "backbone", 'angular-ui-router', 'ui-bootstrap-tpls', 'ui-ace', 'ace/ace' ], function(angular, angularAMD, Backbone ) {

  // Helper for shortening strings
  String.prototype.trunc = String.prototype.trunc ||
  function(n){
    return this.length>n ? this.substr(0,n-1)+'...' : this;
  };

  String.prototype.startsWith = String.prototype.startsWith ||
  function (str){
    return this.indexOf(str) == 0;
  };

  WatchModel = Backbone.Model.extend({
    defaults: {
      worry: 10,
      cron: "0 0 * * * ?"
    }
  });

  WatchCollection = Backbone.Collection.extend({
    model: WatchModel,
    url: '/rest/watch',
  });

  dogwatchApp = angular.module('dogwatchApp', ['ui.router', 'ui.bootstrap', 'ui.ace']);

  dogwatchApp.config(function($stateProvider, $urlRouterProvider) {
    $urlRouterProvider.when('', '/index')
    $urlRouterProvider.otherwise('/index')
    $stateProvider
    .state('index', {
      abstract: false,
      url: "/index",
      templateUrl: 'partials/dogwatch.html',
      controller: 'DogwatchController'
    })
    .state('watches', {
      url: "/watches",
      templateUrl: 'partials/watches.html',
      controller: 'WatchController'
    })
    .state('login', {
      url: "/login",
      templateUrl: 'partials/login.html',
      controller: 'LoginController'
    })
    .state('register', {
      url: "/register",
      templateUrl: 'partials/register.html',
      controller: 'RegisterController'
    }).state('hash', {
      abstract: false,
      url: "/hash",
      templateUrl: 'partials/hash.html',
      controller: 'LoginController'
    }).state('hasherror', {
      abstract: false,
      url: "/hash/error",
      templateUrl: 'partials/hash.error.html'
    })
  });

    // ['$routeProvider',
    // function($routeProvider){
    //   $routeProvider.
    //   when('/', {
    //     templateUrl: 'partials/pools.html',
    //     controller: 'PoolsController'
    //   });
    // }]);

dogwatchApp.controller ( 'LoginController', function($scope,$http,$location) {
  console.log("Starting Login")
  $scope.login = function() {
    console.log ( "Login with ", $scope.user)
    $http(
      { url:"/login",
      method: "POST",
      data: $.param($scope.user),
      headers: {'Content-Type': 'application/x-www-form-urlencoded'}
    })
      .success(function(data) {
        $location.url('/');
    })
    .error(function(data, status, headers, config) {
      $scope.error = data.message
    });
  };
});


dogwatchApp.controller ( 'RegisterController', function($scope,$http,$location) {
  console.log("Starting Register")
  $scope.user = { agree: false, password:null }
  $scope.register = function() {
    $scope.error = ""
    console.log("Register clicked", $scope.user)
    if (!$scope.user.agree) {
      $scope.error = "You must agree to Terms and Conditions before registration!"
      return
    }

    console.log ( "Register with ", $scope.user)
    $http(
      { url:"/login/register",
      method: "POST",
      data: $.param($scope.user),
      headers: {'Content-Type': 'application/x-www-form-urlencoded'}
    })
      .success(function(data) {
        $location.url('/');
    })
    .error(function(data, status, headers, config) {
      $scope.error = data.message
    });
  };
});



  dogwatchApp.controller ( 'WatchController', function($scope,$timeout,$state,$http,$modal) {
    console.log("Starting WatchController");
    $scope.watches = new WatchCollection();
    $scope.watches.fetch({remove:true,async:false})

    $scope.editWatch = function(watch) {
      var title = "Edit the watch"
      if ( !watch ) {
        title = "create a new watch"
        watch = new WatchModel();
      }
      $scope.watch = watch
      $modal.open({
        templateUrl: 'partials/watch.edit.html',
        scope: $scope,
        controller: function($scope,$modalInstance) {
          $scope.title = title
          $scope.watchModel = watch.toJSON();
          $scope.valid = false

          $scope.validate = function(){
            watch.set($scope.watchModel);
            $http.post("/rest/watch/validate", watch)
            .success(function(data) {
              $scope.valid = data.valid
              $scope.explanation = ""
              $scope.error = data.messages.join("\n");
              if ( data.explanation ) {
                $scope.explanation = " (" + data.explanation + ")";
              }
            });
          }


          $scope.save = function(){
            watch.set ( $scope.watchModel )
            // Add emails!
            $scope.watches.add(watch)
            watch.save();
            $modalInstance.close();
          };
          $scope.cancel = function() { $modalInstance.dismiss() };
          $scope.validate();
        }
      })
    }

  });

  dogwatchApp.controller ( 'DogwatchController', function($scope,$timeout,$location,$http) {
    console.log("Starting DogwatchController")
    // See if we're logged in?
    $http.get("/rest/watch").success(function(data){
      console.log("Redirectiong to watches")
      $location.url("/watches")
    });
  });


  // Here is where the fun happens. angularAMD contains support for initializing an angular
  // app after the page load.
  angularAMD.bootstrap(dogwatchApp);


  console.log ("Build dogwatch app")
})

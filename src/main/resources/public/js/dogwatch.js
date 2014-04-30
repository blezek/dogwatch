

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
require(['angular', 'angularAMD', "backbone", 'moment', 'angular-ui-router', 'ui-bootstrap-tpls', 'ui-ace', 'ace/ace' ], function(angular, angularAMD, Backbone, moment ) {


  // NB: all rest calls are relative to this JS file
  var REST = function ( uri ) {
    return "../rest/" + uri
  }

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
      cron: "0 0 * * * ?",
      show: false,
      checks: null,
      timezone: "US/Central"
    }
  });

  WatchCollection = Backbone.Collection.extend({
    model: WatchModel,
    url: REST ('watch'),
  });

  CheckModel = Backbone.Model.extend({});
  CheckCollection = Backbone.Collection.extend({
    model: CheckModel,
    url: function () { return this.urlRoot; },
  });

  dogwatchApp = angular.module('dogwatchApp', ['ui.router', 'ui.bootstrap', 'ui.ace']);

  dogwatchApp.config(function($stateProvider, $urlRouterProvider) {
    $urlRouterProvider.when('', '')
    $urlRouterProvider.otherwise('')
    $stateProvider
    .state('index', {
      abstract: false,
      url: "",
      templateUrl: 'partials/dogwatch.html',
      controller: 'DogwatchController'
    })
    .state('index.home', {
      url: "/home",
      templateUrl: 'partials/dogwatch.index.html',
      controller: 'LoginController'
    })
    .state('index.help', {
      url: "/help",
      templateUrl: 'partials/help.html'
    })
    .state('index.index.login', {
      url: "/login",
      templateUrl: 'partials/login.html',
      controller: 'LoginController'
    })
    .state('index.watches', {
      url: "/watches",
      templateUrl: 'partials/watches.html',
      controller: 'WatchController'
    })
    .state('index.login', {
      url: "/login",
      templateUrl: 'partials/login.html',
      controller: 'LoginController'
    })
    .state('index.register', {
      url: "/register",
      templateUrl: 'partials/register.html',
      controller: 'RegisterController'
    })
    .state('index.hash', {
      abstract: false,
      url: "/hash",
      templateUrl: 'partials/hash.html',
      controller: 'LoginController'
    })
    .state('index.hasherror', {
      abstract: false,
      url: "/hash/error",
      templateUrl: 'partials/hash.error.html'
    })
    .state('index.forgot', {
      abstract: false,
      url: "/forgotpassword",
      templateUrl: 'partials/forgot.html',
      controller: 'ForgotController'
    })
    .state('index.lostpassword', {
      abstract: false,
      url: "/lostpassword/:hash",
      templateUrl: 'partials/lostpassword.html',
      controller: 'LostPasswordController'
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
  $scope.login = function(user) {
    console.log ("Scope: ", $scope)
    console.log ( "Login with ", $scope.user)
    $scope.user = user
    $http(
      { url:REST("login"),
      method: "POST",
      data: $.param($scope.user),
      headers: {'Content-Type': 'application/x-www-form-urlencoded'}
    })
    .success(function(data) {
      $scope.$parent.checkLogin()
      $state.transitionTo('index.watches')
    })
    .error(function(data, status, headers, config) {
      $scope.error = data.message
    });
  };
});

dogwatchApp.controller ( 'ForgotController', function($scope,$http,$location) {
  console.log("Starting Forgot")
  $scope.reset = function() {
    console.log ( "Forgot with ", $scope.user)
    $http(
      { url:REST("login/lostpassword"),
      method: "POST",
      data: $.param($scope.user),
      headers: {'Content-Type': 'application/x-www-form-urlencoded'}
    })
    .success(function(data) {
      $scope.error = data
    })
    .error(function(data, status, headers, config) {
      $scope.error = data.message
    });
  };
});


dogwatchApp.controller ( 'RegisterController', function($scope,$http,$location,$state) {
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
      { url:REST("login/register"),
      method: "POST",
      data: $.param($scope.user),
      headers: {'Content-Type': 'application/x-www-form-urlencoded'}
    }).success(function(data, status, headers, config) {
      console.log(data)
      $state.transitionTo("index.home");
      $scope.$parent.checkLogin();
    }).error(function(data, status, headers, config) {
      $scope.error = data.message
    });
  };
});

dogwatchApp.controller ( 'LostPasswordController', function($scope,$http,$location,$stateParams,$state) {
  console.log("LostPassword")
  $scope.error = ""

  $scope.user = { password: null, matchPassword: null, hash: $stateParams.hash }
  $scope.reset = function() {

    console.log ( "Register with ", $scope.user)
    $http(
      { url:REST("login/changepassword"),
      method: "POST",
      data: $.param($scope.user),
      headers: {'Content-Type': 'application/x-www-form-urlencoded'}
    })
    .success(function(data) {
      if ( data.updated ) {
        $state.transitionTo('index.watches')
          // $location.url('/');
        } else {
          $scope.error = data.message
        }
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
  $scope.moment = moment;
  $scope.humanize = true;
  $scope.$parent.checkLogin();

  $scope.dtime = function(time, otherwise) {
    if ( time == null ) { return otherwise; }
    if ( $scope.humanize ) {
      return moment(time).fromNow();
    } else {
      return moment(time).format('llll');
    }
    return otherwise;
  }


  $scope.status = function(watch) {
    if ( !watch.get('next_check') ) {
      return "danger";
    }
    if ( watch.get('consecutive_failed_checks') > 2 ) {
      return "info"
    }
    if ( watch.get('consecutive_failed_checks') > 5 ) {
      return "warning"
    }
  }

  $scope.reload = function(){
    $scope.watches.fetch({
      success: function() {
        $scope.$apply();
        $scope.watches.models.forEach ( function(element,index,array) {
          if ( element.get('show') ) {
            console.log("Reloading", element)
            $scope.reloadChecks(element)
          }
        });
        $timeout($scope.reload, 60*1000);
      },
      error: function(model, response, options) {
        console.log("Could not get check data")
           // alert("Failed to retrieve data from server");
         }
       });
  };

  $scope.reload();

  $scope.show = function(watch) {
    watch.set("show", !watch.get("show"))
    $scope.reloadChecks(watch);
  };

  $scope.reloadChecks = function(watch) {
    var c;
    if ( !watch.has("checks")) {
      c = new CheckCollection();
      c.urlRoot = REST ("watch/" + watch.get("id") + "/lookout")
      watch.set("checks", c);
    }
    c = watch.get("checks");
    console.log (c )
    c.fetch({
      success: function(data) {
        console.log("Got the checks for ", watch)
        $scope.$apply()
      },
      error: function(model, response, options) {
        console.log("Could not get check data")
        alert("Failed to retrieve data from server");
      }
    });

  };


  $scope.help = function(watch) {
    $scope.user = $scope.$parent.user;
    $modal.open({
      templateUrl: 'partials/watch.help.html',
      scope: $scope,
      windowClass: 'wide-dialog',
      controller: function($scope,$modalInstance) {
        $scope.checks = new CheckCollection();
        $scope.watch = watch;
        $scope.checks.urlRoot = "/rest/watch/" + watch.get("id") + "/lookout"
        $scope.checks.fetch({remove:true, async:false})
        $scope.cancel = function() { $modalInstance.dismiss() }
        $scope.encode = encodeURIComponent
      }
    })
  }

  $scope.delete = function(watch) {
    $modal.open({
      templateUrl: 'partials/watch.delete.html',
      scope: $scope,
      controller: function($scope,$modalInstance) {
        $scope.watch = watch;
        $scope.delete = function() {
          $scope.watches.remove(watch);
          $scope.watch.urlRoot = REST("watch/");
          $scope.watch.destroy();
          $scope.watches.fetch({
            success: function() { $scope.$apply(); },
            error: function(model, response, options) {
              console.log("Could not get check data")
              alert("Failed to retrieve data from server");
            }
          });
          $modalInstance.dismiss();
          $scope.reload();
        }
        $scope.cancel = function() { $modalInstance.dismiss() }
      }
    })
  }


  $scope.editWatch = function(watch) {
    console.log(watch)
    var title = "Edit the watch"
    if ( !watch ) {
      title = "Create a new watch"
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
          var tempWatch = new WatchModel($scope.watchModel);
          console.log("validate", tempWatch)
          $http.post( REST("watch/validate"), tempWatch)
          .success(function(data) {
            $scope.valid = data.valid
            $scope.explanation = ""
            $scope.error = data.messages.join("\n");
            if ( data.explanation ) {
              $scope.explanation = " (" + data.explanation + ")";
            }
          });
        }

        $scope.getTimezone = function(val) {
          return $http.get(REST("watch/tz"), {
            params: {
              timezone: val
            }
          }).then(function(res) {
            return res.data.timezones;
          });
        }

        $scope.save = function(){
          watch.set ( $scope.watchModel )
            // Add emails!
            $scope.watches.add(watch)
            watch.save({
              success: function() {
                $scope.reload();
              },
              error: function(model, response, options) {
                console.log("Could not get check data")
                alert("Failed to save watch!");
              }
            });
            $modalInstance.close();
          };
          $scope.cancel = function() { $modalInstance.dismiss() };
          $scope.validate();
        }
      })
}

});

dogwatchApp.controller ( 'DogwatchController', function($scope,$timeout,$location,$http,$state) {
  console.log("Starting DogwatchController")
    // See if we're logged in?
    $scope.logout = function() {
      $http.post(REST("login/logout")).success(function(data) {
        $scope.checkLogin()
      });
    };


    $scope.checkLogin = function( goto ) {
      console.log("Checking if we are logged in")
      $http.get(REST("login")).success(function(data){
        console.log("Login info", data)
        $scope.data = data
        $scope.loggedIn = false
        // Logged in?
        if ( data.user ) {
          console.log("User: ", data.user)
          $scope.loggedIn = true
          $scope.user = data.user;
          console.log("LoginController -- we are logged in")

          if ( goto ) {
            $location.url(goto)
          } else {
            $state.transitionTo("index.watches");
          }
        } else {
          $state.transitionTo("index.home")
        }
      }).error(function(data){
        console.log("no login info avaliable")
        $state.transitionTo("index.home")
      })
    };
    $scope.checkLogin();
  });


  // Here is where the fun happens. angularAMD contains support for initializing an angular
  // app after the page load.
  angularAMD.bootstrap(dogwatchApp);
  // $state.transitionTo('index.index.login')
  console.log ("Build dogwatch app")
})

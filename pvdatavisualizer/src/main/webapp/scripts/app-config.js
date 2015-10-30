
app.config([ '$routeProvider', '$locationProvider', 'loginServiceProvider',
		function($routeProvider, $locationProvider, loginServiceProvider) {
	
			var loginService = loginServiceProvider.$get();
	
			$routeProvider.when('/login', {
				templateUrl : 'views/login.html',
				controller : 'LoginController',
				anonymousAccessAllowed : true
			});
			$routeProvider.when('/register', {
				templateUrl : 'views/register.html',
				anonymousAccessAllowed : true
			// controller: RegisterController
			});
			$routeProvider.when('/', {
				templateUrl : 'views/dashboard.html',
				controller : 'DashboardController',
				anonymousAccessAllowed : false
			});
			$routeProvider.otherwise({
				redirectTo : '/'
			});

		} ]).run(function($rootScope, $location, loginService){
			$rootScope.$on("$routeChangeStart", 
				function(event, next, current) 
				{
					if( next.anonymousAccessAllowed === undefined || next.anonymousAccessAllowed == false )
					{
						if( loginService.session.currentUser === null )
						{
							$rootScope.returnToAfterLogin = next;
							$location.path("/login");
						}						
					}
				  });
				});



angular
.module('xxApp', [
  'ngResource',
  'ngRoute',
  'strava'
])
.config(function ($routeProvider) {
  $routeProvider
    .when('/', {
      templateUrl: 'views/main.html',
      controller: 'LoginController',
      controllerAs: 'main'
    })
    .when('/about', {
      templateUrl: 'views/about.html',
      controller: 'AboutCtrl',
      controllerAs: 'about',
      loggedin: true
    })
    .when("/loggedin", {
      templateUrl: 'views/loggedin.html',
      controller: 'LoggedInController',
    })
    .otherwise({
      redirectTo: '/'
    });
}).run(function($rootScope, $location){
  $rootScope.$on("$routeChangeStart", function(event, next, current){
    if (next.loggedin !== undefined && next.loggedin == true){
      console.log("loggedin set to "+next.loggedin);
      if($rootScope.access_token === undefined) {
        console.log("not logged in");
        $location.path("/");
      }
    } else {
      console.log("loggedin not set or false");
    }
  });
});
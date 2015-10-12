
app.config([ '$routeProvider', '$locationProvider', 'loginServiceProvider',
		function($routeProvider, $locationProvider, loginServiceProvider) {
	
			var loginService = loginServiceProvider.$get();
	
			$routeProvider.when('/login', {
				templateUrl : 'views/login.html',
				controller : 'LoginController'
			});
			$routeProvider.when('/register', {
				templateUrl : 'views/register.html',
			// controller: RegisterController
			});
			$routeProvider.when('/', {
				templateUrl : 'views/dashboard.html',
				controller : 'DashboardController'
			});
			$routeProvider.otherwise({
				redirectTo : '/'
			});

		} ]);

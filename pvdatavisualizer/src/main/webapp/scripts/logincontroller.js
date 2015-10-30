app.controller('LoginController', [
			'$scope', '$rootScope', '$location', 'loginService',
			function($scope, $rootScope, $location, loginService) {
				
				$scope.session = loginService.session;
				
				$scope.login = function () {
					if( loginService.login() )
					{
						if( $rootScope.returnToAfterLogin !== undefined )
						{
							$location.path($rootScope.returnToAfterLogin.$$route.originalPath);
						}
					};
				};
				
				$scope.logout = function () {
					loginService.logout();
				}	
			
			} ]);